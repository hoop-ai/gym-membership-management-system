import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-built scenarios that mirror the data setups in {@link Main}'s test
 * sections.
 *
 * <p>Each method takes a {@link RecipeManager}, clears its recipes, and
 * populates it with the same data the corresponding {@code Main.java} test
 * builds. This lets the professor see the canonical demos inside the GUI on
 * demand without losing the freedom to add custom recipes.</p>
 *
 * <p><strong>Independent of {@link Main}:</strong> these methods do not call
 * {@code Main} or extract code from it. They duplicate the data setup here so
 * {@code Main.java} stays untouched.</p>
 */
public final class DemoScenarios {

    private DemoScenarios() {}

    /** Removes every recipe currently in the manager. */
    public static void clearAll(RecipeManager manager) {
        // Copy IDs first to avoid concurrent-modification issues.
        List<Integer> ids = new ArrayList<>();
        for (Recipe r : manager.getAllRecipes()) ids.add(r.getId());
        for (int id : ids) manager.removeRecipe(id);
    }

    /** Mirrors Main.java Test 2: 5 recipes with cook-by dates + sweetness for sorting demos. */
    public static void loadStrategyDemo(RecipeManager manager) {
        clearAll(manager);
        manager.createRecipe("DESSERT",     "Lemon tart",          "Bright citrus tart",            5);
        manager.createRecipe("MAIN_COURSE", "Mushroom risotto",    "Creamy arborio rice",           2);
        manager.createRecipe("DESSERT",     "Vanilla panna cotta", "Set cream with vanilla",        1);
        manager.createRecipe("APPETIZER",   "Bruschetta",          "Toasted bread with tomato",     3);
        manager.createRecipe("MAIN_COURSE", "Beef bourguignon",    "Slow-braised beef in red wine", 4);

        List<Recipe> all = manager.getAllRecipes();
        all.get(0).setDeadline(LocalDate.of(2026, 5, 1));
        all.get(1).setDeadline(LocalDate.of(2026, 6, 15));
        all.get(2).setDeadline(null);
        all.get(3).setDeadline(LocalDate.of(2026, 4, 20));
        all.get(4).setDeadline(LocalDate.of(2026, 5, 10));

        if (all.get(0) instanceof DessertRecipe) {
            ((DessertRecipe) all.get(0)).setSweetness("EXTREME");
        }
    }

    /** Mirrors Main.java Test 3: a single recipe ready for state-transition demos. */
    public static void loadLifecycleDemo(RecipeManager manager) {
        clearAll(manager);
        manager.createRecipe("DESSERT", "Cheesecake", "New York style baked cheesecake", 3);
    }

    /** Mirrors Main.java Test 4: 5-recipe mix with some transitions pre-applied. */
    public static void loadIntegrationDemo(RecipeManager manager) {
        clearAll(manager);
        Recipe r1 = manager.createRecipe("MAIN_COURSE", "Pad thai",  "Thai stir-fried noodles",           5);
        Recipe r2 = manager.createRecipe("DESSERT",     "Pavlova",   "Meringue with fruit",               3);
        Recipe r3 = manager.createRecipe("MAIN_COURSE", "Spag bol",  "Family-style spaghetti bolognese",  1);
        manager.createRecipe("APPETIZER", "Gazpacho", "Cold Spanish tomato soup", 2);
        Recipe r5 = manager.createRecipe("DESSERT",     "Affogato",  "Espresso over vanilla ice cream",   4);

        r1.setDeadline(LocalDate.of(2026, 4, 1));
        r2.setDeadline(LocalDate.of(2026, 5, 15));
        r5.setDeadline(LocalDate.of(2026, 4, 30));

        manager.transitionRecipe(r1.getId(), RecipeStatus.TESTING);
        manager.transitionRecipe(r3.getId(), RecipeStatus.TESTING);
        manager.transitionRecipe(r3.getId(), RecipeStatus.APPROVED);
    }
}
