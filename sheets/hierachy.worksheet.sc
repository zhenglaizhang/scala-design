// new Int

12 ##

12.hashCode

"1".##

32 max 1

12 min 1

1 until 4

-2 abs

// scala.runtime.RichInt

// scala.AnyRef == java.lang.Object

def isEqual(x: Int, y: Int) = x == y
isEqual(1, 1)
def isEqual2(x: Any, y: Any) = x == y
isEqual2(1, 1)

"abcd".substring(2) == "abcd".substring(2)

val x1 = new String("abc")
val x2 = new String("abc")
x1 eq x2
x1 ne x2

// bottom types:
// Null
// Nothing -> no value -> signal abnormal termination

def error(msg: String): Nothing = throw new RuntimeException(msg)

//
// value class
//  - Value class is equal to case class extend AnyVal with only one parameter
//  - Value types where the extra runtime overhead is eliminated during compilation.
//  - Value classes are not meant to provide a way to do stack allocation instead of heap allocation. That is
//  generally something you have no control over.
//  - Instead, they are designed to prevent extra object allocations that would occur when otherwise creating a
//  "wrapper" class.
class Dollars(val amount: Int) extends AnyVal {
  override def toString(): String = "$" + amount
}

class FirstName(val value: String) extends AnyVal

class LastName(val value: String) extends AnyVal

val x = new FirstName("bob")
// no instance of FirstName is actually created
// and x is actually just a String at runtime
// (assuming you don't do one of the things the docs describe that would force an allocation, such as pattern
// matching). But in no way does AnyVal change how the wrapped String gets allocated.

// define a tiny type for each domain concept
// the compiler will auto tuple arguments to a method when needed
def m(pair: (Int, String)) = println(pair)
m(1, "two")
