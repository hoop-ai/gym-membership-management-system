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
 * Centre region of the Gym Manager GUI: a member table with a status filter
 * sitting above it. Rows are colour-coded by membership status so the user
 * can see at a glance which members are active, frozen, expiring, etc.
 *
 * <p>Custom header rendering and explicit opaque cell painting are used to
 * bypass Windows L&F, which otherwise ignores background-colour setters.</p>
 */
public class MemberTablePanel extends JPanel {

    // -- Athletic-blue palette ---------------------------------------------
    private static final Color PANEL_BG    = new Color(0xF4F8FB);
    private static final Color CONTROLS_BG = new Color(0xDCE9F2);
    private static final Color HEADER_BG   = new Color(0x1E4D6B);  // deep teal
    private static final Color HEADER_FG   = new Color(0xFFFFFF);
    private static final Color GRID_LINE   = new Color(0xB6CAD9);
    private static final Color LABEL_FG    = new Color(0x14283B);

    private final Gym gym;
    private final MemberTableModel model;
    private final JTable table;
    private final TableRowSorter<MemberTableModel> rowSorter;
    private final JComboBox<String> filterCombo;
    private Runnable onSelectionChanged = () -> {};

    public MemberTablePanel(Gym gym) {
        super(new BorderLayout());
        this.gym = gym;
        this.model = new MemberTableModel(gym);
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(10, 12, 10, 12));

        // -- Status filter strip ---------------------------------------------
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controls.setBackground(CONTROLS_BG);
        controls.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID_LINE));

        JLabel filterLbl = makeBoldLabel("Show only:");
        controls.add(filterLbl);
        filterCombo = new JComboBox<>(new String[]{
                "All statuses", "PENDING", "ACTIVE", "EXPIRING", "EXPIRED", "FROZEN", "CANCELLED"
        });
        filterCombo.setFont(plain(13));
        filterCombo.addActionListener(e -> applyFilter());
        controls.add(filterCombo);

        add(controls, BorderLayout.NORTH);

        // -- Table -----------------------------------------------------------
        table = new JTable(model);
        rowSorter = new TableRowSorter<>(model);
        for (int i = 0; i < model.getColumnCount(); i++) rowSorter.setSortable(i, true);
        table.setRowSorter(rowSorter);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(plain(13));
        table.setShowGrid(true);
        table.setGridColor(GRID_LINE);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(0xA8C8E0));
        table.setSelectionForeground(LABEL_FG);
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());

        JTableHeader header = table.getTableHeader();
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

    private void applyFilter() {
        String choice = (String) filterCombo.getSelectedItem();
        if (choice == null || "All statuses".equals(choice)) {
            rowSorter.setRowFilter(null);
        } else {
            // Column 4 is "Status"
            rowSorter.setRowFilter(RowFilter.regexFilter(
                    "^" + java.util.regex.Pattern.quote(choice) + "$", 4));
        }
    }

    public void refreshTable() {
        model.refresh();
        applyFilter();
    }

    public Member getSelectedMember() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getMemberAt(modelRow);
    }

    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback == null ? () -> {} : callback;
    }

    private JLabel makeBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(boldSans(13));
        l.setForeground(LABEL_FG);
        return l;
    }

    private static Font plain(int size)     { return new Font(Font.SANS_SERIF, Font.PLAIN, size); }
    private static Font boldSans(int size)  { return new Font(Font.SANS_SERIF, Font.BOLD, size); }
    private static Font boldSerif(int size) { return new Font(Font.SERIF, Font.BOLD, size); }

    /** Cream-text-on-teal header renderer. Bypasses Windows L&F. */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBackground(HEADER_BG);
            setForeground(HEADER_FG);
            setFont(new Font(Font.SERIF, Font.BOLD, 14));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x123347)),
                    new EmptyBorder(6, 10, 6, 10)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focused, int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    /** Colours rows by membership status. */
    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (selected) return c;
            Member m = model.getMemberAt(t.convertRowIndexToModel(row));
            if (m == null) return c;
            switch (m.getStatus()) {
                case PENDING:   c.setBackground(new Color(0xF6E9C6)); break; // sand
                case ACTIVE:    c.setBackground(new Color(0xCDE8C5)); break; // mint
                case EXPIRING:  c.setBackground(new Color(0xFFD8A8)); break; // amber
                case EXPIRED:   c.setBackground(new Color(0xE2BFBF)); break; // dusty red
                case FROZEN:    c.setBackground(new Color(0xBFDDE8)); break; // ice blue
                case CANCELLED: c.setBackground(new Color(0xCCCCCC)); break; // grey
                default:        c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
