/**
 * Concrete Creator for {@link DessertRecipe} instances in the Factory Method
 * pattern.
 *
 * <p>This class extends the abstract {@link RecipeFactory} (the Creator) and
 * provides a concrete implementation of the factory method
 * {@link #createRecipe(String, String, int)}. When invoked, the factory method
 * returns a new {@link DessertRecipe} with sensible default values for
 * dessert-specific fields: a sweetness of {@code "MEDIUM"} and empty
 * preparation notes.</p>
 *
 * <h2>Role in the Factory Method Pattern</h2>
 * <p>This is a <strong>Concrete Creator</strong>. Its sole purpose is to decide
 * <em>which</em> concrete product ({@link DessertRecipe}) to instantiate. By
 * encapsulating the {@code new DessertRecipe(...)} call inside this factory,
 * client code never needs to reference {@code DessertRecipe} directly. Instead,
 * clients program against the {@link RecipeFactory} abstraction and receive a
 * {@link Recipe} interface reference.</p>
 *
 * <p>This class also exposes a more specific method
 * {@link #createDessertRecipe(String, String, int, String, String)}, which gives
 * callers full control over every dessert-specific parameter. This is a common
 * extension in real-world factory implementations: the pattern-mandated factory
 * method provides convenience defaults, while a richer method caters to advanced
 * use cases.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class is
 *       responsible only for creating {@code DessertRecipe} objects. It contains
 *       no business logic for main-course or appetizer recipes.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> Adding this factory
 *       required zero modifications to {@link RecipeFactory} or any existing
 *       factory.</li>
 *   <li><strong>Liskov Substitution Principle (LSP):</strong> An instance of
 *       {@code DessertRecipeFactory} can be used anywhere a {@code RecipeFactory}
 *       is expected without altering program correctness.</li>
 * </ul>
 *
 * <h2>Default Values</h2>
 * <table>
 *   <tr><th>Parameter</th><th>Default</th><th>Rationale</th></tr>
 *   <tr><td>sweetness</td><td>{@code "MEDIUM"}</td><td>Pairs with the widest range of menus until tasted</td></tr>
 *   <tr><td>preparationNotes</td><td>{@code ""}</td><td>Often not known at the draft stage</td></tr>
 * </table>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 *     // Through the abstract factory interface (polymorphic)
 *     RecipeFactory factory = new DessertRecipeFactory();
 *     Recipe dessert = factory.createRecipe("Tiramisu", "Classic Italian dessert", 4);
 *
 *     // Through the specific method (full control)
 *     DessertRecipeFactory dessertFactory = new DessertRecipeFactory();
 *     DessertRecipe detailed = dessertFactory.createDessertRecipe(
 *         "Tiramisu", "Classic Italian dessert", 4,
 *         "HIGH", "Chill 4 hours before serving");
 * }</pre>
 *
 * @see RecipeFactory
 * @see DessertRecipe
 */
public class DessertRecipeFactory extends RecipeFactory {

    /**
     * {@inheritDoc}
     *
     * <p>Creates a new {@link DessertRecipe} with default values:
     * sweetness {@code "MEDIUM"} and empty preparation notes. Use
     * {@link #createDessertRecipe(String, String, int, String, String)} to
     * supply the full set of dessert-specific parameters.</p>
     */
    @Override
    public Recipe createRecipe(String title, String description, int priority) {
        return new DessertRecipe(title, description, priority, "MEDIUM", "");
    }

    /**
     * Creates a fully-specified {@link DessertRecipe} with custom sweetness
     * level and preparation notes.
     *
     * @param title            a short name for the dessert
     * @param description      a detailed description of the dessert
     * @param priority         priority from 1 (low) to 5 (high)
     * @param sweetness        sweetness level: LOW, MEDIUM, HIGH, or EXTREME
     * @param preparationNotes textual notes about advance preparation steps
     * @return a fully-configured {@link DessertRecipe}
     */
    public DessertRecipe createDessertRecipe(String title, String description, int priority,
                                             String sweetness, String preparationNotes) {
        return new DessertRecipe(title, description, priority, sweetness, preparationNotes);
    }
}
