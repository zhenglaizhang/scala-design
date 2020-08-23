trait Abstract {
  type T
  def tansform(x: T): T
  val initial: T
  var current: T
}

trait RationalTrait {
  val number: Int
  val denom: Int
}

new RationalTrait {
  val number: Int = 1
  val denom: Int = 2
}

// pre-initialized fields
new {
  val number = 1
  val denom = 2
} with RationalTrait

object twoThirds extends {
  val number = 2
  val denom = 3
} with RationalTrait

object Demo {
  val x = {
    println("init x")
    "done"
  }
}
Demo
Demo.x

object LazyDemo {
  lazy val x = {
    println("lazy init")
    "done"
  }
}
LazyDemo
LazyDemo.x
