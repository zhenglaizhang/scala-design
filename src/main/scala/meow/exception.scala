package meow

object abc {

  def failFn(i: Int): Int = {
    try {
      val x = 32 + 4
      x + ((throw new Exception("fail")): Int)
    } catch {
      case e: Exception => 43
    }
  }

  // total function
  def mean(xs: Seq[Double]): Option[Double] =
    if (xs.isEmpty) None else Some(xs.sum / xs.length)
}

sealed trait Option[+A] {
  def map[B](f: A => B): Option[B] =
    this match {
      case Some(get) => Some(f(get))
      case None      => None
    }
  def flatMap[B](f: A => Option[B]): Option[B] =
    this match {
      case Some(get) => f(get)
      case None      => None
    }
  def getOrElse[B >: A](b: => B): B =
    this match {
      case Some(get) => get
      case None      => b
    }
  def orElse[B >: A](b: => Option[B]): Option[B] =
    this match {
      case Some(_) => this
      case None    => b
    }
  def filter(f: A => Boolean): Option[A] =
    this match {
      case Some(get) if f(get) => this
      case _                   => None
    }
}
case class Some[+A](get: A) extends Option[A]
case object None extends Option[Nothing]

object Option {
  def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

  def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
    for {
      aa <- a
      bb <- b
    } yield f(aa, bb)
  // (a, b) match {
  //   case (Some(av), Some(bv)) => Some(f(av, bv))
  //   case _                    => None
  // }

  def map3[A, B, C, D](a: Option[A], b: Option[B], c: Option[C])(
      f: (A, B, C) => D
  ): Option[D] =
    (a, b, c) match {
      case (Some(av), Some(bv), Some(cv)) => Some(f(av, bv, cv))
      case _                              => None
    }

  def sequence[A](xs: List[Option[A]]): Option[List[A]] = ???

  def traverse[A, B](xs: List[A])(f: A => Option[B]): Option[List[B]] = ???
}

object ExApp extends App {}
