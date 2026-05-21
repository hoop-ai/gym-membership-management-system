# Test Documentation -- Recipe Management System

How to run the automated tests, what each section proves, and how to
fix the most common failures.

---

## How to run

From the project root:

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main
```

A successful run ends with the banner:

```
==========================================================
  ALL TESTS PASSED
==========================================================
```

If the build is clean (no compile errors) and `Main` prints
`ALL TESTS PASSED`, the engine works as designed.

---

## The six test sections

| # | Section | What it proves |
|---|---|---|
| 1 | Factory Method demo | Each concrete factory returns the right concrete recipe through the abstract `RecipeFactory` reference. The richer `createDessertRecipe(...)` method is also exercised. |
| 2 | Strategy demo | The same recipe list reorders correctly under `UrgentFirstStrategy`, `DeadlineFirstStrategy`, and `DessertFirstStrategy` -- with realistic data including null cook-by dates and an `EXTREME`-sweetness dessert. |
| 3 | Lifecycle demo | Every allowed transition in `RecipeStatus` succeeds; every disallowed transition (including the terminal `COOKED -> *`) throws `IllegalArgumentException` with a clear message. The `PAUSED` branch is exercised too. |
| 4 | Integration demo | A realistic 5-recipe workflow: create with `RecipeFactory`-by-key, set cook-by dates, transition through statuses, filter by status, remove by ID, print a summary. |
| 5 | SOLID demo | An anonymous `SortStrategy` is installed at runtime and the system uses it immediately -- a direct demonstration of Open/Closed and Dependency Inversion. The other four SOLID principles are also walked through with code references. |
| 6 | Edge cases | Six guarded failure modes: invalid priority, null title, unknown recipe type, missing ID, null strategy, case-insensitive type lookup. Each must either throw the right exception or, for the case-insensitive path, succeed. |

Every section prints `[PASS]` on success and `FAIL` (with a clear
reason) on failure. Test 6 also prints a final score (`Edge case
score: 6/6`).

---

## Expected output (abridged)

```
##########################################################
#                                                        #
#        SEN3006 -- Recipe Management System Demo        #
#       Factory Method  +  Strategy  (pure Java)         #
#                                                        #
##########################################################

==========================================================
  TEST 1: Factory Method Pattern Demo
==========================================================
  Created via factories:
    Recipe[id=1, type=DESSERT, title='Tiramisu', ...]
    Recipe[id=2, type=MAIN_COURSE, title='Roast chicken', ...]
    Recipe[id=3, type=APPETIZER, title='Caprese skewers', ...]
  ...
[PASS] Factory Method creates correct types polymorphically.

==========================================================
  TEST 2: Strategy Pattern Demo
==========================================================
  ---- Strategy 1: Urgent First (priority descending) ----
  [1] DESSERT      p=5  ...  Lemon tart
  [5] MAIN_COURSE  p=4  ...  Beef bourguignon
  [4] APPETIZER    p=3  ...  Bruschetta
  ...
[PASS] Same recipes, three different orderings via Strategy swap.

...

==========================================================
  ALL TESTS PASSED
==========================================================
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `javac: command not found` | JDK not installed or not on `PATH`. | Install Eclipse Temurin or any OpenJDK build, then reopen the terminal. |
| `Error: Could not find or load main class Main` | Wrong working directory, or `bin/` has not been compiled. | `cd` into the project root; run the `javac` step first. |
| `Unsupported class file major version 65` | Trying to run a Java 21-compiled JAR on a Java 8 runtime. | Either install a newer JDK or recompile from source against your installed JDK. |
| Garbled characters in the demo output | Console is not in UTF-8. | On Windows: `chcp 65001` before running. On Linux/macOS: this is usually fine by default. |
| One of the tests prints `FAIL` | Source files were edited and broke an invariant. | Compare the failing line with the expected behaviour in this document; fix the source. |
| GUI window looks blank or buttons are unreadable | The Windows L&F is hiding custom colours. | Verify you are running the latest JAR (`build-jar.bat` rebuilds it). The current JAR ships with custom renderers that bypass the issue. |

---

## Edge cases in Test 6 in detail

| Case | Input | Expected exception | Why |
|---|---|---|---|
| Invalid priority | `priority = 0` | `IllegalArgumentException` with message "Priority must be between 1 and 5, ..." | The constructor of `AbstractRecipe` enforces the range. |
| Null title | `title = null` | `IllegalArgumentException` with message "Title must not be null or blank." | Title is mandatory for every recipe. |
| Unknown type | `type = "UNKNOWN_TYPE"` | `IllegalArgumentException` that lists the available types. | The manager rejects unregistered factory keys. |
| Missing ID | `getRecipe(99999)` | `IllegalArgumentException` with message "No recipe found with ID: 99999" | The manager performs a linear lookup and throws if no recipe matches. |
| Null strategy | `setSortStrategy(null)` | `IllegalArgumentException` with message "Strategy must not be null." | The manager refuses to install a null strategy. |
| Case-insensitive type | `type = "dessert"` | Success: returns a `DESSERT` recipe. | The factory registry stores keys in uppercase and lowercases lookups. |

All six cases are exercised by `Main.java` and counted in the
`Edge case score` line.

---

## Manual verification (GUI)

If you want to verify the same behaviour visually:

1. Launch the GUI: `java -jar RecipeManagerGUI.jar`
2. Open **Demos -> Load Strategy demo** -- the table fills with the same
   five recipes Test 2 uses.
3. Change the **Sort by** dropdown -- the table reorders live, the same
   way the test demo prints three orderings.
4. Open **Demos -> Load Lifecycle demo**, select the only row, and click
   through *Start testing -> Approve -> Mark cooked*. Try clicking
   *Mark cooked* on a fresh `DRAFT` recipe -- the GUI surfaces the
   engine's `IllegalArgumentException` as a dialog.
5. Type a recipe with priority `0` in the form -- the engine rejects
   it and the dialog explains why.
