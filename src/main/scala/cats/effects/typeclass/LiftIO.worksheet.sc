// LiftIO
// A Monad that can convert any given IO[A] into a F[A], useful for defining parametric signatures and composing monad transformer stacks.

object w {
  import cats.effect.IO
  trait LiftIO[F[_]] {
    def liftIO[A](ioa: IO[A]): F[A]
  }
}

import scala.concurrent.Future
import cats.effect.{LiftIO, IO}
type MyEffect[A] = Future[Either[Throwable, A]]
implicit def myEffectLiftIO: LiftIO[MyEffect] =
  new LiftIO[MyEffect] {
    override def liftIO[A](ioa: IO[A]): MyEffect[A] =
      ioa.attempt.unsafeToFuture()
  }
val ioa: IO[String] = IO("Hello world")
val effect: MyEffect[String] = LiftIO[MyEffect].liftIO(ioa)

import cats.data.EitherT
import scala.concurrent.ExecutionContext.Implicits.global

val L = implicitly[LiftIO[MyEffect]]

val svc1: MyEffect[Int] = Future.successful(Right(12))
val svc2: MyEffect[Boolean] = Future.successful(Right(true))
val svc3: MyEffect[String] = Future.successful(Left(new Exception("boom")))
val program: MyEffect[String] =
  (for {
    _ <- EitherT(svc1)
    x <- EitherT(svc2)
    y <- EitherT(if (x) L.liftIO(IO("from io")) else svc3)
  } yield y).value
