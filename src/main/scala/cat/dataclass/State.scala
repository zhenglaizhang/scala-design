package cat.dataclass

import cats.effect.IOApp
import cats.data.State
import cats.effect.IO
import cats.kernel.Order
import cats.effect.ExitCode

// todo https://github.com/TomTriple/cats-by-example/blob/master/statemonad/src/main/scala/example/Hello.scala

object StateApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = 
    IO {

    }.as(ExitCode.Success)

}

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
    case RInput(x :: xs) =>  (RInput(x), xs)
  }

  def peek(): RState[Option[ARoman]] = State { in => 
    (in, in.chars.headOption)
  }

}