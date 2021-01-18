// todo
//https://de.slideshare.net/debasishg/dependency-injection-in-scala-beyond-the-cake-pattern

// Case classes with > 22 parameters are now allowed”.
// The 22 limit lives on in functions and tuples.

// So it’s great that the restriction was relaxed in Scala 2.11.
// However, while the 22 limit was lifted for some common cases, it was not universally removed.
// https://underscore.io/blog/posts/2016/10/11/twenty-two.html
case class Large(
                  a: Int,
                  b: Int,
                  c: Int,
                  d: Int,
                  e: Int,
                  f: Int,
                  g: Int,
                  h: Int,
                  i: Int,
                  j: Int,
                  k: Int,
                  l: Int,
                  m: Int,
                  n: Int,
                  o: Int,
                  p: Int,
                  q: Int,
                  r: Int,
                  s: Int,
                  t: Int,
                  u: Int,
                  v: Int,
                  w: Int
                )

case class Small(a: Int, b: Int)

// We have field accessors, a constructor, equality, hash code, copy, and product methods, but also these two methods:
//  - unapply - from Product2 (via Tuple2); and
//  - tupled - from Function2.

Small.unapply _
// val res0: Small => Option[(Int, Int)] = <function>
// Small.unapply is the extractor method.
// If we partially apply using _ we end up with a function value

Small.tupled
// val res1: ((Int, Int)) => Small = scala.Function2<function>

val large = Large(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
      19, 20, 21, 22, 23)
large.w
val w = large match {
      case Large(
      a,
      b,
      c,
      d,
      e,
      f,
      g,
      h,
      i,
      j,
      k,
      l,
      m,
      n,
      o,
      p,
      q,
      r,
      s,
      t,
      u,
      v,
      w
      ) =>
            s"w is $w"
}
// But you won’t find Large.tupled or Large.unapply.
// Those methods don’t exist on Large.

// The fix(https://github.com/scala/scala/pull/2305) introduced in Scala 2.11 removed the limitation for the above
// common scenarios: constructing case classes,
// field access (including copying), and pattern matching (baring edge cases).
//
//It did this by omitting unapply and tupled for case classes above 22 fields. In other words, the limit to
// Function22 and Tuple22 still exists.

//(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
//  23)
// tuples may not have more than 22 elements, but 23 given

// workaround the limit

//
// use nested tuples
//
(
  (1, 2, 3, 4, 5, 6, 7),
  (8, 9, 10, 11, 12, 13, 14, 15),
  (16, 17, 18, 19, 20),
  (21, 22, 23)
)

//
// use heterogeneous lists (HLists), where there’s no 22 limit.
//

// - How the 22 limit on case classes was removed in Scala 2.11 for some uses, but not all.
// - Where the limit still applies, on FunctionN and TupleN.
// - An example of how the limit manifests itself in libraries such as Slick.
// - Workarounds using nested tuples and HLists.
