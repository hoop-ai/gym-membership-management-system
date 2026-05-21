# Design Specification -- Recipe Management System

Technical design reference for the SEN3006 Recipe Management System.
Pair this document with the source files in `src/main/java/` and the
UML diagrams in `docs/uml/`.

---

## 1. System overview

The system is a single-JVM, in-memory Java application with three
interchangeable presentation layers:

| Entry point | Class | Purpose |
|---|---|---|
| Scripted demo | `Main` | Runs 6 self-checking test sections. |
| Console menu | `RecipeManagementApp` | Interactive menu-driven CLI. |
| Swing GUI | `gui.RecipeManagerGUI` | Visual demo with live Strategy swap. |

All three drive the same engine -- the `RecipeManager` -- through its
public API. The engine itself has no presentation code, no I/O, and no
persistence: it is a pure object model.

---

## 2. Class signatures (engine)

### 2.1 `Recipe` (interface)

```java
public interface Recipe {
    int             getId();
    String          getTitle();
    String          getDescription();
    RecipeStatus    getStatus();
    void            setStatus(RecipeStatus status);   // throws IllegalArgumentException
    int             getPriority();
    LocalDate       getDeadline();
    void            setDeadline(LocalDate deadline);
    LocalDateTime   getCreatedAt();
    String          getType();
}
```

### 2.2 `AbstractRecipe` (abstract class)

Implements every `Recipe` method except `getType()`. Holds shared
state, validates priority/title in the constructor, and validates state
transitions in `setStatus`. Auto-increments a static `idCounter`.

### 2.3 Concrete recipes

| Class | Type id | Extra fields |
|---|---|---|
| `DessertRecipe` | `"DESSERT"` | `sweetness` (String, mutable), `preparationNotes` (String, final) |
| `MainCourseRecipe` | `"MAIN_COURSE"` | `cookingTimeMinutes` (int), `satisfactionRating` (int, 1-10) |
| `AppetizerRecipe` | `"APPETIZER"` | `serveTemperature` (String), `occasion` (String) |

### 2.4 `RecipeStatus` (enum)

Five constants -- `DRAFT`, `TESTING`, `APPROVED`, `COOKED`, `PAUSED` --
each overriding `allowedTransitions()`. The public method
`canTransitionTo(RecipeStatus)` consults that set.

| From | Allowed to |
|---|---|
| `DRAFT` | `TESTING`, `PAUSED` |
| `TESTING` | `APPROVED`, `PAUSED` |
| `APPROVED` | `COOKED`, `TESTING` |
| `COOKED` | (terminal) |
| `PAUSED` | `DRAFT` |

### 2.5 `RecipeFactory` (abstract class)

```java
public abstract Recipe createRecipe(String title, String description, int priority);
public Recipe createRecipeWithDeadline(String title, String description, int priority, LocalDate cookBy);
```

The second method is a template method that calls the first and then
sets a deadline.

Concrete factories (`DessertRecipeFactory`, `MainCourseRecipeFactory`,
`AppetizerRecipeFactory`) override `createRecipe` and each expose a
type-specific richer creation method.

### 2.6 `SortStrategy` (interface)

```java
List<Recipe> sort(List<Recipe> recipes);
```

Implementations must return a new list and never mutate the input.

| Strategy | Algorithm |
|---|---|
| `UrgentFirstStrategy` | priority descending |
| `DeadlineFirstStrategy` | cook-by date ascending, nulls last |
| `DessertFirstStrategy` | two-phase: desserts by sweetness rank, then others by priority |

### 2.7 `RecipeManager`

```java
public Recipe       createRecipe(String type, String title, String description, int priority);
public void         registerFactory(String type, RecipeFactory factory);
public List<Recipe> getAllRecipes();
public Recipe       getRecipe(int id);
public void         removeRecipe(int id);
public List<Recipe> getRecipesByStatus(RecipeStatus status);
public void         transitionRecipe(int recipeId, RecipeStatus newStatus);
public void         setSortStrategy(SortStrategy strategy);
public String       getCurrentStrategyName();
public List<Recipe> getOrderedRecipes();
public String       getRecipeSummary();
public Set<String>  getRegisteredTypes();
```

