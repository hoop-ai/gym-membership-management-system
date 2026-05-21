import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Swing GUI entry point for the Recipe Management System.
 *
 * <p>Third entry point alongside {@code Main} (deterministic test demo) and
 * {@code RecipeManagementApp} (interactive console menu). Consumes the same
 * public API as both, demonstrating that all three views are powered by the
 * same Factory Method + Strategy engine.</p>
 *
 * <p>The layout deliberately departs from the traditional "form on top, table
 * below" arrangement: a tall <em>recipe-book</em> table fills the left side of
 * the window, while a vertical form panel on the right mimics a cook's
 * index-card station. A warm terracotta header banner anchors the top, and a
 * lifecycle action strip sits along the bottom.</p>
 *
 * <p><strong>No package declaration</strong> -- keeps this class in the default
 * package so it can reference engine types directly (Java 8 forbids importing
 * default-package classes from a named package). The {@code gui/} subfolder is
 * organisational only.</p>
 */
public class RecipeManagerGUI extends JFrame {

    // Warm kitchen palette
    private static final Color WINDOW_BG  = new Color(0xFFFBF1);
    private static final Color BANNER_BG  = new Color(0xA94422);
    private static final Color BANNER_FG  = new Color(0xFFF8E7);
    private static final Color ACTIONS_BG = new Color(0xF3E3C7);
    private static final Color STATUS_BG  = new Color(0x4A2C20);
    private static final Color STATUS_FG  = new Color(0xFFF8E7);

    private final RecipeManager manager = new RecipeManager();
    private final RecipeFormPanel formPanel = new RecipeFormPanel(manager);
    private final RecipeTablePanel tablePanel = new RecipeTablePanel(manager);
    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JLabel statusBar = new JLabel(" ");

    private final JButton btnStartTesting = new JButton("Start testing");
    private final JButton btnApprove      = new JButton("Approve");
    private final JButton btnMarkCooked   = new JButton("Mark cooked");
    private final JButton btnPause        = new JButton("Pause");
    private final JButton btnResume       = new JButton("Resume (-> DRAFT)");
    private final JButton btnDelete       = new JButton("Remove");

