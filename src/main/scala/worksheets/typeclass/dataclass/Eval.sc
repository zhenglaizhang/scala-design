// Eval
// Eval is a data type for controlling synchronous evaluation. Its implementation is designed to provide stack-safety at all times using a technique called trampolining. There are two different factors that play into evaluation: memoization and laziness. Memoized evaluation evaluates an expression only once and then remembers (memoizes) that value. Lazy evaluation refers to when the expression is evaluated. We talk about eager evaluation if the expression is immediately evaluated when defined and about lazy evaluation if the expression is evaluated when it’s first used. For example, in Scala, a lazy val is both lazy and memoized, a method definition def is lazy, but not memoized, since the body will be evaluated on every call. A normal val evaluates eagerly and also memoizes the result. Eval is able to express all of these evaluation strategies and allows us to chain computations using its Monad instance.

// EVAL.NOW
// - eager evaluation
import cats.Eval
import cats.implicits._

val eager = Eval.now {
  println("running expensive ops...")
  1 + 2 * 4
}
eager.value

val eagerEval = Eval.now {
  println("This is eagerly evaluated")
  1 :: 2 :: 3 :: Nil
}

eagerEval.value

// EVAL.LATER
//  - lazy evaluation
val lazyEval = Eval.later {
  println("running expensive ops...")
  1 + 2 * 4
}
lazyEval.value
lazyEval.value
lazyEval.value

// “Running expensive calculation” is printed only once, since the value was memoized internally. Meaning also that the resulted operation was only computed once. Eval.later is different to using a lazy val in a few different ways. First, it allows the runtime to perform garbage collection of the thunk after evaluation, leading to more memory being freed earlier. Secondly, when lazy vals are evaluated, in order to preserve thread-safety, the Scala compiler will lock the whole surrounding class, whereas Eval will only lock itself.

val n = 2
var counter = 0
val lazyEval = Eval.later {
  println("This is lazily evaluated with caching")
  counter = counter + 1
  (1 to n)
}
List.fill(n)("").foreach(_ => lazyEval.value)
lazyEval.value

// EVAL.ALWAYS
//  - lazy evaluation, without memoization akin to Function0
val alwaysEval = Eval.always(println("Always evaluated"))
alwaysEval.value
alwaysEval.value
alwaysEval.value
alwaysEval.value
alwaysEval.value
val n = 4
var counter = 0
val alwaysEval = Eval.always {
  println("This is lazyly evaluated without caching")
  counter = counter + 1
  (1 to n)
}
List.fill(n)("").foreach(_ => alwaysEval.value)
alwaysEval.value

// EVAL.DEFER
//  - Defer a computation which produces an Eval[A] value This is useful when you want to delay execution of an expression which produces an Eval[A] value. Like .flatMap, it is stack-safe. Because Eval guarantees stack-safety, we can chain a lot of computations together using flatMap without fear of blowing up the stack.
val list = List.fill(3)(0)
val deferredEval: Eval[List[Int]] =
  Eval.now(list).flatMap(e => Eval.defer(Eval.later(e)))

Eval.defer(deferredEval).value
