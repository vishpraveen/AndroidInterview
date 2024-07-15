# *Java*

- **OOPs** concepts
- **Abstract** *vs* **Interface**
- **Local** *vs* **Global** variables
- *Access Modifiers*
- Difference between *"=="* *vs* *"equals()"*
- Difference between **Checked** *vs* **Unchecked** Exception
- Difference between **throw** *vs* **throws**?
- What are *multiple* checked blocks?

# *Basic Java Interview Questions*
- What is the difference between **JDK**, **JRE**, and **JVM**?
- What are the main features of Java?
- Explain the concept of **OOP** in Java.
- What is a **Class** and an **Object** in Java?
- What is the difference between **==** and **equals()** in Java?
- How does the **final** keyword work in Java?
- What is the purpose of the **static** keyword?
- Explain the difference between a **constructor** and a **method**.
- What are the main differences between **ArrayList** and **LinkedList**?
- How does **garbage** collection work in Java?
- Explain the concept of **Serialization** and **De-Serialization** in Java
  
# *Intermediate Java Interview Questions*
- What are the main differences between an **interface** and an **abstract class**?
- Explain **exception handling** in Java with examples.
- What is the purpose of the **transient** keyword in Java?
- How do you **handle thread synchronization** in Java?
- What are the different types of **inheritance** in Java?
- Explain the concept of **polymorphism** with examples.
- How does the **HashMap** work in Java?
- What is the difference between **throw** and **throws** in Java?
- Explain the concept of method **overloading** and **overriding**.
- What are **generics** in Java and how do they work?
  
# *Advanced Java Interview Questions*
- What are design patterns? Explain any two design patterns with examples.
- How does the Java memory model work?
- What is the difference between **synchronized** and **volatile**?
- How does the **ConcurrentHashMap** work?
- Explain the concept of immutability and how to create an immutable class in Java.
- What are the differences between a **deep copy** and a **shallow copy**?
- How do you implement a thread-safe singleton in Java?
- Explain the concept of **reflection** in Java and its use cases.
- What are the differences between **Callable** and **Runnable**?
- How does the **Java Stream API** work?
  
# *Concurrency and Multithreading Questions*
- What is the difference between **Thread** and **Runnable** in Java?
- Explain the *lifecycle of a thread* in Java.
- What are the differences between **wait()**, **notify()**, and **notifyAll()**?
- How do you create a thread pool in Java?
- What are **deadlock**, **livelock**, and **starvation**?
- Explain the **ReentrantLock** class and its usage.
- What is a **CountDownLatch** and how is it used?
- What are the differences between **Executor** and **ExecutorService**?
- How does **Future** and **CompletableFuture** work?
- Explain the concept of the *Fork/Join framework*.
  
# *Java 8 and Beyond Questions*
- What are the *main features introduced in Java 8*?
- How do **lambda** expressions work in Java?
- What are **functional interfaces**? Provide examples.
- Explain the *Stream API and its benefits*.
- How do you use the *Optional class* in Java 8?
- What is the difference between **map()** and **flatMap()** in Java Streams?
- Explain **method references** and their use cases.
- What are **default methods in interfaces**?
- How does the **java.time** package work for date and time handling?
- What is a **CompletableFuture** and how is it used?
  
# *Java Collections Framework Questions*
- What are the main differences between **ArrayList** and **Vector**?
- Explain the difference between **HashSet** and **TreeSet**.
- How does a *HashMap handle collisions*?
- What are **Comparable** and **Comparator** interfaces in Java?
- What is the difference between **HashMap** and **Hashtable**?
- How do you sort a List in Java?
- Explain the concept of **fail-fast** and **fail-safe** iterators.
- How does the **LinkedHashMap** work?
- What is a **PriorityQueue** and how does it work?
- Explain the **difference between *ArrayDeque* and LinkedList as a Deque**.
  
# *Java Programming Challenges*
- Write a Java program to reverse a string without using built-in methods.
- Implement a thread-safe singleton class.
- Write a program to find the first non-repeated character in a string.
- Create a Java class to implement a simple stack data structure.
- Write a program to check if a number is a palindrome.
- Implement a binary search algorithm in Java.
- Write a Java program to find the largest and smallest number in an array.
- Create a custom exception and handle it in a Java program.
- Write a program to detect a cycle in a linked list.
- Implement a producer-consumer scenario using Java threads.
  
# *Behavioral and Conceptual Questions*
- Describe a challenging Java project you worked on and how you addressed the challenges.
- How do you keep up with the latest developments in Java?
- Describe a situation where you had to optimize Java code.
- **Explain the concept of SOLID principles and how you apply them in Java.**
- Discuss a time when you found and fixed a critical bug in a Java application.

# **Solutions**

# Difference between Checked vs Unchecked Exception [GFG]([https://www.geeksforgeeks.org/checked-vs-unchecked-exceptions-in-java/])

# Difference between *"=="* *vs* *"equals()"*
In Java, both `==` and `equals()` are used for comparisons, but they serve different purposes:

