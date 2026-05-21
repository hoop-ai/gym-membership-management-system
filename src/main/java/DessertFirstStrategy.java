import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy that surfaces every {@link DessertRecipe} at the top of the
 * list -- ordered among themselves by sweetness rank (EXTREME &gt; HIGH &gt;
 * MEDIUM &gt; LOW) -- and then orders all remaining recipes (main courses,
 * appetizers, anything new) by their numeric priority in descending order.
 *
 * <p>The sort proceeds in two phases:</p>
 * <ol>
 *   <li>All desserts come first, sorted by their sweetness rank (more
 *       sugar-forward items first).</li>
 *   <li>Non-dessert recipes follow, sorted by priority descending.</li>
 * </ol>
 *
 * <p>This guarantees that even a LOW-sweetness dessert appears before a
 * priority-5 main course, reflecting the cook's reality that desserts often
 * dominate planning -- they need the most lead time (chilling, resting,
 * decoration) and are typically the dish guests remember most.</p>
 *
 * <h2>Role in the Strategy Pattern</h2>
 * <p>This is a <strong>Concrete Strategy</strong>. It implements the
 * {@link SortStrategy} interface with the most specialized sorting algorithm of
 * the three strategies. The {@link RecipeManager} (Context) can inject this
 * strategy when the team transitions into a baking-heavy planning phase, such
 * as preparing for a birthday or holiday meal.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> This class
 *       implements exactly one sorting algorithm -- desserts-first by sweetness,
 *       then others by priority.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> If new sweetness levels
 *       are introduced, only the {@code getSweetnessRank} helper needs updating.
 *       Other strategies and the {@code RecipeManager} remain untouched.</li>
 *   <li><strong>Liskov Substitution Principle (LSP):</strong> This class honors
 *       the {@link SortStrategy} contract -- it returns a new sorted list and
 *       does not modify the input. It can replace any other strategy without
 *       side effects.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> While this
 *       strategy uses {@code instanceof DessertRecipe} to detect desserts (a
 *       minor coupling), it still operates through the {@link Recipe} interface
 *       for all non-dessert operations, keeping the overall dependency on
 *       abstractions.</li>
 * </ul>
 *
 * @see SortStrategy
 * @see UrgentFirstStrategy
 * @see DeadlineFirstStrategy
 * @see DessertRecipe
 */
public class DessertFirstStrategy implements SortStrategy {

    /**
     * Sorts recipes with all desserts first (ranked by sweetness), then all
     * other recipes in descending priority order.
     *
     * <p>A new list is created from the input to avoid mutating the original
     * collection.</p>
     *
     * @param recipes the recipes to sort
     * @return a new list with desserts first, then others by priority
     */
    @Override
    public List<Recipe> sort(List<Recipe> recipes) {
        List<Recipe> copy = new ArrayList<>(recipes);
        Collections.sort(copy, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                boolean aIsDessert = a instanceof DessertRecipe;
                boolean bIsDessert = b instanceof DessertRecipe;

                // Phase 1: desserts always come before non-desserts
                if (aIsDessert && !bIsDessert) return -1;
                if (!aIsDessert && bIsDessert) return 1;

                // Phase 2a: both desserts -- compare by sweetness rank (higher first)
                if (aIsDessert && bIsDessert) {
                    int rankA = getSweetnessRank(((DessertRecipe) a).getSweetness());
                    int rankB = getSweetnessRank(((DessertRecipe) b).getSweetness());
                    return Integer.compare(rankB, rankA);
                }

                // Phase 2b: neither is a dessert -- compare by priority descending
                return Integer.compare(b.getPriority(), a.getPriority());
            }
        });
        return copy;
    }

    /**
     * Maps a sweetness label to a numeric rank so the sweetest desserts can be
     * promoted to the top.
     *
     * @param sweetness the sweetness label (case-insensitive)
     * @return a numeric rank: EXTREME=4, HIGH=3, MEDIUM=2, LOW=1, unknown=0
     */
    private int getSweetnessRank(String sweetness) {
        if (sweetness == null) return 0;
        switch (sweetness.toUpperCase()) {
            case "EXTREME": return 4;
            case "HIGH":    return 3;
            case "MEDIUM":  return 2;
            case "LOW":     return 1;
            default:        return 0;
        }
    }
}
