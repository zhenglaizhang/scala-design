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