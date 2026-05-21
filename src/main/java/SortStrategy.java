import java.util.List;

/**
 * Strategy interface for the Strategy design pattern applied to recipe ordering.
 *
 * <p>This interface defines a family of ordering algorithms that can be used
 * interchangeably by the {@link RecipeManager} (the Context in Strategy pattern
 * terminology). Each concrete implementation encapsulates a different sorting
 * algorithm, allowing the system to switch between orderings at runtime without
 * altering the {@code RecipeManager}'s code.</p>
 *
 * <h2>Role in the Strategy Pattern</h2>
 * <p>This is the <strong>Strategy</strong> (abstract strategy) role. It declares
 * the common interface that all concrete strategies must implement. The
 * {@code RecipeManager} holds a reference to this interface and delegates
 * sorting to whichever concrete strategy has been injected, decoupling the
 * algorithm selection from the algorithm usage.</p>
 *
 * <h2>SOLID Principles Demonstrated</h2>
 * <ul>
 *   <li><strong>Interface Segregation Principle (ISP):</strong> This interface
 *       contains a single method, {@code sort}, providing a minimal and focused
 *       contract. Clients are not forced to depend on methods they do not use.</li>
 *   <li><strong>Open/Closed Principle (OCP):</strong> New ordering strategies
 *       can be added by creating new implementations of this interface, without
 *       modifying the {@code RecipeManager} or any existing strategy class.</li>
 *   <li><strong>Dependency Inversion Principle (DIP):</strong> The
 *       {@code RecipeManager} (a high-level module) depends on this abstraction
 *       rather than on concrete sorting implementations. Concrete strategies
 *       (low-level modules) also depend on this abstraction, inverting the
 *       traditional dependency direction.</li>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> Each concrete
 *       strategy is solely responsible for one ordering algorithm, and this
 *       interface is solely responsible for defining the sorting contract.</li>
 * </ul>
 *
 * @see UrgentFirstStrategy
 * @see DeadlineFirstStrategy
 * @see DessertFirstStrategy
 */
public interface SortStrategy {

    /**
     * Sorts a list of recipes according to this strategy's ordering algorithm.
     *
     * <p>Implementations <strong>must return a new sorted list</strong> and must
     * <strong>not modify</strong> the original input list. This preserves
     * immutability of the caller's data and avoids unexpected side effects.</p>
     *
     * @param recipes the list of recipes to be sorted; must not be {@code null}
     * @return a new {@link List} containing the same recipes, ordered according
     *         to this strategy's rules
     */
    List<Recipe> sort(List<Recipe> recipes);
}
