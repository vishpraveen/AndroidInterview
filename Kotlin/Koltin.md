# *Kotlin*

- **Structuring** *&* **Destructuring**
- Difference between **"in"**, **"..."**, **"until"** in *Collections*
- **data class** vs **Class** in Kotlin
- Properties of data class
  - equals(), hashCode(), toString(), copy()
- **var** *vs* **val** *vs* **const**
- **lateinit** *vs* **lazy block**
- **Access Modifiers**
- **Object** *vs* **Companion Object**
- **Fragment Pager Lifecycle**
- Types of **Constructors** in Kotlin
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
- **Scope Functions** in Kotlin
    > In Kotlin, scope functions are a set of functions that allow you to execute a block of code within the context of an object. They provide a concise way to work with objects, without having to use explicit references to them. The most commonly used scope functions are
    - `let`
    - `run`
    - `with`
    - `apply`
    - `also`
    > Each of these functions has specific use cases and behaviors. They differ primarily in how they handle the context object (`this` or `it`) and what they return.
    
    | Function | Context Object | Return Value  | Use Case                                            |
    | :------: | :------------: | :------------ | :-------------------------------------------------- |
    |  `also`  |      `it`      | Object itself | `Perform side-effects without altering the object.` |
    | `apply`  |     `this`     | Object itself | `Initialize or configure an object in a chain.`     |
    |  `let`   |      `it`      | Lambda result | `Perform operations and transformations.`           |
    |  `run`   |     `this`     | Lambda result | `Initialize or configure an object.`                |
    |  `with`  |     `this`     | Lambda result | `Operate on an object without repeating its name.`  |
    
    

