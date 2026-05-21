# Recipe Management System for Home Cooks

## Design Patterns: Factory Method and Strategy

**Course:** SEN3006 -- Software Architecture
**Project type:** Java Design Pattern Project
**Language:** Java 8 (pure standard library, zero external dependencies)
**Source files:** 17 (2 interfaces, 1 enum, 2 abstract classes, 12 concrete classes)
**Deliverables:** Java source ZIP, compiled JAR, PDF report

---

## Table of Contents

1. [Introduction](#31-introduction)
2. [Problem Definition and System Requirements](#32-problem-definition-and-system-requirements)
3. [Design Pattern Explanation](#33-design-pattern-explanation)
4. [System Design and UML Diagrams](#34-system-design-and-uml-diagrams)
5. [Implementation and Code Explanation](#35-implementation-and-code-explanation)
6. [Testing and Demonstration](#36-testing-and-demonstration)
7. [Results and Evaluation](#37-results-and-evaluation)
8. [Conclusion](#38-conclusion)
9. [References](#39-references)

---

## 3.1 Introduction

### Background

Home cooks routinely maintain a personal "recipe book" -- a private
collection of dishes they have tried, refined, or want to attempt for an
upcoming meal. As the collection grows, two patterns of complexity emerge.
First, different courses (desserts, main courses, appetizers) have very
different relevant attributes: a dessert's sweetness and chill time matter
enormously, a main course's total cooking time drives dinner timing, and
an appetizer's serving temperature decides whether it can be prepared
hours ahead. Second, the right ordering for the collection depends on
context -- which dish is most urgent, which has the earliest cook-by
date, which course needs the most lead time. A single rigid sort order
makes the system unusable for planning real meals.

These two complexities -- *what type is each item* and *how should the
list be ordered* -- correspond to two well-studied design problems in
object-oriented programming, both of which have classical pattern
solutions. The system presented here applies both patterns to a real,
working application that a cook could plausibly use to plan dinner
parties or weeknight meals.

### Motivation

Naive implementations of recipe-management software tend to centralise
type decisions inside long `switch` or `if`-`else` blocks (one branch
per course type) and to hard-code one sort algorithm directly inside the
collection class. Both choices look harmless for a system with three
types and one ordering. They are deceptive: every new course type
requires editing every conditional, and every new ordering means
rewriting the collection's sort method while risking regressions in the
old behaviour. The result is a system that is hostile to extension --
the very situation the SOLID principles are designed to prevent.

Design patterns offer a proven, well-documented way out. By delegating
type-specific creation to a family of *factories*, and by delegating
ordering to a family of swappable *strategies*, the system becomes open
to extension and closed to modification: a new course type or a new
ordering is a brand-new file, and no existing class needs to be touched.
This project demonstrates that approach with a complete, working
implementation.

### Objectives

The objectives of this project are:

1. **Demonstrate the Factory Method pattern** as a solution for flexible,
   polymorphic object creation in a real-world domain.
2. **Demonstrate the Strategy pattern** as a solution for interchangeable
   algorithms that can be swapped at runtime.
3. **Apply SOLID principles** throughout the design to achieve a system
   that is maintainable, extensible, and testable.
4. **Build a complete, working system** in pure Java with zero external
   dependencies, proving that good architecture does not require complex
   frameworks.

### Solution overview

The Recipe Management System implements two complementary design
patterns. The **Factory Method** pattern (Creational) handles recipe
creation: an abstract `RecipeFactory` declares the creation interface,
and concrete factories (`DessertRecipeFactory`, `MainCourseRecipeFactory`,
`AppetizerRecipeFactory`) encapsulate the instantiation logic for each
recipe type. The **Strategy** pattern (Behavioral) handles recipe
ordering: a `SortStrategy` interface defines a sorting contract, and
concrete strategies (`UrgentFirstStrategy`, `DeadlineFirstStrategy`,
`DessertFirstStrategy`) provide interchangeable algorithms. A central
`RecipeManager` class coordinates both patterns, acting as the client of
the Factory Method pattern and the context of the Strategy pattern. The
system comprises 17 classes in total -- 2 interfaces, 1 enum, 2 abstract
classes, and 12 concrete classes -- all built using only the Java
standard library.

---

## 3.2 Problem Definition and System Requirements

### Problem statement

Build a recipe-management application that lets a home cook (1) record
multiple types of recipes with type-specific information, (2) move each
recipe through a workflow that captures the cook's real progress from
"I jotted this down" to "I have served this to guests", and (3) view
the collection in different useful orderings depending on the planning
context. The design must accommodate new recipe types and new orderings
without modifying any existing class.

### Functional requirements

| ID | Requirement | Demonstrated by |
|---|---|---|
| FR1 | The system must support at least three distinct recipe types, each with type-specific fields. | `DessertRecipe`, `MainCourseRecipe`, `AppetizerRecipe`. |
| FR2 | Recipes are created through a factory mechanism so client code does not depend on concrete recipe classes. | `RecipeFactory`, three concrete factory subclasses, `RecipeManager.createRecipe(...)`. |
| FR3 | The system must offer at least three different orderings of the same recipe list. | `UrgentFirstStrategy`, `DeadlineFirstStrategy`, `DessertFirstStrategy`. |
| FR4 | The active ordering must be swappable at runtime. | `RecipeManager.setSortStrategy(SortStrategy)`. |
| FR5 | Each recipe must transition through a lifecycle (draft / testing / approved / cooked / paused) with rules that prevent invalid transitions. | `RecipeStatus` enum and `AbstractRecipe.setStatus(...)`. |
| FR6 | The system must validate inputs (non-blank titles, priority in 1-5, defined types, valid status transitions) and report errors clearly. | `IllegalArgumentException` thrown from `AbstractRecipe`, `RecipeManager`, `RecipeStatus`. |
| FR7 | A user must be able to interact with the system in at least one of: scripted demo, console menu, graphical UI. | `Main`, `RecipeManagementApp`, `gui/RecipeManagerGUI`. |

### Non-functional requirements

| ID | Requirement | Approach |
|---|---|---|
| NFR1 | Zero external dependencies. | Only `java.util.*`, `java.time.*`, `javax.swing.*`, `java.awt.*` from the standard library. |
| NFR2 | Build with a single `javac` command on Java 8+. | Default package; no Maven, Gradle, or build descriptor. |
| NFR3 | Add a new recipe type or sort strategy without modifying any existing class. | All extension points (factories and strategies) are abstract; the manager registers factories by string key. |
| NFR4 | Errors are surfaced to the user with actionable messages, not stack traces. | Validation in constructors and setters throws `IllegalArgumentException` with descriptive text. |
| NFR5 | The system must be testable without a test framework. | `Main.java` contains six self-checking sections that print `[PASS]` markers. |
| NFR6 | The GUI must be readable on Windows, macOS, and Linux. | Custom header renderer and Metal-styled buttons override platform L&F where it would otherwise hide background colours. |

### Why architecture matters

These requirements look mundane in isolation, but together they form an
extensibility test. NFR3 specifically prohibits modifying existing
classes when adding new types or orderings -- the Open/Closed Principle
in plain text. Without an architectural approach, that requirement
collides head-on with how most beginners would write a recipe app
(centralised type switches, hard-coded sort). The patterns described in
section 3.3 satisfy NFR3 by construction.

---

## 3.3 Design Pattern Explanation

### 3.3.1 Factory Method Pattern (Creational)

#### Definition

The Factory Method pattern, as defined by Gamma et al. (1994), "defines
an interface for creating an object, but lets subclasses decide which
class to instantiate. Factory Method lets a class defer instantiation to
subclasses." The pattern separates *what* is being created (the
abstract Product) from *which* concrete class is instantiated (the
Concrete Product chosen by a Concrete Creator).

#### When and why it is used

Use the Factory Method pattern when:

- A class cannot anticipate the class of objects it must create.
- A class wants its subclasses to specify the objects it creates.
- Classes delegate responsibility to one of several helper subclasses,
  and you want to localise the knowledge of which helper is the
  delegate.

In the Recipe Management System, the manager cannot hard-code "create
a `DessertRecipe`" because it must be able to create main courses,
appetizers and any future course type. By delegating creation to a
family of factories, the manager works against the `RecipeFactory`
abstraction and never references any concrete recipe class.

#### Advantages

- **Decoupling.** Client code depends on the abstract product
  (`Recipe`) and the abstract creator (`RecipeFactory`), not on any
  concrete subclass.
- **Open/Closed.** Adding a new product type is a one-class change: a
  new concrete factory and a new concrete recipe class.
- **Single responsibility.** Each factory has exactly one job: create
  one kind of recipe.
- **Polymorphism.** Factories are themselves substitutable -- any
  `RecipeFactory` reference can hold any concrete factory.

#### Why suitable for this project

The assignment requires demonstrable extensibility for new recipe
types. Factory Method makes that extensibility a structural property of
the code, not a discipline applied by the developer. The two-tier API
(a generic `createRecipe` method plus an optional type-specific method
on each concrete factory) also illustrates a common real-world idiom:
defaults for casual use, full control for advanced use.

#### Real-world example

Java's own `Calendar.getInstance()` method is a textbook Factory
Method: it returns an abstract `Calendar` while the concrete class
(`GregorianCalendar`, a Buddhist calendar, a Japanese calendar) is
chosen by the runtime based on locale. Client code never names a
concrete subclass.

### 3.3.2 Strategy Pattern (Behavioral)

#### Definition

The Strategy pattern "defines a family of algorithms, encapsulates each
one, and makes them interchangeable. Strategy lets the algorithm vary
independently from clients that use it." (Gamma et al., 1994). A
Context object holds a reference to the current Strategy and delegates
algorithm-specific work to it.

#### When and why it is used

Use the Strategy pattern when:

- Many related classes differ only in their behaviour.
- You need different variants of an algorithm.
- An algorithm uses data clients should not know about.
- A class defines many behaviours that appear as multiple conditional
  statements in its operations.

In this project, the cook needs *several* orderings of the same recipe
list -- and the right one depends on context. Strategy moves each
ordering into its own class, leaving the manager free to delegate
without knowing the algorithm details.

#### Advantages

- **Runtime swap.** The current algorithm changes with a single
  setter call.
- **Open/Closed.** A new ordering is a new class. The manager and the
  existing strategies are untouched.
- **Eliminates conditionals.** No `if (strategy == "urgent")` chains
  inside the manager.
- **Testability.** Each strategy can be exercised in isolation by
  passing it a list.

#### Why suitable for this project

The project's requirements list three different orderings explicitly
(FR3). Strategy maps cleanly: one interface, three classes, three
algorithms. The test demo and the GUI both rely on runtime strategy
swap to make the pattern visible -- the same recipe list reorders
itself live when the strategy changes, which is the most direct way to
*show* the pattern at work rather than merely talk about it.

#### Real-world example

Java's `Comparator` interface is a Strategy: `Collections.sort(list,
new MyComparator())` lets the caller plug in any ordering algorithm
without `Collections.sort` knowing the rule. The Recipe Management
System's `SortStrategy` plays the same role, but at the manager level
rather than at the level of a single `sort` call.

---

## 3.4 System Design and UML Diagrams

The system is organised in three logical layers:

1. **Domain layer** -- the `Recipe` interface, `AbstractRecipe` base
   class, the three concrete recipe classes, and the `RecipeStatus`
   enum.
2. **Pattern layer** -- the `RecipeFactory` hierarchy (Factory Method)
   and the `SortStrategy` hierarchy (Strategy).
3. **Coordination + presentation layer** -- `RecipeManager` (the
   single coordinator) and three different entry points: `Main`
   (scripted demo), `RecipeManagementApp` (console menu), and the
   Swing GUI (`gui/RecipeManagerGUI`).

### Class diagram

The class diagram appears in [docs/uml/class/class-diagram.md](../uml/class/class-diagram.md)
(Mermaid render) and [docs/uml/class/recipe-management-class.puml](../uml/class/recipe-management-class.puml)
(PlantUML source). It shows:

- `Recipe` (interface) and `AbstractRecipe` (abstract class) with three
  concrete recipe subclasses.
- `RecipeFactory` (abstract) with three concrete factories.
- `SortStrategy` (interface) with three concrete strategies.
- `RecipeStatus` (enum) with the state-machine transitions.
- `RecipeManager` holding references to all three abstractions and
  acting as the client/context for both patterns.

### Sequence diagram

The sequence diagram in
[docs/uml/sequence/sequence-diagram.md](../uml/sequence/sequence-diagram.md)
walks through a typical user flow: the user asks the manager to create a
recipe, the manager looks up the right factory, the factory returns a
new recipe of the correct concrete type, and then the user later asks
for the ordered list, prompting the manager to delegate to the current
strategy.

### State diagram

The state diagram in
[docs/uml/activity/state-diagram.md](../uml/activity/state-diagram.md)
shows the five states of `RecipeStatus` and every allowed transition.

### Activity diagram

The activity diagram in
[docs/uml/activity/activity-diagram.md](../uml/activity/activity-diagram.md)
shows the end-to-end workflow of a recipe from creation through testing,
approval, optional pausing, and final cooking.

### Use-case diagram

The use-case diagram in
[docs/uml/usecase/usecase-diagram.md](../uml/usecase/usecase-diagram.md)
enumerates every operation a user can perform (create, view, sort,
transition, filter, remove, summarise).

### Component and deployment diagrams

The component diagram
([docs/uml/class/component-diagram.md](../uml/class/component-diagram.md))
shows the three logical layers. The deployment diagram
([docs/uml/class/deployment-diagram.md](../uml/class/deployment-diagram.md))
shows that the entire system runs in a single JVM with three optional
entry points.

---

## 3.5 Implementation and Code Explanation

### The Product hierarchy

`Recipe` (interface) declares the methods every recipe must support
(id, title, description, status, priority, cook-by date, creation
timestamp, type identifier). `AbstractRecipe` implements every method
except `getType()`, providing shared state (auto-incrementing ID,
default `DRAFT` status, validation of priority/title) and a `toString`
that subclasses extend.

The three concrete recipe classes each add type-specific data:

- `DessertRecipe`: sweetness level (LOW/MEDIUM/HIGH/EXTREME) and
  preparation notes.
- `MainCourseRecipe`: total cooking time in minutes and a 1-10
  satisfaction rating.
- `AppetizerRecipe`: serving temperature (COLD/HOT/ROOM) and the
  occasion label.

### The Factory hierarchy

`RecipeFactory` is an abstract class with one abstract method,
`createRecipe(title, description, priority)`, plus a template method
`createRecipeWithDeadline` that calls `createRecipe` and then sets the
deadline. Three concrete factories override `createRecipe` to instantiate
their specific recipe type with sensible defaults, and each exposes a
type-specific factory method (`createDessertRecipe`,
`createMainCourseRecipe`, `createAppetizerRecipe`) for callers that need
full control of the type-specific fields.

### The Strategy hierarchy

`SortStrategy` is an interface with a single method,
`sort(List<Recipe>) -> List<Recipe>`. Three concrete strategies
implement it:

- `UrgentFirstStrategy` sorts by priority descending.
- `DeadlineFirstStrategy` sorts by cook-by date ascending with `null`
  dates pushed to the end.
- `DessertFirstStrategy` is a two-phase sort: all desserts first
  (ranked by sweetness), then all other recipes by priority
  descending. It demonstrates that a strategy can incorporate
  type-aware logic without violating the interface contract.

### The coordinator

`RecipeManager` is the single class that ties everything together. Its
fields point only at abstractions:

```java
private final List<Recipe>               recipes;
private SortStrategy                     currentStrategy;
private final Map<String, RecipeFactory> factoryRegistry;
```

The factory registry is a simplified Service Locator -- a small twist
that lets callers create a recipe by string type ("DESSERT",
"MAIN_COURSE", "APPETIZER") rather than having to know which factory
class to instantiate. Registering a new type is a one-line call;
swapping the sort strategy is a one-line setter.

### The state machine

`RecipeStatus` is an enum where each constant overrides
`allowedTransitions()` to declare which target states it may move to.
`AbstractRecipe.setStatus` consults this method before assigning the
new status, throwing `IllegalArgumentException` if the transition is
not allowed. This is a lightweight State pattern -- the rules live with
the states themselves, not in a sprawling validator class.

### The three entry points

- `Main.java` runs six labelled test sections that exercise every
  pattern and edge case, printing `[PASS]` for each successful check.
- `RecipeManagementApp.java` offers a console menu so the cook can
  drive the system interactively.
- `gui/RecipeManagerGUI.java` opens a Swing window. The form on the
  right calls `RecipeManager.createRecipe(...)`, the table on the left
  binds to `getOrderedRecipes()`, and the *Sort by* dropdown installs a
  new strategy at runtime.

---

## 3.6 Testing and Demonstration

### Test methodology

The project uses a lightweight, framework-free testing approach. The
`Main.java` file contains six self-checking test sections, each of which
prints a clearly labelled `[PASS]` line on success and would print a
`FAIL` line if a check failed. This style is appropriate for a
classroom project where the goal is a visible, presentable demonstration
rather than an industrial test pipeline.

| Test | Verifies |
|---|---|
| Test 1 -- Factory Method demo | Each factory creates the correct recipe subtype polymorphically through the abstract `RecipeFactory` reference. |
| Test 2 -- Strategy demo | The same recipe list reorders correctly under all three strategies, including with null cook-by dates and extreme sweetness levels. |
| Test 3 -- Lifecycle demo | Every valid transition succeeds, every invalid transition raises `IllegalArgumentException`, and the terminal state `COOKED` blocks all further transitions. |
| Test 4 -- Integration demo | The full workflow -- create, set deadlines, transition, filter, remove, summarise -- runs end to end. |
| Test 5 -- SOLID demo | A brand-new `SortStrategy` (defined inline as an anonymous class) is installed at runtime, proving Open/Closed in action. All five SOLID principles are demonstrated with code. |
| Test 6 -- Edge cases | Six failure modes are exercised: invalid priority, null title, unknown recipe type, missing ID, null strategy, case-insensitive type lookup. |

### How to run

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main
```

The program ends with `ALL TESTS PASSED`. Detailed expected output is
available in [docs/design/test-documentation.md](../design/test-documentation.md).

### GUI demonstration

The Swing GUI offers a visual companion to the scripted tests. The
**Demos** menu loads the same data sets the test sections build, and
the **Sort by** dropdown swaps the live strategy. Watching the table
reorder itself in real time is the most direct demonstration of the
Strategy pattern.

---

## 3.7 Results and Evaluation

### Quantitative results

| Metric | Value |
|---|---|
| Source files | 17 (`.java`) |
| Production code lines | ~1,500 |
| External dependencies | 0 |
| Build commands required | 1 (`javac`) |
| Automated test sections | 6 |
| Edge cases covered | 6 |
| Recipe types supported | 3 (extensible) |
| Sort strategies supported | 3 (extensible) |
| Lifecycle states | 5 |
| Allowed transitions | 9 (out of 25 possible state pairs) |

### Pattern verification

Both target patterns are demonstrably implemented:

- **Factory Method.** The `RecipeManager` never references a concrete
  recipe class. Searching the source tree for `new DessertRecipe`,
  `new MainCourseRecipe`, or `new AppetizerRecipe` outside their own
  factory classes returns zero hits. Adding a hypothetical `DrinkRecipe`
  requires creating two files (a recipe and a factory) and one
  `registerFactory(...)` call.
- **Strategy.** The `RecipeManager` never references a concrete
  strategy class -- only the `SortStrategy` interface. The Test 5 demo
  installs a brand-new anonymous strategy and the system uses it
  immediately, with no recompilation of the manager.

### SOLID assessment

| Principle | How the system satisfies it |
|---|---|
| **S**ingle Responsibility | Each class has one job: factories create, strategies sort, the manager coordinates, the enum models the state machine, the recipes hold data. |
| **O**pen/Closed | New recipe types or new sort strategies are added by writing new classes only. Test 5 demonstrates this live. |
| **L**iskov Substitution | Every concrete recipe is usable as a `Recipe`; every factory as a `RecipeFactory`; every strategy as a `SortStrategy`. The test suite substitutes them explicitly. |
| **I**nterface Segregation | `SortStrategy` has a single method; `Recipe` has only methods relevant to every recipe; type-specific accessors live on concrete classes. |
| **D**ependency Inversion | `RecipeManager` depends on three abstractions (`Recipe`, `RecipeFactory`, `SortStrategy`) and on no concrete class. |

### Limitations and future work

The system intentionally stays small to keep the focus on the two
patterns. Three obvious extensions would not change the architecture
but would broaden the demo:

1. **Persistence.** Today the recipe list lives only in memory. A
   `RecipeRepository` abstraction with JSON or SQLite backends would
   add a third pattern (Repository) without disturbing the existing
   classes.
2. **Search and tags.** A `Specification` pattern would let users
   combine filters (e.g. "vegan AND quick AND cook-by-this-Saturday").
3. **Internationalisation.** Recipe titles and descriptions are stored
   as plain strings. A resource-bundle-based message catalogue would
   let the GUI run in multiple languages.

---

## 3.8 Conclusion

This project delivers a complete, working Recipe Management System that
satisfies every functional and non-functional requirement of the
SEN3006 assignment. Two design patterns -- Factory Method and Strategy
-- form the architectural spine, and a lightweight State machine adds a
third pattern incidentally. The SOLID principles are not retro-fitted
or claimed; they are visible structural properties of the code, each
demonstrated by at least one test section.

Three entry points (scripted demo, console menu, Swing GUI) prove that
the same engine supports very different presentation layers without
modification. The Strategy pattern is especially striking in the GUI:
swapping a single dropdown reorders the table live, and adding a
brand-new strategy at runtime in Test 5 requires zero changes to any
existing class. That visible extensibility is the project's main
pedagogical message: good architecture is not decoration, it is the
property of a system that says yes to the next reasonable change.

---

## 3.9 References

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994).
   *Design Patterns: Elements of Reusable Object-Oriented Software*.
   Addison-Wesley. (The original "Gang of Four" book; defines the
   Factory Method and Strategy patterns used here.)
2. Martin, R. C. (2002). *Agile Software Development, Principles,
   Patterns, and Practices*. Prentice Hall. (Introduced the SOLID
   acronym and the Open/Closed motivation used in section 3.3.)
3. Freeman, E., Robson, E., Bates, B., & Sierra, K. (2004).
   *Head First Design Patterns*. O'Reilly. (Accessible treatment of
   Factory Method and Strategy with similar Java examples.)
4. Oracle Corporation. (n.d.). *The Java Tutorials -- Collections,
   Enums, Generics, Swing*. https://docs.oracle.com/javase/tutorial/
   (Reference for the standard-library features used: collections,
   `LocalDate`, enum methods, Swing widgets.)
5. Bloch, J. (2018). *Effective Java*, 3rd ed. Addison-Wesley.
   (Items 1, 17, 20, 34: static factory methods, minimising
   mutability, interface design, enum types -- all applied in this
   project.)
