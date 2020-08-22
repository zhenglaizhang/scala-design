package elem

abstract class Element extends scala.AnyRef {
  def contents: Array[String]

  def height = contents.length

  // UAP
  def width = if (height == 0) 0 else contents(0).length
}

class ArrayElement(override val contents: Array[String]) extends Element {}

// not good
// is line element a array element? maybe not...
class LineElement(s: String) extends ArrayElement(Array(s)) {
  override def width: Int = s.length

  override def height: Int = 1
}

object TestElement extends App {
  val ae = new ArrayElement(Array("hello", "world"))
  println(ae.width)
}
