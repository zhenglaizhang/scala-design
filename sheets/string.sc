import scala.util.Random
import scala.util.matching.Regex

// treat String as Seq[Char]
val s = "foo bar baz"
s.find(_ == 'f')
s.count(_ == 'b')

s.drop(2)
s.dropRight(2)
s.dropWhile(_ == 'f')

s.filter(_ != 'o')

s.sortWith(_ < _)
s.sortBy(_.toInt)
s.sorted
s.toSeq.sortWith(_ < _).unwrap

s.take(3)
s.takeRight(3)
s.takeWhile(_ != 'r')

"scala".drop(2).take(2).capitalize
"scala".slice(2, 4).capitalize;

//scala.collection.StringOps
//@inline implicit def augmentString(x: String): StringOps = new StringOps(x)



val s1 = "Hello"
val s2 = "H" + "ello"
val s3: String = null
// test object equality
s1 == s2 // null safe
s3 == s1
s1.toUpperCase == s2.toUpperCase
s1.equalsIgnoreCase(s2)

val foo =
"""This is
  |a multiple line
  |String
  |""".stripMargin

val abc =
""" first
# second ' "
# another '' line "
#""".stripMargin('#').replaceAll("\n", " ")




"hello   world".split( " ").map(_.trim)
"hello   world".split( ' ').map(_.trim)
"a, b, c".split(",").map(_.trim)
"hello   world,  ".split("\\s+")
"hello+++world".split('+')


val name = "fred"
val age = 12
println(s"$name has age $age and next year he is ${age+1}")

val weight = 1.0
println(f"$name has age $age and next year he is $weight%.2f years")
val s = f"$name has age $age and next year he is $weight%.0f years"


val normalStrInterpolator = s"foo\nbar"
val rawStringInterpolator = raw"foo\nbar"

"%s is %d years old".format(name, age)

println(f"%%")
println(f"$$")

println("\nabc\bd")


val upper = for (c <- "hello world") yield c.toUpper
val upper2 = "hello world".map(c => c.toUpper)
val upper3 = "hello world".map(_.toUpper)
"hello world".filter(_ != 'a').map(_.toUpper)
"hello world".toUpperCase
"hello world".foreach(println)

"hello".getBytes

def toLower(c: Char): Char = (c.toByte+32).toChar
"HELLO".map(toLower)

val r1 = for {
  c <- "hello, world"
  if c != 'l'
} yield c.toUpper


val x = "HELLO".map { c => // anonymous function
  val i: Int = c.toByte + 32
  i.toChar
}

//def foreach[A](f: A => Unit): Unit = {}


//val numPattern = new Regex(("[0-9]+"))
val numPattern = "[0-9]+".r
val testStr = "123 test then 101"
val match1 = numPattern.findFirstIn(testStr)
val matches2 = numPattern.findAllIn(testStr)
matches2.toVector


val xx = "123 main test 134".replaceAll("[0-9]", "x")

"123 main test 134".replaceFirst("[0-9]", "x")
numPattern.replaceAllIn("123 main test 123", "x")
numPattern.replaceFirstIn("123 main test 123", "x")


val FruitRE = "([0-9]+) ([A-Za-z]+)".r
val FruitRE(count, fruit) = "100 bananas"

"10 apples" match {
  case FruitRE(count, fruit) => s"eat $count $fruit"
  case _ => "i dont know"
}


"hello"(0)
"hello".apply(0)
"hello".charAt(0)
//"hello"(99) // StringIndexOutOfBoundsException


object QInterpolator {
  implicit class QHelper(val sc: StringContext) {
    def Q(expressions: Any*): Seq[String] = {
      val origStr: String = sc.s(expressions: _*)
      origStr.split("\n")
        .toVector
        .map(_.trim)
        .filter(_ != "")
    }
  }
}

import QInterpolator._
val a = "zucc"
val b = "hini"
val c = "carrots"
Q"""
  apple
  $c
  ${a+b}
  ${1+1}
"""

val list = StringContext(
  "\n  apple\n  ",
  "\n  ",
  "\n  ",
  "\n"
).Q(c, "zuchini", 2)



val r = new Random
r.nextString(10)
Random.alphanumeric.take(10).mkString

Random.alphanumeric.take(10).mkString
Random.alphanumeric.take(10)
Random.nextPrintableChar()
