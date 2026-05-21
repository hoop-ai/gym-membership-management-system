import java.time.LocalDate;

/**
 * Abstract Creator in the Factory Method design pattern.
 *
 * <p>This abstract class defines the <em>factory method</em>
 * {@link #createRecipe(String, String, int)} that concrete subclasses must
 * override to instantiate specific {@link Recipe} implementations. By declaring
 * the factory method as abstract, this class defers the decision of which
 * concrete {@code Recipe} to create to its subclasses, enabling polymorphic
 * object creation.</p>
 *
 * <h2>Role in the Factory Method Pattern</h2>
 * <p>This is the <strong>Creator</strong> (also called Abstract Creator). It
 * declares the factory method signature but does not implement it. The Creator
 * may also contain business logic that relies on the product returned by the
 * factory method, as demonstrated by
 * {@link #createRecipeWithDeadline(String, String, int, LocalDate)}, which
 * internally calls the factory method and then configures the resulting recipe.
 * This secondary method is an example of the <em>Template Method</em>
 * mini-pattern: it defines a skeleton algorithm (create + configure) while
 * letting subclasses supply the "create" step.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Open/Closed Principle (OCP):</strong> Adding a new recipe type
 *       (for example, {@code DrinkRecipe}) requires only a new factory subclass.
 *       This class remains closed for modification but open for extension through
 *       inheritance.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> Client code
 *       depends on this abstraction rather than on concrete factory classes,
 *       allowing the concrete factory to be swapped at runtime without affecting
 *       the client.</li>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class has
 *       one responsibility: defining the contract for recipe creation. It does
 *       not contain recipe-specific logic such as sweetness levels or cooking
 *       times.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 *     // Client code works with the abstract type
 *     RecipeFactory factory = new DessertRecipeFactory();
 *     Recipe recipe = factory.createRecipe("Chocolate mousse", "Classic French dessert", 4);
 *
 *     // Or with a cook-by date
 *     Recipe planned = factory.createRecipeWithDeadline(
 *         "Chocolate mousse", "Classic French dessert", 4, LocalDate.now().plusDays(2));
 * }</pre>
 *
 * @see Recipe
 * @see DessertRecipeFactory
 * @see MainCourseRecipeFactory
 * @see AppetizerRecipeFactory
 */
public abstract class RecipeFactory {

    /**
     * Factory method that concrete subclasses must implement to create a specific
     * type of {@link Recipe}.
     *
     * <p>This is the core of the Factory Method pattern. Each subclass decides
     * which concrete {@code Recipe} implementation to instantiate and return. The
     * caller receives a {@code Recipe} reference and remains decoupled from the
     * concrete class, satisfying the Dependency Inversion Principle.</p>
     *
     * <p>Subclass implementations are expected to choose sensible defaults for
     * their type-specific fields so that the generic three-argument form is
     * always usable.</p>
     *
     * @param title       a short name for the recipe (must not be {@code null} or blank)
     * @param description a detailed description (must not be {@code null})
     * @param priority    priority from 1 (low) to 5 (high)
     * @return a newly-created {@code Recipe} of the subclass's specific type
     * @throws IllegalArgumentException if priority is outside the 1-5 range
     */
    public abstract Recipe createRecipe(String title, String description, int priority);

    /**
     * Convenience template method that creates a recipe and immediately assigns
     * it a cook-by date. Delegates the {@code create} step to the subclass's
     * implementation of {@link #createRecipe(String, String, int)}.
     *
     * <p>This is a small example of the Template Method pattern layered on top
     * of the Factory Method: the algorithm "create then assign a deadline" lives
     * here, but the concrete construction is left to subclasses.</p>
     *
     * @param title       a short name for the recipe
     * @param description a detailed description
     * @param priority    priority from 1 (low) to 5 (high)
     * @param cookBy      the date by which the recipe should be cooked
     * @return a configured {@code Recipe} with the given cook-by date set
     */
    public Recipe createRecipeWithDeadline(String title, String description, int priority,
                                           LocalDate cookBy) {
        Recipe recipe = createRecipe(title, description, priority);
        recipe.setDeadline(cookBy);
        return recipe;
    }
}
