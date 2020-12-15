import scala.util.Try

val x = 1_000
val x = 1_000_000
val x = 1_000_000L
val x = 1.123_45
val x = 1.123_45D
val x = 1_234e2
val x: BigInt = 1_000_000
val x: BigDecimal = 1_234.56
val x = 1_000 + 1
x match {
  case 1_000 => println("got 1,000")
  case _ => println("others")
}

for {
  i <- 1 to 1_000
  if i > 999
} println(i)

//Integer.parseInt("1_000")
"1_000".toIntOption



"1".toByte
"1".toShort
"1".toInt
"1".toLong
"1".toFloat
"1".toDouble
"true".toBoolean
"false".toBoolean

val b = BigInt("1")
val b = BigDecimal("1.234")

Integer.parseInt("1", 2)

Integer.parseInt("1", 8)
Integer.parseInt("10", 8)


def makeInt(s: String): Option[Int] = {
  try {
    Some(s.toInt)
  } catch {
    case e: NumberFormatException => None
  }
}

def makeInt(s: String): Option[Int] = Try(s.toInt).toOption
makeInt("a")
makeInt("12")

def makeInt(s: String): Try[Int] = Try(s.toInt)
makeInt("1")
makeInt("a")


@throws(classOf[NumberFormatException])
def makeInt(s: String) = s.toInt



val d = Double.MaxValue
d.isValidInt
d.toInt
d.isValidShort
d.toShort

val e = 1.5D
e.isValidInt
e.toInt
e.isValidByte
e.toByte

//import scala.language.implicitConversions
//-language:implicitConversions

def meWantDouble(d: Double): Unit = println(d.getClass)
meWantDouble(e.asInstanceOf[Double])

val b: Byte = 1
b.asInstanceOf[Byte]
b.asInstanceOf[Double]
b.asInstanceOf[Long]


val bi1 = BigInt(1)
val bi2 = BigInt(2L)
val bi3 = BigInt("103")
bi1.isValidChar
bi1.toChar


BigDecimal(100.5).toBigIntExact
BigDecimal(100).toBigIntExact
BigDecimal("100").isValidInt


val a = 1
// val a: Int = 1
val a = 1: Double
val a: Double = 1

val a = 1l
val a = 1L
val a = 1d
val a = 1D
val a = 1f
val a = 1F


class Foo {
  var a: Short = 0
  var b: Short = _  // defaults to 0
  var s: String = _ // defaults to null
}

var name = null.asInstanceOf[String]

val s = "Hala"
val o = s: Object // type ascription

def printAll(objs: Any*): Unit= objs.foreach(println)
printAll(List(1,2,3): _*)
printAll(List(1,2,3))


// no ++ or -- in scala
var a = 1
a += 1
a -= 1
a.+=(1)

//val x = 1
//value += is not a member of Int
//x += 1



var d = 1.2
d += 1
d *= 2
d /= 2
var f = 1.3F
f += 1
f *= 2
f /= 2
