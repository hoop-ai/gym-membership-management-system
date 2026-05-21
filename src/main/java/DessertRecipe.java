/**
 * Represents a dessert recipe in the Recipe Management System.
 *
 * <p>A dessert captures a sweet course along with its sweetness level and any
 * notes about advance preparation (chilling, resting dough, candy-thermometer
 * targets, etc.). Bakers need this extra information to plan timing the day
 * before a dinner.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>This is a <strong>Concrete Product</strong> in the <strong>Factory Method</strong>
 * pattern. A recipe factory can instantiate {@code DessertRecipe} objects and
 * return them through the {@link Recipe} interface so client code works with
 * recipes polymorphically without coupling to this concrete class.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong> -- This class is
 *       solely responsible for dessert-specific data (sweetness level, preparation
 *       notes). Common recipe behaviour is inherited from {@link AbstractRecipe}.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong> -- A {@code DessertRecipe}
 *       can replace any {@link Recipe} reference without breaking the program,
 *       because it fully honors the {@link Recipe} contract.</li>
 *   <li><strong>Open/Closed Principle (OCP)</strong> -- Adding this dessert-specific
 *       subclass required no changes to {@link AbstractRecipe} or {@link Recipe}.</li>
 * </ul>
 *
 * @see Recipe
 * @see AbstractRecipe
 * @see MainCourseRecipe
 * @see AppetizerRecipe
 */
public class DessertRecipe extends AbstractRecipe {

    // -----------------------------------------------------------------------
    // Dessert-specific fields
    // -----------------------------------------------------------------------

    /**
     * The sweetness level of the dessert. Expected values: LOW, MEDIUM, HIGH,
     * EXTREME. Higher levels indicate richer, more sugar-forward results and
     * help match the dessert to the rest of the menu.
     */
    private String sweetness;

    /**
     * Free-form preparation notes (chill time, resting, equipment, allergens,
     * etc.). These are critical for planning when a dessert must be started
     * the day before serving.
     */
    private final String preparationNotes;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new dessert recipe with the specified details.
     *
     * @param title            a short name for the dessert
     * @param description      a detailed description of the dessert
     * @param priority         priority from 1 (low) to 5 (high)
     * @param sweetness        sweetness level: LOW, MEDIUM, HIGH, or EXTREME
     * @param preparationNotes textual notes about advance preparation steps
     */
    public DessertRecipe(String title, String description, int priority,
                         String sweetness, String preparationNotes) {
        super(title, description, priority);
        this.sweetness = sweetness;
        this.preparationNotes = preparationNotes;
    }

    // -----------------------------------------------------------------------
    // Recipe interface -- type identifier
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return {@code "DESSERT"} for all dessert recipe instances
     */
    @Override
    public String getType() {
        return "DESSERT";
    }

    // -----------------------------------------------------------------------
    // Dessert-specific accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the sweetness level of this dessert.
     *
     * @return the sweetness string (e.g., LOW, MEDIUM, HIGH, EXTREME)
     */
    public String getSweetness() {
        return sweetness;
    }

    /**
     * Updates the sweetness level of this dessert.
     * Useful when a tasting reveals the dessert needs to be sweeter or
     * less sweet than the original draft.
     *
     * @param sweetness the new sweetness level
     */
    public void setSweetness(String sweetness) {
        this.sweetness = sweetness;
    }

    /**
     * Returns the preparation notes for this dessert.
     *
     * @return a textual description of advance preparation steps
     */
    public String getPreparationNotes() {
        return preparationNotes;
    }

    // -----------------------------------------------------------------------
    // toString -- extends the template from AbstractRecipe
    // -----------------------------------------------------------------------

    /**
     * Returns a string representation that includes both the common recipe
     * fields (via {@code super.toString()}) and dessert-specific details.
     *
     * @return a formatted string with all dessert recipe information
     */
    @Override
    public String toString() {
        return super.toString()
                + String.format(" | DessertDetails[sweetness=%s, prep='%s']",
                        sweetness, preparationNotes);
    }
}
