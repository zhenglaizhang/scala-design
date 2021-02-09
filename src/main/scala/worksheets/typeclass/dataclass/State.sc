// State is a structure that provides a functional approach to handling application state.
//  - State[S, A] is basically a function S => (S, A)
//  - Where S is the type that represents your state and A is the result the function produces. In addition to
//  returning the result of type A, the function returns a new S value, which is the updated state.
//  - State’s special power is keeping track of state and passing it along.

final case class Robot(id: Long, sentient: Boolean, name: String, model: String)

val rng = new scala.util.Random(0L)

def createRobot(): Robot = {
  val id = rng.nextLong()
  val sentient = rng.nextBoolean()
  val isCatherine = rng.nextBoolean()
  val name = if (isCatherine) "Catherine" else "Carlos"
  val isReplicant = rng.nextBoolean()
  val model = if (isReplicant) "replicant" else "borg"
  Robot(id, sentient, name, model)
}

val robot = createRobot()

val rng = new scala.util.Random(0L)

def createRobot(): Robot = {
  val id = rng.nextLong()
  val b = rng.nextBoolean()
  val sentient = b
  val isCatherine = b
  val name = if (isCatherine) "Catherine" else "Carlos"
  val isReplicant = b
  val model = if (isReplicant) "replicant" else "borg"
  Robot(id, sentient, name, model)
}

val robot = createRobot()

// the nextBoolean calls we were making had the side effect of mutating internal RNG state,
// and we were depending on that behavior
// When we can’t freely refactor identical code into a common variable,
// the code becomes harder to reason about.
// In functional programming lingo, one might say that such code lacks referential transparency.

//Purely functional pseudorandom values
final case class Seed(long: Long) {
  def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
}

// Instead of mutating the existing long value,
// calling next returns a new Seed instance with an updated long value.
// Since the RNG isn’t updating state internally, we will need to keep track of state outside of the RNG
def nextBoolean(seed: Seed): (Seed, Boolean) =
  (seed.next, seed.long >= 0L)

def nextLong(seed: Seed): (Seed, Long) =
  (seed.next, seed.long)

def createRobot(seed: Seed): Robot = {
  val (seed1, id) = nextLong(seed)
  val (seed2, sentient) = nextBoolean(seed1)
  val (seed3, isCatherine) = nextBoolean(seed2)
  val name = if (isCatherine) "Catherine" else "Carlos"
  val (seed4, isReplicant) = nextBoolean(seed3)
  val model = if (isReplicant) "replicant" else "borg"
  Robot(id, sentient, name, model)
}

val initialSeed = Seed(13L)
val robot = createRobot(initialSeed)
// Now it is a bit more obvious that we can’t extract the three nextBoolean calls into a single variable, because we
// are passing each one a different seed value.
// It is a bit cumbersome to explicitly pass around all of this intermediate state.
// It’s also a bit error-prone.

import cats.data.{State, StateT}

import scala.concurrent.Future

val nextLong: State[Seed, Long] = State(seed => (seed.next, seed.long))
// The map method on State allows us to transform the A value without affecting the S (state) value

val nextBoolean: State[Seed, Boolean] = nextLong.map(long => long >= 0)
// The flatMap method on State[S, A] lets you use the result of one State in a subsequent State.
// The updated state (S) after the first call is passed into the second call.
// These flatMap and map methods allow us to use State in for-comprehensions
val createRobot: State[Seed, Robot] =
  for {
    id <- nextLong
    sentient <- nextBoolean
    isCatherine <- nextBoolean
    name = if (isCatherine) "Catherine" else "Carlos"
    isReplicant <- nextBoolean
    model = if (isReplicant) "replicant" else "borg"
  } yield Robot(id, sentient, name, model)

val (finalState, robot) = createRobot.run(initialSeed).value
val robot = createRobot.runA(initialSeed).value
// The createRobot implementation reads much like the imperative code
// we initially wrote for the mutable RNG.
// However, this implementation is free of mutation and side-effects.
// Since this code is referentially transparent, we can perform the refactoring that we tried earlier without
// affecting the result:
val createRobot: State[Seed, Robot] = {
  // b is a function that takes a seed and returns a Boolean, threading state along the way.
  val b = nextBoolean

  for {
    id <- nextLong
    sentient <- b
    isCatherine <- b
    name = if (isCatherine) "Catherine" else "Carlos"
    isReplicant <- b
    model = if (isReplicant) "replicant" else "borg"
  } yield Robot(id, sentient, name, model)
}
val robot = createRobot.runA(initialSeed).value

// Interleaving effects

import scala.concurrent.ExecutionContext.Implicits.global

final case class AsyncSeed(long: Long) {
  def next: Future[AsyncSeed] =
    Future(AsyncSeed(long * 6364136223846793005L + 1442695040888963407L))
}

//val nextLong: State[AsyncSeed, Future[Long]] = State { seed =>
//  (seed.next, seed.long)
//}
//State[S, A] is an alias for StateT[Eval, S, A]
//  - a monad transformer defined as StateT[F[_], S, A].
//  - this data type represents computations of the form S => F[(S, A)]
val nextLong: StateT[Future, AsyncSeed, Long] = StateT { seed =>
  seed.next zip Future.successful(seed.long)
}

nextLong.run(AsyncSeed(0))

// StateT[F[_], S, A] allows us to interleave effects of type F[_] in the computations wrapped by it

// Changing States
sealed trait DoorState

case object Open extends DoorState

case object Closed extends DoorState

case class Door(state: DoorState)

val openImpl: State[DoorState, Unit] = State {
  case Closed => (Open, ())
  case Open   => ???
}
//The most elegant solution would be to model this requirement statically using types,
// and luckily, StateT is an alias for another type: IndexedStateT[F[_], SA, SB, A]
//  SA => F[(SB, A)]
// a function that receives an initial state of type SA and results in a state of type SB and a result of type A,
// using an effect of F.

import cats.Eval
import cats.data.IndexedStateT

def open: IndexedStateT[Eval, Closed.type, Open.type, Unit] =
  IndexedStateT.set(Open)
def close: IndexedStateT[Eval, Open.type, Closed.type, Unit] =
  IndexedStateT.set(Closed)

// a function that receives an initial state of type SA and results in a state of type SB and a result of type A,
// using an effect of F.
//val invalid = for {
//  _ <- open
//  _ <- close
//  _ <- close
//} yield ()
// We can now reject, at compile time, sequences of open and close that are invalid:

val valid = for {
  _ <- open
  _ <- close
  _ <- open
} yield ()

valid.run(Closed)
//valid.run(Open)
