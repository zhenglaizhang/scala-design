// How to deal with data inside nested effects,
// e.g. an integer inside an Either, which in turn is nested inside an Option
import cats.data.Validated.Valid
import cats.data.{Nested, Validated}
import cats.implicits._

import scala.concurrent.{Await, Future}
val x: Option[Validated[String, Int]] = Some(123.valid)
x.map(_.map(_.toString))

// Nested can help with this by composing the two map operations into one:
//val nested: Nested[Option, Validated[String, *], Int] = Nested(Some(Valid(123)))
//nested.map(_.toString)

// In a sense, Nested is similar to monad transformers like OptionT and EitherT,
// as it represents the nesting of effects inside each other.
// But Nested is more general - it does not place any restriction on the type of the two nested effects:
object w {
  final case class Nested[F[_], G[_], A](value: F[G[A]])
}
// Instead, it provides a set of inference rules based on the properties of F[_] and G[_]. For example:
//  - If F[_] and G[_] are both Functors, then Nested[F, G, *] is also a Functor (we saw this in action in the example
// above)
//  - If F[_] and G[_] are both Applicatives, then Nested[F, G, *] is also an Applicative
//  - If F[_] is an ApplicativeError and G[_] is an Applicative, then Nested[F, G, *] is an ApplicativeError
//  - If F[_] and G[_] are both Traverses, then Nested[F, G, *] is also a Traverse

case class UserInfo(name: String, age: Int)
case class User(id: String, name: String, age: Int)
def createUser(userInfo: UserInfo): Future[Either[List[String], User]] =
  Future.successful(Right(User("user 123", userInfo.name, userInfo.age)))

// Using Nested we can write a function that, given a list of UserInfos, creates a list of Users:
import cats.Applicative
import cats.implicits._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
def createUsers(
    userInfos: List[UserInfo]
): Future[Either[List[String], List[User]]] =
  userInfos.traverse(userInfo => Nested(createUser(userInfo))).value

val userInfos = List(
  UserInfo("Alice", 42),
  UserInfo("Bob", 99)
)

Await.result(createUsers(userInfos), 1.second)
def createUsersNotNested(
    userInfos: List[UserInfo]
): Future[List[Either[List[String], User]]] =
  userInfos.traverse(createUser)

Await.result(createUsersNotNested(userInfos), 1.second)
