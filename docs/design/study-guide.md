# Study Guide -- Recipe Management System

Preparation document for the in-person presentation. Every class is
explained in one paragraph, then the Q&A cheat-sheet answers the most
likely professor questions.

---

## Every class in one paragraph each

### `Recipe` (interface)
The **Product** in the Factory Method pattern. Declares only the
methods that every recipe must support -- id, title, description,
status, priority, cook-by date, creation timestamp, and a `getType()`
identifier. Keeping this interface narrow is the Interface Segregation
Principle in action: type-specific accessors (`getSweetness`,
`getCookingTimeMinutes`, `getServeTemperature`) deliberately stay on
the concrete classes.

### `AbstractRecipe`
The **Abstract Product** that consolidates shared state and behaviour.
Auto-increments a static ID counter, defaults the status to `DRAFT`,
validates priority and title in the constructor, and provides a
`toString()` that subclasses extend by appending their own fields --
the *Template Method* mini-pattern. `setStatus(...)` here delegates
validation to the state machine on `RecipeStatus`.

### `DessertRecipe`, `MainCourseRecipe`, `AppetizerRecipe`
The three **Concrete Products**. Each adds two type-specific fields:
desserts have sweetness and preparation notes, main courses have
cooking time and a satisfaction rating, appetizers have a serving
temperature and an occasion label. Each overrides `getType()` to
return the type identifier the factory registry uses.

### `RecipeStatus` (enum)
The lightweight **State** model. Each constant overrides
`allowedTransitions()` to declare which states it may move to. The
public method `canTransitionTo(RecipeStatus)` consults that set so
clients never need to know the rules directly. `COOKED` is a terminal
state with no outgoing transitions; `PAUSED` is a one-way pause that
returns to `DRAFT` only.

### `RecipeFactory`
The **Abstract Creator**. Declares one abstract method,
`createRecipe(title, description, priority)`, that concrete subclasses
must implement. Also defines `createRecipeWithDeadline(...)`, a
template method that calls the factory method and then attaches a
deadline -- a small layered example of Template Method on top of
Factory Method.

### `DessertRecipeFactory`, `MainCourseRecipeFactory`, `AppetizerRecipeFactory`
The three **Concrete Creators**. Each overrides `createRecipe(...)` to
instantiate one specific concrete recipe with sensible defaults
(MEDIUM sweetness, 45-minute cook time, ROOM serve temperature). Each
also exposes a type-specific richer method (`createDessertRecipe`,
`createMainCourseRecipe`, `createAppetizerRecipe`) for callers that
need to control every field.

### `SortStrategy` (interface)
The **Strategy** abstraction. A single-method interface whose
implementations return a new sorted list without mutating the input.
Choosing one method is intentional -- the Interface Segregation
Principle says clients should not depend on what they do not use.

### `UrgentFirstStrategy`, `DeadlineFirstStrategy`, `DessertFirstStrategy`
Three **Concrete Strategies**. `UrgentFirstStrategy` sorts by priority
descending; `DeadlineFirstStrategy` sorts by cook-by date ascending
with `null` dates last; `DessertFirstStrategy` is a two-phase sort
that puts every dessert first (ranked by sweetness) and then orders
the rest by priority. The third strategy is the most interesting
because it makes a type-aware decision -- showing that Strategy is
not limited to one-line comparators.

### `RecipeManager`
The single coordinator. It is the **Client** of the Factory Method
pattern and the **Context** of the Strategy pattern. All its fields
are abstractions: `List<Recipe>`, `SortStrategy`, and a
`Map<String, RecipeFactory>` registry -- a simplified Service Locator
that lets clients create recipes by string type. State-transition
validation, lookup, removal, and summarisation also live here.

### `Main`
The scripted demonstration. Six labelled test sections walk through
the Factory Method pattern, the Strategy pattern, the lifecycle state
machine, an integration scenario, the SOLID principles (including a
live runtime strategy swap), and six edge cases. Each section ends
with one or more `[PASS]` lines.

### `RecipeManagementApp`
The interactive console driver. Wraps the manager in a menu loop so
the user (or the professor) can create recipes, change strategy,
transition status, filter by status, view summaries, and remove
recipes. Uses only `Scanner` and `System.out`; no third-party CLI
library.

### GUI classes (`gui/RecipeManagerGUI`, `RecipeFormPanel`, `RecipeTablePanel`, `RecipeTableModel`, `DemoScenarios`)
A small Swing front end. The form panel calls
`RecipeManager.createRecipe(...)`. The table panel rebuilds from
`getOrderedRecipes()` whenever the strategy changes -- making the
Strategy pattern visible to a non-coder. The `DemoScenarios` class
provides one-click loaders that mirror the data in `Main.java`'s
test sections.

---

## Q&A cheat-sheet

