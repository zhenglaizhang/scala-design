val empty: List[Nothing] = List()
val x: List[String] = empty

val nums = 1 :: 2 :: 3 :: Nil
1 :: (2 :: (3 :: Nil))
nums.head
nums.headOption
nums.tail
nums.isEmpty

val List(n1, n2, n3) = nums
val a :: rest = nums
println(rest)
