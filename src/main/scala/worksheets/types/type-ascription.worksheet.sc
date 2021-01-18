//
// Being explicit about the type of something, is called a Type Ascription (sometimes called a "Type Annotation"
//

// Type Inference

trait Thing 
def getThing = new Thing {}

// without type ascription, the type is infered to be Thing
val infered = getThing

// with type ascription
val thing: Thing = getThing

// You may decide to always ascribe return types of public methods (that’s very good idea!) 
// in order to make the code more self-documenting and control over exported types


// In case of doubt you can refer to the below hint-questions to wether or not, include a Type Ascription.
//  - Is it a parameter? Yes, you have to.
//  - Is it a public method’s return value? Yes, for self-documenting code and control over exported types.
//  - Is it a recursive or overloaded methods return value? Yes, you have to.
//  - Do you need to return a more general interface than the inferencer would find? Yes, otherwise you’d expose your implementation details for example.
//  - Else… No, don’t include a Type Ascription.
//  - Related hint: Including Type Ascriptions speeds up compilation, also it’s generally nice to see the return type of a method
