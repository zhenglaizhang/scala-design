// Id
// The identity monad can be seen as the ambient monad that encodes the effect of having no effect. 
// It is ambient in the sense that plain pure values are values of Id
object w {
  // type Id[A] is just a synonym for A.
  // We can freely treat values of type A as values of type Id[A], and vice-versa.
  type Id[A] = A
}

import cats._
val x: Id[Int] = 1
val y: Int = x

// Using this type declaration, we can treat our Id type constructor as a Monad and as a Comonad. 
// The pure method, which has type A => Id[A] just becomes the identity function. 
// The map method from Functor just becomes function application:

import cats.Functor
import cats.Monad
val one: Int = 1
// map is function application
Functor[Id].map(one)(_ + 1)
Monad[Id].map(one)(_ + 1)
// for Id, flatMap is also just function application:
Monad[Id].flatMap(one)(_ + 1)

// Compare the signatures of map and flatMap and coflatMap:
//  - def map[A, B](fa: Id[A])(f: A => B): Id[B]
//  - def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B]
//  - def coflatMap[A, B](a: Id[A])(f: Id[A] => B): Id[B]

// coflatMap is just function application
import cats.Comonad
Comonad[Id].coflatMap(one)(_ + 1)
