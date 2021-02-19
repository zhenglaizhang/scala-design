import cats.data.OptionT
import scala.concurrent.Future
import scala.util.Try
// EitherT[F, A, B] is the monad transformer for Either — you can think of it as a wrapper over a F[Either[A, B]] value.
// when Either is placed into effectful types such as Option orFuture, a large amount of boilerplate is required to handle errors.
// EitherT[F[_], A, B] is a lightweight wrapper for F[Either[A, B]] that makes it easy to compose Eithers and Fs together.

import cats.data.EitherT
import cats.implicits._
import scala.concurrent.ExecutionContext.Implicits.global

def parseDouble(s: String): Either[String, Double] =
  Try(s.toDouble).map(Right(_)).getOrElse(Left(s"$s is not a number"))

def divide(a: Double, b: Double): Either[String, Double] =
  Either.cond(b != 0, a / b, "Cannot divide by 0")

def parseDoubleAsync(s: String): Future[Either[String, Double]] =
  Future.successful(parseDouble(s))

def divideAsync(a: Double, b: Double): Future[Either[String, Double]] =
  Future.successful(divide(a, b))

def divisionProgramAsync(
    inputA: String,
    inputB: String
): EitherT[Future, String, Double] =
  for {
    a <- EitherT(parseDoubleAsync(inputA))
    b <- EitherT(parseDoubleAsync(inputB))
    result <- EitherT(divideAsync(a, b))
  } yield result

divisionProgramAsync("3", "2").value
divisionProgramAsync("a", "b").value

// From A or B to EitherT[F, A, B]
val number: EitherT[Option, String, Int] = EitherT.rightT(5)
val error: EitherT[Option, String, Int] = EitherT.leftT("Not a number")

// From F[A] or F[B] to EitherT[F, A, B]
val myOpt: Option[Int] = None
val myOptionList: List[Option[Int]] =
  List(None, Some(1), Some(2), None, Some(5))
val myOptionET = EitherT.fromOption[Future](myOpt, "option not defined")

val numberO: Option[Int] = Some(5)
val errorO: Option[String] = Some("Not a number")

val number: EitherT[Option, String, Int] = EitherT.right(numberO)
val error: EitherT[Option, String, Int] = EitherT.left(errorO)

//From Either[A, B] or F[Either[A, B]] to EitherT[F, A, B]
// Use EitherT.fromEither to lift a value of Either[A, B] into EitherT[F, A, B]. An F[Either[A, B]] can be converted
// into EitherT using the EitherT constructor.
val numberE: Either[String, Int] = Right(100)
val errorE: Either[String, Int] = Left("Not a number")
val numberFE: List[Either[String, Int]] = List(Right(250))

val numberET: EitherT[List, String, Int] = EitherT.fromEither(numberE)
val errorET: EitherT[List, String, Int] = EitherT.fromEither(errorE)
val numberFET: EitherT[List, String, Int] = EitherT(numberFE)

// From Option[B] or F[Option[B]] to EitherT[F, A, B]
// An Option[B] or an F[Option[B]], along with a default value, can be passed to EitherT.fromOption and EitherT
// .fromOptionF, respectively, to produce an EitherT. For F[Option[B]] and default F[A], there is EitherT.fromOptionM.
val myOption: Option[Int] = None
val myOptionList: List[Option[Int]] =
  List(None, Some(2), Some(3), None, Some(5))
val myOptionET = EitherT.fromOption[Future](myOption, "option not defined")
val myOptionListET = EitherT.fromOptionF(myOptionList, "option not defined")
val myOptionListET = EitherT.fromOptionF(myOptionList, "option not defined")

// From ApplicativeError[F, E] to EitherT[F, E, A]
val myTry: Try[Int] = Try(2)
val myFuture: Future[String] = Future.failed(new Exception())

val myTryET: EitherT[Try, Throwable, Int] = myTry.attemptT
val myFutureET: EitherT[Future, Throwable, String] = myFuture.attemptT

// Extracting an F[Either[A, B]] from an EitherT[F, A, B]
val errorT: EitherT[Future, String, Int] = EitherT.leftT("foo")
val error: Future[Either[String, Int]] = errorT.value


abstract class BaseException(msg: String) extends Exception(msg)
case class UserNotFoundException(msg: String) extends BaseException(msg)
def getUserById(userId: Int): Future[Option[User]] = ???
def ensureUserExists(userId: Int): EitherT[Future, BaseException, User] = {
  OptionT(getUserById(userId)).toRight(left => UserNotFoundException(s"user not found, userId=$userId"))
}

case class Item(state: String)
class ItemOrder  { /* ... */ }

case class ItemNotFoundException(message: String) extends BaseException(message)
case class InvalidItemStateException(message: String) extends BaseException(message)

def getItemById(itemId: Int): Future[Option[Item]] = { /* .. */ ??? }

def ensureItemExists(itemId: Int): EitherT[Future, BaseException, Item] = {
  OptionT(getItemById(itemId))
    .toRight(ItemNotFoundException(s"item not found, itemId = $itemId"))
}

def ensureItemStateIs(actual: String, expected: String): EitherT[Future, BaseException, Unit] = {
  // Returns a Unit value wrapped into Right and then into Future if condition is true,
  // otherwise the provided exception wrapped into Left and then into Future.
  EitherT.cond(actual == expected, (), InvalidItemStateException(s"actual=$actual, expected=$expected"))
}

def placeOrderForItem(userId: Int, itemId: Int, count: Int): Future[ItemOrder] = { /* ... */ ??? }

def buyItem(userId: Int, itemId: Int, count: Int): EitherT[Future, BaseException, ItemOrder] = {
  for {
    user <- ensureUserExists(userId)
    item <- ensureItemExists(itemId)
    _ <- ensureItemStateIs(item.state, "AVAILABLE_IN_STOCK")
    // EitherT.liftF is necessary to make EitherT[Future, BaseException, ItemOrder] out of Future[ItemOrder]
    placedOrder <- EitherT.liftF(placeOrderForItem(userId, itemId, count))
  } yield placedOrder
}