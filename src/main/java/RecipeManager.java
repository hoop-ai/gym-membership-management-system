import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central coordinator for the Recipe Management System.
 *
 * <p>This class acts as the <strong>Client</strong> in both the Factory Method
 * and Strategy design patterns:</p>
 * <ul>
 *   <li><strong>Factory Method Client:</strong> Uses a registry of
 *       {@link RecipeFactory} instances to create recipes by type string (e.g.,
 *       {@code "DESSERT"}, {@code "MAIN_COURSE"}, {@code "APPETIZER"}). The
 *       manager never directly instantiates concrete recipe classes -- it
 *       delegates creation to the appropriate factory, staying decoupled from
 *       concrete products.</li>
 *   <li><strong>Strategy Context:</strong> Holds a reference to a
 *       {@link SortStrategy} that can be swapped at runtime. When asked for an
 *       ordered list, it delegates the sorting to whichever strategy is
 *       currently set, allowing the ordering algorithm to change without
 *       modifying this class.</li>
 * </ul>
 *
 * <h2>Additional Pattern: Service Locator (Simplified)</h2>
 * <p>The internal {@code factoryRegistry} map acts as a simplified Service
 * Locator, mapping type strings to factory instances. This adds a creative
 * twist beyond the standard Factory Method pattern -- clients can request a
 * recipe by string type rather than by knowing the factory class.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility (SRP):</strong> Coordinates factories
 *       and strategies. Does not contain creation logic (factories do that) or
 *       sorting logic (strategies do that).</li>
 *   <li><strong>Open/Closed (OCP):</strong> New recipe types or strategies can
 *       be added by registering new factories or setting a new strategy -- no
 *       modification to this class is needed.</li>
 *   <li><strong>Dependency Inversion (DIP):</strong> Depends on abstractions
 *       ({@link Recipe}, {@link RecipeFactory}, {@link SortStrategy}), not on
 *       concrete classes like {@link DessertRecipe} or
 *       {@link UrgentFirstStrategy}.</li>
 * </ul>
 *
 * @see RecipeFactory
 * @see SortStrategy
 * @see Recipe
 */
public class RecipeManager {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    /** The collection of all recipes managed by this instance. */
    private final List<Recipe> recipes;

    /** The currently active sort strategy. Never {@code null}. */
    private SortStrategy currentStrategy;

