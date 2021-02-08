// It would be nice to have all of web form signup errors be reported simultaneously.
// Validated is very similar to Either because it also has two possible values: errors on the left side or successful
// computations on the right side.
// Validated datatype, as with Either has two projections: Valid and Invalid, analogous to Right and Left, respectively

// an error-accumulating data type such as Validated can’t have a valid Monad instance. Sometimes the task at hand
// requires error-accumulation. However, sometimes we want a monadic structure that we can use for sequential
// validation (such as in a for-comprehension).
// Cats has decided to solve this problem by using separate data structures for error-accumulation (Validated) and
// short-circuiting monadic behavior (Either).

// todo why extends Product with Serializable
object w {

  sealed abstract class Validated[+E, +A] extends Product with Serializable {}

  // And its projections:
  final case class Valid[+A](a: A) extends Validated[Nothing, A]

  final case class Invalid[+E](e: E) extends Validated[E, Nothing]

}

final case class RegistrationData(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    age: Int
)

sealed trait DomainValidation {
  def errorMessage: String
}

case object UsernameHasSpecialCharacters extends DomainValidation {
  def errorMessage: String = "Username cannot contain special characters."
}

case object PasswordDoesNotMeetCriteria extends DomainValidation {
  def errorMessage: String =
    "Password must be at least 10 characters long, including an uppercase and a lowercase letter, one number and one " +
      "special character."
}

case object FirstNameHasSpecialCharacters extends DomainValidation {
  def errorMessage: String =
    "First name cannot contain spaces, numbers or special characters."
}

case object LastNameHasSpecialCharacters extends DomainValidation {
  def errorMessage: String =
    "Last name cannot contain spaces, numbers or special characters."
}

case object AgeIsInvalid extends DomainValidation {
  def errorMessage: String =
    "You must be aged 18 and not older than 75 to use our services."
}

import cats.implicits._

sealed trait FormValidator {
  def validateUserName(userName: String): Either[DomainValidation, String] =
    Either.cond(
      userName.matches("^[a-zA-Z0-9]+$"),
      userName,
      UsernameHasSpecialCharacters
    )

  def validatePassword(password: String): Either[DomainValidation, String] =
    Either.cond(
      password.matches(
        "(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"
      ),
      password,
      PasswordDoesNotMeetCriteria
    )

  def validateFirstName(firstName: String): Either[DomainValidation, String] =
    Either.cond(
      firstName.matches("^[a-zA-Z]+$"),
      firstName,
      FirstNameHasSpecialCharacters
    )

  def validateLastName(lastName: String): Either[DomainValidation, String] =
    Either.cond(
      lastName.matches("^[a-zA-Z]+$"),
      lastName,
      LastNameHasSpecialCharacters
    )

  def validateAge(age: Int): Either[DomainValidation, Int] =
    Either.cond(
      age >= 18 && age <= 75,
      age,
      AgeIsInvalid
    )

  def validateForm(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      age: Int
  ): Either[DomainValidation, RegistrationData] = {

    // A for-comprehension is fail-fast.
    for {
      validatedUserName <- validateUserName(username)
      validatedPassword <- validatePassword(password)
      validatedFirstName <- validateFirstName(firstName)
      validatedLastName <- validateLastName(lastName)
      validatedAge <- validateAge(age)
    } yield RegistrationData(
      validatedUserName,
      validatedPassword,
      validatedFirstName,
      validatedLastName,
      validatedAge
    )
  }
}

object FormValidator extends FormValidator

FormValidator.validateForm(
  username = "fakeUs3rname",
  password = "password",
  firstName = "John",
  lastName = "Doe",
  age = 15
)

// An iteration with Validated

import cats.data._
import cats.data.Validated._
import cats.implicits._

def validateUserName(userName: String): Validated[DomainValidation, String] =
  FormValidator.validateUserName(userName).toValidated

def validatePassword(password: String): Validated[DomainValidation, String] =
  FormValidator.validatePassword(password).toValidated

def validateFirstName(firstName: String): Validated[DomainValidation, String] =
  FormValidator.validateFirstName(firstName).toValidated

