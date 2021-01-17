object Foo
Foo
// val res0: Foo.type = Foo$@14898a2b
// Foo.type is the singleton type

// singleton type applied to literal => literal type
val x: 31 = 31

import shapeless.syntax.singleton._
val y = 12.narrow
// val y: Int(12) = 12
true.narrow
"s".narrow
12.0f.narrow

//math.sqrt(12.0).narrow
//Expression scala.math.`package`.sqrt(12.0) does not evaluate to a constant or a stable reference value

x + 1
y + 1
