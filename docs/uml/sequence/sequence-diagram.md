# Sequence Diagram

Two flows through the system. The PlantUML source is in
[recipe-creation-sequence.puml](recipe-creation-sequence.puml).

```mermaid
sequenceDiagram
    actor User
    participant RM as RecipeManager
    participant Reg as factoryRegistry
    participant DF as DessertRecipeFactory
    participant DR as DessertRecipe
    participant ST as currentStrategy

    Note over User,RM: Create a recipe (Factory Method)

    User->>RM: createRecipe("DESSERT", "Tiramisu", "...", 4)
    RM->>Reg: get("DESSERT")
    Reg-->>RM: DessertRecipeFactory
    RM->>DF: createRecipe("Tiramisu", "...", 4)
    DF->>DR: new DessertRecipe(...)
    DR-->>DF: recipe
    DF-->>RM: Recipe
    RM->>RM: recipes.add(recipe)
    RM-->>User: Recipe

    Note over User,RM: Order the recipe list (Strategy)

    User->>RM: setSortStrategy(new DeadlineFirstStrategy())
    User->>RM: getOrderedRecipes()
    RM->>ST: sort(recipes)
    ST-->>RM: sorted copy
    RM-->>User: List<Recipe>
```

## What to look at

- **Factory Method in action.** Notice that the user only passes the
  string `"DESSERT"`. The manager looks up the right factory and the
  factory does the `new DessertRecipe(...)`. The user never names the
  concrete class.
- **Strategy in action.** Swapping `currentStrategy` is a single
  setter call. The next call to `getOrderedRecipes()` runs through the
  newly-installed strategy with no further plumbing.
