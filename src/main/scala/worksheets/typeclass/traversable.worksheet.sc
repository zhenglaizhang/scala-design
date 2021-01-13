import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def getUptime(hostname: String): Future[Int] = Future(hostname.length * 60)

val hostnames = List("abc.com", "defg.com", "abcdefg.com")

val allUptimes: Future[List[Int]] = Future.sequence(hostnames.map(getUptime))

// parallel map
val allUptimes2: Future[List[Int]] = Future.traverse(hostnames)(getUptime)

val allUptimes3: Future[List[Int]] = hostnames.foldLeft(Future(List.empty[Int])) {
  (acc, host) =>
    val up = getUptime(host)
    for {
      acctime <- acc
      uptime <- up
    } yield acctime :+ uptime
}
