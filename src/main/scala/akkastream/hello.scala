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

object Simple {
  def test1() = {
    val source = Source(1 to 10)
    val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
    val runnable: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)
//    val sum: Future[Int] = runnable.run()

    // materialize the flow, getting the sinks materialized value
//    val sum2: Future[Int] = source.runWith(sink)
  }
}

object Hello extends App {
  implicit val system = ActorSystem("Hello")
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done] = source.runForeach(println)
  implicit val ec = system.dispatcher
  done.onComplete(_ => system.terminate())
}

object Hello2 extends App {
//  def lineSink(filenam: String): Sink[String, Future[IOResult]] =
//    Flow[String]
//      .map(s => ByteString(s"\n"))
//      .buffer(2, OverflowStrategy.backpressure)
//      .toMat(FileIO.toPath(Paths.get(filenam)))

  val factorials = Source(1 to 1000).scan(BigInt(1))((acc, next) => acc * next)
//  val result: Future[IOResult] = factorials
//    .throttle(1, 1.second)
//    .map(_ + 3)
//    .async
//    .map(num => ByteString(s"$num\n"))
//    .runWith(FileIO.toPath(Paths.get("factorials.txt")))
}
