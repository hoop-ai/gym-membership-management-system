import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Right-hand panel of the Gym Manager GUI: enrol a member, attach notifiers,
 * change status, and publish notifications.
 *
 * <p>This is the panel where the user drives <strong>both</strong> patterns:
 * picking a plan from the registered catalogue (Builder consumer) and wiring
 * up notifiers + publishing events (Observer).</p>
 */
public class MemberFormPanel extends JPanel {

    // -- Athletic-blue accents -----------------------------------------
    private static final Color PANEL_BG  = new Color(0xEDF3F8);
    private static final Color HEADER_BG = new Color(0x1E4D6B);
    private static final Color HEADER_FG = new Color(0xFFFFFF);
    private static final Color BUTTON_BG = new Color(0x2C7DA0);
    private static final Color BUTTON_FG = Color.WHITE;
    private static final Color LABEL_FG  = new Color(0x14283B);

    private final Gym gym;
    private final JComboBox<String> planCombo;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField phoneField;

    private Runnable onMemberAdded = () -> {};

    public MemberFormPanel(Gym gym) {
        super(new GridBagLayout());
        this.gym = gym;
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setPreferredSize(new Dimension(340, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        // -- Section header ----------------------------------------------
        JLabel sectionHeader = new JLabel("Enrol a Member");
        sectionHeader.setOpaque(true);
        sectionHeader.setBackground(HEADER_BG);
        sectionHeader.setForeground(HEADER_FG);
        sectionHeader.setFont(serifBold(18));
        sectionHeader.setBorder(new EmptyBorder(8, 12, 8, 12));
        sectionHeader.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0; add(sectionHeader, c);

        // -- Name --------------------------------------------------------
        c.gridy = 1; add(makeLabel("Full name"), c);
        nameField = new JTextField();
        nameField.setFont(sansPlain(13));
        c.gridy = 2; add(nameField, c);

        // -- Email -------------------------------------------------------
        c.gridy = 3; add(makeLabel("Email"), c);
        emailField = new JTextField();
        emailField.setFont(sansPlain(13));
        c.gridy = 4; add(emailField, c);

        // -- Phone -------------------------------------------------------
        c.gridy = 5; add(makeLabel("Phone (blank if none)"), c);
        phoneField = new JTextField();
        phoneField.setFont(sansPlain(13));
        c.gridy = 6; add(phoneField, c);

        // -- Plan dropdown ----------------------------------------------
        c.gridy = 7; add(makeLabel("Plan"), c);
        planCombo = new JComboBox<>();
        planCombo.setFont(sansPlain(13));
        refreshPlans();
        c.gridy = 8; add(planCombo, c);

        // -- Enrol button -----------------------------------------------
        c.gridy = 9;
        c.insets = new Insets(14, 4, 14, 4);
        JButton enrolBtn = accentButton("Enrol Member");
        enrolBtn.addActionListener(e -> handleEnrol());
        add(enrolBtn, c);

        // -- Footer / tip -----------------------------------------------
        c.gridy = 10;
        c.insets = new Insets(2, 4, 2, 4);
        JLabel tip = new JLabel("<html><div style='width:280px'>Once a member is enrolled, "
                + "select them in the table on the left, then use the lifecycle and "
                + "notification buttons at the bottom.</div></html>");
        tip.setFont(sansPlain(11));
        tip.setForeground(new Color(0x4A5C6E));
        add(tip, c);

        // -- Spacer ------------------------------------------------------
        c.gridy = 11;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(new JLabel(), c);
    }

    /** Re-pulls the plan list from the gym; call after a new plan is registered. */
    public void refreshPlans() {
        planCombo.removeAllItems();
        List<MembershipPlan> plans = gym.getAllPlans();
        for (MembershipPlan p : plans) {
            planCombo.addItem(p.getName());
        }
    }

    private void handleEnrol() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Object plan  = planCombo.getSelectedItem();
        if (plan == null) {
            JOptionPane.showMessageDialog(this,
                    "No plans registered. Use Plans -> Build a plan first.",
                    "No plans", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            gym.enrolMember(name, email, phone, plan.toString());
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            onMemberAdded.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setOnMemberAdded(Runnable callback) {
        this.onMemberAdded = callback == null ? () -> {} : callback;
    }

    // -- Helpers ---------------------------------------------------------

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sansBold(12));
        l.setForeground(LABEL_FG);
        return l;
    }

    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setUI(new MetalButtonUI());
        b.setBackground(BUTTON_BG);
        b.setForeground(BUTTON_FG);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(sansBold(14));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return b;
    }

    private static Font serifBold(int size) { return new Font(Font.SERIF, Font.BOLD, size); }
    private static Font sansBold(int size)  { return new Font(Font.SANS_SERIF, Font.BOLD, size); }
    private static Font sansPlain(int size) { return new Font(Font.SANS_SERIF, Font.PLAIN, size); }
}
