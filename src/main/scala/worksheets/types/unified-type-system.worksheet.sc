import akka.actor.typed.delivery.ConsumerController.Start
import scala.collection.mutable.ArrayBuffer
// We refer to a Scala’s typesystem as being "unified" because there is a "Top Type", Any
// Scala takes on the idea of having one common Top Type for all Types by introducing Any. Any is a supertype of both AnyRef and AnyVal.
class Person
val allThings = ArrayBuffer[Any]()
val myInt = 42 // Int, kept as low-level `int` during runtime
allThings += myInt // Int (extends AnyVal)
allThings += new Person()
// 35: invokevirtual #47  // Method myInt:()I
// 38: invokestatic  #53  // Method scala/runtime/BoxesRunTime.boxToInteger:(I)Ljava/lang/Integer;
// 41: invokevirtual #57  // Method scala/collection/mutable/ArrayBuffer.$plus$eq:(Ljava/lang/Object;)Lscala/collection/mutable/ArrayBuffer;

// By having a smart compiler and treating everything as an object in this common hierarchy we’re able to get away from the "but primitives are different" edge-cases, at least at the level of our Scala source code - the compiler takes care of it for us. On JVM level, the distinction is still there of course, and scalac will do it’s best to keep using primitives wherever possible, as operations on them are faster, and take less memory
// We can limit a method to only be able to work on "lightweight" Value Types
//  The general idea is that this method will only take Value Classes, be it Int or our own Value Type. While probably not used very often, it shows how nicely the typesystem embraces java primitives, and brings them into the "real" type system, and not as a separate case, as is the case with Java.
def check(in: AnyVal) = ()
check(32)
check(12.0)
// check(new Object)  // AnyRef fails to compile

//
// The Bottom Types - Nothing and Null
//
// A very nice intuition about how bottom types work is: "Nothing extends everything."
//  e.g. Type inference always looks for the "common type" of both branches in an if statement
//
//  Null extends all AnyRefs
//  Nothing extends anything

val test = true
val foo: Int =
  if (test) 32 // : Int
  else throw new Exception("") // : Nothing
//           [Int] -> ... -> AnyVal -> Any
//Nothing -> [Int] -> ... -> AnyVal -> Any

val thing: String =
  if (test)
    "Yay!" // : String
  else
    null // : Null
//         [String] -> AnyRef -> Any
// Null -> [String] -> AnyRef -> Any

val bar = if (false) 23 else null
// bar: Any
// Int  -> NotNull -> AnyVal -> [Any]
// Null            -> AnyRef -> [Any]

// abstract class AnyVal extends Any with NotNull

//
// Type of an object
//
object Obj
def takeObj(obj: Obj.type) = ()
takeObj(Obj)

//
// self type annotation
//
// Self Types are used in order to "require" that, if another class uses this trait, it should also provide implementation of whatever it is that you’re requiring.

class ServiceInModule {
  def doSth() = ()
}
trait ApiModule {
  lazy val serviceInModule = new ServiceInModule
}

trait MongoModule {
  def getDoc() = ()
}

trait Service {
  self: ApiModule with MongoModule => // I’m a Module
  // Someone will have to give us this Module at instantiation time
  // Not same with extending Module
  // In fact, you can use any identifier (not just this or self) and then refer to it from your class.

  def doTheThing() = self.serviceInModule.doSth()
  def get() = self.getDoc()
}

trait TestingModule extends ApiModule with MongoModule {}
// new Service {}
new Service with TestingModule {}

//
// Phantom Type
//
// Types that are not instantiate, ever
// Instead of using them directly, we use them to even more strictly enforce some logic, using our types.

sealed trait ServiceState
final class Started extends ServiceState
final class Stopped extends ServiceState

class MyService[State <: ServiceState] private () {
  def start[T >: State <: Stopped]() = this.asInstanceOf[MyService[Started]]
  def stop[T >: State <: Started]() = this.asInstanceOf[MyService[Stopped]]
  // Since nothing is actually using this type, you won’t bump into class cast exceptions during this conversion.
}
object MyService {
  def apply() = new MyService[Stopped]
  def create() = apply()
}

val initStopped = MyService.create()
val started = initStopped.start()
val stopped = started.stop();
// Phantom Types are yet another great facility to make our code even more type-safe (or shall I say "state-safe"!?).
