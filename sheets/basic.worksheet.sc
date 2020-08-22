import java.io.IOException
import java.io.FileNotFoundException
import java.io.FileReader
import scala.collection.mutable
var capital = Map("US" -> "Washington", "France" -> "Pairs")
capital += ("Janpan" -> "Tokyo")
println(capital("France"))
println(capital.get("Beijing"))

def factorial(x: BigInt): BigInt = if (x == 0) 1 else x * factorial(x - 1)

Array(1) == Array(2)

val x = "x"

def max(x: Int, y: Int): Int = if (x > y) x else y

val a = Array(1, 2, 3)
var i = 0
while (i < a.length) {
  println(a(i))
  i += 1
}

a.apply(1)
a(1) = 4
a.update(2, 5)
a.foreach(println)
a.foreach(println(_))
a.foreach(arg => println(arg))
a.foreach((arg: Int) => println(arg))

for (arg <- a)
  println(arg)

for (i <- 0 to 2)
  println(i)

Console println 12

(1).+(2)

List(1) ::: List(2)

1 :: List(2)

List(2).::(1)

1 :: 2 :: 3 :: Nil

val l = 1 :: 3 :: List()
l(1)

// no apply for tuple as each element of a tuple may be a differenttype
(99, "aa")._1

var set = Set("a", "b")
set += "c"
set + "d"
set.contains("c")

import scala.collection.mutable

val mset = mutable.Set("H", "B")
mset += "test"

val mmap = mutable.Map[Int, String]()
mmap += (1 -> "Go")
mmap += (2 -> "two")

def printArgs(args: Array[String]): Unit = args.foreach(println)
def formatArgs(args: Array[String]) = args.mkString("\n")
println(formatArgs(Array("1", "2")))

val as = Array("a", "bbb", "cc")
val longest = as.reduceLeft((a, b) => if (a.length > b.length) a else b)

// procedure
def greet() = { println("wow") }
() == greet()

for (
  i <- 1 to 10
  if i % 2 == 0
  if i % 3 == 0
)
  println(s"Iteration $i")

for (i <- 1 until 10 by 4)
  println(i)

for (i <- 0 to as.length - 1)
  println(as(i))

val ss =
  for (i <- 1 to 10)
    yield {
      println(i)
      i.toString
    }

//
// exception handling
//
val n = 100
val half =
  if (n % 2 == 0)
    n / 2
  else
    // Nothing type
    throw new RuntimeException("n must be even")

try {
  val f = new FileReader("notexists.txt")
} catch {
  case ex: FileNotFoundException => println("not found")
  case ex: IOException           => println("io exception")
} finally {
  println("finally cleanup")
}
