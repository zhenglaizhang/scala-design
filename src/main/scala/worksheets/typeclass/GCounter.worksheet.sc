val m1 = Map("a" -> 2, "c" -> 3)
val m2 = Map("a" -> 1, "b" -> 2)
m1 ++ m2 // a -> 1

final case class GCounter(counters: Map[String, Int]) {
  def increment(machine: String, amount: Int): GCounter = {
    val v = amount + counters.getOrElse(machine, 0)
    GCounter(counters + (machine -> v))
  }

  // TODO fixme
  // def merge(other: GCounter): GCounter = counters.keySet.union(other.counters.keySet).map(k =>
  //   (k -> (counters.getOrElse(k, 0) + other.counters.getOrElse(k, 0)))
  // ).toMap();

  def merge(that: GCounter): GCounter = {
    GCounter(that.counters ++ this.counters.map({
      case (k, v) => k -> v.max(that.counters.getOrElse(k, 0))
    }))
  }

  // def total: Int = counters.map(_._2).sum
  def total: Int = counters.values.sum
}