The default state on construction registers all three factories and
selects `UrgentFirstStrategy`.

---

## 3. SOLID mapping

| Principle | Evidence |
|---|---|
| **S**RP | Each class has one job; the manager itself does no creation or sorting -- it delegates. |
| **O**CP | Adding a new recipe type or strategy is a new class. `Main.java` test 5 installs an anonymous strategy at runtime. |
| **L**SP | Every concrete recipe / factory / strategy is fully substitutable for its abstraction. The integration test exercises this explicitly. |
| **I**SP | `SortStrategy` has one method. `Recipe` has only universal methods. Type-specific accessors stay on concrete classes. |
| **D**IP | `RecipeManager` references only abstractions. No `new DessertRecipe(...)` exists outside of `DessertRecipeFactory`. |

---

## 4. GUI architecture

The Swing GUI is organised in four cooperating panels inside one
`JFrame`:

| Component | Role |
|---|---|
| Banner (`NORTH`) | Title + subtitle on a terracotta strip. |
| Table panel (`CENTER`) | Sort/filter strip + recipe table. |
| Form panel (`EAST`) | Vertically-stacked form for adding a recipe. |
| Action strip + status bar (`SOUTH`) | Lifecycle transition buttons and a live status line. |

The table model (`RecipeTableModel`) pulls from
`RecipeManager.getOrderedRecipes()` on every refresh, so a strategy
swap reorders the table immediately. The form panel calls
`RecipeManager.createRecipe(...)`, exposing the Factory Method client
behaviour through a single dropdown.

To guarantee readable colours on Windows, two presentation hacks are
in place:

1. The table-header uses a custom `DefaultTableCellRenderer` rather
   than relying on `JTableHeader.setBackground(...)`, which the
   Windows L&F ignores.
2. Accent buttons (`Add to Recipe Book`, action-strip buttons) call
   `setUI(new MetalButtonUI())` so the custom warm colours actually
   paint. This is the standard fix for Windows L&F overriding
   button backgrounds.

Both fixes are local to the GUI classes; the engine is untouched.

---

## 5. Validation rules

| Where | Rule | Exception |
|---|---|---|
| `AbstractRecipe` ctor | title non-blank | `IllegalArgumentException("Title must not be null or blank.")` |
| `AbstractRecipe` ctor | description non-null | `IllegalArgumentException("Description must not be null.")` |
| `AbstractRecipe` ctor | priority in 1-5 | `IllegalArgumentException("Priority must be between 1 and 5, ...")` |
| `MainCourseRecipe` ctor | cooking time >= 0 | `IllegalArgumentException` |
| `MainCourseRecipe` ctor | satisfaction in 1-10 | `IllegalArgumentException` |
| `AbstractRecipe.setStatus` | transition allowed by state machine | `IllegalArgumentException("Cannot transition from X to Y")` |
| `RecipeManager.createRecipe` | type registered (case-insensitive) | `IllegalArgumentException` with list of available types |
| `RecipeManager.setSortStrategy` | strategy not null | `IllegalArgumentException` |
| `RecipeManager.getRecipe` | id exists | `IllegalArgumentException` |

---

## 6. Extension points

| Want to add | Files to touch | Existing files modified |
|---|---|---|
| A fourth recipe type (e.g. `DrinkRecipe`) | Two new files: a recipe class extending `AbstractRecipe`, a factory extending `RecipeFactory`. One `registerFactory(...)` call (e.g. in `RecipeManager` constructor or via runtime). | Zero, except the optional one-line registration. |
| A fourth sort strategy | One new file implementing `SortStrategy`. | Zero. Just call `setSortStrategy(new MyStrategy())`. |
| A new lifecycle state | One new enum constant in `RecipeStatus` plus updates to neighbouring `allowedTransitions()` methods. | Only the affected enum constants. |
| A new entry point (e.g. REST API) | New top-level class that drives `RecipeManager`. | Zero. The engine has no presentation assumptions. |

These extension paths are what NFR3 (the Open/Closed requirement)
demands. They are demonstrated live in Test 5 of `Main.java`.