def validateLastName(lastName: String): Validated[DomainValidation, String] =
  FormValidator.validateLastName(lastName).toValidated

def validateAge(age: Int): Validated[DomainValidation, Int] =
  FormValidator.validateAge(age).toValidated

def validateForm(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    age: Int
): Validated[DomainValidation, RegistrationData] = {
  ???
  // a for-comprehension uses the flatMap method for composition.
  // Monads like Either can be composed in that way,
  // but the thing with Validated is that it isn’t a monad, but an Applicative Functor.
  // That’s why you see the message: error: value flatMap is not a member of cats.data.Validated[DomainValidation,
  // String]
  //  for {
  //    validatedUserName <- validateUserName(username)
  //    validatedPassword <- validatePassword(password)
  //    validatedFirstName <- validateFirstName(firstName)
  //    validatedLastName <- validateLastName(lastName)
  //    validatedAge <- validateAge(age)
  //  } yield RegistrationData(
  //    validatedUserName,
  //    validatedPassword,
  //    validatedFirstName,
  //    validatedLastName,
  //    validatedAge
  //  )
}

// A for-comprehension plays well in a fail-fast scenario,
// but the structure in our previous example was designed to catch one error at a time,
// so, our next step is to tweak the implementation a bit.
sealed trait FormValidatorNec {
  type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private def validateUserName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNec
    else UsernameHasSpecialCharacters.invalidNec

  private def validatePassword(password: String): ValidationResult[String] =
    if (
      password.matches(
        "(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"
      )
    ) password.validNec
    else PasswordDoesNotMeetCriteria.invalidNec

  private def validateFirstName(firstName: String): ValidationResult[String] =
    if (firstName.matches("^[a-zA-Z]+$")) firstName.validNec
    else FirstNameHasSpecialCharacters.invalidNec

  private def validateLastName(lastName: String): ValidationResult[String] =
    if (lastName.matches("^[a-zA-Z]+$")) lastName.validNec
    else LastNameHasSpecialCharacters.invalidNec

  private def validateAge(age: Int): ValidationResult[Int] =
    if (age >= 18 && age <= 75) age.validNec else AgeIsInvalid.invalidNec

  def validateForm(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      age: Int
  ): ValidationResult[RegistrationData] = {
    (
      validateUserName(username),
      validatePassword(password),
      validateFirstName(firstName),
      validateLastName(lastName),
      validateAge(age)
    ).mapN(RegistrationData)
  }
}

object FormValidatorNec extends FormValidatorNec

// NonEmptyChain, a data structure that guarantees that at least one element will be present.
// ValidatedNec[DomainValidation, A] is an alias for Validated[NonEmptyChain[DomainValidation], A]
// .validNec and .invalidNec combinators lets you lift the success or failure in their respective container (either a
// Valid or Invalid[NonEmptyChain[A]]).
// The applicative syntax (a, b, c, ...).mapN(...) provides us a way to accumulatively apply the validation functions
// and yield a product with their successful result or the accumulated errors in the NonEmptyChain. Then, we
// transform that product with mapN into a valid instance of RegistrationData.

// since Cats 1.0.0-MF the cartesian syntax |@| for applicatives is deprecated.
FormValidatorNec.validateForm(
  username = "Joe",
  password = "Passw0r$1234",
  firstName = "John",
  lastName = "Doe",
  age = 21
)
FormValidatorNec.validateForm(
  username = "Joe%%%",
  password = "password",
  firstName = "John",
  lastName = "Doe",
  age = 21
)

// Accumulative Structures
// you can define your own accumulative data structure and you’re not limited to the aforementioned construction
// For doing this, you have to provide a Semigroup instance. NonEmptyChain, by definition has its own Semigroup.
NonEmptyChain.one[DomainValidation](
  UsernameHasSpecialCharacters
) |+| NonEmptyChain(FirstNameHasSpecialCharacters, LastNameHasSpecialCharacters)

FormValidatorNec
  .validateForm(
    username = "Joe",
    password = "Passw0r$1234",
    firstName = "John",
    lastName = "Doe",
    age = 21
  )
  .toEither
FormValidatorNec
  .validateForm(
    username = "Joe123#",
    password = "password",
    firstName = "John",
    lastName = "Doe",
    age = 5
  )
  .toEither