    public RecipeManagerGUI() {
        super("SEN3006 -- Recipe Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(WINDOW_BG);
        setJMenuBar(buildMenuBar());

        add(buildBanner(), BorderLayout.NORTH);

        // Centre: table fills left, form anchored on the right
        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(WINDOW_BG);
        centre.add(tablePanel, BorderLayout.CENTER);
        centre.add(formPanel, BorderLayout.EAST);

        // Wrap centre + actions strip in a south-aware container
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setBackground(WINDOW_BG);

        actionsPanel.setBackground(ACTIONS_BG);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xCBB89E)),
                new EmptyBorder(4, 12, 4, 12)));
        addActionButton(btnStartTesting);
        addActionButton(btnApprove);
        addActionButton(btnMarkCooked);
        addActionButton(btnPause);
        addActionButton(btnResume);
        addActionButton(btnDelete);
        southContainer.add(actionsPanel, BorderLayout.CENTER);

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
        formPanel.setOnRecipeAdded(this::refreshAll);
        tablePanel.setOnSelectionChanged(this::updateActionButtons);
        refreshAll();
    }

    // -----------------------------------------------------------------------
    // Banner / header
    // -----------------------------------------------------------------------

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(BANNER_BG);
        banner.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Recipe Manager");
        title.setForeground(BANNER_FG);
        title.setFont(new Font(Font.SERIF, Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Factory Method + Strategy demo -- a kitchen-themed take on the SEN3006 design-pattern project");
        subtitle.setForeground(new Color(0xF8E3CB));
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

    private void addActionButton(JButton btn) {
        // Force Metal L&F so the warm palette actually shows on Windows.
        btn.setUI(new MetalButtonUI());
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setBackground(new Color(0xFFF4E1));
        btn.setForeground(new Color(0x4A2C20));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC65D3A)),
                new EmptyBorder(4, 10, 4, 10)));
        actionsPanel.add(btn);
    }

    // -----------------------------------------------------------------------
    // Wiring + actions
    // -----------------------------------------------------------------------

    private void wireActions() {
        btnStartTesting.addActionListener(e -> transitionSelected(RecipeStatus.TESTING));
        btnApprove.addActionListener(e -> transitionSelected(RecipeStatus.APPROVED));
        btnMarkCooked.addActionListener(e -> transitionSelected(RecipeStatus.COOKED));
        btnPause.addActionListener(e -> transitionSelected(RecipeStatus.PAUSED));
        btnResume.addActionListener(e -> transitionSelected(RecipeStatus.DRAFT));
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void transitionSelected(RecipeStatus target) {
        Recipe r = tablePanel.getSelectedRecipe();
        if (r == null) return;
        try {
            manager.transitionRecipe(r.getId(), target);
            refreshAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Invalid transition", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Recipe r = tablePanel.getSelectedRecipe();
        if (r == null) return;
        manager.removeRecipe(r.getId());
        refreshAll();
    }

    // -----------------------------------------------------------------------
    // Refresh + status bar
    // -----------------------------------------------------------------------

    private void refreshAll() {
        tablePanel.refreshTable();
        updateActionButtons();
        updateStatusBar();
    }

    private void updateActionButtons() {
        Recipe r = tablePanel.getSelectedRecipe();
        if (r == null) {
            btnStartTesting.setEnabled(false);
            btnApprove.setEnabled(false);
            btnMarkCooked.setEnabled(false);
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnDelete.setEnabled(false);
            return;
        }
        RecipeStatus s = r.getStatus();
        btnStartTesting.setEnabled(s.canTransitionTo(RecipeStatus.TESTING));
        btnApprove.setEnabled(s.canTransitionTo(RecipeStatus.APPROVED));
        btnMarkCooked.setEnabled(s.canTransitionTo(RecipeStatus.COOKED));
        btnPause.setEnabled(s.canTransitionTo(RecipeStatus.PAUSED));
        btnResume.setEnabled(s.canTransitionTo(RecipeStatus.DRAFT));
        btnDelete.setEnabled(true);
    }

    private void updateStatusBar() {
        int total = manager.getAllRecipes().size();
        StringBuilder sb = new StringBuilder();
        sb.append(total).append(" recipe").append(total == 1 ? "" : "s");
        if (total > 0) {
            sb.append("  |  ");
            boolean first = true;
            for (RecipeStatus s : RecipeStatus.values()) {
                int count = manager.getRecipesByStatus(s).size();
                if (count == 0) continue;
                if (!first) sb.append(", ");
                sb.append(count).append(" ").append(s);
                first = false;
            }
        }
        sb.append("   --   Strategy: ").append(manager.getCurrentStrategyName());
        statusBar.setText(sb.toString());
    }

    // -----------------------------------------------------------------------
    // Menus
    // -----------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(0xF3E3C7));

        JMenu demoMenu = new JMenu("Demos");
        demoMenu.add(menuItem("Load Strategy demo (Main.java Test 2)",
                () -> { DemoScenarios.loadStrategyDemo(manager); refreshAll(); }));
        demoMenu.add(menuItem("Load Lifecycle demo (Main.java Test 3)",
                () -> { DemoScenarios.loadLifecycleDemo(manager); refreshAll(); }));
        demoMenu.add(menuItem("Load Integration demo (Main.java Test 4)",
                () -> { DemoScenarios.loadIntegrationDemo(manager); refreshAll(); }));
        demoMenu.addSeparator();
        demoMenu.add(menuItem("Clear all recipes",
                () -> { DemoScenarios.clearAll(manager); refreshAll(); }));
        bar.add(demoMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuItem("About", () -> JOptionPane.showMessageDialog(this,
                "SEN3006 Recipe Management System -- GUI demo\n"
                        + "Patterns: Factory Method + Strategy\n"
                        + "Engine: pure Java, zero external dependencies\n\n"
                        + "Use the form on the right to add recipes, click rows to\n"
                        + "transition them through the kitchen lifecycle, and switch\n"
                        + "the Sort dropdown to see Strategy swap live.",
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
        SwingUtilities.invokeLater(() -> new RecipeManagerGUI().setVisible(true));
    }
}
