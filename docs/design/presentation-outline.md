# Presentation Outline -- Recipe Management System

Slide-by-slide plan for the in-person presentation. Aim is 8-10
minutes of talk + 2-3 minutes of live demo + 2-5 minutes of Q&A.

---

## Slide 1 -- Title

- **Title:** Recipe Management System -- Factory Method + Strategy
- **Course:** SEN3006 Software Architecture
- **Sub-title:** A kitchen-themed application of two classical design patterns
- **Speaker note:** Set the tone in one sentence. "I built a recipe
  organiser to make two design patterns visible."

---

## Slide 2 -- Why this project

- The professor's brief asks for one Creational and one Behavioral
  pattern.
- A cook's recipe book naturally exposes both problems:
  - *What type is each item?* (Creational -- Factory Method).
  - *How should the list be ordered today?* (Behavioral -- Strategy).
- Builds a working application instead of a toy class hierarchy.
- **Speaker note:** Mention that the domain was chosen because the
  patterns map cleanly, not the other way round.

---

## Slide 3 -- What the system does

- Three types of recipes: dessert, main course, appetizer.
- Five-state lifecycle: draft -> testing -> approved -> cooked, with
  a paused branch.
- Three orderings: urgent first, deadline first, dessert first.
- Three entry points share one engine:
  - `Main` (scripted demo).
  - `RecipeManagementApp` (console menu).
  - `RecipeManagerGUI` (Swing window).
- **Speaker note:** Highlight that all three drivers consume the
  same `RecipeManager` public API.

---

## Slide 4 -- Factory Method

- Problem: hard-coding `new DessertRecipe(...)` couples the rest of
  the code to every recipe type.
- Solution: `RecipeFactory` (abstract) -> three concrete factories.
- Manager keeps a `Map<String, RecipeFactory>` registry (a simplified
  Service Locator).
- Adding a new type = two new files + one registration call.
- **Speaker note:** Point at `RecipeManager.createRecipe(...)` to
  show that the manager never names a concrete recipe class.

---

## Slide 5 -- Strategy

- Problem: hard-coding one sort algorithm prevents context-specific
  orderings.
- Solution: `SortStrategy` (interface) -> three concrete strategies.
- Manager holds a current strategy and delegates sorting to it.
- Strategy can be swapped at runtime -- test 5 installs a brand-new
  anonymous strategy live.
- **Speaker note:** Demonstrate live in slide 9.

---

## Slide 6 -- Lifecycle

- `RecipeStatus` enum is itself a state machine -- each constant
  declares its allowed transitions.
- `AbstractRecipe.setStatus(...)` validates and throws
  `IllegalArgumentException` otherwise.
- Diagram on screen showing the five states and nine valid edges.
- **Speaker note:** This is a lightweight State pattern -- mention
  it as a third bonus pattern.

---

## Slide 7 -- SOLID quick-pass

- **S**RP -- one job per class. Manager coordinates only.
- **O**CP -- new type / new strategy = new files.
- **L**SP -- every factory works through the abstract reference.
- **I**SP -- one method on `SortStrategy`; minimal `Recipe`.
- **D**IP -- manager fields are all interfaces / abstract classes.
- **Speaker note:** Don't dwell. Each principle gets ten seconds.

---

## Slide 8 -- Architecture in one picture

- Class diagram (Mermaid render).
- Highlight the three layers:
  - Product layer (Recipe + Abstract + 3 concrete).
  - Pattern layer (Factory + 3 concrete; Strategy + 3 concrete).
  - Coordination + entry-point layer (Manager + 3 drivers).
- **Speaker note:** Point out that arrows only ever go *toward*
  abstractions -- the DIP principle made visible.

---

## Slide 9 -- Live demo (3-4 minutes)

1. **Run the test demo.** `java -cp bin Main`. Scroll quickly, point
   at one `[PASS]` per section.
2. **Open the GUI.** `java -jar RecipeManagerGUI.jar`. Load
   *Strategy demo* from the menu.
3. **Swap the strategy.** Cycle the *Sort by* dropdown through all
   three options. Table reorders live.
4. **Trigger an error.** Type a recipe with priority `0`. The engine
   throws, the dialog explains why.
5. **Trigger an invalid transition.** Pick a `DRAFT` row, hit
   *Mark cooked*. Engine refuses.
- **Speaker note:** Validation belongs to the engine, not the GUI.
  The GUI just surfaces the message.

---

## Slide 10 -- Extension story

- Adding `DrinkRecipe`:
  - `DrinkRecipe.java` (extends `AbstractRecipe`).
  - `DrinkRecipeFactory.java` (extends `RecipeFactory`).
  - One `manager.registerFactory("DRINK", new DrinkRecipeFactory())`.
- Adding `CheapestFirstStrategy`:
  - `CheapestFirstStrategy.java` (implements `SortStrategy`).
  - One `manager.setSortStrategy(new CheapestFirstStrategy())`.
- **Zero edits** to any existing class. That is the project's
  payoff.

---

## Slide 11 -- What went well, what could be next

- Worked well:
  - The two patterns map cleanly to the domain.
  - Test 5 makes Open/Closed visibly true at runtime.
  - The GUI lets a non-coder see Strategy swapping live.
- Could be next:
  - Persistence (Repository pattern).
  - Search and tags (Specification pattern).
  - Internationalisation of labels.
- **Speaker note:** Acknowledge the boundaries -- finite-scope
  project, not an attempt at production software.

---

## Slide 12 -- Q&A

- Open the study-guide cheat-sheet on screen.
- Likely questions:
  - Why an abstract factory class instead of an interface?
  - Why no JUnit?
  - Why is `DessertFirstStrategy` allowed to know about
    `DessertRecipe`?
- All answered in `docs/design/study-guide.md`.

---

## Timing budget

| Slide | Time |
|---|---|
| 1 -- Title | 0:30 |
| 2 -- Why | 0:45 |
| 3 -- What it does | 1:00 |
| 4 -- Factory Method | 1:15 |
| 5 -- Strategy | 1:15 |
| 6 -- Lifecycle | 0:45 |
| 7 -- SOLID | 0:50 |
| 8 -- Architecture | 0:45 |
| 9 -- Live demo | 3:00 |
| 10 -- Extension | 0:45 |
| 11 -- Recap | 0:30 |
| 12 -- Q&A | 2-5:00 |
| **Total** | **~13 min + Q&A** |

Cut slide 7 (SOLID) and slide 11 (recap) if time pressure appears.
