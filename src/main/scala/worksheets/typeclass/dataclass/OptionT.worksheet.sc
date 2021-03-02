// OptionT
// OptionT[F[_], A] is a light wrapper on an F[Option[A]]. 
// Speaking technically, it is a monad transformer for Option, but you don’t need to know what that means for it to be useful. 
// OptionT can be more convenient to work with than using F[Option[A]] directly.

// Reduce map boilerplate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val customGreeting: Future[Option[String]] = Future.successful(Some("welcome back, Lola"))
val excitedGreeting: Future[Option[String]] = customGreeting.map(_.map(_ + "!"))
val hasWelcome: Future[Option[String]] = customGreeting.map(_.filter(_.contains("welcome")))
val noWelcome: Future[Option[String]] = customGreeting.map(_.filterNot(_.contains("welcome")))
val withFallback: Future[String] = customGreeting.map(_.getOrElse("hello, there!"))

// OptionT can help remove some of this boilerplate. It exposes methods that look like those on Option, but it handles the outer map call on the Future
import cats.data.OptionT
import cats.implicits._

val customGreetingT: OptionT[Future, String] = OptionT(customGreeting)
val excitedGreetingT: OptionT[Future, String] = customGreetingT.map(_ + "!")
val withWelcomeT: OptionT[Future, String] = customGreetingT.filter(_.contains("welcome"))
val noWelcomeT: OptionT[Future, String] = customGreetingT.filterNot(_.contains("welcome"))
val withFallbackT: Future[String] = customGreetingT.getOrElse("hello, there!")

// Lift Option[A] and/or F[A] to OptionT[F, A]
val greetingFO: Future[Option[String]] = Future.successful(Some("Hello"))
val firstnameF: Future[String] = Future.successful("Jane")
val lastnameO: Option[String] = Some("Doe")
val ot: OptionT[Future, String] = for {
  g <- OptionT(greetingFO)
  f <- OptionT.liftF(firstnameF)
  l <- OptionT.fromOption[Future](lastnameO)
} yield s"$g $f $l"

val result: Future[Option[String]] = ot.value 

// Lift A to OptionT[F,A]
// If you have only an A and you wish to lift it into an OptionT[F,A] assuming you have an Applicative instance for F you can use some which is an alias for pure. 
// There also exists a none method which can be used to create an OptionT[F,A], where the Option wrapped A type is actually a None
val greet: OptionT[Future,String] = OptionT.pure("Hola!")
val greetAlt: OptionT[Future,String] = OptionT.some("Hi!")
val failedGreet: OptionT[Future,String] = OptionT.none

// Beyond map
val defaultGreeting: Future[String] = Future.successful("hello, there")
val greeting: Future[String] = customGreeting.flatMap(custom =>
  custom.map(Future.successful).getOrElse(defaultGreeting))
val greeting2: Future[String] = OptionT(customGreeting).getOrElseF(defaultGreeting)

// call value method to get the underlying Future[Option[String]] instance
val customGreeting2: Future[Option[String]] = customGreetingT.value
