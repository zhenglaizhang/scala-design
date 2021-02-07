package worksheets.typeclass.dataclass

import cats.Monad
import cats.syntax.either._

object EitherMonad {
  implicit def eitherMonad[Err]: Monad[Either[Err, ?]] =
    new Monad[Either[Err, ?]] {
      def flatMap[A, B](
          fa: Either[Err, A]
      )(f: A => Either[Err, B]): Either[Err, B] =
        fa.flatMap(f)

      def pure[A](x: A): Either[Err, A] = Either.right(x)

      @annotation.tailrec
      def tailRecM[A, B](
          a: A
      )(f: A => Either[Err, Either[A, B]]): Either[Err, B] =
        f(a) match {
          case Right(Right(b)) => Either.right(b)
          case Right(Left(a))  => tailRecM(a)(f)
          case l @ Left(_) =>
            l.rightCast[B] // Cast the right type parameter to avoid allocation
        }
    }

}
