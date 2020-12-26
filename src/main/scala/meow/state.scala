package meow

trait RNG {
  def nextInt: (Int, RNG)
}

case class SimpleRNG(seed: Long) extends RNG {
  def nextInt: (Int, RNG) = {
    val newSeed = (seed * 0x5deece66dL + 0xbL) & 0xffffffffffffL
    val nextRNG = SimpleRNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRNG)
  }
}

object StateApp extends App {
  val rng = new scala.util.Random
  println(rng.nextInt(5))
  val r1 = new SimpleRNG(1)
  val (n1, r2) = r1.nextInt
  println(r1.nextInt._1)
  println(r1.nextInt._1)
  println(r2.nextInt._1)
  println(r2.nextInt._1)
}
