import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
var counter = 0
synchronized {
  counter += 1
}

import scala.concurrent.ExecutionContext.Implicits.global
val fut = scala.concurrent.Future {
  Thread.sleep(1000)
  21 + 21
}

fut.isCompleted
fut.value match {
  case Some(Success(x))  => println(x)
  case Some(Failure(ex)) => println(ex)
  case None              => "not yet completed"
}

fut
  .map(_ + 1)
  .filter(
    _ > 12
  ) // if future value not valid, fails with NoSuchElementException
  .flatMap(x =>
    Future {
      x + 2
    }
  )

val fut1 = Future { 12 }
val fut2 = Future { 12 + 13 }
for {
  x <- fut1
  y <- fut2
} yield x + y

// boom
// for expression serialize their transformations
// this wont run in parallel
for {
  x <- Future { 12 }
  y <- Future { 13 }
} yield x + y

Future.successful { 12 }
Future.failed(new Exception("Number"))
Future.fromTry(Success { 21 + 21 })
Future.fromTry(Failure(new Exception("bumber")))

val pro = Promise[Int]
val f = pro.future
f.value
pro.success(32)
f.value

Future(-1) collect {
  case res if res > 0 => res + 12
}

val failure = Future { 1 / 0 }
failure.value
val expectedFailure = failure.failed
expectedFailure.value
val fallback = failure.fallbackTo(Future { 1 / 1 })
fallback.value

val failedFallback = failure.fallbackTo(
  Future { val res = 21; require(res < 0); res }
)
failedFallback.value

Future { 1 / 0 }.fallbackTo(Future { println("fallback") })

val recovered = failedFallback recover {
  case ex: ArithmeticException => -1
}
recovered.isCompleted
recovered.value

Future { 1 / 0 } recoverWith {
  case ex: ArithmeticException => Future { 1 + 1 }
}

Future { 1 / 0 } transform (res => res + 1, ex =>
  new Exception("see cause", ex))

Future { 1 / 1 } transform (
  res => res + 1,
  ex => ex
)
Future { 1 / 1 } transform {
  case Success(res) => Success(res * -1)
  case Failure(ex)  => Failure(new Exception(ex))
}

// transform a failure into a success
Future { -1 / 0 } transform {
  case Success(value) => Success(value.abs + 2)
  case Failure(_)     => Success(0)
}

val f1 = Future(1) zip Future(2)
f1.value
val f2 = Future(1) zip Future(1 / 0)
f2.value

Future.foldLeft(List(Future(1), Future(2)))(0)(_ + _)
Future.reduceLeft(List(Future(1), Future(2))) { (acc, num) => acc + num }

// NoSuchElementException
Future.reduceLeft(List.empty[Future[Int]]) { (acc, num) => acc + num }

val f3 = Future.sequence(List(Future(1), Future(2)))
f3.value

val traversed: Future[List[Int]] = Future.traverse(List(1, 2, 3)) { i =>
  Future(i)
}

// foreach
Future(1) onComplete {
  case Success(value) => println(value)
  case Failure(ex)    => println(ex)
}

val newf = Future(1) andThen {
  case Success(value)     => println(value)
  case Failure(exception) => println(exception)
}

Future { Future(1) }.flatten

// === zip + map
Future(12).zipWith(Future("12")) {
  case (num, str) => s"$num is the $str"
}

import scala.concurrent.duration._
val fut3 = Future { Thread.sleep(20); 21 + 21 }
// Await.result(fut3, 1 seconds)
// Await.ready()
