# *Java*

- **OOPs** concepts
- **Abstract** *vs* **Interface**
- **Local** *vs* **Global** variables
- *Access Modifiers*
- Difference between *"=="* *vs* *"equals()"* [link](#difference-between--vs-equals)
- Difference between **Checked** *vs* **Unchecked** Exception [link](#difference-between-checked-vs-unchecked-exception-gfg)
- Difference between **throw** *vs* **throws**? [link](#difference-between-throw-vs-throws)
- What are *multiple* checked blocks? [link](#what-are-multiple-checked-blocks)

# *Basic Java Interview Questions*
- What is the difference between **JDK**, **JRE**, and **JVM**?
- What are the main features of Java?
- Explain the concept of **OOP** in Java.
- What is a **Class** and an **Object** in Java?
- What is the difference between **==** and **equals()** in Java? [link](#difference-between--vs-equals)
- How does the **final** keyword work in Java? [link](#how-does-the-final-keyword-work-in-java)
- What is the purpose of the **static** keyword?
- Explain the difference between a **constructor** and a **method**.
- What are the main differences between **ArrayList** and **LinkedList**?
- How does **garbage** collection work in Java?
- Explain the concept of **Serialization** and **De-Serialization** in Java [link](#concept-of-serialization-and-de-serialization-in-java)
  
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
- What is the difference between **synchronized** and **volatile**? [link](#difference-between-_synchronized_-and-_volatile_)
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
public static void main(String[] args) {
    String str1 = "hello";
    String str2 = "hello";
    String str3 = new String("hello");

    System.out.println(str1 == str2); // true (same memory location for string literals)
    System.out.println(str1 == str3); // false (different memory locations)
    System.out.println(str1.equals(str3)); // true (content is the same)
}
```

In summary:
- Use `==` for comparing primitive types and checking if two object references point to the same object.
- Use `equals()` for comparing the content of objects (after overriding the method appropriately).

# How does the **final** keyword work in Java?
In Java, the `final` keyword is a non-access modifier that can be applied to **variables**, **methods**, and **classes**. It serves different purposes depending
on where it's used:
* ##### Final Variables:
  - When applied to a variable, final makes it a `constant`. This means that once the variable is assigned a value, it cannot be changed.
  - This is useful for defining values that should remain constant throughout the program's execution, like mathematical constants (e.g., final double PI = 3.14159;).
    ```java
    public static void main(String[] args){
        final int x = 10;
        x = 20; // This will result in a compilation error 
    }
    ```

* ##### Final Methods:

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

* ##### Final Classes:
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

#### Benefits of using **final**:
- **Immutability**: Ensures that values remain constant, preventing unintended modifications.
- **Security**: Protects against malicious or unintended overriding of methods or classes.
- **Performance**: Allows the compiler to perform optimizations, knowing that the values or behaviors won't change.
- **Design Intent**: Clearly communicates to other developers that a variable, method, or class is not meant to be changed.

## Difference between `throw` vs `throws`?
>In Java, `throw` and `throws` are both keywords used in exception handling, but they have distinct purposes:

* `Throw` keyword:
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

* `Throws` keyword:
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
|:--------------------:|:----------------------------------:|:-----------------------------------------------:|
|       Purpose        |   Explicitly throws an exception   | Declares that a method might throw an exception |
|      Placement       |         Inside method body         |               In method signature               |
| Number of exceptions |           One at a time            |          Multiple, separated by commas          |
|     Followed by      | An instance of the exception class |           The exception class name(s)           |

In summary, use `throw` to explicitly signal an exception within your code, and use `throws` to declare the exceptions that your method might throw, allowing the calling method to handle them appropriately.

# What are multiple checked blocks?
> In Java, the term `multiple checked blocks` isn't commonly used. However, it could refer to two different concepts:
* #### `Multiple Catch Blocks`:
This is the most likely interpretation. In Java, a `try` block can be followed by multiple `catch` blocks to handle different types of exceptions individually.
```java
public static void main(String[] args) {
    try {
        // Code that might throw an exception
    } catch (IOException e) {
        // Handle IOException
    } catch (SQLException e) {
        // Handle SQLException
    } catch (Exception e) {
        // Handle any other exception
    }   
}
```

* #### Multi-Catch Block (Since Java 7):
Instead of having multiple `catch` blocks, Java 7 introduced the ability to catch multiple exception types in a single `catch` block using the pipe (`|`) symbol.
```java
public static void main(String[] args) {
    try {
        // Code that might throw an exception
    } catch (IOException | SQLException e) {
        // Handle both IOException and SQLException
    }
}
```

### Difference between _synchronized_ and _volatile_?
> In Java, `synchronized` and `volatile` are two key mechanisms for handling concurrency and ensuring thread safety in multithreaded environments. They serve different purposes and are used in different scenarios.

### `synchronized` in Java
> synchronized is a keyword that provides a way to ensure that only one thread can access a method or block of code at a time. It is used to avoid thread interference and memory consistency errors.

#### Key Points:
* **Mutex (Mutual Exclusion):** Only one thread can execute a synchronized method or block at a time.
* **Intrinsic Lock (Monitor Lock):** Each object in Java has an intrinsic lock or monitor. When a thread enters a synchronized block or method, it acquires this lock, and releases it when exiting the block or method.
* **Preventing Race Conditions:** By controlling access to shared resources, synchronized helps to prevent race conditions where the outcome depends on the sequence or timing of uncontrollable events.

```java
// synchronized Method  
public class Counter {
  private int count = 0;

  // Synchronized method
  public synchronized void increment() {
    count++;
  }

  public int getCount() {
    return count;
  }
}
```
```java
// synchronized Block
public class Counter {
    private int count = 0;
    private final Object lock = new Object();

    // Synchronized block
    public void increment() {
        synchronized (lock) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
```
#### When to Use `synchronized`:
* When you need to control access to critical sections of code that modify shared resources.
* When you need to ensure that a sequence of operations on shared resources is performed atomically.

### `volatile` in Java
> `volatile` is a keyword used to indicate that a variable's value will be modified by different threads. It ensures visibility of changes to variables across threads.

#### Key Points:
* **Visibility Guarantee**: Changes to a volatile variable are always visible to other threads. When one thread changes the value of a volatile variable, the new value is immediately visible to other threads.
* **No Caching**: Variables declared as volatile are not cached locally in a thread’s cache, and are always read from and written to the main memory.
* **No Atomicity**: Unlike synchronized, volatile does not provide atomicity. It only ensures visibility.
```java
public class SharedData {
    private volatile boolean running = true;

    public void stopRunning() {
        running = false;
    }

    public void runTask() {
        while (running) {
            // Task running
        }
        System.out.println("Task stopped");
    }
}
```
> In this example, the volatile keyword ensures that the running variable's updated value is visible to all threads.

#### When to Use `volatile`:
* When you need to ensure that the latest value of a variable is always visible to other threads.
* When you do not require atomicity, but need to ensure visibility.
* Suitable for flags and state-check variables where you don't need more complex synchronization.

#### Differences Between `synchronized` and `volatile`

| Feature	  | synchronized                                        | volatile                          |
|-----------|-----------------------------------------------------|-----------------------------------|
| Scope     | Methods and code blocks                             | Variables                         |
| Control   | Ensures mutual exclusion                            | Ensures visibility                |
| Atomicity | Yes, ensures atomicity                              | No, does not ensure atomicity     |
| Locking   | Acquires an intrinsic lock (monitor)                | No locking mechanism              |
| Use Case  | Protects critical sections or complex state changes | Simple flags and state indicators |

#### Summary
* Use `synchronized` when you need to ensure exclusive access to shared resources or protect critical sections of code.
* Use `volatile` when you need to ensure visibility of shared variables across threads, and when the variable does not require complex synchronization.

## Concept of **Serialization** and **De-Serialization** in Java
>`Serialization` and `Deserialization` in Java are mechanisms provided by the Java programming language to convert an object into a byte stream (serialization) and recreate the object from the byte stream (deserialization). These processes are essential for various operations like saving objects to files, transmitting objects over networks, and deep copying objects.

#### Steps for Serialization
1. **Implement the `Serializable` Interface**: The class of the object to be serialized must implement the `Serializable` interface. This is a marker interface, meaning it does not have any methods.
2. **Create an ObjectOutputStream**: Use ObjectOutputStream to write the object to an output stream (like a file or network socket).

#### Steps for Deserialization
1. **Create an `ObjectInputStream`**: Use `ObjectInputStream` to read the object from an input stream (like a file or network socket).

#### Use Cases of Serialization and Deserialization
1. **Persistence**: Saving the state of an object to a file or database.
2. **Communication**: Sending objects over a network (e.g., in distributed systems).
3. **Caching**: Storing objects in memory for faster access.
4. **Deep Copy**: Creating an exact copy of an object.

#### Summary
* **Serialization**: Converting an object to a byte stream.
* **Deserialization**: Converting a byte stream back into an object.
* **Serializable Interface**: Implement this interface to make a class serializable.
* **`serialVersionUID`**: Ensures version compatibility.
* **Transient Fields**: Use for fields that should not be serialized.
* **Custom Serialization**: Implement `writeObject` and `readObject` for custom behavior.
