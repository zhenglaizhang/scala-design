// ContT
// A pattern that appears sometimes in functional programming is that of a function first computing some kind of intermediate result and then passing that result to another function which was passed in as an argument, in order to delegate the computation of the final result.

case class User(id: Int, name: String, age: Int)
sealed abstract class UserUpdateResult
case class Succeeded(updatedUserId: Int) extends UserUpdateResult
case object Failed extends UserUpdateResult

import cats.Eval
import cats.data.ContT
def updateUser(
    persistToDatabase: User => Eval[UserUpdateResult]
)(existingUser: User, newName: String, newAge: Int): Eval[UserUpdateResult] = {
  val trimmedName = newName.trim
  val cappedAge = newAge max 150
  val updatedUser = existingUser.copy(name = trimmedName, age = cappedAge)
  persistToDatabase(updatedUser)
}
// Our updateUser function takes in an existing user and some updates to perform. It sanitises the inputs and updates
// the user model, but it delegates the database update to another function which is passed in as an argument.

// This pattern is known as “continuation passing style” or CPS,
// and the function passed in (persistToDatabase) is known as a “continuation”.

// Note the following characteristics:
//  - The return type of our updateUser function (Eval[UserUpdateResult]) is the same as the return type of the
// continuation function that was passed in.
//  - Our function does a bit of work to build an intermediate value, then passes that value to the continuation, which
// takes care of the remainder of the work.
import cats.data.Cont
def updateUserCont(
    existingUser: User,
    newName: String,
    newAge: Int
): ContT[Eval, UserUpdateResult, User] =
  ContT.apply[Eval, UserUpdateResult, User] {
    next: (User => Eval[UserUpdateResult]) =>
      val trimmedName = newName.trim
      val cappedAge = newAge max 150
      val updatedUser = existingUser.copy(name = trimmedName, age = cappedAge)
      next(updatedUser)
  }

val existingUser = User(100, "Alice", 42)
val computation = updateUserCont(existingUser, "Bob", 200)
// computation: ContT[Eval, UserUpdateResult, User] = FromFn(
//   Single(<function1>, 0)
// )

val eval: Eval[UserUpdateResult] = computation.run { user =>
  Eval.later {
    println(s"Persisting updated user to the DB: $user")
    Succeeded(user.id)
  }
}
// Finally we can run the resulting Eval to actually execute the computation:
eval.value

// Composition
//  - Why we use ContT, just encoded in a slightly different way?
//  - The point is that ContT is a monad, so by rewriting our function into a ContT we gain composibility for free.0

val anotherComputation = computation.map { user =>
  Map(
    "id" -> user.id.toString,
    "name" -> user.name,
    "age" -> user.age.toString
  )
}
val anotherEval = anotherComputation.run { userFields =>
  Eval.later {
    println(s"Persisting these fields to the DB: $userFields")
    Succeeded(userFields("id").toInt)
  }
}
anotherEval.value

// And we can use flatMap to chain multiple ContTs together.

val updateUserModel: ContT[Eval, UserUpdateResult, User] =
  updateUserCont(existingUser, "Bob", 200).map { updatedUser =>
    println("Updated user model")
    updatedUser
  }

val persistToDb: User => ContT[Eval, UserUpdateResult, UserUpdateResult] = {
  user =>
    ContT.apply[Eval, UserUpdateResult, UserUpdateResult] { next =>
      println(s"Persisting updated user to the DB: $user")

      next(Succeeded(user.id))
    }
}
val publishEvent
    : UserUpdateResult => ContT[Eval, UserUpdateResult, UserUpdateResult] = {
  userUpdateResult =>
    ContT.apply[Eval, UserUpdateResult, UserUpdateResult] { next =>
      userUpdateResult match {
        case Succeeded(userId) =>
          println(s"Publishing 'user updated' event for user ID $userId")
        case Failed =>
          println("Not publishing 'user updated' event because update failed")
      }

      next(userUpdateResult)
    }
}

val chainOfContinuations =
  updateUserModel flatMap persistToDb flatMap publishEvent

val eval = chainOfContinuations.run { finalResult =>
  Eval.later {
    println("Finished!")
    finalResult
  }
}
eval.value

// Why Eval?
// If you’re wondering why we used Eval in our examples above, it’s because the Monad instance for ContT[M[_], A, B]
// requires an instance of cats.Defer for M[_]. This is an implementation detail - it’s needed in order to preserve
// stack safety.
//
// In a real-world application, you’re more likely to be using something like cats-effect IO, which has a Defer instance
