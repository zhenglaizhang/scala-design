package elem

abstract class Element extends scala.AnyRef {
  def contents: Array[String]

  def height = contents.length

  // UAP
  def width = if (height == 0) 0 else contents(0).length
}

class ArrayElement(conts: Array[String]) extends Element {
  def contents: Array[String] = conts
}

object TestElement extends App {
  val ae = new ArrayElement(Array("hello", "world"))
  println(ae.width)
}
