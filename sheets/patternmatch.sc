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
