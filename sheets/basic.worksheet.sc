var capital = Map("US" -> "Washington", "France" -> "Pairs")
capital += ("Janpan" -> "Tokyo")
println(capital("France"))
println(capital.get("Beijing"))

def factorial(x: BigInt): BigInt = if (x == 0) 1 else x * factorial(x - 1)

Array(1) == Array(2)
