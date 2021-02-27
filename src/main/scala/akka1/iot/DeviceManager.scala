package akka1.iot

import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef

object DeviceManager {

  def apply(): Behavior[Command] =
    Behaviors.setup(ctx => new DeviceManager(ctx))
  sealed trait Command
  final case class RequestTrackDevice(
      groupId: String,
      deviceId: String,
      replyTo: ActorRef[DeviceRegistered]
  ) extends DeviceManager.Command
      with DeviceGroup.Command

  final case class DeviceRegistered(device: ActorRef[Device.Command])
}

class DeviceManager(ctx: ActorContext[DeviceManager.Command])
    extends AbstractBehavior[DeviceManager.Command](ctx) {
  override def onMessage(
      msg: DeviceManager.Command
  ): Behavior[DeviceManager.Command] = ???
}
