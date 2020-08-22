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


