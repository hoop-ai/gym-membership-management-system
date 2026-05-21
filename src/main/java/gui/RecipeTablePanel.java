import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Centre region of the Recipe Manager GUI: a strip of sort/filter dropdowns
 * over a {@link JTable} of recipes.
 *
 * <p>The Sort dropdown calls {@code manager.setSortStrategy(...)} (Strategy
 * pattern, engine-side). The Filter dropdown applies a view-side
 * {@link RowFilter} so the engine retains every recipe; only the table view
 * hides rows that do not match the selected status.</p>
 */
public class RecipeTablePanel extends JPanel {

    private static final Color PANEL_BG          = new Color(0xFFFBF1);
    private static final Color CONTROLS_BG       = new Color(0xF3E3C7);
    private static final Color HEADER_BG         = new Color(0xC65D3A);
    private static final Color HEADER_FG         = new Color(0xFFF8E7);
    private static final Color GRID_LINE         = new Color(0xE6D2A8);
    private static final Color ALT_ROW           = new Color(0xFFF8E5);
    private static final Color LABEL_FG          = new Color(0x4A2C20);

    private final RecipeManager manager;
    private final RecipeTableModel model;
    private final JTable table;
    private final TableRowSorter<RecipeTableModel> rowSorter;
    private final JComboBox<String> sortCombo;
    private final JComboBox<String> filterCombo;
    private Runnable onSelectionChanged = () -> {};

    public RecipeTablePanel(RecipeManager manager) {
        super(new BorderLayout());
        this.manager = manager;
        this.model = new RecipeTableModel(manager);
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(10, 12, 10, 12));

        // -- Controls strip: Sort + Filter -----------------------------------
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controls.setBackground(CONTROLS_BG);
        controls.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID_LINE));

        JLabel sortLbl = makeBoldLabel("Sort by:");
        controls.add(sortLbl);
        sortCombo = new JComboBox<>(new String[]{
                "Urgent First",
                "Deadline First",
                "Dessert First"
        });
        sortCombo.setFont(plain(13));
        sortCombo.addActionListener(e -> applySort());
        controls.add(sortCombo);

        controls.add(Box.createHorizontalStrut(24));
        controls.add(makeBoldLabel("Show only:"));
        filterCombo = new JComboBox<>(new String[]{
                "All statuses", "DRAFT", "TESTING", "APPROVED", "COOKED", "PAUSED"
        });
        filterCombo.setFont(plain(13));
        filterCombo.addActionListener(e -> applyFilter());
        controls.add(filterCombo);

        add(controls, BorderLayout.NORTH);

        // -- Table ------------------------------------------------------------
        table = new JTable(model);
        rowSorter = new TableRowSorter<>(model);
        // Disable per-column click-to-sort: order comes from the engine's Strategy.
        for (int i = 0; i < model.getColumnCount(); i++) rowSorter.setSortable(i, false);
        table.setRowSorter(rowSorter);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(plain(13));
        table.setShowGrid(true);
        table.setGridColor(GRID_LINE);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(0xF7B79C));
        table.setSelectionForeground(LABEL_FG);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        JTableHeader header = table.getTableHeader();
        // Windows L&F ignores setBackground on JTableHeader, so install a
        // custom renderer that paints the warm terracotta background reliably.
        TableCellRenderer headerRenderer = new HeaderRenderer();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setHeaderRenderer(headerRenderer);
        }
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelectionChanged.run();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(PANEL_BG);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, GRID_LINE));
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel makeBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(boldSans(13));
        l.setForeground(LABEL_FG);
        return l;
    }

    private static Font plain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    private static Font boldSans(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    private static Font boldSerif(int size) {
        return new Font(Font.SERIF, Font.BOLD, size);
    }

    private void applySort() {
        String choice = (String) sortCombo.getSelectedItem();
        SortStrategy strategy;
        if ("Deadline First".equals(choice))      strategy = new DeadlineFirstStrategy();
        else if ("Dessert First".equals(choice))  strategy = new DessertFirstStrategy();
        else                                       strategy = new UrgentFirstStrategy();
        manager.setSortStrategy(strategy);
        refreshTable();
    }

    private void applyFilter() {
        String choice = (String) filterCombo.getSelectedItem();
        if (choice == null || "All statuses".equals(choice)) {
            rowSorter.setRowFilter(null);
        } else {
            // Column 4 is "Status" in RecipeTableModel; exact match on the enum name.
            rowSorter.setRowFilter(RowFilter.regexFilter(
                    "^" + java.util.regex.Pattern.quote(choice) + "$", 4));
        }
    }

    /** Rebuilds the table from the engine's current ordered list. */
    public void refreshTable() {
        model.refresh();
        applyFilter();
    }

    public Recipe getSelectedRecipe() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRecipeAt(modelRow);
    }

    public JTable getTable() { return table; }

    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback == null ? () -> {} : callback;
    }

    /**
     * Renders column headers with a high-contrast terracotta background and
     * cream text. Bypasses Windows L&F, which ignores
     * {@link JTableHeader#setBackground(Color)}.
     */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBackground(HEADER_BG);
            setForeground(HEADER_FG);
            setFont(new Font(Font.SERIF, Font.BOLD, 14));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x8B3A20)),
                    new EmptyBorder(6, 10, 6, 10)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focused, int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    /** Renders rows with a warm background colour based on recipe status. */
    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (selected) return c;
            Recipe recipe = model.getRecipeAt(t.convertRowIndexToModel(row));
            if (recipe == null) return c;
            // Warm kitchen palette per status -- distinct from the original Task Manager.
            switch (recipe.getStatus()) {
                case DRAFT:    c.setBackground(ALT_ROW);                  break;  // soft cream
                case TESTING:  c.setBackground(new Color(0xFFD8A8));      break;  // peach
                case APPROVED: c.setBackground(new Color(0xC8E6C9));      break;  // sage
                case COOKED:   c.setBackground(new Color(0x8FBC8F));      break;  // darker sage
                case PAUSED:   c.setBackground(new Color(0xFFAB91));      break;  // coral
                default:       c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
