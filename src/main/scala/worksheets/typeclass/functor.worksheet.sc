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

import cats.instances.function._ // for Funtor
import cats.syntax.functor._ // for map

val func1: Int => Double = x => x.toDouble
val func2: Double => Double = _ * 2
(func1 map func2)(1)
(func1 andThen func2)(1)
func2(func1(1))

val func = ((x: Int) => x.toDouble)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")
func(123)
