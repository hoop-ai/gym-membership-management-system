# Component Diagram

Four logical layers, all running inside one JVM. PlantUML source:
[component-diagram.puml](component-diagram.puml).

```mermaid
flowchart TB
    subgraph Presentation [Presentation layer]
      MAIN[Main<br/>scripted demo]
      APP[GymManagementApp<br/>console]
      GUI[GymManagerGUI<br/>Swing GUI]
    end

    subgraph Coordination [Coordination layer]
      GYM[Gym]
    end

    subgraph Patterns [Pattern layer]
      B[MembershipPlan.Builder]
      EVT[GymEvent hierarchy<br/>abstract + 4 events]
      NOT[MemberNotifier hierarchy<br/>interface + 3 channels]
      STATE[MembershipStatus<br/>state machine]
    end

    subgraph Domain [Domain layer]
      PLAN[MembershipPlan]
      MEMBER[Member]
      TIER[AccessTier]
    end

    MAIN --> GYM
    APP --> GYM
    GUI --> GYM

    GYM --> B
    GYM --> EVT
    GYM --> NOT
    GYM --> PLAN
    GYM --> MEMBER

    B --> PLAN
    PLAN --> TIER
    MEMBER --> PLAN
    MEMBER --> STATE
    MEMBER --> NOT
    NOT --> EVT
```

## What to look at

- Every arrow points *toward* an abstraction or the domain layer --
  never the other way round. That is the Dependency Inversion
  Principle in diagram form.
- The Presentation layer never reaches past the Coordination layer.
  Changing the GUI cannot break the engine.
- Adding a new presentation layer (e.g. a REST API) means adding one
  more box in the Presentation layer with one more arrow into the
  `Gym` -- and nothing else.
