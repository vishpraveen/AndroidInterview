# **Collections Overview**

The Kotlin Standard Library provides a comprehensive set of tools for managing collections – groups of a variable number of items (possibly zero) that are significant to the problem being solved and are commonly operated on.

Collections are a common concept for most programming languages, so if you're familiar with, for example, Java or Python collections, you can skip this introduction and proceed to the detailed sections.

A collection usually contains a number of objects of the same type (and its subtypes). Objects in a collection are called **elements** or **items**. For example, all the students in a department form a collection that can be used to calculate their average age.

The following collection types are relevant for Kotlin:

- **List** is an <ins>ordered collection</ins> with access to elements by <ins>indices</ins> – integer numbers that reflect their position. Elements can occur more than once in a list. An example of a list is a telephone number: it's a group of digits, their order is important, and they can repeat.

- **Set** is a collection of <ins>unique elements</ins>. It reflects the mathematical abstraction of set: a group of objects without repetitions. Generally, the order of set elements has no significance. For example, the numbers on lottery tickets form a set: they are unique, and their order is not important.

- **Map** (or **dictionary**) is a set of key-value pairs. <ins>Keys are unique</ins>, and each of them maps to exactly one value.  <ins>The values can be duplicates </ins>. Maps are useful for storing logical connections between objects, for example, an employee's ID and their position.

Kotlin lets you manipulate collections independently of the exact type of objects stored in them. In other words, you add a <code>String</code> to a list of <code>String</code> s the same way as you would do with <code>Int</code> s or a user-defined class. So, the Kotlin Standard Library offers generic interfaces, classes, and functions for creating, populating, and managing collections of any type.

The collection interfaces and related functions are located in the <code>kotlin.collections</code> package. Let's get an overview of its contents.

