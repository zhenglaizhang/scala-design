import cats.Semigroupal
import cats.instances.option._

// Combine an `F[A]` and an `F[B]` into an `F[(A, B)]` that maintains the effects of both `fa` and `fb`.
Semigroupal[Option].product(Some(123), Some("abc"))
Semigroupal[Option].product(None, Some(123))
Semigroupal.tuple3(Option(1), Option(2), Option(3))
Semigroupal.tuple3(Option(1), Option(2), Option.empty)
Semigroupal.map3(Option(1), Option(2), Option(3))(_ + _ + _)
Semigroupal.map3(Option.empty[Int], Option(1), Option(2))(_ + _ + _)
