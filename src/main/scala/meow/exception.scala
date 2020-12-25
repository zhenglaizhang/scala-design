package meow

object abc {

  def failFn(i: Int): Int = {
    try {
      val x = 32 + 4
      x + ((throw new Exception("fail")): Int)
    } catch {
      case e: Exception => 43
    }
  }
}


sealed trait Option[+A]
case class Some[+A](get: A) extends Option[A]
case object None extends Option[Nothing]

object ExApp extends App {}
