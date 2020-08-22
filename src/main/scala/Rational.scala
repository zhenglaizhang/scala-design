// class parameters
// primary constructor
class Rational(n: Int, d: Int) {
  println(s"Created $n/$d")

  override def toString() = n + "/" + d
}

// StringBuilder => String

object TestRational extends App {
  val r = new Rational(1, 2)
  println(r)
}
