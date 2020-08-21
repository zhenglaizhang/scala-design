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
