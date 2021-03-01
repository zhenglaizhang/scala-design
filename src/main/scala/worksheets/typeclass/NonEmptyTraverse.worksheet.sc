// NonEmptyTraverse
// NonEmptyTraverse is a non-empty version of the Traverse type class, just like Reducible is a non-empty version of Foldable. As such, it extends both Reducible and Traverse in the type class hierarchy. It provides the nonEmptyTraverse and nonEmptySequence methods that require an instance of Apply instead of Applicative
import cats.Apply
import cats.data.NonEmptyList
object w {
  trait NonEmptyTraverse[F[_]] {
    def nonEmptyTraverse[G[_]: Apply, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
    def nonEmptySequence[G[_]: Apply, A](fga: F[G[A]]): G[F[A]]
  }
}
// Apply as a weakened Applicative lacking the pure method.
// One example type lacking an Applicative instance is Map[K, *], it’s impossible to implement a pure method for it.
// We can make use of NonEmptyTraverse, to traverse over a sequence of Maps. One example application one could think of is, when we have a list of text snippets, count the occurrence of each word in each snippet and return all the common words and their occurrences in each snippet:
val snippets = NonEmptyList.of("What do you do", "What are you doing")
def countWords(text: String): Map[String, Int] =
  text.split(" ").groupBy(identity).view.mapValues(_.length).toMap

import cats.implicits._
snippets.nonEmptyTraverse(countWords)
snippets.map(countWords).nonEmptySequence
val x: NonEmptyList[Map[String, Int]] = snippets.map(countWords)
x.nonEmptySequence
// Note that, just like traverse, nonEmptyTraverse(f) is equivalent to map(f).nonEmptySequence, so the above could be
// NonEmptyTraverse also offers flatNonEmptyTraverse and flatNonEmptySequence methods that are analogous to
// flatTraverse and flatSequence in Traverse. Just like with nonEmptyTraverse these methods don’t require a Monad instance, but only a FlatMap, which is the weakened version of Monad without pure.0
