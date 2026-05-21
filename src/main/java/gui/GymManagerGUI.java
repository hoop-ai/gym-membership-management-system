import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;

/**
 * Swing entry point for the Gym Membership Management System.
 *
 * <p>Third entry point alongside {@link Main} (scripted demo) and
 * {@link GymManagementApp} (interactive console). The three drivers share
 * one engine -- the same {@link Gym} class, the same Builder, the same
 * Observer fabric.</p>
 *
 * <p>The layout deliberately mirrors the way a gym front-desk system is
 * usually arranged: a member table dominates the left side, an enrolment
 * form sits on the right, lifecycle and notification action buttons hug
 * the bottom, and an event log strips along the very bottom prints every
 * notification the {@link Gym} has published since startup.</p>
 *
 * <p><strong>No package declaration.</strong> Default-package classes
 * cannot be imported from a named package on Java 8, and the engine lives
 * in the default package. The {@code gui/} folder is organisational only.</p>
 */
public class GymManagerGUI extends JFrame {

    // -- Athletic-blue palette ---------------------------------------------
    private static final Color WINDOW_BG  = new Color(0xF4F8FB);
    private static final Color BANNER_BG  = new Color(0x123347);
    private static final Color BANNER_FG  = new Color(0xE9F2F8);
    private static final Color ACTIONS_BG = new Color(0xDCE9F2);
    private static final Color STATUS_BG  = new Color(0x14283B);
    private static final Color STATUS_FG  = new Color(0xE9F2F8);
    private static final Color LOG_BG     = new Color(0x0E1E2C);
    private static final Color LOG_FG     = new Color(0xCBE5F3);

    private final Gym gym = new Gym("Iron Park Fitness");

    private final MemberTablePanel tablePanel;
    private final MemberFormPanel  formPanel;
    private final JTextArea        eventLog = new JTextArea(7, 60);

    private final JPanel  actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JLabel  statusBar = new JLabel(" ");

    private final JButton btnActivate   = new JButton("Activate");
    private final JButton btnFreeze     = new JButton("Freeze");
    private final JButton btnResume     = new JButton("Resume (-> ACTIVE)");
    private final JButton btnExpiring   = new JButton("Mark expiring");
    private final JButton btnCancel     = new JButton("Cancel");

    private final JButton btnAttachEmail = new JButton("+ Email");
    private final JButton btnAttachSms   = new JButton("+ SMS");
    private final JButton btnAttachPush  = new JButton("+ Push");

    private final JButton btnPaymentDue = new JButton("Payment due...");
    private final JButton btnRenewal    = new JButton("Renewal reminder");
    private final JButton btnCancelClass= new JButton("Cancel class...");
    private final JButton btnPromotion  = new JButton("Promotion...");

