# Answers — §4 Kotlin — Fundamentals to Advanced

Questions: [questions.md](../questions.md#4-kotlin--fundamentals-to-advanced).

---

### Basics and Type System

**1.** `val` / `var` / `const val`.

- [x]

- **Thesis:** `val` single-assignment reference; `var` mutable; **`const val`** compile-time constant inlined at **use sites**, only **primitives/String** on top level or in `object`, **no** custom getters.
- **Mechanism / order:** JVM `public static final` for `const` in class file.
- **Edge cases:** `const` in **interface** (1.9+) rules.
- **Production hook:** Use for **BuildConfig**-like flags consumed from Java.
- **When wrong:** `const val` with **computed** expression depending on runtime.

**2.** `==` vs `===`; `data class` equality.

- [x]

- **Thesis:** `==` maps to **`equals`** (nullable-safe); `===` is **reference** identity. `data class` auto-**component-wise** `equals`/`hashCode`.
- **Mechanism / order:** `equals` can be overridden; `===` never.
- **Edge cases:** **Float/NaN** equality rules.
- **Production hook:** `data class` copy for immutable updates in UDF.
- **When wrong:** Relying on `==` for **arrays**—uses reference unless `contentEquals`.

**3.** Null safety: `?.`, `!!`, `?:`, `let`, `run`; when `!!` OK.

- [x]

- **Thesis:** Type system tracks **T vs T?**; safe calls short-circuit; Elvis supplies default; **`let`** narrows receiver; **`!!`** asserts non-null—acceptable only when **invariant** you can prove (right after `checkNotNull` / test).
- **Mechanism / order:** Smart casts after `!= null` on **val** not mutated.
- **Edge cases:** **Platform types** from Java break guarantees—annotate `@Nullable`.
- **Production hook:** `requireNotNull` with message in domain layer.
- **When wrong:** `!!` in production UI code on **network models**.

**4.** `String` vs `String?` at JVM bytecode.

- [x]

- **Thesis:** Both compile to **`Ljava/lang/String;`**; difference is **Kotlin metadata** + **null checks** inserted at call boundaries (`@Nullable` annotations for interop).
- **Mechanism / order:** Compiler may elide some checks after proof.
- **Edge cases:** `@JvmSuppressWildcards` etc. affect Java view.
- **Production hook:** Enable **nullability** warnings for Java sources.
- **When wrong:** Expecting runtime `Optional` wrapper for Kotlin `String?`.

**5.** `Any`, `Unit`, `Nothing`.

- [x]

- **Thesis:** `Any` root (like Object); `Unit` is **`void` with singleton value** for generics; **`Nothing`** is **subtype of everything**—no instances, used for **throws** / **infinite loops** to satisfy types.
- **Mechanism / order:** Compiler uses `Nothing` for **exhaustiveness** (`TODO()`, `throw`).
- **Edge cases:** `List<Nothing>` empty list type trick.
- **Production hook:** Return `Nothing` in sealed branches that must not return.
- **When wrong:** Declaring API returning `Unit?`—usually mistake.

**6.** `typealias` vs new type.

- [x]

- **Thesis:** `typealias` is **compile-time synonym**—no extra runtime wrapper, **no** new type safety vs assigning wrong underlying type.
- **New type** (value class / wrapper) adds **distinct** type at call site.
- **Mechanism / order:** Useful for long generics (`typealias UserId = String` still just String).
- **Edge cases:** `@JvmName` conflicts.
- **Production hook:** Prefer **`value class UserId`** for domain IDs.
- **When wrong:** Expecting `typealias` to enforce units.

---

### Classes and OOP

**7.** `data class` codegen; inheritance pitfalls.

- [x]

- **Thesis:** Generates **`equals`/`hashCode`/`toString`/`copy`/`componentN`**; **primary ctor** properties only in equals by default.
- **Mechanism / order:** Inheritance with `open` property in hierarchy breaks **`equals` symmetry** unless careful.
- **Edge cases:** **`val` in child** shadowing—avoid data + inheritance; prefer **composition**.
- **Production hook:** Mark sealed hierarchies **final data** leaves.
- **When wrong:** Mutable `var` in `data class` used as **key** in `HashMap`.

**8.** `sealed class` vs `sealed interface`.

- [x]

- **Thesis:** **`sealed class`**: closed hierarchy with **optional shared state** in base; **`sealed interface`**: Kotlin 1.5+ **multiple sealed supertypes**, enables **cross-module** sealing with `-Xsealed-subclasses` flags in newer compilers.
- **Mechanism / order:** Exhaustive **`when`** without `else`.
- **Edge cases:** Sealed across modules Gradle metadata.
- **Production hook:** Domain **Result** / UI **Event** modeling.
- **When wrong:** Sealed base as **data** with many variants duplicating fields.

**9.** `value class` (inline); boxing.

- [x]

- **Thesis:** **Zero-cost** wrapper at compile time where **unboxed** representation used; **boxed** when erased to supertype `Any?` or generics without reified specialization.
- **Mechanism / order:** `@JvmInline value class UserId(val value: String)`.
- **Edge cases:** **Project Valhalla** future alignment; **R8** may optimize further.
- **Production hook:** Domain safety for **ids/currency** without heap churn.
- **When wrong:** Storing as **`Any`** expecting no boxing.

**10.** `object` declaration / `companion object` / `object expression` bytecode.

- [x]

- **Thesis:** **Object declaration** → singleton class with `INSTANCE`; **companion** → nested class `Companion` with **`@JvmStatic`** helpers; **expression** → anonymous class instance.
- **Mechanism / order:** `object : ClickListener { }` creates **new** instance each evaluation unless hoisted.
- **Edge cases:** `companion` **lazy** init thread-safety (class init lock).
- **Production hook:** `companion object { private const val TAG }`.
- **When wrong:** Heavy init in `companion` init block—class load cost.

**11.** Delegation: class `by`, property `by lazy`, `observable`, custom.

- [x]

- **Thesis:** **Class delegation** forwards interface calls to inner object; **property delegates** implement `getValue`/`setValue`.
- **Mechanism / order:** `by lazy` double-checked locking **LazyThreadSafetyMode**.
- **Edge cases:** **`lazy` of `this`** before init complete—leaks/cycles.
- **Production hook:** `delegates.vetoable` for validated state.
- **When wrong:** `lazy` default **SYNCHRONIZED** overhead on hot path—choose `PUBLICATION` if safe.

**12.** `init` order: primary ctor, properties, `init` blocks.

- [x]

- **Thesis:** Order: **primary ctor params** available; **property initializers** top-to-bottom; **`init` blocks** top-to-bottom; **secondary ctor** delegates to primary then body.
- **Mechanism / order:** Base class init completes before derived property init.
- **Edge cases:** Accessing **subclass** property from base `init`—not initialized yet.
- **Production hook:** Keep `init` minimal; factory functions instead.
- **When wrong:** Using `lateinit` before assignment in tangled `init`.

---

### Functions and Lambdas

**13.** `inline`, `noinline`, `crossinline`.

- [x]

- **Thesis:** **`inline`** copies bytecode of function + lambdas at call site—removes lambda allocation for HOFs; **`noinline`** opts a lambda out; **`crossinline`** forbids **non-local returns** from lambda for inlined calls into other execution contexts.
- **Mechanism / order:** Enables **`reified`** generics.
- **Edge cases:** **Binary size** blowup if inlining huge functions across app.
- **Production hook:** Inline small `let`-like utilities, `measureTimeMillis` style.
- **When wrong:** Inlining functions capturing **large** objects graph.

**14.** `reified` type params; why need `inline`.

- [x]

- **Thesis:** At runtime generics **erased**; `reified T` inlines **actual** `Class` reference at call site so you can `T::class`.
- **Mechanism / order:** Only on `inline` functions.
- **Edge cases:** Cannot access `reified` from non-inline nested private helpers without passing `KClass`.
- **Production hook:** Retrofit **`inline <reified T>`** service accessor wrappers.
- **When wrong:** Expecting `reified` on interface methods.

**15.** HOFs; `() -> Unit` vs `Function0<Unit>` bytecode.

- [x]

- **Thesis:** Kotlin lambdas compile to **`FunctionN`** interfaces (or **invokedynamic** + `LambdaMetaFactory` on JVM 8+ default) implementing `invoke`.
- **Mechanism / order:** **SAM** conversions for Java single abstract methods.
- **Edge cases:** **Capturing** lambdas allocate vs non-capturing singletons.
- **Production hook:** Prefer **`inline`** for hot paths.
- **When wrong:** Allocating lambda per row in RV without reuse.

**16.** Extension functions; override? resolution.

- [x]

- **Thesis:** **Static** dispatch on **static type** of receiver—**not polymorphic** like virtual methods; cannot override across subclasses as true overrides.
- **Mechanism / order:** JVM name mangling `receiverType$functionName`.
- **Edge cases:** **Member** wins over extension if same signature? Member wins.
- **Production hook:** Great for **DSL** readability.
- **When wrong:** Expecting dynamic dispatch for extensions on `Any`.

**17.** Scope functions: `let`/`run`/`with`/`apply`/`also`.

- [x]

- **Thesis:** Differ by **receiver vs arg**, **return value** (`lambda result` vs `receiver`), **`this` vs `it`**.
- **Mechanism / order:** `apply`/`also` return receiver for builder pattern.
- **Edge cases:** Nested `it` shadowing—name explicit.
- **Production hook:** `also` for logging side effect returning same object.
- **When wrong:** `run` on nullable without safe call—NPE.

**18.** `operator` overloads examples.

- [x]

- **Thesis:** Kotlin allows **`operator fun`** for `+`, `get`, `invoke`, `contains`, iterator, rangeTo, etc.
- **Mechanism / order:** Enables **DSL** (`vector + offset`).
- **Edge cases:** **`componentN`** for destructuring tied to `operator`.
- **Production hook:** Domain **`Money` + `operator plus`**.
- **When wrong:** Non-intuitive operators harming readability.

---

### Generics

**19.** Variance `in`/`out`/invariant.

- [x]

- **Thesis:** **`out T` producer/covariant** (returns T, not accepts); **`in T` consumer/contravariant**; **invariant** default for **mutable** structures.
- **Mechanism / order:** `List<out Number>` can read `Number` but not write specific `Int` list safely.
- **Edge cases:** **`@JvmWildcard`** for Java interop exposure.
- **Production hook:** `Flow<out UiState>` read-only exposure.
- **When wrong:** Declaring **`MutableList<out Foo>`**—compiler forbids useful writes.

**20.** Star projection `*` vs `Any?`.

- [x]

- **Thesis:** `Foo<*>` means **unknown type argument** preserving variance rules—**not** same as `Foo<Any?>` which fixes argument to top type and breaks variance safety for producers/consumers.
- **Mechanism / order:** `Function<*, *>` types parameters exist but unknown.
- **Edge cases:** `Array<*>` gives **out-projected** elements read as `Any?`.
- **Production hook:** Use star in **public** APIs hiding implementation generics.
- **When wrong:** Using `Any?` where star needed—loses variance.

**21.** Type erasure; runtime checks; `reified` help.

- [x]

- **Thesis:** JVM erases generic params; **`is List<String>`** unchecked cast warning—only **`List<*>`** check is honest without `reified`.
- **Mechanism / order:** **`reified`** inlines actual class token.
- **Edge cases:** **`checkCast`** with `KClass`.
- **Production hook:** Moshi **`Types.newParameterizedType`** for reflective parsing.
- **When wrong:** `(x as List<String>)` on erased list from Java raw types.

**22.** `where` multiple constraints.

- [x]

- **Thesis:** `fun <T> foo() where T : Serializable, T : Parcelable` requires **T** satisfy **all** upper bounds.
- **Mechanism / order:** intersection types encoded via synthetic bridges rarely.
- **Edge cases:** Conflicting defaults in generics.
- **Production hook:** Constrain **repository** interfaces to `CoroutineScope` owner + `Closeable` etc.
- **When wrong:** Deep intersection types harming readability—typealias or wrapper.

---

### Advanced Features

**23.** Contracts; `callsInPlace`.

- [x]

- **Thesis:** **Contracts** tell compiler extra facts about lambdas (`callsInPlace` exactly once) enabling **smart casts** after `run` blocks.
- **Mechanism / order:** `@ExperimentalContracts` stabilized for stdlib `also` patterns.
- **Edge cases:** Lying contracts → **unsound** bytecode—only for stdlib-like APIs you control.
- **Production hook:** Rare in app code—library authors’ tool.
- **When wrong:** DIY contracts without tests—dangerous.

**24.** Context receivers / context parameters vs extensions.

- [x]

- **Thesis:** **Context receivers** add **implicit capabilities** (`context(Logger, CoroutineScope)`) without threading every receiver through extensions—reduces **`with(...)`** noise.
- **Mechanism / order:** Kotlin 2.0+ **context parameters** evolution—syntax shifting; JVM lowers to **extra hidden parameters**.
- **Edge cases:** Binary compatibility / IDE support maturing.
- **Production hook:** Evaluate for **KMP** shared services layer.
- **When wrong:** Using as **global singleton** replacement without discipline.

**25.** `suspend` at bytecode; CPS.

- [x]

- **Thesis:** **`suspend` functions** compile to **state machine** with **`Continuation` parameter**—**CPS** style; no native stack for each coroutine frame beyond JVM stack for current segment.
- **Mechanism / order:** Label switches + **spilled** locals fields in generated class.
- **Edge cases:** **tail-call** optimization limited on JVM.
- **Production hook:** `-Xdebug` decompile to teach juniors.
- **When wrong:** Calling `suspend` from Java without **`runBlocking`** bridge.

**26.** Kotlin Multiplatform; `expect`/`actual`; source sets.

- [x]

- **Thesis:** **`commonMain`** declares **`expect`** API; **platform sources** supply **`actual`** implementations; **shared** business logic, **native** UI per platform.
- **Mechanism / order:** Gradle **hierarchical** source sets compile to each target.
- **Edge cases:** **Default** hierarchy template; **test** `expect/actual` too.
- **Production hook:** **Ktor** client in common, OkHttp/Darwin actual engines.
- **When wrong:** Putting **Android-only** APIs in `commonMain`.

**27.** KSP vs KAPT.

- [x]

- **Thesis:** **KSP** is **Kotlin-native** symbol processor—faster incremental, understands Kotlin types directly; **KAPT** stubs Java then runs Java annotation processors—slower, stub issues.
- **Mechanism / order:** Room/Moshi/Hilt modules migrating to KSP.
- **Edge cases:** Some libs still **KAPT-only** (Dagger/Hilt historically—improving).
- **Production hook:** Enable **KSP** + **Gradle build cache**.
- **When wrong:** Running **both** KAPT+KSP on same module unnecessarily.

**28.** `@JvmStatic`, `@JvmField`, `@JvmOverloads`, `@JvmName`.

- [x]

- **Thesis:** Interop knobs: static method/field exposure, **default parameter** overload generation, **rename** Kotlin file facade / overload clashes for Java callers.
- **Mechanism / order:** `@file:JvmName` controls facade class.
- **Edge cases:** `@JvmOverloads` on `@Composable` discouraged—Compose compiler special.
- **Production hook:** `@JvmStatic` for `companion` APIs called from Java SDK.
- **When wrong:** `@JvmField` on property with custom getter—illegal.

**29.** Structured concurrency (Kotlin design perspective).

- [x]

- **Thesis:** Language + stdlib design encourages **parent-child** `Job` relationships, **structured** cancellation propagation, **scoped** builders vs raw global threads.
- **Mechanism / order:** `coroutineScope` ties async work to block lifetime.
- **Edge cases:** **Unstructured** `GlobalScope` anti-pattern documented.
- **Production hook:** Always inject **`CoroutineScope`** with supervision rules.
- **When wrong:** Fire-and-forget without scope—lost exceptions.

**30.** `Sequence` vs `Iterable`.

- [x]

- **Thesis:** **`Sequence`** is **lazy** chained ops (iterator pull), can be **one-shot**; **`Iterable`** eager materialization when wrapped by collections ops on lists.
- **Mechanism / order:** `sequence { yield }` builder suspends per element.
- **Edge cases:** **Multiple consumption** of one-shot sequence—undefined/broken.
- **Production hook:** Large file line parse without loading all lines—`useLines`.
- **When wrong:** Using `Sequence` for **hot** reactive streams—use Flow.
