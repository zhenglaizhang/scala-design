import scala.collection.mutable.Map

//trait Memoizer {
object Memoizer {
  def memo[X, Y](f: X => Y): X => Y = {
    val cache = Map[X, Y]()
    (x: X) => cache.getOrElseUpdate(x, f(x))
  }
}

def plus1(x: Int) = {
  println("meow")
  x + 1
}

val memoPlus1 = Memoizer.memo(plus1)
memoPlus1(1)
memoPlus1(2)
memoPlus1(3)
memoPlus1(2)
memoPlus1(1)
