// extension methods

implicit class StringExtensions(val s: String) extends AnyVal {
  def isAllUpperCase: Boolean = s.toCharArray.forall(_.isUpper)
}
