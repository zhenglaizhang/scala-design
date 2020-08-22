import cats.instances.boolean
// class parameters
// primary constructor
class Rational(n: Int, d: Int) {
  require(d != 0, "Denominator can not be zero")

  private val g = gcd(n.abs, d.abs)
  val numer = n / g
  val denom = d / g
  println(s"Created $numer/$denom")

  def this(n: Int) = this(n, 1)

  def +(that: Rational): Rational =
    new Rational(
      this.numer + that.denom + that.numer * this.denom,
      this.denom * that.denom
    )

  def <(that: Rational): Boolean =
    this.numer * that.denom < that.numer * this.denom

  def max(that: Rational): Rational = if (this < that) that else this

  override def toString() = numer + "/" + denom

  private def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
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

  println(new Rational(12))
}
