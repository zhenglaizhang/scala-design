//
// Path Dependant Type
//

// ref:
//  1. https://medium.com/virtuslab/path-dependent-types-9f2d7927c1fa

// A path dependent type is a type that depends on a value.
// This Type allows us to type-check on a Type internal to another class.
// If you could somehow encode the length of a list (a value) into its type then it’d be trivial to state that list of length 0 (type that depends on a value, mind you) has no head to be accessed

// There is no way to encode a number that is not compile-time constant.

class Outer {
  class Inner
  var b: Option[Inner] = None
}

val a1 = new Outer
val a2 = new Outer

type PathDepType1 = a1.Inner // Path dependant type, the path is "inside a1"
type PathDepType2 = a2.Inner

val b1: PathDepType1 = new a1.Inner
val b2: a2.Inner = new a2.Inner
a1.b = Some(b1)
a2.b = Some(b2)
// a1.b = Some(b2)
// type mismatch
//   found App.this.a2.B
//   required: App.this.a1.B

// Each Outer class has its own Inner class
// so it’s a different Type - dependent on which path we use to get there.
// We’re able to enforce getting the type from inside of a concrete parameter

class Parent {
  class Child
}

// class ChildrenContainer(p: Parent) {
class ChildrenContainer(val p: Parent) {
  // private value p escapes its defining scope as part of type ChildrenContainer.this.p.Childmdoc
  type ChildOfThisParent = p.Child
  // Using the path dependent type we have now encoded in the type system, the logic, that this container should only contain children of this parent - and not "any parent".
  def add(c: ChildOfThisParent) = ???
}

// The "child of any parent" Type in the section about Type Projections
// As a general rule, the type of a value or method must always be visible anywhere that is allowed to see that value or method.

class A {

  import A.B

  // private class B escapes its defining scope as part of type App.this.A.Bmdoc
  // @transient protected var x: B = null // this line gives compilation error
}

object A {
  private final class B {}
}

//
// Type Projection
//  - Similar to Path Dependent Types in the way that they allow you to refer to a type of an inner class.
//  - Type Projections can be used for "type level programming" ;-) == Existential Types
class Out {
  class In
}

// Type Projection (and alias) refering to Inner
type OutInProjection = Out#In
val out1 = new Out
val outin = new out1.In
val outin1: OutInProjection = outin
val outin2: Out#In = new out1.In

// Existential Types are something that deeply relates to Type Erasure, which JVM languages "have to live with".
val thingy: Any = 12
thingy match {
  // case l: List[a] => // lower case 'a', matches all types... what type is 'a'?!
  case l: List[_] => // shortcut for existentional type
  case _ => 
}
// We don’t know the type of a, because of runtime type erasure. 
// We know though that List is a type constructor, * -> *, 
// so there must have been some type, it could have used to construct a valid List[T]. 
// This "some type", is the existentional type!


// Let’s say you’re working with some Abstract Type Member, that in our case will be some Monad. We want to force users of our class into using only Cool instances within this Monad, because for example, only for these Types our Monad has any meaning. We can do this via Type Bounds on these Existential Type T:
// http://mikeslinn.blogspot.com/2012/08/scala-existential-types.html
// type Monad[T] for Some{ type T >: Cool }

