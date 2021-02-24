package cat.dataclass

import cats.effect.IOApp
import cats.data.State
import cats.effect.IO
import cats.kernel.Order
import cats.effect.ExitCode
import cats.data.OptionT
import cats.implicits._

// todo https://github.com/TomTriple/cats-by-example/blob/master/statemonad/src/main/scala/example/Hello.scala

/*
    1. as input we get a list of roman characters e.g. List(X, M, I, D, M)
    2. we want to inspect that list pairwise: if the first character is greater than or equal to the second we 
       create an “addition expression”, otherwise an “subtraction expression”.
    3. as output we want a list of expressions e.g. List(ExprPlus, ExprMinus, ExprLiteral)
*/

object StateApp extends IOApp {
  import Parse._
  def run(args: List[String]): IO[ExitCode] = {
    val result = combineStates().run(RInput(List(RSymM, RSymD, RSymM, RSymC, RSymL, RSymC, RSymM)))      
    IO {
      println(result)
    }.as(ExitCode.Success)
  }
}

sealed trait Expr[A]
case class ExprLit[A](a: A) extends Expr[A]
case class ExprBinPlus[A](a: A, b: A) extends Expr[A]
case class ExprBinMinus[A](a: A, b: A) extends Expr[A]

object Parse {
  abstract class ARoman(val symbol:Char, val value:Int)
  case object RSymI extends ARoman('I', 1)
  case object RSymV extends ARoman('V', 5)
  case object RSymX extends ARoman('X', 10)
  case object RSymL extends ARoman('L', 50)
  case object RSymC extends ARoman('C', 100)
  case object RSymD extends ARoman('D', 500)
  case object RSymM extends ARoman('M', 1000)

  object ARoman {
    implicit val order: Order[ARoman] = Order.from((a, b) => a.value.compareTo(b.value))
  }

  case class RInput(chars: List[ARoman])

  type RState[A] = State[RInput, A] // (RInput) => (RInput, A)

  def pop(): RState[ARoman] = State {
    case RInput(x :: xs) =>  (RInput(xs), x)
  }

  def peek(): RState[Option[ARoman]] = State { in => 
    (in, in.chars.headOption)
  }

  // combine: State[ARoman] + State[Option[ARoman]] => State[Expr]
  def pairwise(): RState[Expr[ARoman]] =  for {
    a <- pop()
    b <- (OptionT(peek()) *> OptionT(pop().map(Option(_)))).value
    expr = b.fold[Expr[ARoman]](ExprLit(a)) { b => 
      if (a >= b) ExprBinPlus(a, b) else ExprBinMinus(a, b)
    }
  } yield expr

  def combineStates(): RState[List[Expr[ARoman]]] = for {
    expr <- pairwise()
    s <- State.get
    exprList <- if (s.chars.size > 1) combineStates() else pop().map(it => List(ExprLit(it)))
  } yield expr :: exprList
}