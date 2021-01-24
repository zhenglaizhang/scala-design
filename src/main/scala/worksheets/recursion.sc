import scala.annotation.tailrec
// Recursion
//  - Recursion is the pure way to implement “looping,”
//    because you can’t have mutable loop counters

// two disadvantages with recursion:
//  - the performance overhead of repeated function invocations
//  - the risk of a stack overflow.

// tail-call self-recursion
//  - which occurs when a function calls itself and the call is the final (“tail”) operation it does.
//  - although tail- call self-recursion optimizations are not yet supported natively by the JVM, scalac will attempt
//  them.

//@tailrec
def factorialNotTailRec(i: BigInt): BigInt =
  if (i == 1) i
  // factorial calls itself and then does a multiplication with the results.
  else i * factorialNotTailRec(i - 1)

def factorial(i: BigInt): BigInt = {
  @tailrec
  def fact(i: BigInt, accumulator: BigInt): BigInt =
    if (i == 1) accumulator
    else fact(i - 1, i * accumulator)

  fact(i, 1)
}

//
// trampoline
//  - A trampoline is a loop that works through a list of functions, calling each one in turn
//  - A calls another function B, which then calls A, which calls B, etc. This kind of back-and- forth recursion can
//  also be converted into a loop using a trampoline00

import scala.util.control.TailCalls._

object Wow {
  def isEven(xs: List[Int]): TailRec[Boolean] =
    if (xs.isEmpty) done(true) else tailcall(isOdd(xs.tail))

  def isOdd(xs: List[Int]): TailRec[Boolean] =
    if (xs.isEmpty) done(false) else tailcall(isEven(xs.tail))

}

import Wow._

for (i <- 1 to 5) {
  val even = isEven((1 to i).toList).result
  println(s"$i is even? $even")
}

def fib(n: Int): TailRec[BigInt] =
  if (n < 2) done(2)
  else
    for {
      x <- tailcall(fib(n - 1))
      y <- tailcall(fib(n - 2))
    } yield x + y

fib(40).result
