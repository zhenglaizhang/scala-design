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

class ChildrenContainer(p: Parent) {
  type ChildOfThisParent = p.Child
  // Using the path dependent type we have now encoded in the type system, the logic, that this container should only contain children of this parent - and not "any parent".
  def add(c: ChildOfThisParent) = ???
}


// The "child of any parent" Type in the section about Type Projections 