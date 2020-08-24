object Email extends ((String, String) => String) {
  def apply(user: String, domain: String) = user + "@" + domain

  def unapply(str: String): Option[(String, String)] = {
    var parts = str split "@"
    if (parts.length == 2)
      Some(parts(0), parts(1))
    else
      None
  }
}

Email("zhenglai", "hotmail")
Email.unapply("zhenglaizhang@hotmail.com") equals Some(
  "zhenglaizhang",
  "hotmail.com"
)
Email.unapply("test") == None

"wow@hotmail.com" match {
  case Email(user, domain) => println(user)
}

object UpperCase {
  def unapply(s: String): Boolean = s.toUpperCase == s
}

"ABC" match {
  case UpperCase() => "upper case"
  case _           => "not all upper case"
}

object ExpandedEmail {
  def unapplySeq(email: String): Option[(String, Seq[String])] = {
    var parts = email split "@"
    if (parts.length == 2) {
      Some(parts(0), parts(1).split("\\.").reverse)
    } else {
      None
    }
  }
}

val s = "wow@meow.hotmail.com"
val ExpandedEmail(name, meow, subdoms @ _*) = s
