//The compiler can’t tell whether or not the
//match clauses on Enumeration values are exhaustive. If we converted this example to
//use Enumeration and forgot a match clause for Trace, we would only know at runtime
//when a MatchError is thrown

// match on type
def doSeqMatch[A](seq: Seq[A]): String =
  seq match {
    case Nil => "Nothing"
    case head +: _ =>
      head match {
        case _: Double => "Double"
        case _: String => "String"
        case _: Int => "Int"
        case _ => "Unmatched seq element"
      }

  }

// workaround type erasure
for {
  x <- Seq(List(1, 12), Nil, Vector("a", "b"))
} yield x match {
  case s: List[_] => s"${doSeqMatch(s)}"
  case s: Vector[_] => s"${doSeqMatch(s)}"
  case _ => "unknown"
}

// Case class has another method generated called unapply,
// which is used for extraction or “deconstruction.”
// Indeed there is such an extractor method and it is invoked when a pattern-match
// expression like this is encountered:
case class Address(s: String)

case class Person(name: String, age: Int, address: Address)

object Person {
  def apply(name: String, age: Int, address: Address) =
    new Person(name, age, address)

  // To gain some perf benefits, unapply can return any type as long as it has following methods:
  //  - def isEmpty: Boolean
  //  - def get: T
  // unapply methods are invoked recursively
  def unapply(p: Person): Option[(String, Int, Address)] =
    Some((p.name, p.age, p.address))
}

val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) ::
  ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil
def wordsWithoutOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String] =
  wordFrequencies.filter(wf => wf._2 > 3 && wf._2 < 25).map(_._1)

//
// using pattern matching anonymous functions.
//
def worksWIthoutOutliersGood(wordFrequencies: Seq[(String, Int)]): Seq[String] =
  wordFrequencies
    .filter { case (_, f) => f > 3 && f < 25 }
    .map {
      case (w, _) => w
    }

// Scala compiler cannot infer it for pattern matching anonymous functions
val p: (String, Int) => Boolean = {
  case (_, f) => f > 3 && f < 25
}

//
// You will risk a MatchError at runtime.
//

//
// Partial functions
//

// the partial function both filters and maps the sequence
val pf: PartialFunction[(String, Int), String] = {
  case (word, freq) if freq > 3 && freq < 25 => word
}
def worksWithoutOutliersGood2(wf: Seq[(String, Int)]) = {
  wordFrequencies.collect(pf)
  //   wordFrequencies.map(pf) // will throw a MatchError
}

val pf2 = new PartialFunction[(String, Int), String] {
  def apply(wf: (String, Int)): String =
    wf match {
      case (w, f) if f > 3 && f < 25 => w
    }

  def isDefinedAt(wf: (String, Int)) =
    wf match {
      case (_, f) if f > 3 && f < 25 => true
      case _ => false
    }
}

//
// PF provide the means to be chained,
// allowing for a neat functional alternative to the chain of responsibility pattern
//
// The way an Akka actor processes messages sent to it is defined in terms of a partial function
