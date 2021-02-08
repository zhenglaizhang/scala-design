// Chain is a data structure that allows constant time prepending and appending.
// This makes it especially efficient when used as a Monoid, e.g. with Validated or Writer.
// As such it aims to be used where List and Vector incur a performance penalty.

// List - It has very low overhead for the most important functions such as fold and map and
// also supports prepending a single element in constant time.
// - appending a single element requires iterating over the entire data structure and therefore takes linear time.
// Traversing a data structure with something like Writer[List[Log], A] or ValidatedNel[Error, A]
//  - Use the List monoid or NonEmptyList semigroup
//  - If you use traverse with a data structure with n elements and
//    Writer or Validated as the Applicative type, you will end up with a runtime of O(n^2)

//  Similar to List, Chain is also a very simple data structure, but unlike List it supports both constant O(1) time
//  append and prepend. This makes its Monoid
//  instance super performant and a much better fit for usage with Validated,Writer, Ior or Const
//  type aliases
//  - ValidatedNec
//  - IorNec
//  - helper functions like groupByNec or Validated.invalidNec

//[info] Benchmark                                  Mode  Cnt   Score   Error  Units
//[info] CollectionMonoidBench.accumulateChain     thrpt   20  51.911 ± 7.453  ops/s
//[info] CollectionMonoidBench.accumulateList      thrpt   20   6.973 ± 0.781  ops/s
//[info] CollectionMonoidBench.accumulateVector    thrpt   20   6.304 ± 0.129  ops/s

//[info] Benchmark                           Mode  Cnt          Score         Error  Units
//[info] ChainBench.foldLeftLargeChain      thrpt   20        117.267 ±       1.815  ops/s
//[info] ChainBench.foldLeftLargeList       thrpt   20        135.954 ±       3.340  ops/s
//[info] ChainBench.foldLeftLargeVector     thrpt   20         61.613 ±       1.326  ops/s
//[info]
//[info] ChainBench.mapLargeChain           thrpt   20         59.379 ±       0.866  ops/s
//[info] ChainBench.mapLargeList            thrpt   20         66.729 ±       7.165  ops/s
//[info] ChainBench.mapLargeVector          thrpt   20         61.374 ±       2.004  ops/s

// accumulating things with Chain is more than 7 times faster than List and over 8 times faster than Vector.
// It won’t have the random access performance of something like Vector,
// but in a lot of other cases, Chain seems to outperform it quite handily.
// So if you don’t perform a lot of random access on your data structure,
// then you should be fine using Chain extensively instead.
// next time you write any code that uses List or Vector as a Monoid, be sure to use Chain instead!

object w {

  sealed abstract class Chain[+A]

  case object Empty extends Chain[Nothing]

  case class Singleton[A](a: A) extends Chain[A]

  case class Append[A](left: Chain[A], right: Chain[A]) extends Chain[A]

  case class Wrap[A](seq: Seq[A]) extends Chain[A]

}

// Concatenating two existing Chains, is just a call to the Append constructor, which is always constant time O(1).
// In case we want to append or prepend a single element, all we have to do is wrap the element with the Singleton
// constructor and then use the Append constructor to append or prepend the Singleton Chain.
// The Wrap constructor lifts any Seq into a Chain.
// This can be useful for concatenating already created collections that don’t have great concatenation performance.
// Append(Wrap(list1), Wrap(list2)).foldMap(f) will in general be much faster than just concatenating list1 and list2
// and folding the result.

import cats.data.Chain
import cats.data.Chain.{Wrap, nil, one}

// todo
//import cats.data.Chain._
//def concat[A](c1: Chain[A], c2: Chain[A]) =
//  if (c1.isEmpty) c2
//  else if (c2.isEmpty) c1
//  else Wrap(c1, c2)
//def fromSeq[A](s: Seq[A]): Chain[A] = {
//  if (s.isEmpty) nil
//  else if (s.lengthCompare(1) == 0) one(s.head)
//  else Wrap(s)
//}

//NonEmptyChain is the non empty version of Chain it does not have a Monoid instance since it cannot be empty, but it
// does have a Semigroup instance. Likewise, it defines a NonEmptyTraverse instance, but no TraverseFilter instance.
import cats.data._

NonEmptyChain(1, 2, 3, 4)
NonEmptyChain.fromNonEmptyList(NonEmptyList(1, List(2, 3, 4)))
NonEmptyChain.fromNonEmptyVector(NonEmptyVector(1, Vector(2, 3)))
NonEmptyChain.one(1)
NonEmptyChain.fromChain(Chain(1, 2, 3))
NonEmptyChain.fromSeq(List.empty[Int])
NonEmptyChain.fromSeq(Vector(1, 3, 2))
NonEmptyChain.fromChainAppend(Chain(1, 2, 3), 4)
NonEmptyChain.fromChainAppend(Chain.empty[Int], 4)
NonEmptyChain.fromChainPrepend(1, Chain(2, 3, 4))
