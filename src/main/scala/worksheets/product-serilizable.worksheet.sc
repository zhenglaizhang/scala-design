object EnumExample1 {
  sealed abstract class Status
  case object Pending extends Status
  case object InProgress extends Status
  case object Finished extends Status
}

val incomplete = Set(EnumExample1.Pending, EnumExample1.InProgress)
Set[EnumExample1.Status](EnumExample1.Pending, EnumExample1.InProgress)
// val incomplete: scala.collection.immutable.Set[Product with EnumExample1.Status with java.io.Serializable] = Set(Pending, InProgress)

// The compiler generally tries to infer the most specific type possible.
// The compiler was a bit too clever and realized that not only is every item in the set an instance of Status, they are also instances of Product and Serializable since every case object (and case class) automatically extends Product and Serializable. Therefore, when it calculates the least upper bound (LUB) of the types in the set, it comes up with Product with Serializable with Status.
// E.g could not find implicit value for evidence parameter of type appserializer.Upickle.Writer[Product with
//  Serializable with model.panel.ViewElement] just happen in my code :)

// While there’s nothing inherently wrong with the return type of Product with Serializable with Status, it is verbose, it wasn’t what I intended, and in certain situations it might cause inference issues. Luckily there’s a simple workaround to get the inferred type that I want:
object EnumExample2 {
  sealed abstract class Status extends Product with Serializable
  case object Pending extends Status
  case object InProgress extends Status
  case object Finished extends Status
}

import EnumExample2._
val incomplete = Set(Pending, InProgress)
// val incomplete: scala.collection.immutable.Set[EnumExample2.Status] = Set(Pending, InProgress)
// Now since Status itself already includes Product and Serializable, Status is the LUB type of Pending, InProgress,
// and Finished.
