package cats.effects.datatypes

import cats.effect.{ContextShift, Sync}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

// ContextShift is the pure equivalent to:
//  - Scala’s ExecutionContext
//  - Java’s Executor
//  - JavaScript’s setTimeout(0) or setImmediate
//It provides the means to do cooperative yielding, or on top of the JVM to switch thread-pools for execution of
// blocking operations or other actions that are problematic.
object w {
  import scala.concurrent.ExecutionContext
  trait ContextShift[F[_]] {
    def shift: F[Unit]
    def evalOn[A](ec: ExecutionContext)(f: F[A]): F[A]
  }
  // Important: this is NOT a type class, meaning that there is no coherence restriction. This is because the ability
  // to customize the thread-pool used for shift is essential on top of the JVM at least.
}

// The shift operation is an effect that triggers a logical fork.
//
//For example, say we wanted to ensure that the current thread isn’t occupied forever on long running operations, we
// could do something like this:
import cats.effect._
import cats.syntax.all._
object ContextShift1 extends App {
  def fib[F[_]](n: Int, a: Long = 0, b: Long = 1)(implicit
      F: Sync[F],
      cs: ContextShift[F]
  ): F[Long] = {

    F.suspend {
      val next =
        if (n > 0) fib(n - 1, b, a + b)
        else F.pure(a)

      // Triggering a logical fork every 100 iterations
      if (n % 100 == 0)
        cs.shift *> next
      else
        next
    }
  }
}

// evalOn
// The evalOn operation is about executing a side effectful operation on a specific ExecutionContext, but then
// “return” to the “default” thread-pool or run-loop for the bind continuation.
object CS2 extends IOApp {
  def blockingThreadPool[F[_]](implicit
      F: Sync[F]
  ): Resource[F, ExecutionContext] =
    Resource(
      F.delay {
        val executor = Executors.newCachedThreadPool()
        val ec = ExecutionContext.fromExecutor(executor)
        (ec, F.delay(executor.shutdown()))
      }
    )

  def readName[F[_]](implicit F: Sync[F]): F[String] =
    F.delay {
      println("Enter your name: ")
      scala.io.StdIn.readLine()
    }

  def run(args: List[String]): IO[ExitCode] = {
    val name = blockingThreadPool[IO].use { ec =>
      contextShift.evalOn(ec)(readName[IO])
    }
    for {
      n <- name
      _ <- IO(println(s"Hello, $n"))
    } yield ExitCode.Success
  }
}

// Blocker
// Blocker provides an ExecutionContext that is intended for executing blocking tasks and integrates directly with ContextShift
// In this version, Blocker was passed as an argument to readName to ensure the constructed task is never used on a
// non-blocking execution context.
object CS3 extends IOApp {
  def readName[F[_]: Sync: ContextShift](blocker: Blocker): F[String] =
    // blocking operation, executed on special thread-pool
    blocker.delay {
      println("Enter your name: ")
      scala.io.StdIn.readLine()
    }

  def run(args: List[String]): IO[ExitCode] = {
    val name = Blocker[IO].use { blocker =>
      readName[IO](blocker)
    }
    for {
      n <- name
      _ <- IO(println(s"Hello, $n"))
    } yield ExitCode.Success
  }
}
