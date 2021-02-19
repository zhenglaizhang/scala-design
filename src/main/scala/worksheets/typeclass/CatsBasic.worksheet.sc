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
//  - good practice to use narrow imports to take some of the implicit resolution burden off the compiler
import cats.implicits._

// The apply package provides (..., ..., ...).mapN syntax, 
// which allows for an intuitive construct for applying a function that takes more than one parameter to multiple effectful values (like futures).
import scala.concurrent.ExecutionContext.Implicits.global
class ProcessingResult
def intFuture: Future[Int] = ???
def stringFuture: Future[String] = ???
def userFuture: Future[User] = ???
def process(value: Int, contents: String, user: User): ProcessingResult = ???
import cats.instances.future._
import cats.syntax.apply._
//todo fixme
// def processAsync: Future[ProcessingResult] = {
//   (intFuture, stringFuture, userFuture).mapN {
//     (v, c, u) => process(v, c, u)
//   }
// }
// or shorter:
// def processAsync2: Future[ProcessingResult] = (intFuture, stringFuture, userFuture).mapN(process)

// If any of the chained futures fails, the resulting future will also fail with the same exception as the first failing future in the chain (this is fail-fast behavior).
// What’s important, all futures will run in parallel, as opposed to what would happen in a for comprehension
def processAsync: Future[ProcessingResult] = {
  for {
    v <- intFuture
    c <- stringFuture
    u <- userFuture
  } yield process(v, c, u)
}
// In the above snippet (which under the hood translates to flatMap and map calls), which run in sequential 

// Traversing
// If you call traverse instead of map, like obj.traverse(fun), you’ll get G[F[A]], which will be Future[Option[B]]
import cats.implicits._
val xs: List[Future[Int]] = List(Future(1), Future(2), Future(3))
xs.sequence
// obj.sequence is in fact implemented in Cats as obj.traverse(identity).
// On the other hand, obj.traverse(fun) is roughly equivalent to obj.map(fun).sequence.

// flatTraverse
// If you have an obj of type F[A] and a function fun of type A => G[F[B]], then doing obj.map(f) yields result of type F[G[F[B]]] — very unlikely to be what you wanted.
// Traversing the obj instead of mapping helps a little — you’ll get G[F[F[B]] instead. 
// Since G is usually something like Future and F is List or Option, you would end up with Future[Option[Option[A]] or Future[List[List[A]]]
lazy val valueOpt: Option[Int] = ???
def compute(v: Int): Future[Option[Int]] = ???
def computeOverValue: Future[Option[Option[Int]]] = valueOpt.traverse(compute)
def computeOverValue2: Future[Option[Int]] = valueOpt.traverse(compute).map(_.flatten)
def computeOverValue3: Future[Option[Int]] = valueOpt.flatTraverse(compute)

// Monad transformers
// An instance of OptionT[F, A] can be thought of as a wrapper over F[Option[A]] which adds a couple of useful methods specific to nested types that aren’t available in F or Option itself. Most typically, your F will be Future (or sometimes slick’s DBIO, but this requires having an implementation of Cats type classes like Functor or Monad for DBIO). Wrappers such as OptionT are generally known as monad transformers.
lazy val rf: Future[Option[Int]] = ???
def mappedResultFuture: Future[Option[Int]] = rf map { maybeVal => maybeVal.map { v => ??? }}

import cats.data.OptionT
import cats.instances.future._
def mappedResultFuture2: OptionT[Future, Int] = OptionT(rf).map {v => ???}
val r: Future[Option[Int]] = mappedResultFuture2.value
// Also a viable solution to fully switch to OptionT[Future, A] in method parameter/return types and completely (or almost completely) ditch Future[Option[A]] in type declarations.
OptionT.fromOption[List](Some(2)) // OptionT(List(Some(2)))
OptionT.liftF(List(1))
OptionT.pure[List](12)
// Mostly use OptionT(...) syntax in order to wrap an instance of Future[Option[A]] into Option[F, A].
class Money { /* ... */ }

class Account
def findUserById(userId: Long): OptionT[Future, User] = { /* ... */ ??? }
def findAccountById(accountId: Long): OptionT[Future, Account] = { /* ... */ ??? }
def getReservedFundsForAccount(account: Account): OptionT[Future, Money] = { /* ... */ ??? }
def getReservedFundsForUser(userId: Long): OptionT[Future, Money] = for {
  user <- findUserById(userId)
  account <- findAccountById(user.accountId)
  funds <- getReservedFundsForAccount(account)
} yield funds

