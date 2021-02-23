package cats.effects.concurrent

import cats.effect.concurrent._
import cats.effect.{ContextShift, IO, IOApp}

import scala.concurrent.ExecutionContext

// An MVar2 is a mutable location that can be empty or contain a value, asynchronously blocking reads when empty and blocking writes when full.
//  - a pure concurrent queue
//  - synchronization and mutual exclusion
//  - a mutable location that can be empty
//  - queue of buffer size 1
//  - reads block on empty
//  - writes block on full
//  - e.g. asynchronous lock, synchronized mutable variable, producer/consumer channel

// Use-cases:
//  - As synchronized, thread-safe mutable variables
//  - As channels, with take and put acting as “receive” and “send”
//  - As a binary semaphore, with take and put acting as “acquire” and “release”

object mv {
  abstract class MVar2[F[_], A] {
    def put(a: A): F[Unit]
    def swap(b: A): F[A]
    def take: F[A]
    def read: F[A]

    def tryPut(a: A): F[Boolean]
    def tryTake: F[Option[A]]
    def tryRead: F[Option[A]]
  }
  // MVar2 adds new methods to the MVar interface without breaking binary compatibility. Please upgrade to MVar2 that supports tryRead and swap
}

// It has these fundamental (atomic) operations:
//
//put: fills the MVar if it is empty, or blocks (asynchronously) if the MVar is full, until the given value is next
// in line to be consumed on take
//take: tries reading the current value (also emptying it), or blocks (asynchronously) until there is a value
// available, at which point the operation resorts to a take followed by a put
//read: which reads the current value without modifying the MVar, assuming there is a value available, or otherwise
// it waits until a value is made available via put
//swap: put a new value and return the taken one. It is not atomic.
//tryRead: returns the value immediately and None if it is empty.
//tryPut and tryTake variants of the above, that try those operation once and fail in case (semantic) blocking would
// be involved

// In this context “asynchronous blocking” means that we are not blocking any threads. Instead the implementation uses callbacks to notify clients when the operation has finished (notifications exposed by means of Async or Concurrent data types such as IO) and it thus works on top of JavaScript as well.

// Inspiration
// Appropriate for building synchronization primitives and performing simple interthread communication, it’s the equivalent of a BlockingQueue(capacity = 1), except that there’s no actual thread blocking involved and it is powered by data types such as IO.

// Use-case: Synchronized Mutable Variables
object MVarApp1 extends App {
  import cats.effect.concurrent._
  def sum(state: MVar2[IO, Int], xs: List[Int]): IO[Int] =
    xs match {
      case Nil => state.take
      case x :: tail =>
        state.take.flatMap { current =>
          state.put(current + x).flatMap(_ => sum(state, tail))
        }
    }

//  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  // todo fix this
//  MVar.of[IO, Int](0).flatMap(sum(_, (0 until 100).toList))
}

// https://titanwolf.org/Network/Articles/Article?AID=0b4d1ea5-8eff-4121-b874-94c05c0df851#gsc.tab=0
final class MLock(mvar: MVar2[IO, Unit]) {
  def acquire: IO[Unit] = mvar.take
  def release: IO[Unit] = mvar.put(())
  def greenLight[A](fa: IO[A]): IO[A] = acquire.bracket(_ => fa)(_ => release)
}
object MLock {
//  def apply(): IO[MLock] =
//    MVar[IO].of(()).map(ref => new MLock(ref))
}

// Use-case: Producer/Consumer Channel
//  Say that you have a producer that needs to push events. But we also need some back-pressure, so we need to wait
//  on the consumer to consume the last event before being able to generate a new event.

object PCApp extends App {
  // Signaling option, because we need to detect completion
  type Channel[A] = MVar2[IO, Option[A]]

  def producer(ch: Channel[Int], xs: List[Int]): IO[Unit] =
    xs match {
      case Nil => ch.put(None)
      case h :: t =>
        ch.put(Some(h)) *> producer(ch, t)
    }

  def consumer(ch: Channel[Int], sum: Long): IO[Long] =
    ch.take.flatMap {
      case Some(x) => consumer(ch, sum + x)
      case None    => IO.pure(sum)
    }

  // ContextShift required for
  // 1) MVar.empty
  // 2) IO.start
  implicit val cs = IO.contextShift(ExecutionContext.Implicits.global)
  val ioa = for {
    ch <- MVar[IO].empty[Option[Int]]
    count = 100000
    pt = producer(ch, (0 until count).toList)
    ct = consumer(ch, 0L)
    fp <- pt.start
    fc <- ct.start
    _ <- fp.join
    sum <- fc.join
  } yield sum
  val r: Long = ioa.unsafeRunSync()
  println(Long)
}
