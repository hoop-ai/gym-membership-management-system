/**
 * Concrete Creator for {@link MainCourseRecipe} instances in the Factory Method
 * pattern.
 *
 * <p>This class extends the abstract {@link RecipeFactory} (the Creator) and
 * provides a concrete implementation of {@link #createRecipe(String, String, int)}
 * that returns {@link MainCourseRecipe} objects. When the generic factory method
 * is used, the recipe is configured with default main-course parameters: a
 * cooking time of {@code 45} minutes and a satisfaction rating of {@code 5}.</p>
 *
 * <h2>Role in the Factory Method Pattern</h2>
 * <p>This is a <strong>Concrete Creator</strong>. It encapsulates the
 * construction of a specific {@link Recipe} subtype, keeping instantiation
 * details hidden from client code that works through the {@link RecipeFactory}
 * abstraction.</p>
 *
 * <p>The additional method
 * {@link #createMainCourseRecipe(String, String, int, int, int)} provides full
 * control over main-course-specific parameters. This two-tier API (generic
 * factory method plus specific creation method) is a recurring idiom in factory
 * implementations.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class is
 *       responsible only for creating {@code MainCourseRecipe} instances. Changes
 *       to how main-course recipes are constructed affect only this class.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> This class was introduced
 *       without modifying any existing factory or the abstract
 *       {@code RecipeFactory}.</li>
 *   <li><strong>Liskov Substitution Principle (LSP):</strong> Any code expecting
 *       a {@code RecipeFactory} can receive a {@code MainCourseRecipeFactory} and
 *       function correctly.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> Client code that
 *       receives this factory through a {@code RecipeFactory} reference depends
 *       on the abstraction, not on this concrete class.</li>
 * </ul>
 *
 * <h2>Default Values</h2>
 * <table>
 *   <tr><th>Parameter</th><th>Default</th><th>Rationale</th></tr>
 *   <tr><td>cookingTimeMinutes</td><td>{@code 45}</td><td>A plausible weeknight-dinner baseline</td></tr>
 *   <tr><td>satisfactionRating</td><td>{@code 5}</td><td>Neutral starting score before tasting</td></tr>
 * </table>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 *     // Polymorphic usage: client only knows about RecipeFactory
 *     RecipeFactory factory = new MainCourseRecipeFactory();
 *     Recipe dinner = factory.createRecipe("Roast chicken", "Sunday roast chicken", 3);
 *
 *     // Detailed usage with type-specific parameters
 *     MainCourseRecipeFactory mainCourseFactory = new MainCourseRecipeFactory();
 *     MainCourseRecipe detailed = mainCourseFactory.createMainCourseRecipe(
 *         "Roast chicken", "Sunday roast chicken", 3, 90, 9);
 * }</pre>
 *
 * @see RecipeFactory
 * @see MainCourseRecipe
 */
public class MainCourseRecipeFactory extends RecipeFactory {

    /**
     * {@inheritDoc}
     *
     * <p>Creates a new {@link MainCourseRecipe} with default values:
     * cooking time of {@code 45} minutes and satisfaction rating of {@code 5}.
     * Use {@link #createMainCourseRecipe(String, String, int, int, int)} to
     * supply the full set of main-course-specific parameters.</p>
     */
    @Override
    public Recipe createRecipe(String title, String description, int priority) {
        return new MainCourseRecipe(title, description, priority, 45, 5);
    }

    /**
     * Creates a fully-specified {@link MainCourseRecipe} with custom cooking
     * time and satisfaction rating.
     *
     * @param title              a short name for the dish
     * @param description        a detailed description of the dish
     * @param priority           priority from 1 (low) to 5 (high)
     * @param cookingTimeMinutes total cooking time in minutes (must be non-negative)
     * @param satisfactionRating cook's score from 1 to 10
     * @return a fully-configured {@link MainCourseRecipe}
     */
    public MainCourseRecipe createMainCourseRecipe(String title, String description, int priority,
                                                   int cookingTimeMinutes, int satisfactionRating) {
        return new MainCourseRecipe(title, description, priority, cookingTimeMinutes, satisfactionRating);
    }
}
