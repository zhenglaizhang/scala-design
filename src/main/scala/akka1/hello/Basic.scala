package akka1.hello

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Basic extends App {

  // message type is String
  val greeter: Behavior[String] =
    Behaviors.receiveMessage[String] { whom =>
      println(s"Hello $whom!")
      Behaviors.stopped
    }

  // start a system with this primitive guardian
  val system = ActorSystem(greeter, "hello")

  // send a message to the guardian
  system ! "word"
  system ! "word"
  system ! "word"

  // system stops when guardian stops
}
