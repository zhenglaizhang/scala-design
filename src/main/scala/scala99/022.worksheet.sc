//Create a list containing all integers within a given range.
//Example:
//scala> range(4, 9)
//res0: List[Int] = List(4, 5, 6, 7, 8, 9)
def range(s: Int, n: Int): List[Int] = List.range(s, n + 1)

def rangeRec(start: Int, end: Int): List[Int] =
  if (end < start) Nil
  else start :: rangeRec(start + 1, end)

def rangeTailRec(start: Int, end: Int): List[Int] = {
  def rangeR(end: Int, result: List[Int]): List[Int] = {
    if (end < start) result
    else rangeR(end - 1, end :: start)
  }
  rangeR(end, Nil)
}

range(4, 9)
rangeRec(4, 9)
