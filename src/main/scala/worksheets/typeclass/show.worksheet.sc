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
