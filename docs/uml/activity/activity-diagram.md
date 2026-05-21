# Activity Diagram -- Recipe lifecycle

End-to-end workflow of a recipe, from the moment a cook jots it down
to the moment it is served to guests. The PlantUML source is in
[recipe-lifecycle-activity.puml](recipe-lifecycle-activity.puml).

```mermaid
flowchart TD
    A[Cook jots a new recipe] --> B[RecipeManager.createRecipe]
    B --> C[Recipe enters DRAFT]
    C --> D{Ready to trial?}

    D -- no --> E[Transition to PAUSED]
    E --> F[Wait for obstacle to clear]
    F --> G[Transition back to DRAFT]
    G --> D

    D -- yes --> H[Transition to TESTING]
    H --> I{Obstacle?}
    I -- yes --> E
    I -- no --> J{Reliable result?}
    J -- no --> H
    J -- yes --> K[Transition to APPROVED]
    K --> L{Revisions needed?}
    L -- yes --> H
    L -- no --> M[Serve to guests]
    M --> N[Transition to COOKED]
    N --> Z((End))
```

## How the activity diagram maps to the state machine

| Activity step | Concrete method call |
|---|---|
| Jot a recipe | `manager.createRecipe(type, title, description, priority)` |
| Trial it | `manager.transitionRecipe(id, RecipeStatus.TESTING)` |
| Finalise | `manager.transitionRecipe(id, RecipeStatus.APPROVED)` |
| Revise | `manager.transitionRecipe(id, RecipeStatus.TESTING)` |
| Pause | `manager.transitionRecipe(id, RecipeStatus.PAUSED)` |
| Resume | `manager.transitionRecipe(id, RecipeStatus.DRAFT)` |
| Serve and close | `manager.transitionRecipe(id, RecipeStatus.COOKED)` |

Every step is validated by `AbstractRecipe.setStatus(...)`; an
invalid call (e.g. trying to jump from `DRAFT` straight to `COOKED`)
throws `IllegalArgumentException`.
