// A view bound specifies a type that can be “viewed as” another. 
// This makes sense for an operation that needs to “read” an object but doesn’t modify the object.
import scala.language.implicitConversions


// Implicit functions allow automatic conversion. 
// More precisely, they allow on-demand function application when this can help satisfy type inference
implicit def strToInt(s: String): Int = s.length

val x: Int = "bac"

// View bounds, like type bounds demand such a function exists for the given type. 
// You specify a view bound with <%
// A has to be “viewable” as Int
class Container[A <% Int] {
  def addIt(x: A) = 123 + x
}

(new Container[String]).addIt("123")
(new Container[Int]).addIt(123)
// (new Container[Float]).addIt(123.2F)
// no implicit view available from Float => Int


// Methods can enforce more complex type bounds via implicit parameters. 
// For example, List supports sum on numeric contents but not on others. 
// Alas, Scala’s numeric types don’t all share a superclass, so we can’t just say T <: Number. 
// Instead, to make this work, Scala’s math library defines an implicit Numeric[T] for the appropriate types T
// List's definition of sum
// sum[B >: A](implicit num: Numeric[B]): B

List(123).sum
// List("123").sum


// Methods may ask for some kinds of specific “evidence” for a type without setting up strange objects as with Numeric. Instead, you can use one of these type-relation operators:
//  A =:= B	A must be equal to B
//  A <:< B	A must be a subtype of B
//  A <%< B	A must be viewable as B
class Meow[A](x: A) {
  def addIt(implicit evidence: A =:= Int) = 124 + x
}

(new Meow[Int](12)).addIt
(new Meow[Int]("12")).addIt
// (new Meow[Int](12.0F)).addIt


class ContainerB[A](value: A) { def addIt(implicit evidence: A <:< Int) = 123 + value }


trait Foo[M[_]] { type t[A] = M[A] }
val x: Foo[List]#t[Int] = List(1)