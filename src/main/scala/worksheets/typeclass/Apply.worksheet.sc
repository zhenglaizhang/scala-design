// Apply extends the Functor type class (which features the familiar map function) with a new function ap. The ap function is similar to map in that we are transforming a value in a context (a context being the F in F[A]; a context can be Option, List or Future for example). However, the difference between ap and map is that for ap the function that takes care of the transformation is of type F[A => B], whereas for map it is A => B:

import cats._

implicit val optionApply: Apply[Option] = new Apply[Option] {
  def ap[A, B](f: Option[A => B])(fa: Option[A]): Option[B] =
    fa.flatMap(a => f.map(ff => ff(a)))
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa map f
  override def product[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
    fa.flatMap(a => fb.map(b => (a, b)))
}

implicit val listApply: Apply[List] = new Apply[List] {
  def ap[A, B](f: List[A => B])(fa: List[A]): List[B] =
    fa.flatMap(a => f.map(ff => ff(a)))
  def map[A, B](fa: List[A])(f: A => B): List[B] = fa map f
  override def product[A, B](fa: List[A], fb: List[B]): List[(A, B)] =
    fa.zip(fb)
}


// map
import cats.implicits._

val intToString: Int => String = _.toString
val double: Int => Int = _ * 2
val addTwo: Int => Int = _ + 2
Apply[Option].map(Some(1))(intToString) 
Apply[Option].map(Some(1))(double) 
Apply[Option].map(None)(addTwo) 

// The ap method is a method that Functor does not have:
Apply[Option].ap(Some(intToString))(Some(1)) 
Apply[Option].ap(Some(double))(Some(1)) 
Apply[Option].ap(Some(double))(None) 
Apply[Option].ap(None)(Some(1)) 
Apply[Option].ap(None)(None) 

// ap2, ap3, ...
// Apply also offers variants of ap. The functions apN (for N between 2 and 22) accept N arguments where ap accepts 1.
// Note that if any of the arguments of this example is None, the final result is None as well. The effects of the context we are operating on are carried through the entire computation:
val addArity2 = (a: Int, b: Int) => a + b
Apply[Option].ap2(Some(addArity2))(Some(1), Some(2)) 
Apply[Option].ap2(Some(addArity2))(Some(1), None) 
val addArity3 = (a: Int, b: Int, c: Int) => a + b + c
Apply[Option].ap3(Some(addArity3))(Some(1), Some(2), Some(3)) 

// MAP2, MAP3, ...
Apply[Option].map2(Some(1), Some(2))(addArity2) 
Apply[Option].map3(Some(1), Some(2), Some(3))(addArity3) 

// TUPLE2, TUPLE3, ETC
Apply[Option].tuple2(Some(1), Some(2)) 
Apply[Option].tuple3(Some(1), Some(2), Some(3)) 

// APPLY BUILDER SYNTAX
import cats.implicits._
// In order to use functions apN, mapN and tupleN *infix*, import cats.implicits._.
val option2 = (Option(1), Option(2))
val option3 = (option2._1, option2._2, Option.empty[Int])
option2 mapN addArity2 
option3 mapN addArity3 
option2 apWith Some(addArity2) 
option3 apWith Some(addArity3) 
option2.tupled 
option3.tupled 

