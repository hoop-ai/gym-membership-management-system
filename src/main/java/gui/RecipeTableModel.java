import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Swing table model backed by a {@link RecipeManager}'s ordered recipe list.
 *
 * <p>Re-reads from the manager whenever {@link #refresh()} is called. The
 * current sort strategy held by the manager governs row order, so switching
 * strategies and calling {@code refresh()} makes the Strategy pattern visibly
 * swap before the user.</p>
 */
public class RecipeTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Type", "Title", "Priority", "Status", "Cook by"
    };
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RecipeManager manager;
    private List<Recipe> rows;

    public RecipeTableModel(RecipeManager manager) {
        this.manager = manager;
        this.rows = manager.getOrderedRecipes();
    }

    /** Re-pulls the ordered list from the manager and fires a full refresh. */
    public void refresh() {
        this.rows = manager.getOrderedRecipes();
        fireTableDataChanged();
    }

    /** Returns the recipe at the given model row, or null if out of range. */
    public Recipe getRecipeAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
        Recipe r = rows.get(row);
        switch (col) {
            case 0: return r.getId();
            case 1: return r.getType();
            case 2: return r.getTitle();
            case 3: return r.getPriority();
            case 4: return r.getStatus().toString();
            case 5: return r.getDeadline() == null ? "--" : r.getDeadline().format(DATE_FMT);
            default: return "";
        }
    }
}
