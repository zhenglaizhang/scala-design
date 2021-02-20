// Find out whether a list is a palindrome.

def isPalindrome[A](xs: List[A]): Boolean = {
  if (xs.length < 2) true
  else {
    val h = xs.head
    val last = xs.last
    val others = xs.tail.init
    h == last && isPalindrome(others)
  }
}

// clear and concise, though traverse the list twice
def isPalindrome2[A](xs: List[A]): Boolean = xs.reverse == xs

def isPalindromeFast[A](xs: List[A]): Boolean = {
  var l = 0
  var r = xs.length - 1
  while (l < r && xs(l) == xs(r)) {
    l = l + 1
    r = r - 1
  }
  return l >= r
}

val xs = List(1, 2, 3, 2, 1)
val xs2 = List(1, 2, 3, 1, 1)
isPalindrome(xs)
isPalindrome(xs2)
isPalindrome2(xs)
isPalindrome2(xs2)
isPalindromeFast(xs2)
isPalindromeFast(xs)

var x = 2
// x++
// ++x