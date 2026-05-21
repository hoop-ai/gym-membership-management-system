/**
 * Represents an appetizer recipe in the Recipe Management System.
 *
 * <p>An appetizer captures a starter course along with the temperature at
 * which it is served (COLD, HOT, or ROOM-temperature) and a label for the
 * occasion it suits best (e.g. {@code "Cocktail party"}, {@code "Family
 * dinner"}, {@code "Brunch"}). These two extra fields let the cook quickly
 * shortlist appetizers that fit a planned event.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>This is a <strong>Concrete Product</strong> in the <strong>Factory Method</strong>
 * pattern. A recipe factory can instantiate {@code AppetizerRecipe} objects and
 * return them through the {@link Recipe} interface, allowing client code to handle
 * all recipe types uniformly.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong> -- This class manages
 *       only appetizer-specific data (serve temperature, occasion). Shared recipe
 *       behaviour lives in {@link AbstractRecipe}.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong> -- An
 *       {@code AppetizerRecipe} can substitute for any {@link Recipe} reference
 *       without breaking expectations.</li>
 *   <li><strong>Open/Closed Principle (OCP)</strong> -- This concrete product was
 *       introduced without any modifications to {@link AbstractRecipe} or
 *       {@link Recipe}.</li>
 * </ul>
 *
 * @see Recipe
 * @see AbstractRecipe
 * @see DessertRecipe
 * @see MainCourseRecipe
 */
public class AppetizerRecipe extends AbstractRecipe {

    // -----------------------------------------------------------------------
    // Appetizer-specific fields
    // -----------------------------------------------------------------------

    /**
     * The temperature at which the appetizer is served.
     * Expected values: COLD, HOT, or ROOM.
     * This helps plan kitchen flow: COLD appetizers can be made hours in
     * advance, HOT ones need last-minute attention, ROOM-temp dishes are
     * the most forgiving.
     */
    private final String serveTemperature;

    /**
     * A short label describing the occasion the appetizer fits best
     * (for example {@code "Cocktail party"}, {@code "Family dinner"},
     * or {@code "Picnic"}). Knowing the occasion shapes presentation,
     * portion size, and serving vessels.
     */
    private final String occasion;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new appetizer recipe with the specified details.
     *
     * @param title            a short name for the appetizer
     * @param description      a detailed description of the appetizer
     * @param priority         priority from 1 (low) to 5 (high)
     * @param serveTemperature serving temperature: COLD, HOT, or ROOM
     * @param occasion         the occasion the appetizer is suited for
     */
    public AppetizerRecipe(String title, String description, int priority,
                           String serveTemperature, String occasion) {
        super(title, description, priority);
        this.serveTemperature = serveTemperature;
        this.occasion = occasion;
    }

    // -----------------------------------------------------------------------
    // Recipe interface -- type identifier
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return {@code "APPETIZER"} for all appetizer recipe instances
     */
    @Override
    public String getType() {
        return "APPETIZER";
    }

    // -----------------------------------------------------------------------
    // Appetizer-specific accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the serving temperature for this appetizer.
     *
     * @return the serve temperature string (e.g., COLD, HOT, ROOM)
     */
    public String getServeTemperature() {
        return serveTemperature;
    }

    /**
     * Returns the occasion this appetizer is best suited for.
     *
     * @return a textual description of the recommended occasion
     */
    public String getOccasion() {
        return occasion;
    }

    // -----------------------------------------------------------------------
    // toString -- extends the template from AbstractRecipe
    // -----------------------------------------------------------------------

    /**
     * Returns a string representation that includes both the common recipe
     * fields (via {@code super.toString()}) and appetizer-specific details.
     *
     * @return a formatted string with all appetizer recipe information
     */
    @Override
    public String toString() {
        return super.toString()
                + String.format(" | AppetizerDetails[serve=%s, occasion='%s']",
                        serveTemperature, occasion);
    }
}
