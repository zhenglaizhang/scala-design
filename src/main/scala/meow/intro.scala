package meow

object Module {
  def abs(n: Int): Int = if (n < 0) -n else n

  private def format(name: String, n: Int, f: Int => Int): String = {
    val msg = "The %s of %d is %d"
    msg.format(name, n, f(n))
  }

  def factorial(n: Int): Int = {
    @annotation.tailrec
    def go(x: Int, acc: Int): Int = {
      if (x <= 1) acc
      else go(x - 1, acc * x)
    }
    go(n, 1)
  }

  val lessThan = new Function2[Int, Int, Boolean] {
    def apply(x: Int, y: Int): Boolean = x < y
  }
  val lessThan2: (Int, Int) => Boolean = (x: Int, y: Int) => x < y

  def isSorted[A](xs: IndexedSeq[A], ordered: (A, A) => Boolean): Boolean = {
    @annotation.tailrec
    def go(n: Int): Boolean = {
      if (n >= xs.length - 1) true
      else ordered(xs(n), xs(n + 1)) && go(n + 1)
    }
    xs.length > 1 && go(0)
  }

  // 0 1 1 2 3 5 8 ...
  // TODO: use local tail-recursive function
  def fabonacci(n: Int): Int = {
    if (n <= 2) n - 1
    else fabonacci(n - 1) + fabonacci(n - 2)
  }

  def partial[A, B, C](a: A, f: (A, B) => C): B => C = b => f(a, b)

  // currying transforms a function that takes multiple arguments into a chain of functions, each taking a single
  // argument.
  def curry[A, B, C](f: (A, B) => C): A => B => C = a => b => f(a, b)

  def compose[A, B, C](f: B => C, g: A => B): A => C = a => f(g(a))

  def main(args: Array[String]): Unit = {
    println(format("abs", -2, abs))
    println(format("factorial", 4, factorial))
    println(format("fabonacci", 7, fabonacci))
    println(isSorted(Vector(1, 2, 3, 4), (_: Int) < (_: Int)))
  }

  def foo(xs: List[Int]): Unit =
    xs match {
      case head +: next if head => foo(next) // head is of type Any but not Int
      case Nil                  => Nil
    }
}
