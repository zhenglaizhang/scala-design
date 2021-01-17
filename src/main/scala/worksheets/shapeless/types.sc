//import shapeless.syntax.singleton._

//var x = 31.narrow
//x = 14

// literal type
var x: 12 = 12
//x = 13

// type tagging & phantom type

// phantom type
trait Cherries
val num: Int = 12
val numWithCherries = 12.asInstanceOf[Int with Cherries]

