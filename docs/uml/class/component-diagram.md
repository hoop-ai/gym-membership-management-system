# Component Diagram

Four logical layers, all running inside one JVM. The PlantUML source is in
[component-diagram.puml](component-diagram.puml).

```mermaid
flowchart TB
    subgraph Presentation [Presentation layer]
      MAIN[Main<br/>scripted demo]
      APP[RecipeManagementApp<br/>console]
      GUI[RecipeManagerGUI<br/>Swing GUI]
    end

    subgraph Coordination [Coordination layer]
      MGR[RecipeManager]
    end

    subgraph Patterns [Pattern layer]
      FACT[RecipeFactory hierarchy]
      STRAT[SortStrategy hierarchy]
      STATE[RecipeStatus state machine]
    end

    subgraph Product [Product layer]
      PROD[Recipe / AbstractRecipe<br/>+ 3 concrete recipes]
    end

    MAIN --> MGR
    APP --> MGR
    GUI --> MGR

    MGR --> FACT
    MGR --> STRAT
    MGR --> STATE
    MGR --> PROD

    FACT --> PROD
    STRAT --> PROD
    PROD --> STATE
```

## What to look at

- Every arrow points *toward* an abstraction or the product layer --
  never the other way round. That is the Dependency Inversion Principle
  in diagram form.
- The Presentation layer never reaches past the Coordination layer. A
  GUI change cannot break the engine.
- Adding a new presentation layer (e.g. a REST API) means adding one
  more box in the Presentation layer with one more arrow into the
  `RecipeManager` -- and nothing else.
