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
