// IO
//  - A data type for encoding side effects as pure values, capable of expressing both synchronous and asynchronous computations.
//  - A value of type IO[A] is a computation which, when evaluated, can perform effects before returning a value of type A.
//  - IO values are pure, immutable values and thus preserves referential transparency, being usable in functional programming.
//  - An IO is a data structure that represents just a description of a side effectful computation.

// IO can describe synchronous or asynchronous computations that:
//  - on evaluation yield exactly one result
//  - can end in either success or failure and in case of failure flatMap chains get short-circuited (IO implementing the algebra of MonadError)
//  - can be canceled, but note this capability relies on the user to provide cancellation logic

// Effects described via this abstraction are not evaluated until the “end of the world”, which is to say, when one of the “unsafe” methods are used. Effectful results are not memoized, meaning that memory overhead is minimal (and no leaks), and also that a single effect may be run multiple times in a referentially-transparent manner

import cats.effect.IO
val ioa = IO { println("hey") }
val program: IO[Unit] =
  for {
    _ <- ioa
    _ <-
      ioa //  the effect re-runs each time it is sequenced in the monadic chain.
  } yield ()

program.unsafeRunSync()
program.unsafeRunSync()

// IO can suspend side effects and is thus a lazily evaluated data type
//
//                Eager                       Lazy
// Synchronous     A                        () => A
// Asynchronous   (A => Unit) => Unit       () => (A => Unit) => Unit
//                Future[A]                  IO[A]
//
// In comparison with Scala’s Future, the IO data type preserves referential transparency even when dealing with side effects and is lazily evaluated.
// In an eager language like Scala, this is the difference between a result and the function producing it.
// Similar with Future, with IO you can reason about the results of asynchronous processes,
// but due to its purity and laziness IO can be thought of as a specification (to be evaluated at the “end of the world”), yielding more control over the evaluation model and being more predictable, for example when dealing with sequencing vs parallelism, when composing multiple IOs or when dealing with failure.

def addToGauge(n: Int): IO[Int] = IO(n)
for {
  _ <- addToGauge(32)
  _ <- addToGauge(32)
} yield ()

// Note laziness goes hand in hand with referential transparency
// If we have referential transparency, we can rewrite that example as:
val task = addToGauge(32)
for {
  _ <- task
  _ <- task
} yield ()
// This doesn’t work with Future, but works with IO and this ability is essential for functional programming.

// Stack safety
// IO is trampolined in its flatMap evaluation. This means that you can safely call flatMap in a recursive function of arbitrary depth, without fear of blowing the stack:
def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
  IO(a + b).flatMap { b2 =>
    if (n > 0) fib(n - 1, b, b2)
    else IO.pure(a)
  }

fib(5).unsafeRunSync()

// IO implements all the typeclasses in https://typelevel.org/cats-effect/typeclasses/

// IO is a potent abstraction that can efficiently describe multiple kinds of effects:

// Pure Values — IO.pure & IO.unit
//  - You can lift pure values into IO, yielding IO values that are “already evaluated”

// lift a number (pure value) into IO and compose it with another IO that wraps a side a effect in a safe manner, as nothing is going to be executed
IO.pure(25).flatMap(n => IO(println(s"Number is: $n")))

// IO.pure cannot suspend side effects, because IO.pure is eagerly evaluated, with the given parameter being passed by value, so don’t do this:
IO.pure(println("THIS IS WRONG"))
// println will trigger a side effect that is not suspended in IO
val unit: IO[Unit] = IO.pure(())

// Given IO[Unit] is so prevalent in Scala code, the Unit type itself being meant to signal completion of side effectful routines,
// this proves useful as a shortcut and as an optimization, since the same reference is returned.

// Synchronous Effects — IO.apply
//  def apply[A](body: => A): IO[A] = ???
// Note the given parameter is passed ‘‘by name’’, its execution being “suspended” in the IO context.
// It’s probably the most used builder and the equivalent of Sync[IO].delay, describing IO operations that can be evaluated immediately, on the current thread and call-stack:
def putStrLn(s: String): IO[Unit] = IO(println(s))
val readLn = IO(scala.io.StdIn.readLine())
for {
  _ <- putStrLn("What's your name?")
  n <- readLn
  _ <- putStrLn(s"Hello, $n")
} yield ()
readLn.unsafeRunSync()

// Asynchronous Effects — IO.async & IO.cancelable
//  - IO.async is the operation that complies with the laws of Async#async (see Async) and can describe simple asynchronous processes that cannot be canceled
//    def async[A](k: (Either[Throwable, A] => Unit) => Unit): IO[A] = ???
// The provided registration function injects a callback that you can use to signal either successful results (with Right(a)), or failures (with Left(error)). Users can trigger whatever asynchronous side effects are required, then use the injected callback to signal completion.

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}

def convert[A](fa: => Future[A])(implicit ec: ExecutionContext): IO[A] =
  IO.async { cb: (Either[Throwable, A] => Unit) =>
    // This triggers evaluation of the by-name param and of onComplete,
    // so it's OK to have side effects in this callback
    fa.onComplete {
      case Failure(e) => cb(Left(e))
      case Success(a) => cb(Right(a))
    }
  }

// “Unsafe” Operations
//  - All of the operations prefixed with unsafe are impure functions and perform side effects
//  - You should write your programs in a monadic way using functions such as map and flatMap to compose other functions and ideally you should just call one of these unsafe operations only once, at the very end of your program.

//convert(Future.successful(12))
// unsafeRunSync
//  - Produces the result by running the encapsulated effects as impure side effects.
//
//If any component of the computation is asynchronous, the current thread will block awaiting the results of the
// async computation.
IO(println("Sync!")).unsafeRunSync()

// IO.never
// IO.never represents a non-terminating IO defined in terms of async, useful as shortcut and as a reusable reference:

// Keep Granularity
// - It’s better to keep the granularity, so please don’t do something like this:
// IO {
//  readingFile
//  writingToDatabase
//  sendBytesOverTcp
//  launchMissiles
//}
//
// In FP we embrace reasoning about our programs and since IO is a Monad you can compose bigger programs from small
// ones in a for-comprehension. For example:
//
//val program =
//  for {
//    data <- readFile
//    _ <- writeToDatabase(data)
//    _ <- sendBytesOverTcp(data)
//    _ <- launchMissiles
//  } yield ()

// Use pure functions in map / flatMap
//  - When using map or flatMap it is not recommended to pass a side effectful function, as mapping functions should
//  also be pure. So this should be avoided:
IO.pure(123).map(n => println(s"NOT RECOMMENDED! $n"))
IO.pure(123).flatMap { n =>
  println(s"NOT RECOMMENDED! $n")
  IO.unit
}

IO.pure(123)
  .flatMap { n =>
    // Properly suspending the side effect
    IO(println(s"RECOMMENDED! $n"))
  }
  .unsafeRunSync()

IO(println("async")).unsafeRunAsync(_ => ())
