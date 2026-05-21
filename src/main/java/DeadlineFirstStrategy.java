import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy that orders recipes by their cook-by date in ascending
 * order. Recipes with the earliest cook-by date come first; recipes with no
 * date assigned are pushed to the end of the list.
 *
 * <p>This is the right ordering when the cook is planning a sequence of
 * upcoming events and wants to see which recipes need attention soonest,
 * regardless of priority or course type.</p>
 *
 * <h2>Role in the Strategy Pattern</h2>
 * <p>This is a <strong>Concrete Strategy</strong>. It implements the
 * {@link SortStrategy} interface with a date-based ordering algorithm. The
 * {@link RecipeManager} (Context) can swap this strategy in at runtime whenever
 * deadline-driven planning is needed.</p>
 *
 * <h2>Null Handling</h2>
 * <p>Recipes whose {@code deadline} is {@code null} are deliberately ordered
 * after all recipes with a non-null deadline. This keeps the most time-sensitive
 * items at the top of the list while still showing undated recipes for context.</p>
 *
 * @see SortStrategy
 * @see UrgentFirstStrategy
 * @see DessertFirstStrategy
 */
public class DeadlineFirstStrategy implements SortStrategy {

    /**
     * Sorts recipes by their cook-by date in ascending order, with nulls last.
     *
     * <p>A new list is created from the input to avoid mutating the original
     * collection.</p>
     *
     * @param recipes the recipes to sort
     * @return a new list ordered from earliest cook-by date to latest, with
     *         dateless recipes at the end
     */
    @Override
    public List<Recipe> sort(List<Recipe> recipes) {
        List<Recipe> copy = new ArrayList<>(recipes);
        Collections.sort(copy, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                LocalDate dateA = a.getDeadline();
                LocalDate dateB = b.getDeadline();

                // Null handling: nulls go to the end
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;

                // Both non-null: ascending date comparison
                return dateA.compareTo(dateB);
            }
        });
        return copy;
    }
}
