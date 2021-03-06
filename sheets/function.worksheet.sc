import java.util.Date
import java.io.PrintWriter
import java.io.File
import scala.annotation.tailrec
val increase = (x: Int) => x + 1
increase(12)

val add = (_: Int) + (_: Int)
add(12, 12)

val a = List(1, 2, 3)
a.foreach(println(_))
a.foreach((println _))
a.reduce(_ + _)

def sum(a: Int, b: Int, c: Int) = a + b + c
// transform a def into a function value
val sumf = sum _
sumf.apply(1, 2, 3)
sumf(1, 2, 3)

val b = sum(1, _: Int, 3)
b(12)

// val c = sum

//
// closure
//

var more = 1
val addMore = (x: Int) => x + more
addMore(1)
more = 10
addMore(1)

def makeIncreaser(more: Int) = (x: Int) => x + more
val inc1 = makeIncreaser(1)
val inc999 = makeIncreaser(999)

//
// parameters
//

// repeated parameters
def echo(args: String*): Unit = for (arg <- args) println(arg)
echo("a", "b", "c")
echo()
val xs = Array("a", "c")
echo(xs: _*)

// named parameters
echo(args = "a", "c")

// default parameters

def printTime(out: java.io.PrintStream = Console.out) =
  out.println(System.currentTimeMillis())

printTime()

@tailrec
final def boom(x: Int): Int =
  if (x == 0) throw new Exception("bang")
  else boom(x - 1)

// -g:notailcalls to close TCO
// check the call stack trace
// boom(3)

12
12

def containsNeg(nums: List[Int]) = nums.exists(_ < 0)
def containsOdd(nums: List[Int]) = nums.exists(_ % 2 == 1)

def curriedSum(x: Int)(y: Int) = x + y
curriedSum(1)(2)
val onePlus: Int => Int = curriedSum(1) _
val x: Int => Int => Int = curriedSum _

def twice(op: Double => Double, x: Double) = op(op(x))
twice(_ + 1, 4)

def withPrinter(file: File)(op: PrintWriter => Unit) = {
  val writer = new PrintWriter(file)
  try {
    op(writer)
  } finally {
    writer.close()
  }
}

// withPrinter(new File(".")) { printer =>
//   printer.print(new Date())
// }

def byNameAssert(predicate: => Boolean) =
  if (!predicate) throw new AssertionError
byNameAssert(4 > 1)

def sum(x: String, y: String) = x + y
val csum: String => (String => String) = sum.curried
csum("a")("b")
val csum2 = (sum _).curried
Function.uncurried(csum)("a", "b")

Function.tupled(sum)("a" -> "b")
sum.tupled("a" -> "b")
Function.untupled(sum.tupled)("a", "b")
