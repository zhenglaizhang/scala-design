import cats.instances.boolean
// class parameters
// primary constructor
class Rational(val numer: Int, val denom: Int) {
  require(denom != 0, "Denominator can not be zero")
  println(s"Created $numer/$denom")

  def +(that: Rational): Rational =
    new Rational(
      this.numer + that.denom + that.numer * this.denom,
      this.denom * that.denom
    )

  def <(that: Rational): Boolean =
    this.numer * that.denom < that.numer * this.denom

  def max(that: Rational): Rational = if (this < that) that else this

  override def toString() = numer + "/" + denom
}

// StringBuilder => String

object TestRational extends App {
  val r = new Rational(1, 2)
  println(r)
  // val boom = new Rational(1, 0)
  val m = new Rational(2, 3)
  val add = r + m
  println(add)

  println(r < m)
}
