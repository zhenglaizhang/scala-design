class A {
  private var sum = 0
  def value = sum;
}
object A {
  def get(init: Int = 0) = {
    val a = new A
    a.sum = init
    a
  }
}
new A

// side effect => procedure

1
+2

(1
  + 2)

1 +
  2 +
  3

A.get(1).value
A.get().value

// java.lang
// scala
// scala.Predef
Predef.println(12)
Console println 12
