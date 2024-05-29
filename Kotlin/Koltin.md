# *Kotlin*

- **Structuring** *&* **Destructuring**
- Difference between **"in"**, **"..."**, **"until"** in *Collections*
- **data class** vs **Class** in Koltin
- Properties of data class
  - equals(), hashCode(), toString(), copy()
- **var** *vs* **val** *vs* **const**
- **lateinit** *vs* **lazy block**
- **Access Modifiers**
- **Object** *vs* **Companion Object**
- **Fragment Pager Lifecycle**
- Types of **Constructors** in koltin
- **Primary** *vs* **Secondary** *vs* **init block**
- **init block**
- **Singleton** in *Kotlin*
- Difference between **"=="** *vs* **"==="** *vs* **"equals()"**

- Inline functions
- Infix function
- Higher order function
  - Write a HOF to add two Float numbers 2.3 & 4.5
```
fun addTwoFloatNumber(a: Float, b: Float, calculation: (Float, Float) -> Float): Float {
    return calculation(a, b)
}
// Simple addition function
fun add(x: Float, y: Float): Float {
    return x + y
}

fun main() {
    val x= 2.3F
    val y = 4.5F
    val resultFloat = addTwoFloatNumber(x, y, ::add) //function reference
    val resultFloat = addTwoFloatNumber(x, y, { x, y -> x + y })// Using Lambda
    println(resultFloat)
}
```

