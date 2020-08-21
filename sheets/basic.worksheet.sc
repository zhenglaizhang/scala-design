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
