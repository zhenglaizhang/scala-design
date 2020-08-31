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
