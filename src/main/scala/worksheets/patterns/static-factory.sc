trait Animal

class Bird extends Animal

class Fish extends Animal

class Mammal extends Animal

object Animal {
  def apply(animal: String): Animal =
    animal.toLowerCase match {
      case "bird"    => new Bird
      case "mammal"  => new Mammal
      case "fish"    => new Fish
      case x: String => throw new RuntimeException(s"Unknown animal: $x")
    }
}
