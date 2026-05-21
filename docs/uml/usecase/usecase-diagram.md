# Use Case Diagram

Every operation a cook can perform. The PlantUML source is in
[recipe-management-usecase.puml](recipe-management-usecase.puml).

```mermaid
flowchart LR
    Cook(("Cook<br/>(User)"))
    subgraph RMS [Recipe Management System]
      UC1[Create a recipe]
      UC2[View all recipes]
      UC3[View ordered recipes]
      UC4[Change sort strategy]
      UC5[Transition recipe status]
      UC6[Filter recipes by status]
      UC7[View recipe details]
      UC8[Show summary]
      UC9[Remove a recipe]
      UC10[Load demo scenario]
    end
    Cook --- UC1
    Cook --- UC2
    Cook --- UC3
    Cook --- UC4
    Cook --- UC5
    Cook --- UC6
    Cook --- UC7
    Cook --- UC8
    Cook --- UC9
    Cook --- UC10
```

## Pattern annotations

| Use case | Pattern involvement |
|---|---|
| Create a recipe | Factory Method -- the manager looks up the right factory by type key and delegates the `new` call to it. |
| View ordered recipes | Strategy -- the manager delegates ordering to the current `SortStrategy`. |
| Change sort strategy | Strategy swap -- a single setter call replaces the algorithm at runtime. |
| Transition recipe status | State -- validated by the `RecipeStatus` enum's transition rules. |
| Load demo scenario | GUI helper -- the `DemoScenarios` class mirrors the data sets used in the scripted tests. |

Every use case is reachable from all three entry points (`Main`,
`RecipeManagementApp`, `gui/RecipeManagerGUI`) because all three drive
the same `RecipeManager` public API.