    /**
     * Maps type strings (e.g., {@code "DESSERT"}) to their factory instances.
     * Acts as a simplified Service Locator.
     */
    private final Map<String, RecipeFactory> factoryRegistry;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new {@code RecipeManager} with all built-in factories
     * registered and the {@link UrgentFirstStrategy} as the default sort
     * strategy.
     *
     * <p>The constructor pre-registers factories for DESSERT, MAIN_COURSE, and
     * APPETIZER recipe types. Additional factories can be registered later via
     * {@link #registerFactory(String, RecipeFactory)}.</p>
     */
    public RecipeManager() {
        this.recipes = new ArrayList<>();
        this.currentStrategy = new UrgentFirstStrategy();
        this.factoryRegistry = new HashMap<>();
        registerFactory("DESSERT",     new DessertRecipeFactory());
        registerFactory("MAIN_COURSE", new MainCourseRecipeFactory());
        registerFactory("APPETIZER",   new AppetizerRecipeFactory());
    }

    // -----------------------------------------------------------------------
    // Factory registry
    // -----------------------------------------------------------------------

    /**
     * Registers a factory under a type key.
     *
     * <p>Keys are stored as upper-case so lookups can be made case-insensitive.
     * Registering a new type does not require modifying any existing class --
     * an example of the Open/Closed Principle in action.</p>
     *
     * @param type    the type key (case-insensitive)
     * @param factory the factory to register
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public void registerFactory(String type, RecipeFactory factory) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type must not be null or blank.");
        }
        if (factory == null) {
            throw new IllegalArgumentException("Factory must not be null.");
        }
        factoryRegistry.put(type.toUpperCase(), factory);
    }

    // -----------------------------------------------------------------------
    // Recipe creation (Factory Method client)
    // -----------------------------------------------------------------------

    /**
     * Creates a new recipe of the requested type via the registered factory.
     *
     * @param type        the type key (case-insensitive); must be registered
     * @param title       a short name for the recipe
     * @param description a detailed description
     * @param priority    priority from 1 (low) to 5 (high)
     * @return the newly-created {@link Recipe}, also added to this manager
     * @throws IllegalArgumentException if no factory is registered for the type
     */
    public Recipe createRecipe(String type, String title, String description, int priority) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null. Available: " + factoryRegistry.keySet());
        }
        RecipeFactory factory = factoryRegistry.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException(
                    "No factory registered for type '" + type
                            + "'. Available types: " + factoryRegistry.keySet());
        }
        Recipe recipe = factory.createRecipe(title, description, priority);
        recipes.add(recipe);
        return recipe;
    }

    /**
     * Returns the set of registered recipe type keys.
     *
     * @return a defensive copy of the registered type keys
     */
    public java.util.Set<String> getRegisteredTypes() {
        return new java.util.HashSet<>(factoryRegistry.keySet());
    }

    // -----------------------------------------------------------------------
    // Recipe management
    // -----------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of all recipes managed by this instance,
     * in the order they were added.
     *
     * @return an unmodifiable list of recipes
     */
    public List<Recipe> getAllRecipes() {
        return Collections.unmodifiableList(recipes);
    }

    /**
     * Returns the recipe with the given ID, or throws if not found.
     *
     * @param id the recipe ID
     * @return the matching {@link Recipe}
     * @throws IllegalArgumentException if no recipe with the given ID exists
     */
    public Recipe getRecipe(int id) {
        for (Recipe r : recipes) {
            if (r.getId() == id) return r;
        }
        throw new IllegalArgumentException("No recipe found with ID: " + id);
    }

    /**
     * Removes the recipe with the given ID.
     *
     * @param id the recipe ID
     * @throws IllegalArgumentException if no recipe with the given ID exists
     */
    public void removeRecipe(int id) {
        Recipe r = getRecipe(id);
        recipes.remove(r);
    }

    /**
     * Returns all recipes currently in the given status.
     *
     * @param status the status to filter by
     * @return a new list of matching recipes (possibly empty)
     */
    public List<Recipe> getRecipesByStatus(RecipeStatus status) {
        List<Recipe> matches = new ArrayList<>();
        for (Recipe r : recipes) {
            if (r.getStatus() == status) matches.add(r);
        }
        return matches;
    }

    // -----------------------------------------------------------------------
    // State transition management
    // -----------------------------------------------------------------------

    /**
     * Transitions a recipe to a new status, validating against the state
     * machine.
     *
     * <p>If the transition is not allowed by
     * {@link RecipeStatus#canTransitionTo(RecipeStatus)}, an
     * {@link IllegalArgumentException} is thrown. This prevents invalid state
     * changes and ensures data integrity.</p>
     *
     * @param recipeId  the ID of the recipe to transition
     * @param newStatus the target status
     * @throws IllegalArgumentException if the recipe is not found or the
     *                                  transition is invalid
     */
    public void transitionRecipe(int recipeId, RecipeStatus newStatus) {
        Recipe recipe = getRecipe(recipeId);
        // Recipe validates the transition internally
        recipe.setStatus(newStatus);
    }

    // -----------------------------------------------------------------------
    // Strategy Pattern integration
    // -----------------------------------------------------------------------

    /**
     * Swaps the current sort strategy at runtime.
     *
     * <p>This is the core of the Strategy pattern: the algorithm for ordering
     * recipes is encapsulated in strategy objects that can be exchanged freely.
     * After calling this method, all subsequent calls to
     * {@link #getOrderedRecipes()} will use the newly-installed strategy.</p>
     *
     * @param strategy the strategy to use; must not be {@code null}
     * @throws IllegalArgumentException if {@code strategy} is {@code null}
     */
    public void setSortStrategy(SortStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null.");
        }
        this.currentStrategy = strategy;
    }

    /**
     * Returns the current strategy name for display purposes.
     *
     * @return the simple class name of the current strategy
     */
    public String getCurrentStrategyName() {
        return currentStrategy.getClass().getSimpleName();
    }

    /**
     * Returns all recipes sorted according to the current strategy.
     *
     * <p>Delegates to {@link SortStrategy#sort(List)}, which returns a new
     * sorted list without modifying the original collection.</p>
     *
     * @return a new list of recipes ordered by the current strategy
     */
    public List<Recipe> getOrderedRecipes() {
        return currentStrategy.sort(recipes);
    }

    // -----------------------------------------------------------------------
    // Reporting
    // -----------------------------------------------------------------------

    /**
     * Returns a short human-readable summary of the manager's current state.
     *
     * @return a formatted summary including counts per status and the active
     *         strategy
     */
    public String getRecipeSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total recipes: %d\n", recipes.size()));

        for (RecipeStatus status : RecipeStatus.values()) {
            int count = 0;
            for (Recipe r : recipes) {
                if (r.getStatus() == status) count++;
            }
            if (count > 0) {
                sb.append(String.format("  %s: %d\n", status, count));
            }
        }

        sb.append(String.format("  Current strategy: %s\n", getCurrentStrategyName()));
        return sb.toString();
    }
}
