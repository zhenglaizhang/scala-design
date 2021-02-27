package akka1.hello

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Greeting1 extends App {
  // Proper channels with algebraic data type
  sealed trait Greeter
  final case class Greet(whom: String) extends Greeter
  final case object Stop extends Greeter

  val greeter: Behavior[Greeter] =
    Behaviors.receiveMessage[Greeter] {
      case Greet(whom) =>
        println(s"Hello $whom")
        Behaviors.same
      case Stop =>
        println("shutdown")
        Behaviors.stopped
    }

  ActorSystem[Nothing](
    Behaviors.setup[Nothing] { ctx =>
      val greeterRef = ctx.spawn(greeter, "greeter")
      ctx.watch(greeterRef) // sign death pact
      greeterRef ! Greet("world")
      greeterRef ! Greet("akka")
      greeterRef ! Stop
      Behaviors.empty
    },
    "helloworld"
  )
}
