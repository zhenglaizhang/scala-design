trait Hasher {
  def algo(str: String): String
}

trait Salter {
  def salt(str: String): String
}

trait Hashing {
  self: Hasher with Salter =>  // define dependency with traits (abstraction)

  def hash(str: String) = algo(str)
}

trait Md5Hasher extends Hasher {
  def algo(str: String): String = ???
}
trait SHAHasher extends Hasher {
  def algo(str: String): String = ???
}

trait SimpleSalter extends Salter {
  def salt(str: String): String = ???
}

// implement the dependency
// inversion of control
// you choose which hasher to use when then time of hashing
// cake pattern
// compiler time safety
object Foo extends Hashing with Md5Hasher with SimpleSalter {
  val str = "plain text"
  hash(str)
}