Note: Arrays are not a type of collection. For more information, see [Arrays](https://kotlinlang.org/docs/arrays.html#create-arrays).

# **Collection types**
The Kotlin Standard Library provides implementations for basic collection types: sets, lists, and maps. A pair of interfaces represent each collection type:

- A **read-only** interface that provides operations for accessing collection elements.

- A **mutable** interface that extends the corresponding read-only interface with write operations: adding, removing, and updating its elements.

Note that a mutable collection doesn't have to be assigned to a <code>var</code>. Write operations with a mutable collection are still possible even if it is assigned to a <code>val</code>. The benefit of assigning mutable collections to <code>val</code> is that you protect the reference to the mutable collection from modification. Over time, as your code grows and becomes more complex, it becomes even more important to prevent unintentional modification to references. Use <code>val</code> as much as possible for safer and more robust code. If you try to reassign a <code>val</code> collection, you get a compilation error:
```
val numbers = mutableListOf("one", "two", "three", "four")
numbers.add("five")   // this is OK
println(numbers)
//numbers = mutableListOf("six", "seven") 

```
The read-only collection types are <ins>covariant</ins>. This means that, if a <code>Rectangle</code> class inherits from <code>Shape</code>, you can use a <code>List<Rectangle></code> anywhere the <code>List<Shape></code> is required. In other words, the collection types have the same subtyping relationship as the element types. Maps are covariant on the value type, but not on the key type.

In turn, mutable collections aren't covariant; otherwise, this would lead to runtime failures. If <code>MutableList<Rectangle></code> was a subtype of <code>MutableList<Shape></code>, you could insert other  <code>Shape</code> inheritors (for example, <code>Circle</code>) into it, thus violating its Rectangle type argument.


Below is a diagram of the Kotlin collection interfaces:

<img src="https://github.com/vishpraveen/AndroidInterview/assets/14356494/9a332e43-2bb0-4ebc-a0dc-7866adc86702" width="640" height="400">

Let's walk through the interfaces and their implementations. To learn about Collection, read the section below. To learn about <code>List</code>, <code>Set</code>, and <code>Map</code>, you can either read the corresponding sections or watch a video by Sebastian Aigner, Kotlin Developer Advocate: [Link](https://youtu.be/F8jj7e-_jFA)

# **Collection﻿**
<code>Collection<T></code> is the root of the collection hierarchy. This interface represents the common behavior of a read-only collection: retrieving size, checking item membership, and so on. Collection inherits from the <code>Iterable<T></code> interface that defines the operations for iterating elements. You can use <code>Collection</code> as a parameter of a function that applies to different collection types. For more specific cases, use the <code>Collection's</code> inheritors: <code>List</code> and <code>Set</code>.

```
fun printAll(strings: Collection<String>) {
    for(s in strings) print("$s ")
    println()
}
    
fun main() {
    val stringList = listOf("one", "two", "one")
    printAll(stringList)
    
    val stringSet = setOf("one", "two", "three")
    printAll(stringSet)
}
```

<code>MutableCollection<T></code> is a <code>Collection</code> with write operations, such as <code>add</code> and <code>remove</code>.
```
fun List<String>.getShortWordsTo(shortWords: MutableList<String>, maxLength: Int) {
    this.filterTo(shortWords) { it.length <= maxLength }
    // throwing away the articles
    val articles = setOf("a", "A", "an", "An", "the", "The")
    shortWords -= articles
}

fun main() {
    val words = "A long time ago in a galaxy far far away".split(" ")
    val shortWords = mutableListOf<String>()
    words.getShortWordsTo(shortWords, 3)
    println(shortWords)//[ago, in, far, far]
}
```

# **List﻿**
<code>List<T></code> stores elements in a specified order and provides indexed access to them. Indices start from zero – the index of the first element – and go to <code>lastIndex</code> which is the <code>(list.size - 1)</code>.

```
val numbers = listOf("one", "two", "three", "four")
println("Number of elements: ${numbers.size}")
println("Third element: ${numbers.get(2)}")
println("Fourth element: ${numbers[3]}")
println("Index of element \"two\" ${numbers.indexOf("two")}")
```

Output
```
Number of elements: 4
Third element: three
Fourth element: four
Index of element "two" 1
```

List elements (including nulls) can duplicate: a list can contain any number of equal objects or occurrences of a single object. Two lists are considered equal if they have the same sizes and <ins>structurally equal</ins> elements at the same positions.

```
val bob = Person("Bob", 31)
val people = listOf(Person("Adam", 20), bob, bob)
val people2 = listOf(Person("Adam", 20), Person("Bob", 31), bob)
println(people == people2) //true
bob.age = 32
println(people == people2) //false
```
<code>MutableList<T></code> is a <code>List</code> with list-specific write operations, for example, to add or remove an element at a specific position.

```
val numbers = mutableListOf(1, 2, 3, 4)
numbers.add(5)
numbers.removeAt(1)
numbers[0] = 0
numbers.shuffle()
println(numbers)//[3, 0, 4, 5]
```
As you see, in some aspects lists are very similar to arrays. However, there is one important difference: an array's size is defined upon initialization and is never changed; in turn, a list doesn't have a predefined size; a list's size can be changed as a result of write operations: adding, updating, or removing elements.

In Kotlin, the default implementation of MutableList is ArrayList which you can think of as a resizable array.
As you see, in some aspects lists are very similar to arrays. However, there is one important difference: an array's size is defined upon initialization and is never changed; in turn, a list doesn't have a predefined size; a list's size can be changed as a result of write operations: adding, updating, or removing elements.

In Kotlin, the default implementation of <code>MutableList</code> is <code>ArrayList</code> which you can think of as a resizable array.

# **Set﻿**

<code>Set<T></code> stores unique elements; their order is generally undefined. <code>null</code> elements are unique as well: a <code>Set</code> can contain only one <code>null</code>. Two sets are equal if they have the same size, and for each element of a set there is an equal element in the other set.


```
val numbers = setOf(1, 2, 3, 4)
println("Number of elements: ${numbers.size}")
if (numbers.contains(1)) println("1 is in the set")

val numbersBackwards = setOf(4, 3, 2, 1)
println("The sets are equal: ${numbers == numbersBackwards}")
```
Output
```
Number of elements: 4
1 is in the set
The sets are equal: true
```
<code>MutableSet</code> is a <code>Set</code> with write operations from <code>MutableCollection</code>.

The default implementation of <code>MutableSet</code> – <code>LinkedHashSet</code> – preserves the order of elements insertion. Hence, the functions that rely on the order, such as <code>first()</code> or <code>last()</code>, return predictable results on such sets.

```
val numbers = setOf(1, 2, 3, 4)  // LinkedHashSet is the default implementation
val numbersBackwards = setOf(4, 3, 2, 1)

println(numbers.first() == numbersBackwards.first())//false
println(numbers.first() == numbersBackwards.last())//true
```
An alternative implementation – <code>HashSet</code> – says nothing about the elements order, so calling such functions on it returns unpredictable results. However, <code>HashSet</code> requires less memory to store the same number of elements.

# **Map﻿**
<code>Map<K, V></code> is not an inheritor of the <code>Collection</code> interface; however, it's a Kotlin collection type as well. A <code>Map</code> stores **key-value** pairs (or **entries**); keys are unique, but different keys can be paired with equal values. The <code>Map</code> interface provides specific functions, such as access to value by key, searching keys and values, and so on.

```
val numbersMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3, "key4" to 1)

println("All keys: ${numbersMap.keys}")
println("All values: ${numbersMap.values}")
if ("key2" in numbersMap) println("Value by key \"key2\": ${numbersMap["key2"]}")    
if (1 in numbersMap.values) println("The value 1 is in the map")
if (numbersMap.containsValue(1)) println("The value 1 is in the map") // same as previous
```
Output
```
All keys: [key1, key2, key3, key4]
All values: [1, 2, 3, 1]
Value by key "key2": 2
The value 1 is in the map
The value 1 is in the map
```
Two maps containing the equal pairs are equal regardless of the pair order.
```
val numbersMap = mapOf("key1" to 1, "key2" to 2, "key3" to 3, "key4" to 1)    
val anotherMap = mapOf("key2" to 2, "key1" to 1, "key4" to 1, "key3" to 3)

println("The maps are equal: ${numbersMap == anotherMap}")
```
Output
```
The maps are equal: true
```

<code>MutableMap</code> is a <code>Map</code> with map write operations, for example, you can add a new key-value pair or update the value associated with the given key.

```
val numbersMap = mutableMapOf("one" to 1, "two" to 2)
numbersMap.put("three", 3)
numbersMap["one"] = 11

println(numbersMap)
```
Output
```
{one=11, two=2, three=3}
```

The default implementation of <code>MutableMap</code> – <code>LinkedHashMap</code> – preserves the order of elements insertion when iterating the map. In turn, an alternative implementation – <code>HashMap</code> – says nothing about the elements order.

# **ArrayDeque﻿**
<code>ArrayDeque<T></code> is an implementation of a double-ended queue, which allows you to add or remove elements both at the beginning or end of the queue. As such, <code>ArrayDeque</code> also fills the role of both a Stack and Queue data structure in Kotlin. Behind the scenes, <code>ArrayDeque</code> is realized using a resizable array that automatically adjusts in size when required:

```
fun main() {
    val deque = ArrayDeque(listOf(1, 2, 3))

    deque.addFirst(0)
    deque.addLast(4)
    println(deque) // [0, 1, 2, 3, 4]

    println(deque.first()) // 0
    println(deque.last()) // 4

    deque.removeFirst()
    deque.removeLast()
    println(deque) // [1, 2, 3]
}
```


