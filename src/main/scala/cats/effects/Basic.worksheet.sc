import cats.effect.ExitCode
import cats.effect.IOApp
import cats.effect.std.Semaphore
// “the IO monad”, 
// - for capturing and controlling actions, often referred to as “effects”, 
// - that your program wishes to perform within a resource-safe, typed context with seamless support for concurrency and coordination. 
// - These effects may be asynchronous (callback-driven) or synchronous (directly returning values); they may return within microseconds or run infinitely.
// Cats Effect defines a set of typeclasses which define what it means to be a purely functional runtime system
//  - solving problems such as dependency injection, multiple error channels, shared state across modules, tracing,
//  - These abstractions power a thriving ecosystem consisting of streaming frameworks, JDBC database layers, HTTP servers and clients, asynchronous clients for systems like Redis and MongoDB...


// Copying files - basic concepts, resource handling and cancellation
// But this is functional programming! So invoking the function shall not copy anything, 
// instead it will return an IO instance that encapsulates all the side effects involved (opening/closing files, reading/writing content), that way purity is kept. 
// Only when that IO instance is evaluated all those side-effectful actions will be run
import cats.effect.IO
import java.io.File
// def copy(origin: File, destination: File): IO[Long] = ???

// Acquiring and releasing Resources
// We consider opening a stream to be a side-effect action, so we have to encapsulate those actions in their own IO instances.
// We want to ensure that streams are closed once we are done using them, no matter what.
import cats.effect.{IO, Resource}
import cats.syntax.all._
import java.io._
def inputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileInputStream] = 
  Resource.make {
    IO(new FileInputStream(f))
  } { inStream => 
    // todo
    // guard.withPermit {}
    // guard.permit.
    guard.acquire >> IO(inStream.close()).handleErrorWith(_ => IO.unit) >> guard.release
  }

// simple but no more control
// def inputStream2(f: File): Resource[IO, InputStream] = Resource.fromAutoCloseable(IO(new FileInputStream(f)))

def outputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileOutputStream] = 
  Resource.make {
    IO(new FileOutputStream(f))
  } { outStream => 
    guard.acquire >> IO(outStream.close()).handleErrorWith(_ => IO.unit) >> guard.release
  }

import cats.effect.Concurrent
def inputOutputStreams(in: File, out: File, guard: Semaphore[IO]): Resource[IO, (InputStream, OutputStream)] = 
  for {
    inStream <- inputStream(in, guard)
    outStream <- outputStream(out, guard)
  } yield (inStream, outStream)

def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] = {
  // IO being a monad, we can sequence them using a for-comprehension to create another IO
  // >> is a Cats operator to sequence two operations where the output of the first is not needed by the second (i.e. it is equivalent to first.flatMap(_ => second))
  // as IO is stack safe we are not concerned about stack overflow issues after we recursively call transmit again
  for {
    amount <- IO(origin.read(buffer, 0, buffer.size))
    count <- if (amount > -1) IO(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount) else IO.pure(acc)
  } yield count
}
def transfer(origin: InputStream, destination: OutputStream): IO[Long] = {
  for {
    buffer <- IO(new Array[Byte](1024 * 10))
    total <- transmit(origin, destination, buffer, 0L)
  } yield total
}

// def copy(origin: File, destination: File)(implicit concurrent: Concurrent[IO]): IO[Long] = inputOutputStreams(origin, destination).use {
//   case (in, out) => transfer(in, out)
// }

def copy(origin: File, destination: File)(implicit concurrent: Concurrent[IO]): IO[Long] = {
  // The use call ensures that the semaphore will be released under any circumstances, whatever the result of transfer (success, error, or cancellation).
  for {
    guard <- Semaphore[IO](1)
    count <- inputOutputStreams(origin, destination, guard).use{case (in, out) => guard.acquire >> transfer(in, out) } // todo guard.withPermit ???
  } yield count
}


// The new method transfer will perform the actual copying of data, once the resources (the streams) are obtained. When they are not needed anymore, whatever the outcome of transfer (success or failure) both streams will be closed. If any of the streams could not be obtained, then transfer will not be run. Even better, because of Resource semantics, if there is any problem opening the input file then the output file will not be opened. On the other hand, if there is any issue opening the output file, then the input stream will be closed.

// There are three stages when using bracket: resource acquisition, usage, and release. 
// Each stage is defined by an IO instance. 
// A fundamental property is that the release stage will always be run regardless whether the usage stage finished correctly or an exception was thrown during its execution. 
// In our case, in the acquisition stage we would create the streams, then in the usage stage we will copy the contents, and finally in the release stage we will close the streams.
// To solve this we should first get the first stream with one bracket call, and then the second stream with another bracket call inside the first. But, in a way, that’s precisely what we do when we flatMap instances of Resource. And the code looks cleaner too. So, while using bracket directly has its place, Resource is likely to be a better choice when dealing with multiple resources at once.
def copy2(origin: File, destination: File): IO[Long] = {
  val inIO: IO[InputStream] = IO(new FileInputStream(origin))
  val outIO: IO[OutputStream] = IO(new FileOutputStream(destination))
  (inIO, outIO)
    .tupled
    .bracket {
      case (in, out) => 
        transfer(in, out)
    } {case (in, out) => // When using bracket, if there is a problem when getting resources in the first stage, then the release stage will not be run
      (IO(in.close()), IO(out.close()))
        .tupled
        .handleErrorWith(_ => IO.unit).void
    }
}

// If any exception is raised when transfer is running, then the streams will be automatically closed by Resource. 
// But there is something else we have to take into account: IO instances execution can be canceled!.
// And cancellation should not be ignored, as it is a key feature of cats-effect

// In cats-effect, some IO instances can be canceled ( e.g. by other IO instaces running concurrently) meaning that their evaluation will be aborted. 
// If the programmer is careful, an alternative IO task will be run under cancellation, for example to deal with potential cleaning up activities.
// Now, IOs created with Resource.use can be canceled. 
// The cancellation will trigger the execution of the code that handles the closing of the resource. In our case, that would close both streams. 
// So far so good! But what happens if cancellation happens while the streams are being used? 
// This could lead to data corruption as a stream where some thread is writing to is at the same time being closed by another thread.
// Must use some concurrency control mechanism that ensures that no stream will be closed while the IO returned by transfer is being evaluated. Cats-effect provides several constructs for controlling concurrency, for this case we will use a semaphore. A semaphore has a number of permits, its method .acquire ‘blocks’ if no permit is available until release is called on the same semaphore. It is important to remark that there is no actual thread being really blocked, the thread that finds the .acquire call will be immediately recycled by cats-effect. When the release method is invoked then cats-effect will look for some available thread to resume the execution of the code after .acquire.

// Use IOApp as it allows to maintain purity in our definitions up to the program main function.
// IOApp is a kind of ‘functional’ equivalent to Scala’s App, where instead of coding an effectful main method we code a pure run function. When executing the class a main method defined in IOApp will call the run function we have coded. Any interruption (like pressing Ctrl-c) will be treated as a cancellation of the running IO. Also IOApp provides implicit instances of Timer[IO] and ContextShift[IO]
object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = 
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files")) else IO.unit
      orig = new File(args(0))
      dest = new File(args(1))
      count <- copy(orig, dest)
      _ <- IO(println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"))
    } yield ExitCode.Success
}

// As IO implements MonadError we can at any moment call to IO.raiseError to interrupt a sequence of IO operations.

// todo
// https://typelevel.org/cats-effect/tutorial/tutorial.html