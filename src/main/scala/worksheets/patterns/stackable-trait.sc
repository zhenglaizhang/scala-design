import scala.collection.mutable.ArrayBuffer
// Scala design pattern in which traits provide stackable modifications to underlying core classes or traits.
// A trait (or class) can play one of three roles: the base, a core, or a stackable.
// The base trait (or abstract class) defines an abstract interface that all the cores and stackables extend,
// as shown below.
// The core traits (or classes) implement the abstract methods defined in the base trait, and provide basic, core functionality.
// Each stackable overrides one or more of the abstract methods defined in the base trait, using Scala's abstract override modifiers,
// and provides some behavior and at some point invokes the super implementation of the same method.
// In this manner, the stackables modify the behavior of whatever core they are mixed into.
// core -----------
//                |
//                -----> base
//                |
// stackable ------

//  Similar in structure to the decorator pattern, except it involves decoration for the purpose of class composition
//  instead of object composition. Stackable traits decorate the core traits at compile time, similar to the way
//  decorator objects modify core objects at run time in the decorator pattern.

// An abstract IntQueue class (the "base")
abstract class IntQueue {
  def get(): Int
  def put(x: Int): Unit
}

// Following three traits represent modifications,
// because they modify the behavior of an underlying "core" queue class rather than defining a full queue class themselves.
// The three are also stackable. You can select any of the three you like, mix them into a class, and obtain a new class that has all of the modifications you chose.
class BasicIntQueue extends IntQueue {
  private val buf = new ArrayBuffer[Int]()
  def get(): Int = buf.remove(0)
  def put(x: Int): Unit = {
    buf += x
  }
}

val q = new BasicIntQueue
q.put(1)
q.get()

// So far so good, let's take a look at using traits to modify this behavior.
// 1. Doubling declares a superclass, IntQueue.
//    This declaration means that the trait can only be mixed into a class that also extends IntQueue.
// 2. The trait has a super call on a method declared abstract.
//    Such calls are illegal for normal classes, because they will certainly fail at run time.
//    For a trait, however, such a call can actually succeed.
//    Since super calls in a trait are dynamically bound,
//    the super call in trait Doubling will work so long as the trait is mixed in after another trait or class that gives a concrete definition to the method.
//    This arrangement is frequently needed with traits that implement stackable modifications. To tell the compiler you are doing this on purpose, you must mark such methods as abstract override. This combination of modifiers is only allowed for members of traits, not classes, and it means that the trait must be mixed into some class that has a concrete definition of the method in question.
trait Doubling extends IntQueue {
  abstract override def put(x: Int): Unit = {
    super.put(2 * x)
  }
}

class MyQ extends BasicIntQueue with Doubling
val mq = new MyQ
mq.put(10)
mq.get()

abstract class StringWriter {
  def write(s: String): String
}

class BasicStringWriter extends StringWriter {
  override def write(s: String): String = s"Writing the following data: $s"
}

trait CapitalizingStringWriter extends StringWriter {
  abstract override def write(s: String): String =
    super.write(s.split("\\s+").map(_.capitalize).mkString(" "))
}

trait UppercastingStringWriter extends StringWriter {
  abstract override def write(s: String): String = super.write(s.toUpperCase)
}

val writer = new BasicStringWriter
  with UppercastingStringWriter
  with CapitalizingStringWriter
writer.write("we like learning scala")

// Stackable traits are always executed from the right mixin to the left.
// Sometimes, however, if we only get output and it doesn't depend on what is passed to the method,
// we simply end up with method calls on a stack, which then get evaluated and it will appear as if things are applied from left to right.
