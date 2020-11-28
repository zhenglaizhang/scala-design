package akka.iot

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Signal
import akka.actor.typed.PostStop
import akka.actor.typed.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.Actor

object IotSupervisor {
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](ctx => new IotSupervisor(ctx))
}

class IotSupervisor(ctx: ActorContext[Nothing])
    extends AbstractBehavior[Nothing](ctx) {
  ctx.log.info("IoT Application started")
  // no need to handle any messages
  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      ctx.log.info("IoT Application stopped")
      this
  }
}

object IotApp extends App {
  ActorSystem[Nothing](IotSupervisor(), "iot-system")
}

// Writing your actors such that every message could possibly be lost is the safe, pessimistic bet.
object Device {
  sealed trait Command

  final case class ReadTemperature(
      requestId: Long,
      replyTo: ActorRef[RespondTemperature]
  ) extends Command
  final case class RespondTemperature(requestId: Long, value: Option[Double])

  final case class RecordTemperature(
      requestId: Long,
      value: Double,
      replyTo: ActorRef[TemperatureRecorded]
  ) extends Command
  final case class TemperatureRecorded(requestId: Long)

  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(ctx => new Device(ctx, groupId, deviceId))
}

class Device(
    ctx: ActorContext[Device.Command],
    groupId: String,
    deviceId: String
) extends AbstractBehavior[Device.Command](ctx) {
  import Device._

  var lastTemperatureReading: Option[Double] = None
  ctx.log.info("Device actor {}-{} started", groupId, deviceId)

  override def onMessage(msg: Device.Command): Behavior[Device.Command] =
    msg match {
      case ReadTemperature(requestId, replyTo) =>
        replyTo ! RespondTemperature(requestId, lastTemperatureReading)
        this

      case RecordTemperature(requestId, value, replyTo) =>
        ctx.log.info(
          "Recorded temperature reading {} with {}",
          value,
          requestId
        )
        lastTemperatureReading = Some(value)
        replyTo ! TemperatureRecorded(requestId)
        this
    }

  override def onSignal: PartialFunction[Signal, Behavior[Device.Command]] = {
    case PostStop =>
      ctx.log.info("Device actor {}-{} stopped", groupId, deviceId)
      this
  }

}

object DeviceGroup {
  trait Command
  private final case class DeviceTerminated(
      device: ActorRef[Device.Command],
      groupId: String,
      deviceId: String
  ) extends Command
}

abstract class DeviceGroup(ctx: ActorContext[DeviceGroup.Command], groupId: String)
    extends AbstractBehavior[DeviceGroup.Command](ctx) {
  import DeviceGroup._
}
