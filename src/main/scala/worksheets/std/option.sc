// Observation: People are using Option too often where their business logic clearly
// indicates they should use their own, custom ADT.
// An example: A Query type where empty means match everything. Have that in our code right now

// The semantics of the Option type are pretty clear:
// it is about potential absence of a value
Map("a" -> 1, "b" -> 2) get "c"

//
// The problem of overloaded semantics
//
trait Retailer
trait Offer

def searchOffers(
    productTitle: Option[String],
    retailer: Option[Retailer]
): Seq[Offer] = ???
// It looks like we have attached some new semantics to the Option type. It seems that if productTitle is None, the user wants to search for all offers, regardless of the product title, which means that None has the meaning of a wildcard
// The problem is that we are overloading the semantics of None and Some. The former is very similar to how null in Java is sometimes used with meanings that are different from simple absence.

//
// Towards meaningful types
//
sealed trait SearchCriteria
object SearchCriteria {
  final case object MatchAll extends SearchCriteria
  final case class Contains(s: String) extends SearchCriteria
  final case class Exactly(s: String) extends SearchCriteria
}

sealed trait RetailerCriteria
object RetailerCriteria {
  final case object AnyRetailer extends RetailerCriteria
  final case class Only(retailer: Retailer) extends RetailerCriteria
  final case object NotRetailer extends RetailerCriteria
}

//
// Option being used in function arguments. It seems like this is rarely a good idea
//

trait Request
trait Response
def territoryFrom(request: Request) = ???
def accepted(unit: Unit) = ???
def requestBlockedResponse(str: String) = ???

sealed abstract class Verdict
case object Pass extends Verdict
final case class Block(reason: String) extends Verdict

object Block {
  def requestsFrom(territory: Nothing) = ???
}

def checkTerritory(request: Request): Verdict = {
  val territory = territoryFrom(request)
  if (accepted(territory)) Pass
  else Block.requestsFrom(territory)
}

def contentFilter(request: Request, next: Request => Response): Response = {
  checkTerritory(request) match {
    case Pass          => next(request)
    case Block(reason) => requestBlockedResponse(reason)
  }
}

/*
It is often possible to express a concept from your domain as an Option, or if that doesn't work, as an Either. Nevertheless, it is sometimes better to not use these generic types. If your actual semantics are different from potential absence of values, don't force it, as this causes unnecessary indirection when reasoning about the code and the domain. Unsurprisingly, this can easily lead to bugs. The domain logic is easier to understand if you use your own types from the ubiquitous language.
 */
