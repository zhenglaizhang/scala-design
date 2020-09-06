package net.zhenglai

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import GreeterMain.SayHello

object Greeter {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] =
    Behaviors.receive { (context, message) =>
      context.log.info("Hello {}!", message.whom)
      message.replyTo ! Greeted(message.whom, context.self)
      Behaviors.same
    }
}

object GreeterBot {
  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max)
  }

  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info("Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! Greeter.Greet(message.whom, context.self)
        bot(n, max)
      }
    }
}

// guardian actor for bootstraping everything
object GreeterMain {
  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup { context =>
      val greeter = context.spawn(Greeter(), "greeter")
      Behaviors.receiveMessage { message =>
        val replyTo = context.spawn(GreeterBot(max = 3), message.name)
        greeter ! Greeter.Greet(message.name, replyTo)
        Behaviors.same
      }
    }
}

object AkkaQuickstart extends App {
  val greeterMain: akka.actor.typed.ActorSystem[GreeterMain.SayHello] =
    ActorSystem(
      GreeterMain(),
      "AkkaQuickStart"
    )
  greeterMain ! SayHello("Zhenglai")
}
