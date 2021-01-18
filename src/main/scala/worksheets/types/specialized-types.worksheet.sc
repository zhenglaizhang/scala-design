// Type specialization is actually more of an performance technique

// Ways to limit this exponential explosion - just specialize with most frequently used target types
case class Parcel[@specialized(Int, Long) A](value: A) {
  def foo: A = value
}
// as A can be anything, it will be represented as an Java object, even if we’d only ever put Int into boxes.
val i: Int = Int.unbox(Parcel.apply(Int.box(1)).value)
// int <=> object Int

// "specialize" our Parcel for all primitive types
// case class IntParcel(intValue: Int) {
//   override def foo: Int = intValue // work on low leve Int, no wrapping
// }
//  the code basically has just become far less maintanable, with N implementations, for each primitive that we want to support
// so @specialized comes into play
// So we’re applying the @specialized annotation to the type parameter A, thus instructing the compiler to generate all specialized variants of this class - that is: ByteParcel, IntParcel, LongParcel, FloatParcel, DoubleParcel, BooleanParcel, CharParcel, ShortParcel, CharParcel and even VoidParcel (not actual names of the implementors, but you get the idea)

val pi = Parcel(1) // will use `int` specialized methods
val pl = Parcel(1L) // will use `long` specialized methods
val pb = Parcel(false) // will use `boolean` specialized methods
val po = Parcel("pi") // will use `Object` methods

// It can speed-up low level operations multiple times with lowering memory usage at the same time!
// Sadly, it comes at a high price: the generated code quickly becomes huge when used with multiple parameters like this:
case class Thing[A, B](@specialized a: A, @specialized b: B)
// the above code would generate 8 * 8 = 64 (sic!) implementations,

// :javap Parcel

// Parcel, specialized for Int and Long
// public class Parcel extends java.lang.Object implements scala.Product,scala.Serializable{
//     public java.lang.Object value(); // generic version, "catch all"
//     public int value$mcI$sp();       // int specialized version
//     public long value$mcJ$sp();}     // long specialized version

//     public boolean specInstance$();  // method to check if we're a specialized class impl.
// }

// If you’re curious, currently these classes are specialized in Scala (list may be incomplete): 
// - Function0, Function1, Function2, Tuple1, Tuple2, Product1, Product2, AbstractFunction0, AbstractFunction1, AbstractFunction2. 
// Due to how costy it is to specialize beyond 2 parameters, it’s a trend to not specialize for more params, although certainly possible.


// Specialization is quite powerful, yet at the same time it’s a bit of a "compiler bomb", with it’s exponential growth potential.
// http://scala-miniboxing.org/