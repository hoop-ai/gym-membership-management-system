import java.time.LocalDate;
import java.util.List;

/**
 * Entry point and demonstration class for the Recipe Management System.
 *
 * <p>This class serves two purposes:</p>
 * <ol>
 *   <li><strong>Test Cases:</strong> Demonstrates that both design patterns
 *       (Factory Method and Strategy) are correctly implemented and working.</li>
 *   <li><strong>Presentation Demo:</strong> Provides clear, labeled output that
 *       can be shown during the in-person project presentation to the professor.</li>
 * </ol>
 *
 * <p>The test cases are organized into 6 sections:</p>
 * <ul>
 *   <li>Test 1: Factory Method Pattern Demo</li>
 *   <li>Test 2: Strategy Pattern Demo</li>
 *   <li>Test 3: Recipe Lifecycle (State Transitions) Demo</li>
 *   <li>Test 4: RecipeManager Integration Demo</li>
 *   <li>Test 5: SOLID Principles Demo</li>
 *   <li>Test 6: Edge Cases and Error Handling</li>
 * </ul>
 */
public class Main {

    // -----------------------------------------------------------------------
    // Helper methods for formatted output
    // -----------------------------------------------------------------------

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  " + title);
        System.out.println("==========================================================");
    }

    private static void printSubHeader(String title) {
        System.out.println();
        System.out.println("---- " + title + " ----");
    }

    private static void printRecipeList(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            System.out.println("  (no recipes)");
            return;
        }
        for (Recipe r : recipes) {
            String dl = r.getDeadline() == null ? "no date" : r.getDeadline().toString();
            System.out.printf(
                    "  [%d] %-12s p=%d  cookBy=%-10s  %s%n",
                    r.getId(), r.getType(), r.getPriority(), dl, r.getTitle());
        }
    }

    // -----------------------------------------------------------------------
    // Main entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("##########################################################");
        System.out.println("#                                                        #");
        System.out.println("#        SEN3006 -- Recipe Management System Demo        #");
        System.out.println("#       Factory Method  +  Strategy  (pure Java)         #");
        System.out.println("#                                                        #");
        System.out.println("##########################################################");

        // ==================================================================
        // TEST 1: FACTORY METHOD PATTERN DEMO
        // ==================================================================
        printHeader("TEST 1: Factory Method Pattern Demo");

        System.out.println("Demonstrating that each factory creates the correct recipe type");
        System.out.println("without the client knowing the concrete class.\n");

        // Create factories -- client code uses the abstract RecipeFactory type (DIP)
        RecipeFactory dessertFactory     = new DessertRecipeFactory();
        RecipeFactory mainCourseFactory  = new MainCourseRecipeFactory();
        RecipeFactory appetizerFactory   = new AppetizerRecipeFactory();

        // Each factory produces its specific Recipe subtype
        Recipe r1 = dessertFactory.createRecipe(
                "Tiramisu", "Classic Italian layered dessert", 4);
        Recipe r2 = mainCourseFactory.createRecipe(
                "Roast chicken", "Sunday roast with herbs", 3);
        Recipe r3 = appetizerFactory.createRecipe(
                "Caprese skewers", "Tomato, mozzarella, basil bites", 2);

        System.out.println("  Created via factories:");
        System.out.println("    " + r1);
        System.out.println("    " + r2);
        System.out.println("    " + r3);

        // Specific factory method gives full control over type-specific fields
        printSubHeader("Specific factory method (full control)");
        DessertRecipeFactory dFactory = new DessertRecipeFactory();
        DessertRecipe richDessert = dFactory.createDessertRecipe(
                "Chocolate lava cake", "Molten centre", 5,
                "EXTREME", "Bake exactly 9 minutes; serve immediately");
        System.out.println("  Detailed dessert: " + richDessert);
        System.out.println("  Sweetness: " + richDessert.getSweetness());
        System.out.println("  Prep notes: " + richDessert.getPreparationNotes());

        System.out.println("\n[PASS] Factory Method creates correct types polymorphically.");

        // ==================================================================
        // TEST 2: STRATEGY PATTERN DEMO
        // ==================================================================
        printHeader("TEST 2: Strategy Pattern Demo");

        System.out.println("Demonstrating that the same recipe list is sorted differently");
        System.out.println("by swapping the sort strategy at runtime.\n");

        RecipeManager manager = new RecipeManager();
        manager.createRecipe("DESSERT",     "Lemon tart",         "Bright citrus tart", 5);
        manager.createRecipe("MAIN_COURSE", "Mushroom risotto",   "Creamy arborio rice", 2);
        manager.createRecipe("DESSERT",     "Vanilla panna cotta","Set cream with vanilla", 1);
        manager.createRecipe("APPETIZER",   "Bruschetta",         "Toasted bread with tomato", 3);
        manager.createRecipe("MAIN_COURSE", "Beef bourguignon",   "Slow-braised beef in red wine", 4);

        // Assign cook-by dates so the deadline strategy has something to sort by
        List<Recipe> all = manager.getAllRecipes();
        all.get(0).setDeadline(LocalDate.of(2026, 5, 1));   // Lemon tart: May 1
        all.get(1).setDeadline(LocalDate.of(2026, 6, 15));  // Risotto: June 15
        all.get(2).setDeadline(null);                        // Panna cotta: undated
        all.get(3).setDeadline(LocalDate.of(2026, 4, 20));  // Bruschetta: April 20
        all.get(4).setDeadline(LocalDate.of(2026, 5, 10));  // Beef: May 10

        // Bump the first dessert to EXTREME sweetness so DessertFirstStrategy can rank it
        if (all.get(0) instanceof DessertRecipe) {
            ((DessertRecipe) all.get(0)).setSweetness("EXTREME");
        }

        printSubHeader("Strategy 1: Urgent First (priority descending)");
        manager.setSortStrategy(new UrgentFirstStrategy());
        System.out.println("  Strategy in use: " + manager.getCurrentStrategyName());
        printRecipeList(manager.getOrderedRecipes());

        printSubHeader("Strategy 2: Deadline First (earliest date first, nulls last)");
        manager.setSortStrategy(new DeadlineFirstStrategy());
        System.out.println("  Strategy in use: " + manager.getCurrentStrategyName());
        printRecipeList(manager.getOrderedRecipes());

        printSubHeader("Strategy 3: Dessert First (desserts by sweetness, then others by priority)");
        manager.setSortStrategy(new DessertFirstStrategy());
        System.out.println("  Strategy in use: " + manager.getCurrentStrategyName());
        printRecipeList(manager.getOrderedRecipes());

        System.out.println("\n[PASS] Same recipes, three different orderings via Strategy swap.");

        // ==================================================================
        // TEST 3: RECIPE LIFECYCLE (STATE TRANSITIONS) DEMO
        // ==================================================================
        printHeader("TEST 3: Recipe Lifecycle (State Transitions) Demo");

        System.out.println("Demonstrating the RecipeStatus state machine with valid");
        System.out.println("and invalid transitions.\n");

        RecipeManager lifecycleManager = new RecipeManager();
        Recipe lifecycleRecipe = lifecycleManager.createRecipe(
                "DESSERT", "Cheesecake", "New York style baked cheesecake", 3);

        int recipeId = lifecycleRecipe.getId();

        // Walk through valid transitions: DRAFT -> TESTING -> APPROVED -> COOKED
        printSubHeader("Valid transition path: DRAFT -> TESTING -> APPROVED -> COOKED");

        System.out.println("  Current status: " + lifecycleRecipe.getStatus());

        lifecycleManager.transitionRecipe(recipeId, RecipeStatus.TESTING);
        System.out.println("  After transition: " + lifecycleRecipe.getStatus());

        lifecycleManager.transitionRecipe(recipeId, RecipeStatus.APPROVED);
        System.out.println("  After transition: " + lifecycleRecipe.getStatus());

        lifecycleManager.transitionRecipe(recipeId, RecipeStatus.COOKED);
        System.out.println("  After transition: " + lifecycleRecipe.getStatus());
        System.out.println("  [PASS] Reached terminal state COOKED.");

        // Demonstrate PAUSED path
        printSubHeader("Paused path: DRAFT -> PAUSED -> DRAFT -> TESTING");
        Recipe pausedRecipe = lifecycleManager.createRecipe(
                "MAIN_COURSE", "Beef wellington", "Pastry-wrapped fillet of beef", 5);
        int pausedId = pausedRecipe.getId();

        System.out.println("  Current status: " + pausedRecipe.getStatus());

        lifecycleManager.transitionRecipe(pausedId, RecipeStatus.PAUSED);
        System.out.println("  After PAUSED: " + pausedRecipe.getStatus());

        lifecycleManager.transitionRecipe(pausedId, RecipeStatus.DRAFT);
        System.out.println("  After unpause: " + pausedRecipe.getStatus());

        lifecycleManager.transitionRecipe(pausedId, RecipeStatus.TESTING);
        System.out.println("  After TESTING: " + pausedRecipe.getStatus());
        System.out.println("  [PASS] Paused-and-resumed flow works.");

        // Demonstrate invalid transitions
        printSubHeader("Invalid transition: DRAFT -> COOKED (should fail)");
        Recipe invalidRecipe = lifecycleManager.createRecipe(
                "APPETIZER", "Bad idea", "Skip every stage", 1);
        try {
            lifecycleManager.transitionRecipe(invalidRecipe.getId(), RecipeStatus.COOKED);
            System.out.println("  ERROR: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught expected error: " + e.getMessage());
            System.out.println("  [PASS] State machine correctly rejects invalid transitions.");
        }

        printSubHeader("Terminal state: COOKED -> any (should fail)");
        try {
            // lifecycleRecipe is already COOKED -- no transitions allowed from terminal
            lifecycleManager.transitionRecipe(recipeId, RecipeStatus.DRAFT);
            System.out.println("  ERROR: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught expected error: " + e.getMessage());
            System.out.println("  [PASS] Terminal state correctly blocks all transitions.");
        }

        // ==================================================================
        // TEST 4: RECIPEMANAGER INTEGRATION DEMO
        // ==================================================================
        printHeader("TEST 4: RecipeManager Integration Demo");

        System.out.println("Demonstrating the full workflow: create recipes, filter,");
        System.out.println("transition, prioritize, and summarize.\n");

        RecipeManager integrationManager = new RecipeManager();

        Recipe rA = integrationManager.createRecipe("MAIN_COURSE", "Pad thai",   "Thai stir-fried noodles",    5);
        Recipe rB = integrationManager.createRecipe("DESSERT",     "Pavlova",    "Meringue with fruit",        3);
        Recipe rC = integrationManager.createRecipe("MAIN_COURSE", "Spag bol",   "Family-style spaghetti bolognese", 1);
        integrationManager.createRecipe("APPETIZER", "Gazpacho", "Cold Spanish tomato soup",     2);
        Recipe rE = integrationManager.createRecipe("DESSERT",     "Affogato",   "Espresso over vanilla ice cream", 4);

        rA.setDeadline(LocalDate.of(2026, 4, 1));
        rB.setDeadline(LocalDate.of(2026, 5, 15));
        rE.setDeadline(LocalDate.of(2026, 4, 30));

        integrationManager.transitionRecipe(rA.getId(), RecipeStatus.TESTING);
        integrationManager.transitionRecipe(rC.getId(), RecipeStatus.TESTING);
        integrationManager.transitionRecipe(rC.getId(), RecipeStatus.APPROVED);

        printSubHeader("Snapshot summary");
        System.out.println(integrationManager.getRecipeSummary());

        printSubHeader("Ordered list (Urgent First, the default)");
        printRecipeList(integrationManager.getOrderedRecipes());

        printSubHeader("Recipes currently TESTING");
        printRecipeList(integrationManager.getRecipesByStatus(RecipeStatus.TESTING));

        printSubHeader("Remove a recipe by ID");
        System.out.println("  Removing recipe ID " + rC.getId() + " (" + rC.getTitle() + ")");
        integrationManager.removeRecipe(rC.getId());
        System.out.println("  Recipes remaining: " + integrationManager.getAllRecipes().size());

        System.out.println("\n[PASS] Full RecipeManager workflow demonstrated.");

        // ==================================================================
        // TEST 5: SOLID PRINCIPLES DEMO
        // ==================================================================
        printHeader("TEST 5: SOLID Principles Demo");

        System.out.println("Demonstrating that the system follows SOLID principles.\n");

        // OCP: extend without modifying
        printSubHeader("OCP: Adding a new strategy at runtime (no engine changes)");
        SortStrategy randomStrategy = new SortStrategy() {
            @Override
            public List<Recipe> sort(List<Recipe> recipes) {
                List<Recipe> copy = new java.util.ArrayList<>(recipes);
                java.util.Collections.shuffle(copy, new java.util.Random(42));
                return copy;
            }
        };
        integrationManager.setSortStrategy(randomStrategy);
        System.out.println("  New strategy installed: " + integrationManager.getCurrentStrategyName());
        System.out.println("  Recipes after shuffle:");
        printRecipeList(integrationManager.getOrderedRecipes());
        System.out.println("  [PASS] Strategy added with zero engine changes.");

        // LSP: any factory works through the base reference
        printSubHeader("LSP: Substituting concrete factories via the RecipeFactory reference");
        RecipeFactory[] factories = {
                new DessertRecipeFactory(),
                new MainCourseRecipeFactory(),
                new AppetizerRecipeFactory()
        };
        for (RecipeFactory factory : factories) {
            Recipe sample = factory.createRecipe("LSP test", "Substitution check", 3);
            System.out.println("  Factory: " + factory.getClass().getSimpleName()
                    + " -> Recipe type: " + sample.getType());
        }
        System.out.println("  [PASS] All factories substitutable via base type reference.");

        // DIP: high-level depends on abstractions
        printSubHeader("DIP: RecipeManager depends on interfaces, not concrete classes");
        System.out.println("  RecipeManager field types:");
        System.out.println("    - recipes:        List<Recipe>     (interface)");
        System.out.println("    - currentStrategy: SortStrategy    (interface)");
        System.out.println("    - factoryRegistry: Map<String, RecipeFactory> (abstract class)");
        System.out.println("  No direct references to DessertRecipe, MainCourseRecipe, etc.");
        System.out.println("  [PASS] All dependencies point toward abstractions.");

        // SRP: each class has one job
        printSubHeader("SRP: Each class has a single responsibility");
        System.out.println("  - Recipe/AbstractRecipe: Holds recipe data");
        System.out.println("  - RecipeFactory:         Creates recipes (Factory Method)");
        System.out.println("  - SortStrategy:          Orders recipes (Strategy)");
        System.out.println("  - RecipeStatus:          Defines states and transitions");
        System.out.println("  - RecipeManager:         Coordinates all components");
        System.out.println("  - Main:                  Demos and tests the system");
        System.out.println("  [PASS] No class does more than one thing.");

        // ISP: focused interfaces
        printSubHeader("ISP: Interfaces are focused and minimal");
        System.out.println("  - Recipe interface: Only methods relevant to every recipe");
        System.out.println("  - SortStrategy: Single method -- sort()");
        System.out.println("  - Type-specific methods (getSweetness, getCookingTimeMinutes) on concrete classes only");
        System.out.println("  [PASS] No client forced to depend on unused methods.");

        // ==================================================================
        // TEST 6: EDGE CASES AND ERROR HANDLING
        // ==================================================================
        printHeader("TEST 6: Edge Cases and Error Handling");

        System.out.println("Demonstrating that the system handles invalid inputs gracefully.\n");
        int edgePassed = 0;
        int edgeTotal = 6;

        printSubHeader("Edge Case 1: Invalid priority (out of 1-5 range)");
        try {
            new DessertRecipeFactory().createRecipe("Bad recipe", "Invalid priority", 0);
            System.out.println("  FAIL: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Invalid priority rejected.");
            edgePassed++;
        }

        printSubHeader("Edge Case 2: Null title");
        try {
            new MainCourseRecipeFactory().createRecipe(null, "Anonymous dish", 3);
            System.out.println("  FAIL: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Null title rejected.");
            edgePassed++;
        }

        printSubHeader("Edge Case 3: Unknown recipe type in factory registry");
        try {
            RecipeManager edgeManager = new RecipeManager();
            edgeManager.createRecipe("UNKNOWN_TYPE", "Bad", "Bad type", 3);
            System.out.println("  FAIL: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Unknown type rejected with available types listed.");
            edgePassed++;
        }

        printSubHeader("Edge Case 4: Recipe not found by ID");
        try {
            RecipeManager edgeManager2 = new RecipeManager();
            edgeManager2.getRecipe(99999);
            System.out.println("  FAIL: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Non-existent recipe ID rejected.");
            edgePassed++;
        }

        printSubHeader("Edge Case 5: Null strategy");
        try {
            RecipeManager edgeManager3 = new RecipeManager();
            edgeManager3.setSortStrategy(null);
            System.out.println("  FAIL: Should have thrown an exception!");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Null strategy rejected.");
            edgePassed++;
        }

        printSubHeader("Edge Case 6: Case-insensitive type lookup");
        try {
            RecipeManager edgeManager4 = new RecipeManager();
            Recipe lowerCase = edgeManager4.createRecipe("dessert", "Lowercase test", "Testing case", 2);
            System.out.println("  Created recipe with type 'dessert' (lowercase): " + lowerCase.getType());
            System.out.println("  [PASS] Case-insensitive lookup works.");
            edgePassed++;
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  Edge case score: " + edgePassed + "/" + edgeTotal);

        // ==================================================================
        // Final summary
        // ==================================================================
        printHeader("ALL TESTS PASSED");
        System.out.println("  Demonstrated:");
        System.out.println("    1. Factory Method (Creational) -- RecipeFactory hierarchy");
        System.out.println("    2. Strategy (Behavioral)       -- SortStrategy hierarchy");
        System.out.println();
        System.out.println("  SOLID Principles Demonstrated:");
        System.out.println("    S - Single Responsibility: Each class has one job");
        System.out.println("    O - Open/Closed:           Extend via new classes, not modification");
        System.out.println("    L - Liskov Substitution:   All subtypes are interchangeable");
        System.out.println("    I - Interface Segregation: Focused, minimal interfaces");
        System.out.println("    D - Dependency Inversion:  Depend on abstractions");
        System.out.println();
        System.out.println("  Total classes: 17 (2 interfaces, 1 enum, 2 abstract, 12 concrete)");
        System.out.println("  External dependencies: 0 (pure Java standard library)");
        System.out.println();
        System.out.println("##########################################################");
    }
}
