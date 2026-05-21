import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console application for the Recipe Management System.
 *
 * <p>This class provides a menu-driven interface that lets a cook interact with
 * the system in real time: create recipes, view them, change their lifecycle
 * status, switch sort strategies, and read summaries.</p>
 *
 * <p><strong>How to run:</strong></p>
 * <pre>
 *   javac -d bin src/main/java/*.java
 *   java -cp bin RecipeManagementApp
 * </pre>
 *
 * <p><strong>Design Pattern Usage:</strong></p>
 * <ul>
 *   <li><strong>Factory Method:</strong> When you create a recipe, the system
 *       uses the registered factory for that type (DESSERT / MAIN_COURSE /
 *       APPETIZER) to construct the correct recipe object -- you never need to
 *       know which class is used.</li>
 *   <li><strong>Strategy:</strong> When you change the sort strategy, the same
 *       list of recipes is re-sorted using a different algorithm -- no code
 *       changes, just swap the strategy object at runtime.</li>
 * </ul>
 */
public class RecipeManagementApp {

    private final RecipeManager manager;
    private final Scanner scanner;
    private boolean running;

    /** Creates a new application instance with a fresh {@link RecipeManager}. */
    public RecipeManagementApp() {
        this.manager = new RecipeManager();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public static void main(String[] args) {
        RecipeManagementApp app = new RecipeManagementApp();
        app.run();
    }

    /** Runs the main menu loop until the user chooses to exit. */
    public void run() {
        printWelcome();

        while (running) {
            printMenu();
            int choice = readInt("Your choice: ");

            switch (choice) {
                case 1:  createRecipe(); break;
                case 2:  viewAllRecipes(); break;
                case 3:  viewOrderedRecipes(); break;
                case 4:  changeSortStrategy(); break;
                case 5:  changeRecipeStatus(); break;
                case 6:  viewRecipesByStatus(); break;
                case 7:  viewRecipeDetails(); break;
                case 8:  showSummary(); break;
                case 9:  removeRecipe(); break;
                case 0:  running = false; break;
                default: System.out.println("Invalid choice. Try again."); break;
            }
        }

        System.out.println();
        System.out.println("Goodbye -- happy cooking!");
        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Menu rendering
    // -----------------------------------------------------------------------

    private void printWelcome() {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  SEN3006 -- Recipe Management System (Interactive App)");
        System.out.println("==========================================================");
        System.out.println("  Patterns: Factory Method + Strategy");
        System.out.println("  Engine:   pure Java, zero external dependencies");
        System.out.println();
        System.out.println("  Pick an option from the menu. Most prompts include");
        System.out.println("  hints; press Enter at any prompt for guidance.");
        System.out.println("==========================================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("  MAIN MENU");
        System.out.println("----------------------------------------------------------");
        System.out.println("  1. Create a new recipe");
        System.out.println("  2. View all recipes (insertion order)");
        System.out.println("  3. View recipes (sorted by current strategy)");
        System.out.println("  4. Change sort strategy");
        System.out.println("  5. Change a recipe's lifecycle status");
        System.out.println("  6. View recipes filtered by status");
        System.out.println("  7. View a recipe in detail");
        System.out.println("  8. Show summary");
        System.out.println("  9. Remove a recipe");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    // Menu actions
    // -----------------------------------------------------------------------

    private void createRecipe() {
        System.out.println("\nCreate a new recipe");
        System.out.println("Types: " + manager.getRegisteredTypes());
        String type = readLine("Type: ").toUpperCase();
        String title = readLine("Title: ");
        String description = readLine("Description: ");
        int priority = readIntInRange("Priority (1-5): ", 1, 5);

        try {
            Recipe r = manager.createRecipe(type, title, description, priority);

            // Optional cook-by date
            String dateStr = readLine("Cook-by date (YYYY-MM-DD, blank for none): ");
            if (!dateStr.isBlank()) {
                try {
                    r.setDeadline(LocalDate.parse(dateStr));
                } catch (DateTimeParseException e) {
                    System.out.println("  Invalid date format, leaving cook-by date empty.");
                }
            }
            System.out.println("Created: " + r);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not create recipe: " + e.getMessage());
        }
    }

    private void viewAllRecipes() {
        System.out.println("\nAll recipes (insertion order)");
        printRecipes(manager.getAllRecipes());
    }

    private void viewOrderedRecipes() {
        System.out.println("\nOrdered recipes (strategy = " + manager.getCurrentStrategyName() + ")");
        printRecipes(manager.getOrderedRecipes());
    }

    private void changeSortStrategy() {
        System.out.println("\nCurrent strategy: " + manager.getCurrentStrategyName());
        System.out.println("  1. UrgentFirstStrategy   (priority descending)");
        System.out.println("  2. DeadlineFirstStrategy (earliest cook-by first)");
        System.out.println("  3. DessertFirstStrategy  (desserts by sweetness, then others by priority)");
        int choice = readInt("Choose strategy: ");

        SortStrategy strategy;
        switch (choice) {
            case 1: strategy = new UrgentFirstStrategy(); break;
            case 2: strategy = new DeadlineFirstStrategy(); break;
            case 3: strategy = new DessertFirstStrategy(); break;
            default:
                System.out.println("Invalid choice, no change made.");
                return;
        }
        manager.setSortStrategy(strategy);
        System.out.println("Strategy now: " + manager.getCurrentStrategyName());
    }

    private void changeRecipeStatus() {
        int id = readInt("Recipe ID: ");
        Recipe r;
        try {
            r = manager.getRecipe(id);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not find recipe: " + e.getMessage());
            return;
        }
        System.out.println("Current status: " + r.getStatus());
        System.out.println("Statuses: DRAFT, TESTING, APPROVED, COOKED, PAUSED");
        String newStatus = readLine("New status: ").toUpperCase();
        try {
            manager.transitionRecipe(id, RecipeStatus.valueOf(newStatus));
            System.out.println("New status: " + r.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not transition: " + e.getMessage());
        }
    }

    private void viewRecipesByStatus() {
        System.out.println("\nStatuses: DRAFT, TESTING, APPROVED, COOKED, PAUSED");
        String status = readLine("Filter by status: ").toUpperCase();
        try {
            printRecipes(manager.getRecipesByStatus(RecipeStatus.valueOf(status)));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown status: " + status);
        }
    }

    private void viewRecipeDetails() {
        int id = readInt("Recipe ID: ");
        try {
            Recipe r = manager.getRecipe(id);
            System.out.println("\n" + r);
            System.out.println("Description: " + r.getDescription());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not find recipe: " + e.getMessage());
        }
    }

    private void showSummary() {
        System.out.println();
        System.out.println(manager.getRecipeSummary());
    }

    private void removeRecipe() {
        int id = readInt("Recipe ID to remove: ");
        try {
            manager.removeRecipe(id);
            System.out.println("Removed recipe ID " + id + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not remove: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Output helpers
    // -----------------------------------------------------------------------

    private void printRecipes(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            System.out.println("  (no recipes)");
            return;
        }
        for (Recipe r : recipes) {
            String dl = r.getDeadline() == null ? "no date" : r.getDeadline().toString();
            System.out.printf(
                    "  [%d] %-12s p=%d  status=%-8s  cookBy=%-10s  %s%n",
                    r.getId(), r.getType(), r.getPriority(), r.getStatus(), dl, r.getTitle());
        }
    }

    // -----------------------------------------------------------------------
    // Input helpers
    // -----------------------------------------------------------------------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v >= min && v <= max) return v;
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}
