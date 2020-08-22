val escapes = "\\\"\''"
println(escapes)

println("""welcome
another line""")
println("""welcome
          |another line""".stripMargin)

// symbol
// interned
def updateRecordByName(r: Symbol, value: Any) = {}
updateRecordByName('favourteAlbum, "Ok Computer")

val s = 'aSymbol
println(s.name)

val name = "wow"
println(s"Hello ${name + " meow"}")

println(raw"No \\\\escape")

1 + 2
1.+(2)
1 + 2L
1.+(2L)

// infix operator
name indexOf "o"
name indexOf ("o", 0)

// prefix
-1
(1).unary_-

// postfix
1 toLong

"""|hello""".stripMargin toLowerCase

// value equality
List(1, 2, 3) == null
null == List(1, 2, 3)
("He" + "llo") == "Hello"

// reference equality
// 1 eq 2
List(1) eq List(1)
"a" eq "a"

1 * 2
1.*(2)
List(1) ::: List(2)
List(2).:::(List(1))

{ val x = 12; List(x).:::(List(x)) }

// Rich wrappers
0 max 4

-2.7 abs

1.5 isInfinity

4 to 6

"robert" drop 2

"bob" capitalize
