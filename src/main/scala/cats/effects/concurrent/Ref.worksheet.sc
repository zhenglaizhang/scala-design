// Ref
//  - A pure atomic reference
//  - mutual exclusion (communication channel)
//  - cannot be empty
//  - always initialized to a value
//  - modify is atomic
//  - allows concurrent update
//  - e.g. concurrent counter, cache
//  - Ref + Deferred can be used to build more complex structures

object w {
  abstract class Ref[F[_], A] {
    def get: F[A]
    def set(a: A): F[Unit]
    def modify[B](f: A => (A, B)): F[B]
    // ...
  }
}
// Provides safe concurrent access and modification of its content, but no functionality for synchronisation, which
// is instead handled by Deferred.
// For this reason, a Ref is always initialised to a value.
//
//The default implementation is nonblocking and lightweight, consisting essentially of a purely functional wrapper
// over an AtomicReference.

// Concurrent Counter

import cats.effect.{IO, Sync}
import cats.effect.concurrent.Ref
import cats.syntax.all._
import scala.concurrent.ExecutionContext

// Needed for triggering evaluation in parallel
implicit val ctx = IO.contextShift(ExecutionContext.global)
