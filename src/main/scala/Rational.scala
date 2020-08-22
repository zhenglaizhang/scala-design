// class parameters
// primary constructor
class Rational(n: Int, d: Int) {
  require(d != 0, "Denominator can not be zero")
  println(s"Created $n/$d")

  override def toString() = n + "/" + d
}

// StringBuilder => String

object TestRational extends App {
  val r = new Rational(1, 2)
  println(r)
  val boom = new Rational(1, 0)
}
