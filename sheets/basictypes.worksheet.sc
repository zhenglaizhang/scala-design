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
