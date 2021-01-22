sealed trait Maybe[A]
final case class Just[A](v: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]

sealed trait \/[A, B]
final case class -\/[A, B](v: A) extends \/[A, B]
final case class \/-[A, B](v: B) extends \/[A, B]
type Either[A, B] = A \/ B

sealed trait Validation[A, B]
final case class Failure[A, B](v: A) extends Validation[A, B]
final case class Success[A, B](v: B) extends Validation[A, B]
