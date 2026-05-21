# Class Diagram

Recipe Management System -- 17 classes organised in three layers
(product / pattern / coordination). The PlantUML source is in
[recipe-management-class.puml](recipe-management-class.puml).

```mermaid
classDiagram
    class Recipe {
        <<interface>>
        +getId() int
        +getTitle() String
        +getDescription() String
        +getStatus() RecipeStatus
        +setStatus(RecipeStatus)
        +getPriority() int
        +getDeadline() LocalDate
        +setDeadline(LocalDate)
        +getCreatedAt() LocalDateTime
        +getType() String
    }

    class AbstractRecipe {
        <<abstract>>
        -id int
        -title String
        -description String
        -status RecipeStatus
        -priority int
        -deadline LocalDate
        -createdAt LocalDateTime
        +AbstractRecipe(title, description, priority)
    }

    class DessertRecipe {
        -sweetness String
        -preparationNotes String
        +getSweetness() String
        +setSweetness(String)
        +getPreparationNotes() String
    }

    class MainCourseRecipe {
        -cookingTimeMinutes int
        -satisfactionRating int
        +getCookingTimeMinutes() int
        +getSatisfactionRating() int
    }

    class AppetizerRecipe {
        -serveTemperature String
        -occasion String
        +getServeTemperature() String
        +getOccasion() String
    }

    class RecipeStatus {
        <<enum>>
        DRAFT
        TESTING
        APPROVED
        COOKED
        PAUSED
        +canTransitionTo(RecipeStatus) boolean
    }

    class RecipeFactory {
        <<abstract>>
        +createRecipe(title, description, priority) Recipe
        +createRecipeWithDeadline(title, description, priority, cookBy) Recipe
    }

    class DessertRecipeFactory
    class MainCourseRecipeFactory
    class AppetizerRecipeFactory

    class SortStrategy {
        <<interface>>
        +sort(List~Recipe~) List~Recipe~
    }

    class UrgentFirstStrategy
    class DeadlineFirstStrategy
    class DessertFirstStrategy

    class RecipeManager {
        -recipes List~Recipe~
        -currentStrategy SortStrategy
        -factoryRegistry Map~String,RecipeFactory~
        +createRecipe(type, title, desc, priority) Recipe
        +registerFactory(type, factory)
        +getAllRecipes() List~Recipe~
        +getOrderedRecipes() List~Recipe~
        +setSortStrategy(SortStrategy)
        +transitionRecipe(id, RecipeStatus)
    }

    Recipe <|.. AbstractRecipe
    AbstractRecipe <|-- DessertRecipe
    AbstractRecipe <|-- MainCourseRecipe
    AbstractRecipe <|-- AppetizerRecipe
    AbstractRecipe ..> RecipeStatus

    RecipeFactory <|-- DessertRecipeFactory
    RecipeFactory <|-- MainCourseRecipeFactory
    RecipeFactory <|-- AppetizerRecipeFactory
    DessertRecipeFactory ..> DessertRecipe : creates
    MainCourseRecipeFactory ..> MainCourseRecipe : creates
    AppetizerRecipeFactory ..> AppetizerRecipe : creates

    SortStrategy <|.. UrgentFirstStrategy
    SortStrategy <|.. DeadlineFirstStrategy
    SortStrategy <|.. DessertFirstStrategy
    DessertFirstStrategy ..> DessertRecipe : inspects type

    RecipeManager o--> Recipe : holds many
    RecipeManager o--> SortStrategy : current
    RecipeManager o--> RecipeFactory : registry
```

## What to look at

- **Two pattern hierarchies sit side by side.** `RecipeFactory` and its
  three subclasses are the Factory Method side. `SortStrategy` and its
  three implementations are the Strategy side.
- **`RecipeManager` only points at abstractions.** Its three fields are
  `List<Recipe>`, `SortStrategy`, `Map<String, RecipeFactory>` -- never
  a concrete recipe, factory, or strategy class.
- **`RecipeStatus` enum sits to the side** as a lightweight state
  machine. The dashed arrow from `AbstractRecipe` represents the
  state-transition check inside `setStatus(...)`.
