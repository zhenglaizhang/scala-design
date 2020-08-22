// class parameters
// primary constructor
class Rational(n: Int, d: Int) {
  println(s"Created $n/$d")
}

// StringBuilder => String

object Test extends App {
  new Rational(1, 2)
}