**Q. Why two patterns instead of one?**
A. The two problems the assignment poses are different: creating
different *kinds* of objects (Creational -> Factory Method) and
swapping *algorithms* on those objects (Behavioral -> Strategy). Using
both makes the project demonstrate one Creational and one Behavioral
pattern, which the assignment explicitly recommends.

**Q. Why an abstract `RecipeFactory` class instead of a `RecipeFactory`
interface?**
A. The class hosts a useful template method
(`createRecipeWithDeadline`) that calls the abstract factory method and
then attaches a deadline. Interfaces in Java 7 cannot carry shared
behaviour; the abstract class can. Java 8 default methods would work
too, but the abstract-class form matches Gamma et al.'s original
Factory Method definition more faithfully.

**Q. What if I want a new recipe type?**
A. Two new files: one extending `AbstractRecipe`, one extending
`RecipeFactory`. Then one call to `manager.registerFactory("NEW_TYPE",
new MyNewFactory())`. Zero modifications to any existing class. This
is the Open/Closed Principle made executable.

**Q. What if I want a new sort strategy?**
A. One new class implementing `SortStrategy`, then
`manager.setSortStrategy(new MyStrategy())`. Test 5 in `Main.java`
does exactly that, using an anonymous class defined inline.

**Q. How are invalid status transitions prevented?**
A. The `RecipeStatus` enum is itself a state machine: each constant
declares its allowed targets. `AbstractRecipe.setStatus(...)` consults
`canTransitionTo(...)` and throws `IllegalArgumentException` if the
target is not allowed. The terminal state `COOKED` returns an empty
allowed-set, so nothing can leave it.

**Q. Why is `DessertFirstStrategy` allowed to know about
`DessertRecipe` -- isn't that a coupling violation?**
A. The strategy still depends on the `Recipe` interface for everything
except one `instanceof DessertRecipe` check used to decide ranking.
The same coupling appears in the original Strategy pattern when the
algorithm is genuinely type-aware -- for example, Comparator
implementations that down-cast in `compare(...)`. Keeping the rest of
the algorithm against the abstraction limits the blast radius.

**Q. Why no Maven or Gradle?**
A. The assignment explicitly mandates "pure Java with the standard
library". Maven and Gradle would add dependencies, configuration, and
build-tool noise without changing any architectural property of the
system. A single `javac` line and a single `java` line are clearer for
a demonstration of patterns.

**Q. Where is the Factory Method pattern actually visible at runtime?**
A. In `RecipeManager.createRecipe(...)`, line by line: the method
looks up a `RecipeFactory` by string key, calls
`factory.createRecipe(...)`, and stores the returned `Recipe`. The
manager never names a concrete recipe class. Add a `println` to any
factory's `createRecipe` and you will see the polymorphic call live.

**Q. Where is the Strategy pattern actually visible at runtime?**
A. In `RecipeManager.getOrderedRecipes()`, which calls
`currentStrategy.sort(recipes)`. Swap the strategy with
`setSortStrategy(...)` and the next call to `getOrderedRecipes()`
returns a list in a different order, with no other code changes. The
GUI binds this method to the *Sort by* dropdown so the swap is
literally visible.

**Q. Why are GUI classes in a `gui/` subfolder but with no
`package gui;` declaration?**
A. Java 8 forbids importing default-package classes from a named
package. The GUI needs to reference engine types
(`Recipe`, `RecipeManager`, ...) which live in the default package.
Keeping the GUI in the default package too lets the GUI reference
those types directly. The `gui/` directory is purely organisational --
it has no language-level meaning.

**Q. How is the project tested without JUnit?**
A. `Main.java` contains six self-checking sections that print
`[PASS]` on success. Edge cases are exercised in Test 6 and scored
out of six. The decision to skip JUnit avoids any external dependency
while still giving the professor a single deterministic output to
verify.

---

## Live-demo checklist

1. **Show the file tree.** `tree -L 3` or just open the workspace --
   point out that the 17 source files split cleanly into product,
   factory, strategy, manager, and entry-point layers.
2. **Run the test demo.** `java -cp bin Main` -- talk through the
   `[PASS]` markers as they fly past.
3. **Open the GUI.** `java -jar RecipeManagerGUI.jar`. Load the
   *Strategy demo*. Switch *Sort by* from `Urgent First` to
   `Dessert First` -- watch the table reorder live. Mention that the
   manager is unchanged; the only difference is the installed
   `SortStrategy`.
4. **Trigger a validation error.** Try to type priority `0`. The
   engine throws, the GUI surfaces the message in a dialog. Make the
   point that the validation belongs to the engine, not the GUI.
5. **Trigger an invalid transition.** Pick a `DRAFT` row, hit
   *Mark cooked*. The GUI shows the state-machine rejection.
6. **Close the demo with the file structure.** Re-emphasise that
   adding a new recipe type or strategy means new files only -- the
   manager never changes. That is the project's main message.
