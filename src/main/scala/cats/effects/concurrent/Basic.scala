// https://typelevel.org/cats-effect/concurrency/basics.html

package cats.effects.concurrent

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Fiber}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

// Parallelism
//  - two queues, two bowls
//  - Using multiple computational resources (like more processor cores) to perform a computation faster, usually
//  executing at the same time.
//  - Example: summing a list of Integers by dividing it in half and calculating both halves in parallel.
//  - Main concern: efficiency.

// Concurrency
//  - two queues, one bowl
//  - Multiple tasks interleaved. Concurrency doesn’t have to be multithreaded. We can write concurrent applications
//  on single processor using methods such as event loops.
//  - Example: Communicating with external services through HTTP.
//  - Main concern: interaction with multiple, independent and external agents.

// CPU-bound task
//  - Operation that mostly requires processor resources to finish its computation.
// IO-bound task
//  - Operation that mostly does I/O and it doesn’t depend on your computation resources, e.g. waiting for disk
//  operation to finish or external service to answer your request.
// Non-terminating task
//  - Task that will never signal its result. A task can be non-terminating without blocking threads or consuming CPU.

object NonTerminateApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    IO.never *> IO(println("done")).as(ExitCode.Success)
  // The above will never print “done”, block a thread (unless .unsafeRunSync is run on it), or consume CPU after its
  // creation.
}

// Threading (on JVM)
//  - Threads in JVM map 1:1 to the operating system’s native threads
//  - If we try to run too many threads at once we will suffer because of many context switches.
//  - The most efficient situation for CPU-bound tasks is when we execute as many threads as the number of available cores because we can avoid this overhead.
//  - Synchronous execution can have better throughput than parallel execution. If you parallelize it too much, it won’t make your code magically faster. The overhead of creating or switching threads is often greater than the speedup, so make sure to benchmark.
//  - threads are scarce resource on JVM. If you exploit them at every opportunity it may turn out that your most performance critical parts of the application suffer because the other part is doing a lot of work in parallel, taking precious native threads.

// Thread Pools
//  - Creating a Thread has a price to it.
//  - it involves making too many threads for short-lived tasks is very inefficient . It may turn out that process of creating thread and possible context switches has higher costs than the task itself
//  - A thread pool consists of work queue and a pool of running threads.
//  - java.util.concurrent.Executor
//  - In Scala, we avoid explicitly working with Runnable and use abstractions that do that under the hood (Future and IO implementations). Thread pools can reuse and cache threads to prevent some of the problems mentioned earlier.

// Choosing thread pool
//  https://typelevel.org/cats-effect/img/concurrency-thread-pools.png
//  - computation
//    - work-stealing
//    - cpu-bounded (#cpus)
//    - finite resources
//    - avoid blocking at all costs
//  - event dispatcher
//    - avoid work at all costs
//    - highest priority
//    - 1 or couple of threads
//  - blocking IO
//    - caching
//    - unbounded size
//
//  disclaimer:
//    - test and measure
//    - when it comes to concurrency, nobody has idea, what they are doing

// We can configure thread pools in multiple ways:
//
// Bounded
//  - Limiting number of available threads to certain amount. Example could be newSingleThreadExecutor to execute only one task at the time or limiting number of threads to number of processor cores for CPU-bound tasks.
// Unbounded
//  - No maximum limit of available threads. Note that this is dangerous because we could run out of memory by
//  creating too many threads, so it’s important to use cached pool (allowing to reuse existing threads) with
//  keepalive time (to remove useless threads) and control number of tasks to execute by other means (backpressure,
//  rate limiters).
//    Despite those dangers it is still very useful for blocking tasks. In limited thread pool if we block too many
//    threads which are waiting for callback from other (blocked) thread for a long time we risk getting deadlock that
//    prevents any new tasks from starting their work.

// Blocking Threads
//  - As a rule we should never block threads, but sometimes we have to work with interface that does it. Blocking a
//  thread means that it is being wasted and nothing else can be scheduled to run on it. As mentioned, this can be
//  very dangerous and it’s best to use dedicated thread pool for blocking operations. This way they won’t interfere
//  with CPU-bound part of application.

// Blocker[IO] can be used to safely handle blocking operations in an explicit way.
// In most circumstances use a shared Blocker when carrying out blocking operations.
object BlockerApp extends App {
  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)
  def blockingOp: IO[Unit] = IO(println("a blocking io"))
  def doSth(): IO[Unit] = IO(println("do sth"))
  val program = Blocker[IO].use { blocker =>
    for {
      _ <- blocker.blockOn(
        blockingOp
      ) // execute on blocker, backed by cached thread pool
      _ <- doSth() // executes on contextShift
    } yield ()
  }
}

// Green Threads
// There are more types of threads and they depend on the platform. One of them is green thread. The main difference
// between model represented by JVM Threads and Green Threads is that the latter aren’t scheduled on OS level. They
// are much more lightweight, which allows starting a lot of them without many issues.
//
// They are often characterized by cooperative multitasking which means the thread decides when it’s giving up control
// instead of being forcefully preempted, as happens on the JVM. This term is important for Cats Effect, whose Fiber
// and shift design have a lot of similarities with this model.

