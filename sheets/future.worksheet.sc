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
