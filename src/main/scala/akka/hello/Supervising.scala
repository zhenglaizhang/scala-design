package akka.hello

import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal
import akka.actor.typed.PreRestart
import akka.actor.typed.PostStop
import akka.actor.typed.ActorSystem

object SupervisingActor {
  def apply(): Behavior[String] =
    Behaviors.setup(ctx => new SupervisingActor(ctx))
}

class SupervisingActor(ctx: ActorContext[String])
    extends AbstractBehavior[String](ctx) {

  private val child = ctx.spawn(
    Behaviors
      .supervise(SupervisedActor())
      .onFailure(SupervisorStrategy.restart),
    name = "supervised-actor"
  )

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "failChild" =>
        child ! "fail"
        this
    }
}

object SupervisedActor {
  def apply(): Behavior[String] =
    Behaviors.setup(ctx => new SupervisedActor(ctx))
}

class SupervisedActor(ctx: ActorContext[String])
    extends AbstractBehavior[String](ctx) {
  ctx.log.info("supervised actor started")

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "fail" =>
        ctx.log.info("supervised actor fails now")
        throw new Exception("I failed")
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PreRestart =>
      ctx.log.info("supervised actor will be restarted")
      this
    case PostStop =>
      ctx.log.info("supervised actor stopped")
      this
  }
}

object Main extends App {
  val testSystem = ActorSystem(SupervisingActor(), "supervisingSystem")
  testSystem ! "failChild"
}
