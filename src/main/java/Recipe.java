import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Defines the contract for every recipe type in the Recipe Management System.
 *
 * <h3>Design Pattern Role</h3>
 * <p>This is the <strong>Product</strong> interface in the
 * <strong>Factory Method</strong> pattern. Concrete products such as
 * {@link DessertRecipe}, {@link MainCourseRecipe}, and {@link AppetizerRecipe}
 * implement this interface (via {@link AbstractRecipe}). A recipe factory can
 * create any of these concrete products and return them through this common
 * interface so client code works with recipes polymorphically without knowing
 * the concrete type.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Interface Segregation Principle (ISP)</strong> -- Every method
 *       declared here is relevant to <em>all</em> recipe types. There are no
 *       "fat interface" methods that only some implementations need. Type-specific
 *       behaviour (e.g., sweetness for desserts, cooking time for main courses) is
 *       kept on the concrete classes, not forced into this interface.</li>
 *   <li><strong>Dependency Inversion Principle (DIP)</strong> -- High-level modules
 *       (managers, UI layers, sort strategies) depend on this abstraction rather
 *       than on concrete recipe classes, making the system easier to extend and test.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong> -- Any concrete Recipe
 *       implementation can be used wherever a {@code Recipe} reference is expected
 *       without altering the correctness of the program.</li>
 * </ul>
 *
 * @see AbstractRecipe
 * @see DessertRecipe
 * @see MainCourseRecipe
 * @see AppetizerRecipe
 * @see RecipeStatus
 */
public interface Recipe {

    /**
     * Returns the unique identifier for this recipe.
     * IDs are auto-generated and monotonically increasing.
     *
     * @return the recipe ID
     */
    int getId();

    /**
     * Returns the short, human-readable title of this recipe.
     *
     * @return the recipe title, never {@code null}
     */
    String getTitle();

    /**
     * Returns a detailed description of what this recipe involves.
     *
     * @return the recipe description, never {@code null}
     */
    String getDescription();

    /**
     * Returns the current lifecycle status of this recipe.
     *
     * @return the current {@link RecipeStatus}
     */
    RecipeStatus getStatus();

    /**
     * Updates the lifecycle status of this recipe.
     * Implementations should validate that the transition is allowed
     * by the {@link RecipeStatus} state machine.
     *
     * @param status the new status to set
     * @throws IllegalArgumentException if the transition is not permitted
     */
    void setStatus(RecipeStatus status);

    /**
     * Returns the priority level of this recipe.
     * Priority ranges from 1 (lowest, "make whenever") to 5 (highest,
     * "must cook for a planned occasion").
     *
     * @return an integer between 1 and 5 inclusive
     */
    int getPriority();

    /**
     * Returns the deadline (cook-by date) for this recipe, or {@code null}
     * if no specific date is planned.
     *
     * @return the cook-by date, or {@code null}
     */
    LocalDate getDeadline();

    /**
     * Sets the cook-by date for this recipe. May be {@code null} to clear
     * a previously assigned date.
     *
     * @param deadline the new cook-by date, or {@code null}
     */
    void setDeadline(LocalDate deadline);

    /**
     * Returns the timestamp captured when this recipe was created.
     *
     * @return the creation timestamp
     */
    LocalDateTime getCreatedAt();

    /**
     * Returns a short, uppercase identifier of this recipe's type
     * (for example {@code "DESSERT"}, {@code "MAIN_COURSE"}, or
     * {@code "APPETIZER"}). Each concrete recipe class supplies its own value.
     *
     * @return the recipe type identifier
     */
    String getType();
}
