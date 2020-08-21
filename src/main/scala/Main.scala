object Test {}

object Main extends App {
  // cal by name function
  def timed[T](t: => T) = {
    val start = System.nanoTime();
    val res = t
    val end = System.nanoTime();
    (res, (end - start) / 1000000)
  }

  val message = """|
                   |
                   |
                   |""".stripMargin

  println("Hello, World!")

  val timedVal = timed {
    Thread.sleep(2000)
    14
  }

  println(s"timedVal = $timedVal")
}
