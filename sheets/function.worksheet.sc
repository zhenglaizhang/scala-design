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
