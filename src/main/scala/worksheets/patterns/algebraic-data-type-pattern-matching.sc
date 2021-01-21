//Goal: translate data descriptions into code
//Model data with logical ors and logical ands
//Two patterns: product types (and) sum types (or)
//Product type: A has a B and C
//Sum type: A is a B or C
//Sum and product together make algebraic data types

// A has a B and C
class B
class C
case class A(b: B, c: C)

// A is a B or C
sealed trait A
case class B() extends A
case class C() extends A

// They have only data and do not contain any functionality on top of this data as normal classes would.

case class Point(x: Double, y: Double)
sealed trait Shape
case class Circle(point: Point, radius: Double) extends Shape
case class Rectangle(point: Point, height: Double, width: Double) extends Shape

object Shape {
  def area(shape: Shape): Double =
    shape match {
      case Circle(Point(x, y), radius) =>
        Math.PI * Math.pow(radius, 2) // use pattern matching to process
      case Rectangle(_, h, w) => h * w
    }
}
