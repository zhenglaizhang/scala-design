import cats.conversions.all
import scala.collection.mutable.ArrayBuffer
// We refer to a Scala’s typesystem as being "unified" because there is a "Top Type", Any
// Scala takes on the idea of having one common Top Type for all Types by introducing Any. Any is a supertype of both AnyRef and AnyVal.
class Person
val allThings = ArrayBuffer[Any]()
val myInt = 42             // Int, kept as low-level `int` during runtime
allThings += myInt         // Int (extends AnyVal)
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