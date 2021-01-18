// todo
// http://ktoso.github.io/scala-types-of-types/#delayed-init

// DelayedInit is actually a "compiler trick"
object Main extends App {
  println("hello")
}

// So the println is actually in the constructor of the Main class!".
// And this would usually be true, but not in this case, since we inherited the DelayedInit trait
// trait App extends DelayedInit {}

//trait DelayedInit {
//  def delayedInit(x: => Unit): Unit
//}
// All the work around it is actually performed by the compiler, which will treat all classes and objects inheriting
// DelayedInit in a special way (note: trait’s will not be rewriten like this).
//  - imagine your class/object body is a function, doing all these things that are in the class/object body,
//  - the compiler creates this function for you, and will pass it into the delayedInit(x: => Unit) method (notice
//  the call-by-name in the parameter).

object Main1 extends DelayedInit {
  override def delayedInit(x: => Unit): Unit = ???
}
