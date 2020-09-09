package akkastream

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

object Hello extends App {
  implicit val system = ActorSystem("Hello")
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done] = source.runForeach(println)
  implicit val ec = system.dispatcher
  done.onComplete(_ => system.terminate())
}