`==` Operator:
- Compares references: Checks if two variables `point to the same memory location`.
- Primitive types: Compares the actual `values of primitive data types` (int, char, boolean, etc.).
- Object types: `Compares memory addresses`, not the content of objects.

`equals()` Method:
- Compares content: Checks if `two objects have the same value based on their internal state`.
- Inherited from Object class: All classes inherit equals() from the Object class.
- Overriding required: To `compare objects based on content`, you must override the equals() method in your class.

```java
    String str1 = "hello";
    String str2 = "hello";
    String str3 = new String("hello");

    System.out.println(str1 == str2); // true (same memory location for string literals)
    System.out.println(str1 == str3); // false (different memory locations)
    System.out.println(str1.equals(str3)); // true (content is the same)
```

In summary:
- Use `==` for comparing primitive types and checking if two object references point to the same object.
- Use `equals()` for comparing the content of objects (after overriding the method appropriately).

# How does the **final** keyword work in Java?
In Java, the `final` keyword is a non-access modifier that can be applied to **variables**, **methods**, and **classes**. It serves different purposes depending on where it's used:
1. Final Variables:
- When applied to a variable, final makes it a `constant`. This means that once the variable is assigned a value, it cannot be changed.
- This is useful for defining values that should remain constant throughout the program's execution, like mathematical constants (e.g., final double PI = 3.14159;).

```java
    final int x = 10;
    x = 20; // This will result in a compilation error
```
2. Final Methods:
- When applied to a method, final prevents the method from being `overridden` in any subclasses.
- This is useful when you want to ensure that a particular method's implementation remains the same across all derived classes, preventing accidental or intentional modifications.

```java
    class A {
        final void display() {
            System.out.println("This is a final method");
        }
    }

    class B extends A {
        void display() { // This will result in a compilation error
            // Method cannot be overridden
        }
    }
```

3. Final Classes:
- When applied to a class, final `prevents the class from being subclassed or inherited`.
- This is useful when you want to create a non-extendable class, ensuring that its behavior cannot be altered through inheritance.

```java
    final class A {
        // Class definition
    }

    class B extends A { // This will result in a compilation error
        // Cannot inherit from a final class
    }
```

Benefits of using **final**:
- **Immutability**: Ensures that values remain constant, preventing unintended modifications.
- **Security**: Protects against malicious or unintended overriding of methods or classes.
- **Performance**: Allows the compiler to perform optimizations, knowing that the values or behaviors won't change.
- **Design Intent**: Clearly communicates to other developers that a variable, method, or class is not meant to be changed.

# Difference between `throw` vs `throws`?

In Java, `throw` and `throws` are both keywords used in exception handling, but they have distinct purposes:

`Throw` keyword:
- **Purpose**: Used to explicitly throw an exception from within a method or block of code.
- **Syntax**: throw new ExceptionType("Error message");
- **Placement**: Inside the method body.
- **Number of exceptions thrown**: Only one exception can be thrown at a time.

```java
    public void divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero"); 
        }
        // ... rest of the code
    }
```

`Throws` keyword:
- **Purpose**: Used to declare the exceptions that a method can potentially throw.
- **Syntax**: returnType methodName(parameters) throws ExceptionType1, ExceptionType2, ...
- **Placement**: In the method signature, after the parameter list.
- **Number of exceptions declared**: Multiple exceptions can be declared, separated by commas.

```java
    public void readFile(String filename) throws FileNotFoundException, IOException {
        // ... code that might throw FileNotFoundException or IOException
    }
```

|       Feature        |              `throw`               |                    `throws`                     |
| :------------------: | :--------------------------------: | :---------------------------------------------: |
|       Purpose        |   Explicitly throws an exception   | Declares that a method might throw an exception |
|      Placement       |         Inside method body         |               In method signature               |
| Number of exceptions |           One at a time            |          Multiple, separated by commas          |
|     Followed by      | An instance of the exception class |           The exception class name(s)           |

In summary, use `throw` to explicitly signal an exception within your code, and use `throws` to declare the exceptions that your method might throw, allowing the calling method to handle them appropriately.

# What are multiple checked blocks?

In Java, the term `multiple checked blocks` isn't commonly used. However, it could refer to two different concepts:
1. `Multiple Catch Blocks`:
This is the most likely interpretation. In Java, a `try` block can be followed by multiple `catch` blocks to handle different types of exceptions individually.

```java
    try {
        // Code that might throw an exception
    } catch (IOException e) {
        // Handle IOException
    } catch (SQLException e) {
        // Handle SQLException
    } catch (Exception e) {
        // Handle any other exception
    }
```

2. Multi-Catch Block (Since Java 7):
Instead of having multiple `catch` blocks, Java 7 introduced the ability to catch multiple exception types in a single `catch` block using the pipe (`|`) symbol.

```java
    try {
        // Code that might throw an exception
    } catch (IOException | SQLException e) {
        // Handle both IOException and SQLException
    }
```
