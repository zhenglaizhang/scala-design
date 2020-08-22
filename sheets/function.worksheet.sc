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
