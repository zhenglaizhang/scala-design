// Do not confuse partially applied functions with partially defined functions,
// which are represented by the PartialFunction type in Scala.

case class Email(
                  subject: String,
                  text: String,
                  sender: String,
                  recipient: String
                )

// type alias
type EmailFilter = Email => Boolean
type IntPairPred = (Int, Int) => Boolean
def sizeConstraint(pred: IntPairPred, n: Int, email: Email) =
  pred(email.text.size, n)

val gt: IntPairPred = _ > _
val ge: IntPairPred = _ >= _
val lt: IntPairPred = _ < _
val le: IntPairPred = _ <= _
val eq: IntPairPred = _ == _

//  You have to use the placeholder _ for all parameters not bound to an argument value
val minimumSize: (Int, Email) => Boolean = sizeConstraint(ge, _: Int, _: Email)
val maximumSize: (Int, Email) => Boolean = sizeConstraint(le, _, _)
val constr20: (IntPairPred, Email) => Boolean = sizeConstraint(_, 20, _)

// From methods to function objects
// When doing partial application on a method, you can also decide to not bind any parameters whatsoever. The parameter list of the returned function object will be the same as for the method. You have effectively turned a method into a function that can be assigned to a val or passed around:
val sizeConstraintFn: (IntPairPred, Int, Email) => Boolean = sizeConstraint _

val min20: EmailFilter = minimumSize(20, _: Email)
val max20: EmailFilter = maximumSize(20, _: Email)

// Methods in Scala can have more than one parameter list.
def sizeConstr(pred: IntPairPred)(n: Int)(email: Email): Boolean =
  pred(email.text.size, n)

val sizeConstraintFn2: IntPairPred => Int => Email => Boolean = sizeConstr _
// Such a chain of one-parameter functions is called a curried function
// In the Haskell programming language, all functions are in curried form by default.
val minSize2: Int => Email => Boolean = sizeConstr(ge)
// There is no need to use any placeholders for parameters left blank, because we are in fact not doing any partial function application.
val min20: Email => Boolean = sizeConstraintFn2(ge)(20)

// Transforming a function with multiple parameters in one list to curried form
val sum: (Int, Int) => Int = _ + _
val sumCurried: Int => Int => Int = sum.curried
sumCurried(10)(20)
Function.uncurried(sumCurried)(10, 20)

//
// Injecting your dependencies the functional way
//

case class User(name: String)

trait EmailRepository {
  def getMails(user: User, unread: Boolean): Seq[Email]
}

trait FilterRepository {
  def getEmailFilter(user: User): EmailFilter
}

trait MailboxService {
  // These dependencies are declared as parameters to the getNewMails method,
  // each in their own parameter list.
  def getNewMails(
                   emailRepo: EmailRepository
                 )(filterRepo: FilterRepository)(user: User) =
    emailRepo.getMails(user, true).filter(filterRepo.getEmailFilter(user))

  val newMails: User => Seq[Email]
}

object MockEmailRepository extends EmailRepository {
  def getMails(user: User, unread: Boolean): Seq[Email] = Nil
}

object MockFilterRepository extends FilterRepository {
  def getEmailFilter(user: User): EmailFilter = _ => true
}

object MailboxServiceWithMockDeps extends MailboxService {
  val newMails: (User) => Seq[Email] =
    getNewMails(MockEmailRepository)(MockFilterRepository) _
}

MailboxServiceWithMockDeps.newMails(User("daniel"))