// Thread Scheduling
//  - Working with cats.effect.IO you should notice a lot of calls to IO.shift, described in Thread Shifting section
//  in IO documentation
//  - This function allows to shift computation to different thread pool or simply send it to current ExecutionContext to schedule it again. This is often called introducing asynchronous boundary.
//  - The Essential term is thread scheduling. Since we can’t run all our threads in parallel all the time, they each
//  get their own slice of time to execute, interleaving with the rest of them so every thread has a chance to run.
//  When it is time to change threads, the currently running thread is preempted. It saves its state and the context
//  switch happens.
//
// This is a bit different when using thread pools (ExecutionContexts), because they are in charge of scheduling
// threads from their own pool. If there is one thread running, it won’t change until it terminates or higher
// priority thread is ready to start doing work. Note that IO without any shifts is considered one task, so if it’s
// infinite IO, it could hog the thread forever and if we use single threaded pool, nothing else will ever run on it!
//
//In other words, IO is executing synchronously until we call IO.shift or use function like parSequence. In terms of
// individual thread pools, we can actually treat IO like green thread with cooperative multitasking. Instead of
// preemption, we can decide when we yield to other pending fibers from the same pool by calling shift. Calling IO
// .shift schedules the work again, so if there are other IOs waiting to execute, they can have their chance.

// From the thread pool’s point of view, the process of yielding to other fibers can be described like this:
// - When shift is called on some fiber:
//  - Remove that fiber from its current thread and put it in the pool of pending fibers
//  - For each available thread (including the one from the previous step), assign it one of the pending fibers from
//    the pool

// Allowing different fibers to advance their work is called fairness. Let’s illustrate this:
object FairnessApp extends App {
  val ecOne = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val ecTwo = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val csOne: ContextShift[IO] = IO.contextShift(ecOne)
  val csTwo: ContextShift[IO] = IO.contextShift(ecTwo)

  def infiniteIO(id: Int)(cs: ContextShift[IO]): IO[Fiber[IO, Unit]] = {
    def repeat: IO[Unit] = IO(println(id)).flatMap(_ => repeat)
    repeat.start(cs)
  }
  // Note repeat.start and return type of IO[Fiber[IO, Unit]] which means that we run this computation in the background. It will run on thread pool provided by cs, which we will pass explicitly:
  val program: IO[Unit] =
    for {
      _ <- infiniteIO(1)(csOne)
      _ <- infiniteIO(11)(csOne)
    } yield ()
  // It will never print 11 despite using .start! Why? The ecOne execution context executes its IO on the only thread it has, but needs to wait for its completion before it can schedule the other one.

//  program.unsafeRunSync()

  val program2 =
    for {
      _ <- infiniteIO(1)(csOne)
      _ <- infiniteIO(11)(csOne)
      _ <- infiniteIO(2)(csTwo)
      _ <- infiniteIO(22)(csTwo)
    } yield ()
  program2.unsafeRunSync()
  // Now it will keep printing both 1 and 2 but neither 11 nor 22. What changed? Those thread pools are independent
  // and interleave because of thread scheduling done by the operating system. Basically, the thread pool decides
  // which task gets a thread to run but the OS decides what is actually evaluating on the CPU.
}

object RealFairnessApp extends App {
  val ecOne =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val ecTwo =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  val csOne: ContextShift[IO] = IO.contextShift(ecOne)
  val csTwo: ContextShift[IO] = IO.contextShift(ecTwo)
  def infiniteIO(
      id: Int
  )(implicit cs: ContextShift[IO]): IO[Fiber[IO, Unit]] = {
    // Notice the IO.shift *> repeat call. *> means that we execute first operation, ignore its result and then call repeat.
    // Calling IO.shift fixed the problem because when the currently running IO was rescheduled, it gave an opportunity to execute the other one.
    def repeat: IO[Unit] = IO(println(id)).flatMap(_ => IO.shift *> repeat)
    repeat.start
  }

  val prog =
    for {
      _ <- infiniteIO(1)(csOne)
      _ <- infiniteIO(11)(csOne)
      _ <- infiniteIO(2)(csTwo)
      _ <- infiniteIO(22)(csTwo)
    } yield ()

  prog.unsafeRunSync()
}
// Knowing what exactly happens in concurrent scenarios in your application just by reading the piece of code can really speedup debugging process or even allow to get it right the first time.

// Fortunately cats.effect.IO doesn’t always require to do it manually. Operations like race, parMapN or parTraverse
// introduce asynchronous boundary at the beginning, but if you have limited thread pool and long running tasks, keep
// fairness in mind.
//
// Scala’s Future is optimized for fairness, doing shift equivalent after each map or flatMap. We wouldn’t have the
// problem described above but doing it too much results in putting a lot of pressure on scheduler causing low
// throughput. In typical purely functional programs we have many flatMaps because our entire application is just one
// big IO composed of many smaller ones. Constant shifting is not feasible but there’s always the option to do it if
// our application has strict latency requirements.
//
//If you are looking for less manual work - monix.eval.Task is great middleground which by default shifts tasks
// automatically in batches preserving both great throughput and decent latency off the shelf and exposes very rich
// configuration options if you have more advanced use case.

// Asynchronous / Semantic blocking
// Sometimes we use term semantic blocking or asynchronous blocking which is different than blocking thread. It means
// that we suspend our IO/Task waiting for some action to happen (e.g. Deferred.get waits until the result is
// available) without blocking a threads. Other IOs are free to execute on the thread in the meantime. This is
// further explained in Fabio Labella’s comment.
//
// It is important to recognize that not all I/O operations are blocking and need to execute on dedicated thread pool.
// For instance we can have HTTP requests using non-blocking client such as http4s with Blaze, which uses
// non-blocking network I/O and is free to execute on a “normal” pool.
