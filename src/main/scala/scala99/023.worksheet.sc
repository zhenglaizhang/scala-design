import scala.util.Random

//Extract a given number of randomly selected elements from a list.
//Example:
//scala> randomSelect(3, List('a, 'b, 'c, 'd, 'f, 'g, 'h))
//res0: List[Symbol] = List('e, 'd, 'a)
def randomSelect[A](n: Int, xs: List[A]): List[A] = {
  val r = new Random()
}
