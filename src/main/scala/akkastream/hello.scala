package akkastream

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import java.nio.file.Files

object Hello extends App {
  implicit val system = ActorSystem("Hello")
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done] = source.runForeach(println)
  implicit val ec = system.dispatcher
  done.onComplete(_ => system.terminate())
}

object Hello2 extends App {
  val factorials = Source(1 to 1000).scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] = factorials
    .throttle(1, 1.second)
    .map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toPath(Paths.get("factorials.txt")))
}
