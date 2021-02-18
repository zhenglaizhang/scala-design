import cats.syntax.option._
// Importing this package enables obj.some syntax — equivalent to Some(obj). 
// The only real difference is that the value is already upcast to Option[T] from Some[T].
12.some
"a".some

import scala.concurrent.Future
implicit class ToFutureSuccessful[T](obj: T) {
  def asFuture: Future[T] = Future.successful(obj)
}

12.asFuture

Some(12).asFuture // Future[Some[Int]]
12.some.asFuture // Future[Option[Int]]

// none[T], in turn, is shorthand for Option.empty[T] which is just None, but already upcast from None.type to Option[T]
none[Int]
none[String]

import cats.syntax.either._
// obj.asRight is Right(obj), obj.asLeft is Left(obj). 
// In both cases the type of returned value is widened from Right or Left to Either. 
// Just as was the case with .some

case class User(accountId: Long)
trait UserService {
  def ensureUserExists(id: Int): Future[Either[Exception, User]]
}

class UserServiceSpec extends UserService {
  def dummyUser: User = ???
  override def ensureUserExists(id: Int): Future[Either[Exception,User]] = dummyUser.asRight.asFuture
}

Either.fromOption(1.some, 2)
Either.fromOption(none[Int], "error")

// import cats.instances.<F>._
//  functor/applicative/monad
// To use most Cats syntax, you also need to import the implicit type class instances for the structures you’re operating with.
import cats.instances.list._
import cats.instances.option._
import cats.instances.future._ // when you’re doing the transformations on futures

// if you have trouble finding the necessary instances or syntax package, 
// the quick workaround is to just import cats.implicits._. 
// This is not a preferred solution, though, as it can significantly increase compile times 
//  — especially if used in many files across the project. 
// It is generally considered good practice to use narrow imports to take some of the implicit resolution burden off the compiler
import cats.implicits._