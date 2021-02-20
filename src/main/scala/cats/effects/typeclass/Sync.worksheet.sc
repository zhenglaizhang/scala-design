// Sync
//  - A Monad that can suspend the execution of side effects in the F[_] context.

object w {
  // import cats.Defer
  // import cats.effect.effect.std.BracketThrow
  // trait Sync[F[_]] extends BracketThrow[F] with Defer[F] {
  //   def suspend[A](thunk: => F[A]): F[A]
  //   def delay[A](thunk: => A): F[A] = suspend(pure(thunk))
  // }
}
// Its implementation of flatMap is stack safe, meaning that you can describe tailRecM in terms of it as demonstrated in the laws module.
import cats.effect.{IO, Sync}
import cats.effect.unsafe.implicits.global
val ioa = Sync[IO].delay(println("Hello world"))
ioa.unsafeRunSync()

val F = Sync[IO]
lazy val stackSafetyRepeatedRightBinds = {
  val result = (0 until 10000).foldRight(F.delay(())) { (_, acc) => F.delay(()).flatMap(_ => acc)
    result <-> F.pure(())
  }
}
// stackSafetyRepeatedRightBinds

Sync[IO].defer(IO(println("hello world"))).unsafeRunSync()

// using Sync[IO].delay is equivalent to using IO.apply.
// The use of suspend is useful for trampolining (i.e. when the side effect is conceptually the allocation of a stack frame) and it’s used by delay to represent an internal stack of calls. Any exceptions thrown by the side effect will be caught and sequenced into the F[_] context.
