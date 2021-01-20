import eu.timepit.refined.collection.Empty
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.api.Refined
// https://www.youtube.com/watch?v=UlxxQRniW-k
// refined types
//  - if it compiles, then it works

// name and namespace could not be empty, and version had to be a positive integer.
case class Schema(name: String, namespace: Namespace, version: Int)

type NameRestrictions = Not[Empty]
type NamespaceRestrictions = Not[Empty]

// type NameRestrictions      = MatchesRegex[W.`"[a-zA-Z]+[a-zA-Z0-9]*(?:[_-][a-zA-Z0-9]+)*"`.T]
// type NamespaceRestrictions = MatchesRegex[W.`"[a-zA-Z]+[a-zA-Z0-9]*(?:[._-][a-zA-Z0-9]+)*"`.T]

type VersionRestrictions = Positive

type Name = String Refined NameRestrictions
type Namespace = String Refined NamespaceRestrictions
type Version = Int Refined VersionRestrictions

def getLatestVersion(name: Name, namespace: Namespace): Version = ???
def getAllSchemasFromNamespace(namespace: Namespace): List[Schema] = ???

// Our Domain is really clean, as any object being read/returned is checked very strictly to verify that represents exactly what we want. This greatly reduces the number of bugs and helps in the long term maintenance of the code base,