    public GymManagerGUI() {
        super("SEN3006 -- Gym Membership Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(WINDOW_BG);

        // Seed the catalogue so the form has something to offer immediately.
        DemoScenarios.seedPlans(gym);

        this.tablePanel = new MemberTablePanel(gym);
        this.formPanel  = new MemberFormPanel(gym);

        setJMenuBar(buildMenuBar());

        add(buildBanner(), BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(WINDOW_BG);
        centre.add(tablePanel, BorderLayout.CENTER);
        centre.add(formPanel, BorderLayout.EAST);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setBackground(WINDOW_BG);

        actionsPanel.setBackground(ACTIONS_BG);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xB6CAD9)),
                new EmptyBorder(6, 12, 6, 12)));

        addActionButton(btnActivate);
        addActionButton(btnFreeze);
        addActionButton(btnResume);
        addActionButton(btnExpiring);
        addActionButton(btnCancel);
        actionsPanel.add(separator());
        addActionButton(btnAttachEmail);
        addActionButton(btnAttachSms);
        addActionButton(btnAttachPush);
        actionsPanel.add(separator());
        addActionButton(btnPaymentDue);
        addActionButton(btnRenewal);
        addActionButton(btnCancelClass);
        addActionButton(btnPromotion);

        southContainer.add(actionsPanel, BorderLayout.NORTH);
        southContainer.add(buildEventLog(), BorderLayout.CENTER);

        statusBar.setBackground(STATUS_BG);
        statusBar.setForeground(STATUS_FG);
        statusBar.setOpaque(true);
        statusBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusBar.setBorder(new EmptyBorder(6, 12, 6, 12));
        southContainer.add(statusBar, BorderLayout.SOUTH);

        JPanel centreWithSouth = new JPanel(new BorderLayout());
        centreWithSouth.setBackground(WINDOW_BG);
        centreWithSouth.add(centre, BorderLayout.CENTER);
        centreWithSouth.add(southContainer, BorderLayout.SOUTH);
        add(centreWithSouth, BorderLayout.CENTER);

        wireActions();
        formPanel.setOnMemberAdded(this::refreshAll);
        tablePanel.setOnSelectionChanged(this::updateButtons);
        refreshAll();
    }

    // -----------------------------------------------------------------------
    // Banner + event log
    // -----------------------------------------------------------------------

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(BANNER_BG);
        banner.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Gym Membership Manager");
        title.setForeground(BANNER_FG);
        title.setFont(new Font(Font.SERIF, Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Builder + Observer demo -- SEN3006 Software Architecture (Elif)");
        subtitle.setForeground(new Color(0xB6D1E0));
        subtitle.setFont(new Font(Font.SERIF, Font.ITALIC, 13));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        banner.add(left, BorderLayout.WEST);
        return banner;
    }

    private JScrollPane buildEventLog() {
        eventLog.setEditable(false);
        eventLog.setLineWrap(false);
        eventLog.setBackground(LOG_BG);
        eventLog.setForeground(LOG_FG);
        eventLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        eventLog.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scroll = new JScrollPane(eventLog);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xB6CAD9)));
        scroll.setPreferredSize(new Dimension(0, 150));
        return scroll;
    }

    private JLabel separator() {
        JLabel l = new JLabel(" | ");
        l.setForeground(new Color(0x7A93A6));
        return l;
    }

    private void addActionButton(JButton btn) {
        btn.setUI(new MetalButtonUI());
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setBackground(new Color(0xFFFFFF));
        btn.setForeground(new Color(0x14283B));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2C7DA0)),
                new EmptyBorder(4, 10, 4, 10)));
        actionsPanel.add(btn);
    }

    // -----------------------------------------------------------------------
    // Wiring
    // -----------------------------------------------------------------------

    private void wireActions() {
        btnActivate.addActionListener(e -> transitionSelected(MembershipStatus.ACTIVE));
        btnFreeze.addActionListener(e -> transitionSelected(MembershipStatus.FROZEN));
        btnResume.addActionListener(e -> transitionSelected(MembershipStatus.ACTIVE));
        btnExpiring.addActionListener(e -> transitionSelected(MembershipStatus.EXPIRING));
        btnCancel.addActionListener(e -> transitionSelected(MembershipStatus.CANCELLED));

        btnAttachEmail.addActionListener(e -> attachNotifier("EMAIL"));
        btnAttachSms.addActionListener(e -> attachNotifier("SMS"));
        btnAttachPush.addActionListener(e -> attachNotifier("PUSH"));

        btnPaymentDue.addActionListener(e -> publishPaymentDueDialog());
        btnRenewal.addActionListener(e -> publishRenewalDialog());
        btnCancelClass.addActionListener(e -> publishClassCancelDialog());
        btnPromotion.addActionListener(e -> publishPromotionDialog());
    }

    private void transitionSelected(MembershipStatus target) {
        Member m = tablePanel.getSelectedMember();
        if (m == null) return;
        try {
            m.setStatus(target);
            refreshAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid transition", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attachNotifier(String channel) {
        Member m = tablePanel.getSelectedMember();
        if (m == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a member in the table first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        switch (channel) {
            case "EMAIL": m.attachNotifier(new EmailMemberNotifier(m)); break;
            case "SMS":   m.attachNotifier(new SmsMemberNotifier(m));   break;
            case "PUSH":  m.attachNotifier(new PushMemberNotifier(m));  break;
        }
        appendLog(String.format("Attached %s notifier to %s", channel, m.getName()));
        refreshAll();
    }

    // -----------------------------------------------------------------------
    // Dialogs for publishing events
    // -----------------------------------------------------------------------

    private void publishPaymentDueDialog() {
        Member m = tablePanel.getSelectedMember();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Select a member first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dueStr = (String) JOptionPane.showInputDialog(this,
                "Due date (YYYY-MM-DD):", "Payment Due",
                JOptionPane.PLAIN_MESSAGE, null, null, LocalDate.now().plusDays(7).toString());
        if (dueStr == null) return;
        String amtStr = (String) JOptionPane.showInputDialog(this,
                "Amount:", "Payment Due", JOptionPane.PLAIN_MESSAGE, null, null,
                String.format("%.2f", m.getPlan().getMonthlyFee()));
        if (amtStr == null) return;
        try {
            int before = gym.getEventJournal().size();
            gym.publishPaymentDue(m.getId(), LocalDate.parse(dueStr), Double.parseDouble(amtStr));
            appendNewJournalEntries(before);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not publish", JOptionPane.ERROR_MESSAGE);
        }
        refreshAll();
    }

    private void publishRenewalDialog() {
        Member m = tablePanel.getSelectedMember();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Select a member first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int before = gym.getEventJournal().size();
            gym.publishRenewalReminder(m.getId());
            appendNewJournalEntries(before);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not publish", JOptionPane.ERROR_MESSAGE);
        }
        refreshAll();
    }

    private void publishClassCancelDialog() {
        String name = JOptionPane.showInputDialog(this, "Class name:", "Spinning");
        if (name == null || name.isBlank()) return;
        String dateStr = (String) JOptionPane.showInputDialog(this,
                "Class date (YYYY-MM-DD):", "Class cancellation",
                JOptionPane.PLAIN_MESSAGE, null, null, LocalDate.now().plusDays(1).toString());
        if (dateStr == null) return;
        try {
            int before = gym.getEventJournal().size();
            gym.publishClassCancellation(name, LocalDate.parse(dateStr));
            appendNewJournalEntries(before);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not publish", JOptionPane.ERROR_MESSAGE);
        }
        refreshAll();
    }

    private void publishPromotionDialog() {
        String pctStr = JOptionPane.showInputDialog(this, "Discount percent (0-100):", "20");
        if (pctStr == null) return;
        String message = JOptionPane.showInputDialog(this,
                "Marketing message:", "20% off Premium plans this week!");
        if (message == null || message.isBlank()) return;
        try {
            int before = gym.getEventJournal().size();
            gym.publishPromotion(Double.parseDouble(pctStr), message);
            appendNewJournalEntries(before);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not publish", JOptionPane.ERROR_MESSAGE);
        }
        refreshAll();
    }

    // -----------------------------------------------------------------------
    // Refresh + log helpers
    // -----------------------------------------------------------------------

    private void refreshAll() {
        formPanel.refreshPlans();
        tablePanel.refreshTable();
        updateButtons();
        updateStatusBar();
    }

    private void updateButtons() {
        Member m = tablePanel.getSelectedMember();
        boolean any = m != null;
        btnAttachEmail.setEnabled(any);
        btnAttachSms.setEnabled(any);
        btnAttachPush.setEnabled(any);
        btnPaymentDue.setEnabled(any);
        btnRenewal.setEnabled(any);

        if (!any) {
            btnActivate.setEnabled(false);
            btnFreeze.setEnabled(false);
            btnResume.setEnabled(false);
            btnExpiring.setEnabled(false);
            btnCancel.setEnabled(false);
            return;
        }
        MembershipStatus s = m.getStatus();
        btnActivate.setEnabled(s.canTransitionTo(MembershipStatus.ACTIVE));
        btnFreeze.setEnabled(s.canTransitionTo(MembershipStatus.FROZEN));
        btnResume.setEnabled(s == MembershipStatus.FROZEN);
        btnExpiring.setEnabled(s.canTransitionTo(MembershipStatus.EXPIRING));
        btnCancel.setEnabled(s.canTransitionTo(MembershipStatus.CANCELLED));
    }

    private void updateStatusBar() {
        int total = gym.getAllMembers().size();
        StringBuilder sb = new StringBuilder();
        sb.append(total).append(" member").append(total == 1 ? "" : "s");
        if (total > 0) {
            sb.append("   --   ");
            boolean first = true;
            for (MembershipStatus s : MembershipStatus.values()) {
                long count = gym.getAllMembers().stream().filter(m -> m.getStatus() == s).count();
                if (count == 0) continue;
                if (!first) sb.append(", ");
                sb.append(count).append(" ").append(s);
                first = false;
            }
        }
        sb.append("   --   Events emitted: ").append(gym.getEventJournal().size());
        statusBar.setText(sb.toString());
    }

    /** Appends every journal entry from {@code fromIndex} onward to the log area. */
    private void appendNewJournalEntries(int fromIndex) {
        List<GymEvent> journal = gym.getEventJournal();
        for (int i = fromIndex; i < journal.size(); i++) {
            appendLog(journal.get(i).toString());
        }
    }

    private void appendLog(String line) {
        eventLog.append(line + System.lineSeparator());
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }

    // -----------------------------------------------------------------------
    // Menus
    // -----------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(ACTIONS_BG);

        JMenu demoMenu = new JMenu("Demos");
        demoMenu.add(menuItem("Load Observer demo (Main.java Test 2)",
                () -> { DemoScenarios.loadObserverDemo(gym); refreshAll(); }));
        demoMenu.add(menuItem("Load Lifecycle demo (Main.java Test 3)",
                () -> { DemoScenarios.loadLifecycleDemo(gym); refreshAll(); }));
        demoMenu.add(menuItem("Load Integration demo (Main.java Test 4)",
                () -> { DemoScenarios.loadIntegrationDemo(gym); refreshAll(); }));
        demoMenu.addSeparator();
        demoMenu.add(menuItem("Clear all members",
                () -> { DemoScenarios.clearAll(gym); refreshAll(); }));
        bar.add(demoMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuItem("About", () -> JOptionPane.showMessageDialog(this,
                "SEN3006 Gym Membership Manager -- GUI demo\n"
                        + "Patterns: Builder + Observer\n"
                        + "Engine: pure Java, zero external dependencies\n\n"
                        + "Enrol a member on the right. Select rows on the left to\n"
                        + "transition status, attach notifiers, or publish events.\n"
                        + "Every published event appears in the dark log strip below.",
                "About", JOptionPane.INFORMATION_MESSAGE)));
        bar.add(helpMenu);

        return bar;
    }

    private JMenuItem menuItem(String label, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> action.run());
        return item;
    }

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { /* fall back silently */ }
        SwingUtilities.invokeLater(() -> new GymManagerGUI().setVisible(true));
    }
}
