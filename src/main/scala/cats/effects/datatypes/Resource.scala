package cats.effects.datatypes

// Effectfully allocates and releases a resource. Forms a MonadError on the resource type when the effect type has a
// Bracket instance.
//  - Nested resources are released in reverse order of acquisition. Outer resources are released even if an inner
//    acquisition, use or release fails.
//  - You can lift any F[A] with an Applicative instance into a Resource[F, A] with a no-op release via Resource.liftF:

import cats.effect.{Blocker, BracketThrow, ContextShift, IO, Resource}

import java.io.{BufferedReader, File, FileReader}
import scala.io.Source
object w1 {
  abstract class Resource[F[_], A] {
    def use[B](f: A => F[B])(implicit F: BracketThrow[F]): F[B]
  }
}

object Resource1 extends App {
  val greet: String => IO[Unit] = x => IO(println("Hello " + x))
  Resource.liftF(IO.pure("world")).use(greet).unsafeRunSync()
}

// Moreover it’s possible to apply further effects to the wrapped resource without leaving the Resource context via
// evalMap:
object Resource2 extends App {
  val acquire: IO[String] = IO(println("Acquire cats...")) *> IO("cats")
  val release: String => IO[Unit] = _ => IO(println("...release everything"))
  val addDogs: String => IO[String] = x =>
    IO(println("...more animals")) *> IO.pure(x + " and dogs")
  val report: String => IO[String] = x =>
    IO(println("...produce weather report...")) *> IO("It's raining " + x)
  val r = Resource
    .make(acquire)(release)
    .evalMap(addDogs)
    .use(report)
    .unsafeRunSync()
  println(r)
}

object Resource3 extends App {
  def mkRes(s: String): Resource[IO, String] = {
    val acquire = IO(println(s"Acquiring $s")) *> IO.pure(s)
    def release(s: String) = IO(println(s"Releaseing $s"))
    Resource.make(acquire)(release)
  }

  val r = for {
    outer <- mkRes("outer")
    inner <- mkRes("inner")
  } yield (outer, inner)
  r.use { case (a, b) => IO(println(s"Using $a and $b")) }
    .unsafeRunSync()
}

// If using an AutoCloseable create a resource without the need to specify how to close.
// with scala.io.Source
object R3 extends App {
  val acquire = IO { scala.io.Source.fromString("Hello world") }
  Resource
    .fromAutoCloseable(acquire)
    .use((source: Source) => IO(println(source.mkString)))
    .unsafeRunSync()
}

// with java.io using I/O
object R4 extends App {
  import scala.jdk.CollectionConverters._
  def readAllLines(bufferReader: BufferedReader, blocker: Blocker)(implicit
      cs: ContextShift[IO]
  ): IO[List[String]] =
    blocker.delay[IO, List[String]] {
      bufferReader.lines().iterator().asScala.toList
    }

  def reader(file: File, blocker: Blocker)(implicit
      cs: ContextShift[IO]
  ): Resource[IO, BufferedReader] =
    Resource.fromAutoCloseableBlocking(blocker)(IO {
      new BufferedReader(new FileReader(file))
    })

  def readLinesFromFile(file: File, blocker: Blocker)(implicit
      cs: ContextShift[IO]
  ): IO[List[String]] = {
    reader(file, blocker).use(br => readAllLines(br, blocker))
  }
}

object R5 extends App {
  import java.io._
  import cats.effect._
  import cats.syntax.flatMap._

  def reader[F[_]](file: File, blocker: Blocker)(implicit
      F: Sync[F],
      cs: ContextShift[F]
  ): Resource[F, BufferedReader] =
    Resource.fromAutoCloseableBlocking(blocker)(F.delay {
      new BufferedReader(new FileReader(file))
    })

  def dumpResource[F[_]](
      res: Resource[F, BufferedReader],
      blocker: Blocker
  )(implicit F: Sync[F], cs: ContextShift[F]): F[Unit] = {
    def loop(in: BufferedReader): F[Unit] =
      F.suspend {
        blocker.delay(in.readLine()).flatMap { line =>
          if (line != null) {
            System.out.println(line)
            loop(in)
          } else {
            F.unit
          }
        }
      }
    res.use(loop)
  }

  def dumpFile[F[_]](file: File, blocker: Blocker)(implicit
      F: Sync[F],
      cs: ContextShift[F]
  ): F[Unit] =
    dumpResource(reader(file, blocker), blocker)
}
