"foo".length
List(1, 2, 3).max

// EOP
val a = 0
val b = 2
val greeter = if (a > b) a else b

val i = 1
val evenOrOdd = i match {
  case 1 | 3 | 5 | 7 | 9  => "odd"
  case 2 | 4 | 6 | 8 | 10 => "even"
}

def toInt(s: String): Int = {
  try {
    s.toInt
  } catch {
    case _: Throwable => 0
  }
}

val x = List.range(1, 10)
x.filter(_ % 2 == 0)

x.foreach((i: Int) => println(i))
x.foreach((i) => println(i));
x.foreach(i => println(i))
x.foreach(println(_))
x.foreach(println)

val m = Map(1 -> 10, 2 -> 20, 3 -> 30)
m.transform((k, v) => k + v)
m.foreach(x => println(x._1 + "-->" + x._2))

val double = (i: Int) => i * 2
// val double2 = (i: Int): Int => i * 2
val double3: Int => Double = _ * 2.0
double(12)
x.map(double)

// implicit approach
val add = (x: Int, y: Int) => x + y
val add2: (Int, Int) => Int = (x, y) => x + y

// Eta expansion
def isEven(x: Int) = x % 2 == 0
List.range(1, 10, 2).filter(isEven)

// partially applied function
val isEvenF = isEven _
val c = scala.math.cos _
val c2 = scala.math.cos(_)

def sub(x: Int, y: Int) = x - y
val subF = sub(_, _)
val subF2 = sub _

val square = scala.math.pow(_, 2)
square(2)
square(5)

def exec(f: () => Unit, times: Int = 1) = { for (i <- 1 to times) f() }
val sayHelloF = () => println("hello")
def sayHelloM(): Unit = println("hello")
exec(sayHelloF)
exec(sayHelloM, 2)

def sum(a: Int, b: Int, c: Int) = a + b + c
val addTo3 = sum(1, 2, _)
addTo3(3)

def wrap(prefix: String, html: String, suffix: String) = s"$prefix$html$suffix"
val wrapWithDiv = wrap("<div>", _: String, "</div>")
wrapWithDiv("wow")

def saySth(prefix: String): String => String = (s: String) => s"$prefix $s"
val sayHello = saySth("Hello")
sayHello("wow")
