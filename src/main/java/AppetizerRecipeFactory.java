/**
 * Concrete Creator for {@link AppetizerRecipe} instances in the Factory Method
 * pattern.
 *
 * <p>This class extends the abstract {@link RecipeFactory} (the Creator) and
 * provides a concrete implementation of {@link #createRecipe(String, String, int)}
 * that returns {@link AppetizerRecipe} objects. When the generic factory method
 * is used, the recipe is configured with default appetizer parameters: a
 * serving temperature of {@code "ROOM"} and an occasion label of
 * {@code "Family dinner"}.</p>
 *
 * <h2>Role in the Factory Method Pattern</h2>
 * <p>This is a <strong>Concrete Creator</strong>. Together with
 * {@link DessertRecipeFactory} and {@link MainCourseRecipeFactory}, it forms
 * the family of concrete creators that the system supports. Each concrete
 * creator encapsulates the construction of a specific {@link Recipe} subtype,
 * keeping instantiation details hidden from client code that works through the
 * {@link RecipeFactory} abstraction.</p>
 *
 * <p>The additional method
 * {@link #createAppetizerRecipe(String, String, int, String, String)} provides
 * full control over appetizer-specific parameters.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class is
 *       responsible only for creating {@code AppetizerRecipe} instances.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> The system accommodated
 *       this new recipe type purely through extension -- no existing class was
 *       touched.</li>
 *   <li><strong>Liskov Substitution Principle (LSP):</strong> Any code expecting
 *       a {@code RecipeFactory} can receive an {@code AppetizerRecipeFactory}
 *       and function correctly. The {@code Recipe} objects it produces fully
 *       satisfy the {@code Recipe} interface contract.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> Client code that
 *       receives this factory through a {@code RecipeFactory} reference depends
 *       on the abstraction, not on this concrete class.</li>
 * </ul>
 *
 * <h2>Default Values</h2>
 * <table>
 *   <tr><th>Parameter</th><th>Default</th><th>Rationale</th></tr>
 *   <tr><td>serveTemperature</td><td>{@code "ROOM"}</td><td>Most flexible -- works for both standing and sit-down service</td></tr>
 *   <tr><td>occasion</td><td>{@code "Family dinner"}</td><td>The most common everyday context for an appetizer</td></tr>
 * </table>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 *     // Polymorphic usage: client only knows about RecipeFactory
 *     RecipeFactory factory = new AppetizerRecipeFactory();
 *     Recipe starter = factory.createRecipe("Caprese skewers", "Tomato, mozzarella, basil", 2);
 *
 *     // Detailed usage with type-specific parameters
 *     AppetizerRecipeFactory appFactory = new AppetizerRecipeFactory();
 *     AppetizerRecipe detailed = appFactory.createAppetizerRecipe(
 *         "Caprese skewers", "Tomato, mozzarella, basil", 2,
 *         "COLD", "Cocktail party");
 * }</pre>
 *
 * @see RecipeFactory
 * @see AppetizerRecipe
 */
public class AppetizerRecipeFactory extends RecipeFactory {

    /**
     * {@inheritDoc}
     *
     * <p>Creates a new {@link AppetizerRecipe} with default values: serve
     * temperature {@code "ROOM"} and occasion {@code "Family dinner"}. Use
     * {@link #createAppetizerRecipe(String, String, int, String, String)} to
     * supply the full set of appetizer-specific parameters.</p>
     */
    @Override
    public Recipe createRecipe(String title, String description, int priority) {
        return new AppetizerRecipe(title, description, priority, "ROOM", "Family dinner");
    }

    /**
     * Creates a fully-specified {@link AppetizerRecipe} with custom serve
     * temperature and occasion.
     *
     * @param title            a short name for the appetizer
     * @param description      a detailed description of the appetizer
     * @param priority         priority from 1 (low) to 5 (high)
     * @param serveTemperature serving temperature: COLD, HOT, or ROOM
     * @param occasion         the occasion the appetizer is best suited for
     * @return a fully-configured {@link AppetizerRecipe}
     */
    public AppetizerRecipe createAppetizerRecipe(String title, String description, int priority,
                                                 String serveTemperature, String occasion) {
        return new AppetizerRecipe(title, description, priority, serveTemperature, occasion);
    }
}
