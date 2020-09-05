import scala.util.Try
import scala.util.Try

def parseInt(str: String): Option[Int] = Try(str.toInt).toOption

def divide(a: Int, b: Int): Option[Int] = if (b == 0) None else Some(a / b)

def strDivideBy(a: String, b: String): Option[Int] =
  parseInt(a).flatMap(a =>
    parseInt(b).flatMap { b =>
      divide(a, b)
    }
  )

def strDivideBy2(a: String, b: String): Option[Int] =
  for {
    an <- parseInt(a)
    bn <- parseInt(b)
    ans <- divide(an, bn)
  } yield ans

strDivideBy("4", "2")
strDivideBy("4", "0")

object hidden {
  trait Monad[F[_]] {
    def pure[A](a: A): F[A]

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(f.andThen(pure))
  }
}

import cats.Monad
import cats.instances.option._
import cats.instances.list._

val opt1 = Monad[Option].pure(3)
val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2))
val list1 = Monad[List].pure(3)
val list2 = Monad[List].flatMap(list1)(a => List(a, a * 10))

import cats.syntax.applicative._
1.pure[Option]
1.pure[List]

import cats.syntax.flatMap._
import cats.syntax.functor._
def sumSquare[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] =
  a.flatMap(x => b.map(y => x * x + y * y))

sumSquare(Option(1), Option(2))
sumSquare(Option(1), Option.empty[Int])

object hiddenid {
  type Id1[A] = A
}

import cats.Id
sumSquare(3: Id[Int], 4: Id[Int])

"Dave": Id[String]

123: Id[Int]

List(1, 2, 3): Id[List[Int]]

val a = Monad[Id].pure(3)
val b = Monad[Id].flatMap(a)(_ + 1)
import cats.syntax.functor._
import cats.syntax.flatMap._
for {
  x <- a
  y <- b
} yield x + y

val e1: Either[String, Int] = Right(10)
val e2: Either[String, Int] = Right(11)
for {
  a <- e1.right
  b <- e2.right
} yield a + b

for {
  a <- e1
  b <- e2
} yield a + b

import cats.syntax.either._
// smart constructor
val e3 = 3.asRight[String]
val e4 = 4.asRight[String]
for {
  a <- e3
  b <- e4
} yield a + b

def countPositive(nums: List[Int]) =
  nums.foldLeft(0.asRight[String]) { (acc, num) =>
    if (num > 0) {
      acc.map(_ + 1)
    } else {
      Left("Negative. Stopping!")
    }
  }

countPositive(List(1, 2, 3))
countPositive(List(1, 2, 3, -1, -2))

Either.catchOnly[NumberFormatException]("foo".toInt)
Either.catchNonFatal(sys.error("badness"))
Either.fromTry(Try("foo".toInt))
Either.fromOption[String, Int](None, "Badness")

import cats.syntax.either._
"error".asLeft[Int].getOrElse(0)
"error".asLeft[Int].orElse(2.asRight[String])

-1.asRight[String].ensure("Must be non-negative")(_ > 0)

"foo".asLeft[Int].leftMap(_.reverse)
6.asRight[String].bimap(_.reverse, _ * 7)
123.asRight[String].swap

// fail fast error handling
for {
  a <- 1.asRight[String]
  b <- 0.asRight[String]
  c <-
    if (b == 0) "DIV0".asLeft[Int]
    else (a / b).asRight[String]
} yield c * 100

type Result[A] = Either[Throwable, A]

object wrapper {
  sealed trait LoginError extends Product with Serializable
  final case class UserNotFound(username: String) extends LoginError
  case object UnexptectedError extends LoginError
}
import wrapper._
case class User(username: String, password: String)
type LoginResult = Either[LoginError, User]

def handleError(error: LoginError): Unit =
  error match {
    case UserNotFound(username) => println(s"user not found: $username")
    case UnexptectedError       => println("unexpected")
  }

val eager = {
  println("eager and memorized")
  math.random()
}

def lazy1 = {
  println("lazy and not memorized")
  math.random()
}

eager
eager
lazy1
lazy1

lazy val callByNeed = {
  println("computing z")
  math.random()
}

callByNeed
callByNeed

import cats.Eval
val now = Eval.now(math.random())
val always = Eval.always(math.random())
val later = Eval.later(math.random())

now.value
now.value
always.value
always.value
later.value
later.value

val m = Eval
  .always { println("step1"); "hello" }
  .map { str => println("step2"); s"$str world" }
  .memoize
  .map { str => println("step3"); str }
m.value
m.value

def factorial(n: BigInt): Eval[BigInt] =
  if (n == 1) {
    Eval.now(n)
  } else {
    Eval.defer(factorial(n - 1)).map(_ * n)
  }

// factorial(100000).value

import cats.data.Writer
// type Writer[L, V] = WriterT[Id, L, V]
import cats.data.WriterT
import cats.Id
import cats.instances.vector._ // for Monoid
val w: WriterT[Id, Vector[String], Int] = Writer(
  Vector(
    "the best",
    "the worst"
  ),
  1895
)
import cats.syntax.applicative._
type Logged[A] = Writer[Vector[String], A]
123.pure[Logged]

import cats.syntax.writer._
Vector("no", "result", "only", "unit").tell

val w1 = 123.writer(Vector("msg1", "msg2", "msg3"))
w1.value
w1.written

val (log, result) = w1.run

val writer1 = for {
  a <- 10.pure[Logged]
  _ <- Vector("a", "b", "c").tell
  b <- 32.writer(Vector("x", "y", "z"))
} yield a + b

writer1.run
// transform the log
writer1.mapWritten(_.map(_.toUpperCase)).run

import cats.data.Reader
final case class Cat(name: String, food: String)
val catName: Reader[Cat, String] = Reader(cat => cat.name)
catName.run(Cat("name", "food"))
val greet: Reader[Cat, String] = catName.map(name => s"Hello $name")
greet.run(Cat("name", "food"))
val feed: Reader[Cat, String] = Reader(cat => s"Hive a nice ${cat.food}")
val greetAndFeed: Reader[Cat, String] = for {
  g <- greet
  f <- feed
} yield s"$g, $f"

greetAndFeed(Cat("wow", "meow"))
