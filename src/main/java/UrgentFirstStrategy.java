import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy that orders recipes by their numeric priority in descending
 * order: recipes the cook has marked priority 5 (highest) appear first, while
 * priority 1 (lowest) recipes appear last.
 *
 * <p>This is the right ordering when the cook is preparing for an imminent
 * event and needs to immediately see which recipes are mission-critical for the
 * menu, regardless of cuisine or cook-by date.</p>
 *
 * <h2>Role in the Strategy Pattern</h2>
 * <p>This is a <strong>Concrete Strategy</strong>. It implements the
 * {@link SortStrategy} interface and provides one specific ordering algorithm:
 * sorting by the numeric priority field in descending order. The
 * {@link RecipeManager} (Context) can swap this strategy in at runtime whenever
 * urgent-first ordering is needed.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class
 *       implements one sorting algorithm and nothing else.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> This class is closed for
 *       modification but the overall system is open for extension. Adding a new
 *       strategy does not require changes here.</li>
 *   <li><strong>Liskov Substitution Principle (LSP):</strong> This class can be
 *       substituted anywhere a {@link SortStrategy} is expected without breaking
 *       the program's correctness.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> This class
 *       depends on the {@link Recipe} abstraction, not on concrete recipe
 *       implementations.</li>
 * </ul>
 *
 * @see SortStrategy
 * @see DeadlineFirstStrategy
 * @see DessertFirstStrategy
 */
public class UrgentFirstStrategy implements SortStrategy {

    /**
     * Sorts recipes by their priority value in descending order (5 to 1).
     *
     * <p>A new list is created from the input to avoid mutating the original
     * collection. The sorting uses {@link Collections#sort} with a
     * {@link Comparator} that compares priorities and reverses the natural order
     * so that the highest priorities come first.</p>
     *
     * @param recipes the recipes to sort
     * @return a new list ordered from highest priority to lowest
     */
    @Override
    public List<Recipe> sort(List<Recipe> recipes) {
        List<Recipe> copy = new ArrayList<>(recipes);
        Collections.sort(copy, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                // Descending: higher priority comes first
                return Integer.compare(b.getPriority(), a.getPriority());
            }
        });
        return copy;
    }
}
