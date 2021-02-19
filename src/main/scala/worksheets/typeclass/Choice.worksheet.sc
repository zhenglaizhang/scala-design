import cats.arrow.Choice
import cats.effect.IO
// If we have two functions A => C and B => C, how can we compose them into a single function that can take either A or B and produce a C?
// Infix notation of choice is |||
// A very useful case of Choice is middleware in HTTP server.
object w {
  trait Choice[F[_, _]] {
    def choice[A, B, C, D](fac: F[A, C], fbc: F[B, C]): F[Either[A, B], C]
  }
}

import cats.implicits._
import cats.data.Kleisli
import cats.data.OptionT
import cats.Monad
trait Request[F[_]]
trait Response[F[_]]

type HttpRoutes[F[_]] = Kleisli[OptionT[F, *], Request[F], Response[F]]
def routes[F[_]]: HttpRoutes[F] = ???

// If we like to have an authentication middleware that composes the route, we can simply define middleware as:
type Middleware[F[_]] = Kleisli[OptionT[F, *], Request[F], Either[Response[F], Request[F]]]
def auth[F[_]]: Middleware[F] = ???
// Which means the Request[F] goes through the middleware, will become option of Either[Response[F], Request[F]], 
// where Left means the request is denied and return immediately, Right means the authentication is OK and request will get pass.
// Now we need to define what we should do when middleware returns Left:
def reject[F[_]:Monad]: Kleisli[OptionT[F, *], Response[F], Response[F]] = Kleisli.ask[OptionT[F, *], Response[F]]
def authedRoute[F[_]:Monad] = auth[F] andThen (reject[F] ||| routes[F])
// You will then get a new route that has authentication ability by composing Kleisli.

// Another example will be HTTP response handler.
// attempt is syntax from MonadError
val resp: IO[Either[Throwable, String]] = httpClient.expect[String](uri"https://google.com/").attempt
// without Choice
resp.flatMap{
  case Left => ???
  case Right => ???
}

// with Choice
def recover[A](error: Throwable): IO[A] = ???
def processResp[A](resp: String): IO[A] = ???
resp >>= (recover _ ||| processResp _)

// ArrowChoice
// ArrowChoice is an extended version of Choice, which has one more method choose, with syntax +++
// With the middleware example, you can think ArrowChoice is middleware of middleware.
object m {
  trait ArrowChoice[F[_, _]] extends Choice[F] {
    def choose[A, B, C, D](f: F[A, C])(g: F[B, D]): F[Either[A, B], Either[C, D]]
  }
}
// For example if we want to append log to middleware of auth, that can log both when rejected response and pass request:
def logReject[F[_]]: Kleisli[OptionT[F, *], Response[F], Response[F]] = ???
def logThrough[F[_]]: Kleisli[OptionT[F, *], Request[F], Request[F]] = ???
def authedRoute[F[_]:Monad] = auth[F] andThen (logReject[F] +++ logThrough[F]) andThen (reject[F] ||| routes[F])



