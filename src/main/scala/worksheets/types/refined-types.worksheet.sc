import eu.timepit.refined.collection.Size
import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.auto._
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.collection.Empty
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.api.Refined
// todo: read
// https://www.youtube.com/watch?v=UlxxQRniW-k
// https://medium.com/swlh/refined-types-the-good-the-bad-and-the-ugly-ee971e5d9137
// https://kwark.github.io/refined-in-practice/#1
// http://www.beyondthelines.net/programming/refined-types/
// https://tech.ovoenergy.com/safe-expressive-code-with-refinement-types/
// https://stackoverflow.com/questions/43051076/refined-and-existential-types-for-runtime-values
// https://kubuszok.com/2018/kinds-of-types-in-scala-part-3/
// https://www.slideshare.net/techtriveni/lets-refine-your-scala-code
// https://www.john-cd.com/cheatsheets/Scala/Scala_Design_Patterns/#static-factory

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

//
// Say goodbye to Isomorphisms, say hello to messy Either
//

type PositiveInt = Int Refined Positive
type Id = Int Refined Interval.Open[100, 500]
type CodeArticle = String Refined MatchesRegex["^[0-9]{3,12}$"]

object PositiveInt extends RefinedTypeOps[PositiveInt, Int]
object Id extends RefinedTypeOps[Id, Int]
object CodeArticle extends RefinedTypeOps[CodeArticle, String]

// By inheriting from RefinedTypeOps all the usual functions are defined for our type. We get apply and unapply definitions along with from and unsafeFrom

// Because positiveInt value (first line) is known at compile time, the apply function hold
val pi: PositiveInt = PositiveInt(5)
val int: Int = 5
// the apply function is a bit more complex. It will only work with literal values which are values known at compile time
// the int value is a reference, meaning it’s not possible to check it’s value at compile time
// val fail: PositiveInt = PositiveInt(int)
// val ni: PositiveInt = PositiveInt(-1)

// The apply function call a macro, transforming the Scala code at the call site. The Expr instance taken by the macro contains only the parameter of the apply call, no more information. If this Expr does not contain an instance of Literal its impossible (with the current Scala macro implementation) to check what the parameter hold, even if its value have been hard-coded somewhere else.

val errorOrPo: Either[String, PositiveInt] = PositiveInt.from(int)
val pi2: PositiveInt = PositiveInt.unsafeFrom(int)
// val fail: PositiveInt =
//   PositiveInt.unsafeFrom(
//     -1
//   ) //will throw an IllegalArgumentException with error: -1 < 0

// Refined can simplify your validations while also making them safer
final case class Article(id: Id, codeArticle: CodeArticle)
val id = 102
val code = "001"
val errorOrArticle: Either[String, Article] = for {
  validId <- Id.from(id)
  validCode <- CodeArticle.from(code)
} yield Article(validId, validCode)

// But maybe you dont want your validation to stop at the first one ? For this you can use a Validated from the cats library.

// Refined let you engrave business into the Type System
//  - clarity
//  - By having the validation inside the type system one can derive a lot of boilerplate from it.
//  - Easier testing
//  - Refined enforce business rules in the Type System, therefore reducing the range of values a type can take. The more precise a function definition is, the less test it needs.

//  A function can be used in two contexts : before input data is validated or after. As validation is a side-effect, functions working with non-valid data are rare, and the other group is the majority. For this majority, inputs should be strictly typed. It avoid protective programming (no IllegalArgumentException ) and transform the tests of this function into specs, as we can describe the nominal behavior of the function alone. Tests then become references we can trust.

type Siret = String Refined MatchesRegex["^[0-9]{14}$"]
type PostCode = String Refined MatchesRegex["^[0-9]{5}$"]
object PostCode extends RefinedTypeOps[PostCode, String]
type PhoneNumber =
  String Refined MatchesRegex["^((00|\\+)33|0)([0-9]{5}|[0-9]{9})$"]
type Shelf = String Refined Size[Interval.Closed[1, 25]]

// Drawbacks — Base Type

val p1: PositiveInt = 100
val p2: PositiveInt = 200
val positive: Int = p1.value + p2.value
//For now, will complains about + function not being define in PositiveInt
// val stillPositive: Int = plus(p1, p2)

object RefinedOps {
  import language.implicitConversions
  import eu.timepit.refined.api.Refined

  @inline implicit def refinedType2Base[Predicate, Base](
      refinedType: Base Refined Predicate
  ): Base = refinedType.value
}

object TestOps {
  import RefinedOps._

  type PositiveInt = Int Refined Positive

  val i1: PositiveInt = 5
  val i2: PositiveInt = 5

  //Work as expected, will be translated to i1.value + i2.value at compile time.
  val i3: Int = i1 + i2
}

// DrawBacks — Compilation Time
// Refined use Shapeless inside and a lot of it’s own macros, which directly impact the compilation time. This can become a problem for large projects, but solutions exist. The simpler one is to split a project into smaller, multiple ones. The compilation will only happen on the project you modified.
// Still, tests are a place where we will use a lot of values.

// low compiler time cost but at high runtime cost
val postCode: PostCode = PostCode.unsafeFrom("92345")

// asInstanceOf - very fast
// The unsafeApply function does not check the value you pass it, possibly creating an error in your input
val postCode2: PostCode = Refined.unsafeApply("92345")

// DrawBacks — Singleton Types
// A type is called singleton if reduced to a value.

//With Singleton Types
type Idx = Int Refined Interval.Open[100, 500]
// The Idx type is built using two singletons : 100 and 500. They are types


// With Shapeless Notation (Shapeless Witness)
// type Id = Int Refined Interval.Open[W.`100`.T, W.`500`.T]


// Refined is a library to reduce the values a type can take (type refinement). It can be of help to enforce business rules in the TypeLevel, giving you power over functions by declaring clearer parameters. 
// This way, you make sure values are valid in every functions and nothing can have break, simplifying both, validation and testing. Validation can be made in one unique place, and Refined offer built-in functions. Testing only need to care about business cases, not technical ones, as your functions inputs will necessary be valid.
// This come at some cost, which can be mostly avoided. It may add boilerplate for now, but a workaround exist and the library will soon be patched. Compilation time can become a burden, especially tests compilation. This can be avoided by calling the right function and avoid auto-wrapping at compile time. Lastly, it’s much clearer with Singleton Types but they were not part of Scala before 2.13. The alternatives are the TypeLevel compiler or the Shapeless Notation.