// Perhaps you’re reading from a configuration file. One could imagine the configuration library you’re using returns
// a scala.util.Try, or maybe a scala.util.Either. Your parsing may look something like:
//for {
//  url <- config[String]("url")
//  port <- config[Int]("port")
//} yield ConnectionParams(url, port)

// Our goal is to report any and all errors across independent bits of data
trait Read[A] {
  def read(s: String): Option[A]
}

object Read {
  def apply[A](implicit A: Read[A]): Read[A] = A

  implicit val stringRead: Read[String] = (s: String) => Some(s)
  implicit val intRead: Read[Int] = (s: String) =>
    if (s.matches("-?[0-9]+")) Some(s.toInt)
    else None
}

sealed abstract class ConfigError

final case class MissingConfig(field: String) extends ConfigError

final case class ParseError(field: String) extends ConfigError

//sealed abstract class Validated[+E, +A]
//object Validated {
//  final case class Valid[+A](a: A) extends Validated[Nothing, A]
//  final case class Invalid[+E](e: E) extends Validated[E, Nothing]
//}

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

case class Config(map: Map[String, String]) {
  def parse[A: Read](key: String): Validated[ConfigError, A] =
    map.get(key) match {
      case None => Invalid(MissingConfig(key))
      case Some(value) =>
        Read[A].read(value) match {
          case None    => Invalid(ParseError(key))
          case Some(a) => Valid(a)
        }
    }
}

// Recall that we can only do parallel validation if each piece is independent.
// How do we enforce the data is independent? By asking for all of it up front.

import cats.Semigroup

def parallelValidate[E: Semigroup, A, B, C](
    v1: Validated[E, A],
    v2: Validated[E, B]
)(f: (A, B) => C): Validated[E, C] = {
  (v1, v2) match {
    case (Valid(a), Valid(b))       => Valid(f(a, b))
    case (Valid(_), i @ Invalid(_)) => i
    case (i @ Invalid(_), Valid(_)) => i
    case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
  }
}

import cats.SemigroupK
import cats.data.NonEmptyChain
import cats.implicits._

case class ConnectionParams(url: String, port: Int)

val config = Config(Map(("endpoint", "127.0.0.1"), ("port", "not an int")))
implicit val necSemigroup: Semigroup[NonEmptyChain[ConfigError]] =
  SemigroupK[NonEmptyChain].algebra[ConfigError]
implicit val readString: Read[String] = Read.stringRead
implicit val readInt: Read[Int] = Read.intRead

val v1 = parallelValidate(
  config.parse[String]("url").toValidatedNec,
  config.parse[Int]("port").toValidatedNec
)(ConnectionParams.apply)
val v2 = parallelValidate(
  config.parse[String]("endpoint").toValidatedNec,
  config.parse[Int]("port").toValidatedNec
)(ConnectionParams.apply)
val config = Config(Map(("endpoint", "127.0.0.1"), ("port", "1234")))
val v3 = parallelValidate(
  config.parse[String]("endpoint").toValidatedNec,
  config.parse[Int]("port").toValidatedNec
)(ConnectionParams.apply)

// Our parallelValidate function looks awfully like the Apply#map2 function.
//  - def map2[F[_], A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
//  - Which can be defined in terms of Apply#ap and Apply#map, the very functions needed to create an Apply instance.
// the goodness of Applicative, which includes map{2-22}, as well as the Semigroupal tuple syntax.

// Sequential Validation
// The andThen method is similar to flatMap (such as Either.flatMap).
config.parse[Int]("house_number").andThen { n =>
  if (n >= 0) Validated.valid(n)
  else Validated.invalid(ParseError("house_number"))
}
// The withEither method allows you to temporarily turn a Validated instance into an Either instance and apply it to
// a function.
def positive(field: String, i: Int): Either[ConfigError, Int] = {
  if (i >= 0) Right(i)
  else Left(ParseError(field))
}
val houseNumber = config.parse[Int]("house_number").withEither {
  either: Either[ConfigError, Int] =>
    either.flatMap { i =>
      positive("house_number", i)
    }
}
