package http4ss

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s._ 
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.Implicits.global

// An HttpRoutes[F] is a simple alias for Kleisli[OptionT[F, *], Request, Response]. 
// If that’s meaningful to you, great. If not, don’t panic: 
// Kleisli is just a convenient wrapper around a Request => F[Response], and F is an effectful operation.
object Main extends IOApp {
  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name")
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = 
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(helloWorldService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}