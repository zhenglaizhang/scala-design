// Show is an alternative to the Java toString method
// - toString is defined on Any(Java’s Object) and
// - can therefore be called on anything, not just case classes.
// - Most often, this is unwanted behaviour, as the standard implementation of toString on non case classes is mostly
//   gibberish
// - Show allows us to only have String-conversions defined for the data types we actually want.
object w {

  trait Show[A] {
    def show(a: A): String
  }

}

(new {}).toString
// val res0: String = $anon$1@44044dbc
// The fact that this code compiles is a design flaw of the Java API.
// We want to make things like this impossible

import java.{util => ju}
import cats.Show
import cats.instances.int._ // for Show
import cats.instances.string._ // for Show

val showInt: Show[Int] = Show.apply[Int]
val showString: Show[String] = Show.apply[String]

showInt.show(123)
showString.show("abc")

// import interface syntax to make Show easier to use
import cats.syntax.show._
val shownInt = 124.show
val shownString = "abc".show

import java.util.Date
implicit val dateShow: Show[Date] = new Show[Date] {
  def show(t: ju.Date): String = s"${t.getTime()}ms since the eoch"
}

new ju.Date().show

case class Cat(name: String, color: String)

implicit val catShow: Show[Cat] =
  Show.show(cat => s"${cat.name} has color ${cat.color}")

Cat("abc", "red").show

case class Person(name: String, age: Int)

implicit val showPerson: Show[Person] = Show.show(p => p.name)

case class Department(id: Int, name: String)

implicit val showDep: Show[Department] = Show.fromToString

val john = Person("john", 12)
val engineering = Department(12, "engineering")

// String interpolator, which works just like the standard s"..." interpolator, but uses Show instead of toString:
show"$john works at $engineering"
