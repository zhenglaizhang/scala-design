import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// map leaves the structure of the context unchanged
List(1, 2, 3)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

val future: Future[String] = Future(123)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

Await.result(future, 1.second)
