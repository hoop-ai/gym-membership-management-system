# Recipe Management System

A small Java application that demonstrates two classic object-oriented design
patterns — **Factory Method** and **Strategy** — by managing a personal
collection of cooking recipes (desserts, main courses, appetizers). Built for
the **SEN3006 — Software Architecture** course.

The whole project is **pure Java with zero external libraries**. If you have a
JDK installed, you can compile and run everything from a terminal in under a
minute.

---

## Contents

- [What you need on your computer](#what-you-need-on-your-computer)
- [Quick start (easiest way to see the project)](#quick-start-easiest-way-to-see-the-project)
- [The three ways to run the project](#the-three-ways-to-run-the-project)
- [Project structure](#project-structure)
- [The two design patterns](#the-two-design-patterns)
- [Recipe lifecycle (state machine)](#recipe-lifecycle-state-machine)
- [Documentation index](#documentation-index)
- [Build everything from source](#build-everything-from-source)
- [Troubleshooting](#troubleshooting)

---

## What you need on your computer

| Requirement | Minimum version | How to check |
|---|---|---|
| **Java JDK** | 8 (Java 1.8) or newer | Open a terminal and run `java -version` |
| **Java compiler** | `javac` from the same JDK | Run `javac -version` |
| Operating system | Windows, macOS, or Linux | Any of the three works |
| Disk space | About 2 MB | The whole repository is small |

That's it. No build tools (Maven, Gradle), no package managers, no
internet connection, no accounts.

### Don't have a JDK yet?

Pick one of these free distributions, install it, and reopen your terminal:

- [Eclipse Temurin (recommended)](https://adoptium.net/temurin/releases/)
- [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)
- Whatever your operating-system package manager ships (e.g. `apt install
  default-jdk` on Ubuntu, `brew install openjdk` on macOS).

After installation, `java -version` should print something like
`openjdk version "21.0.x"`.

---

## Quick start (easiest way to see the project)

The repository ships a ready-to-run GUI as a `.jar` file. Anyone with a JDK
can launch it with a single command:

```sh
java -jar RecipeManagerGUI.jar
```

On Windows you can also **double-click `run-gui.bat`** (macOS / Linux:
run `./run-gui.sh`). A window titled *Recipe Manager* opens. Use the menu
**Demos -> Load Strategy demo** to populate it with example recipes, then
switch the *Sort by* dropdown to watch the Strategy pattern re-order the
table in real time.

---

## The three ways to run the project

| # | Mode | Command | What it shows |
|---|---|---|---|
| 1 | **Graphical UI** | `java -jar RecipeManagerGUI.jar` | A window with a recipe form, table, sort/filter, status transitions |
| 2 | **Interactive console** | `java -cp bin RecipeManagementApp` | A menu-driven console app (create, list, transition, sort) |
| 3 | **Automated tests** | `java -cp bin Main` | A scripted 6-section demo with `[PASS]` markers |

Mode 1 requires only the JDK and the bundled JAR. Modes 2 and 3 also need a
compiled `bin/` directory — see [Build everything from source](#build-everything-from-source).

---

## Project structure

```
.
├── README.md                       <- you are here
├── SUBMISSION_README.md            <- how to package the deliverable for submission
├── RecipeManagerGUI.jar            <- runnable Swing GUI (Quick start)
├── run-gui.bat / run-gui.sh        <- double-click launchers for the JAR
├── build-jar.bat / build-jar.sh    <- rebuild the JAR from source
├── guide.md                        <- assignment description from the professor
├── src/
│   └── main/
│       └── java/                   <- 17 Java source files
│           ├── Recipe.java                  (Product interface)
│           ├── AbstractRecipe.java          (Abstract base class)
│           ├── DessertRecipe.java           (Concrete recipe -- desserts)
│           ├── MainCourseRecipe.java        (Concrete recipe -- main courses)
│           ├── AppetizerRecipe.java         (Concrete recipe -- appetizers)
│           ├── RecipeStatus.java            (Lifecycle state machine)
│           ├── RecipeFactory.java           (Abstract creator)
│           ├── DessertRecipeFactory.java
│           ├── MainCourseRecipeFactory.java
│           ├── AppetizerRecipeFactory.java
│           ├── SortStrategy.java            (Strategy interface)
│           ├── UrgentFirstStrategy.java
│           ├── DeadlineFirstStrategy.java
│           ├── DessertFirstStrategy.java
│           ├── RecipeManager.java           (Coordinator)
│           ├── Main.java                    (Automated test demo)
│           ├── RecipeManagementApp.java     (Interactive console app)
│           └── gui/                         (Swing GUI, 5 files)
│               ├── RecipeManagerGUI.java
│               ├── RecipeFormPanel.java
│               ├── RecipeTablePanel.java
│               ├── RecipeTableModel.java
│               └── DemoScenarios.java
└── docs/
    ├── report/report.md            <- full 9-section project report
    ├── design/                     <- design spec, study guide, test docs,
    │                                  presentation outline
    └── uml/                        <- class, sequence, state, activity,
                                       component, deployment, use-case diagrams
```

There are no hidden helpers, no generated code, no transitive dependencies.

---

## The two design patterns

### 1. Factory Method (Creational)

**Problem.** A recipe can be a dessert, a main course or an appetizer -- each
needs its own type-specific data (sweetness, cooking time, serving
temperature). Hard-coding `if (type == "DESSERT") new DessertRecipe(...)`
everywhere couples the rest of the system to every recipe class, and every
new type requires editing those branches.

**Solution.** An abstract `RecipeFactory` declares `createRecipe(...)`.
Three concrete factories (`DessertRecipeFactory`,
`MainCourseRecipeFactory`, `AppetizerRecipeFactory`) each instantiate one
concrete recipe type. The `RecipeManager` registers factories by string
key, so adding a fourth type (say, *DrinkRecipe*) is a one-class change.

```
RecipeFactory (abstract)
├── DessertRecipeFactory     -> creates DessertRecipe
├── MainCourseRecipeFactory  -> creates MainCourseRecipe
└── AppetizerRecipeFactory   -> creates AppetizerRecipe
```

### 2. Strategy (Behavioral)

**Problem.** A cook plans the same recipe list in different ways depending
on context -- "show me the most urgent dishes", "show me what needs cooking
soonest", "put desserts at the top because they need lead time".
Hard-coding every ordering inside the manager makes adding a new ordering
risky.

**Solution.** A `SortStrategy` interface has a single `sort(List<Recipe>)`
method. Three concrete strategies implement different algorithms:

```
SortStrategy (interface)
├── UrgentFirstStrategy    -> sort by priority descending (5 -> 1)
├── DeadlineFirstStrategy  -> sort by cook-by date ascending, undated last
└── DessertFirstStrategy   -> desserts first by sweetness, then others by priority
```

The `RecipeManager` keeps a current strategy and re-orders on demand.
Swapping strategy at runtime -- including a brand-new strategy written on
the spot -- requires zero changes to the manager.

Both patterns work through the same `RecipeManager`, which acts as the
**Client** for Factory Method and the **Context** for Strategy.

---

## Recipe lifecycle (state machine)

Recipes move through a small workflow that mirrors how a cook actually
builds and uses a recipe:

```
DRAFT  ---->  TESTING  ---->  APPROVED  ---->  COOKED   (terminal)
  |             |               |
  v             v               v
PAUSED  <-- PAUSED            TESTING   (back-step for revisions)
  |
  v
DRAFT     (resume after an obstacle is cleared)
```

Invalid transitions (for example `DRAFT -> COOKED`) throw an
`IllegalArgumentException`, so the data can never end up in an impossible
state.

---

## Documentation index

| Document | What's inside |
|---|---|
| [docs/report/report.md](docs/report/report.md) | Full 9-section project report (introduction, problem, patterns, UML, implementation, testing, evaluation, conclusion, references) |
| [docs/design/design-spec.md](docs/design/design-spec.md) | Class signatures, design rationale, SOLID mapping |
| [docs/design/test-documentation.md](docs/design/test-documentation.md) | What each test in `Main.java` proves and how to run it |
| [docs/design/study-guide.md](docs/design/study-guide.md) | Presentation prep: every class explained, Q&A cheat-sheet |
| [docs/design/presentation-outline.md](docs/design/presentation-outline.md) | Slide-by-slide plan with speaker notes |
| [docs/uml/](docs/uml/) | Class, sequence, state, activity, component, deployment, use-case diagrams (PlantUML + Mermaid renders) |
| [guide.md](guide.md) | The original assignment description |

---

## Build everything from source

If you only have the source tree (no compiled `bin/` and no JAR), this is
the full rebuild:

### Step 1 -- Compile

From the project root:

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Success looks like silence -- there is no output, and a `bin/` directory
appears with `.class` files inside.

### Step 2 -- Run the automated tests

```sh
java -cp bin Main
```

You should see six test sections, each ending with `[PASS]`, and a final
`ALL TESTS PASSED` banner.

### Step 3 -- Run the interactive console app

```sh
java -cp bin RecipeManagementApp
```

Pick options from the menu to create recipes, change the sort strategy,
move recipes through the lifecycle, and read summaries.

### Step 4 -- Rebuild the GUI JAR (optional)

If you want a fresh `RecipeManagerGUI.jar`:

- **Windows:** double-click `build-jar.bat` (or run it from a terminal).
- **macOS / Linux:** run `./build-jar.sh`.

The script wraps three lines:

```sh
rm -rf bin
javac -d bin src/main/java/*.java src/main/java/gui/*.java
(cd bin && jar cfe ../RecipeManagerGUI.jar RecipeManagerGUI *.class)
```

Then launch with:

```sh
java -jar RecipeManagerGUI.jar
```

---

## Troubleshooting

**`java: command not found` / `javac: command not found`** -- a JDK is not
installed, or it is installed but the executables are not on your `PATH`.
Reinstall using one of the links in
[What you need on your computer](#what-you-need-on-your-computer) and
reopen your terminal.

**`Error: Could not find or load main class Main`** -- you ran the command
from the wrong directory, or you have not compiled yet. Make sure you are
in the project root (the folder containing `README.md`) and that a `bin/`
directory exists with `Main.class` inside.

**`Unsupported class file major version`** -- the JAR was compiled with a
newer JDK than the one you are running. Either install a newer JDK or
recompile from source against your installed JDK using
`javac -d bin src/main/java/*.java src/main/java/gui/*.java`.

**The GUI window opens but looks blank** -- drag a corner to resize the
window. Some window managers initialise the layout slightly differently on
the first paint.

**`error: unmappable character (0x...) for encoding ...` during compile** --
add `-encoding UTF-8` to the `javac` command:

```sh
javac -encoding UTF-8 -d bin src/main/java/*.java src/main/java/gui/*.java
```

---

## License and authorship

This is a student project submitted for SEN3006. All source code and
documentation in this repository are original work produced for that
course.
