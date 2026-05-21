import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Swing table model backed by the live member list inside a {@link Gym}.
 *
 * <p>Re-reads the member list from the gym on every {@link #refresh()} call
 * so that newly-enrolled members, status changes, and removals are picked up
 * immediately by the GUI.</p>
 */
public class MemberTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Name", "Plan", "Tier", "Status", "Renewal", "Channels"
    };

    private final Gym gym;
    private List<Member> rows;

    public MemberTableModel(Gym gym) {
        this.gym = gym;
        this.rows = gym.getAllMembers();
    }

    public void refresh() {
        this.rows = gym.getAllMembers();
        fireTableDataChanged();
    }

    public Member getMemberAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount()                { return rows.size(); }
    @Override public int getColumnCount()             { return COLUMNS.length; }
    @Override public String getColumnName(int col)    { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
        Member m = rows.get(row);
        switch (col) {
            case 0: return m.getId();
            case 1: return m.getName();
            case 2: return m.getPlan().getName();
            case 3: return m.getPlan().getAccessTier();
            case 4: return m.getStatus().toString();
            case 5: return m.getRenewalDate().toString();
            case 6: return channelsString(m);
            default: return "";
        }
    }

    private String channelsString(Member m) {
        if (m.getNotifiers().isEmpty()) return "--";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getNotifiers().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(m.getNotifiers().get(i).getChannel());
        }
        return sb.toString();
    }
}
