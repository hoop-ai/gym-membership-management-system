import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Right-hand side panel of the Recipe Manager GUI: a vertically-stacked form
 * for entering new recipes.
 *
 * <p>Demonstrates the Factory Method pattern: the type dropdown drives
 * {@code manager.createRecipe(type, ...)}, which routes through the registered
 * factory for that type without the GUI knowing the concrete class.</p>
 *
 * <p>The layout deliberately differs from a traditional top-bar form: fields
 * are stacked vertically in a side panel so the recipe table can take the full
 * height of the window, matching how a cookbook or kitchen prep board is laid
 * out -- entries on one side, the list of dishes on the other.</p>
 */
public class RecipeFormPanel extends JPanel {

    // Warm kitchen accent colours used across the form.
    private static final Color HEADER_BG = new Color(0xC65D3A);    // terracotta
    private static final Color HEADER_FG = new Color(0xFFF8E7);    // cream
    private static final Color PANEL_BG  = new Color(0xFFF4E1);    // light cream
    private static final Color BUTTON_BG = new Color(0xA94422);    // deep terracotta
    private static final Color BUTTON_FG = Color.WHITE;
    private static final Color LABEL_FG  = new Color(0x4A2C20);    // dark cocoa

    private final RecipeManager manager;
    private final JComboBox<String> typeCombo;
    private final JTextField titleField;
    private final JComboBox<Integer> priorityCombo;
    private final JTextArea descriptionField;
    private Runnable onRecipeAdded = () -> {};

    public RecipeFormPanel(RecipeManager manager) {
        super(new GridBagLayout());
        this.manager = manager;
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setPreferredSize(new Dimension(320, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        // Section header at the top of the form
        JLabel sectionHeader = new JLabel("Add a Recipe");
        sectionHeader.setOpaque(true);
        sectionHeader.setBackground(HEADER_BG);
        sectionHeader.setForeground(HEADER_FG);
        sectionHeader.setFont(serifBold(18));
        sectionHeader.setBorder(new EmptyBorder(8, 12, 8, 12));
        sectionHeader.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        add(sectionHeader, c);

        // Type field
        c.gridy = 1; add(makeLabel("Type"), c);
        typeCombo = new JComboBox<>(new String[]{"DESSERT", "MAIN_COURSE", "APPETIZER"});
        typeCombo.setFont(sansPlain(13));
        c.gridy = 2; add(typeCombo, c);

        // Title field
        c.gridy = 3; add(makeLabel("Title"), c);
        titleField = new JTextField();
        titleField.setFont(sansPlain(13));
        c.gridy = 4; add(titleField, c);

        // Priority field
        c.gridy = 5; add(makeLabel("Priority (1 = casual, 5 = headline dish)"), c);
        priorityCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        priorityCombo.setSelectedItem(3);
        priorityCombo.setFont(sansPlain(13));
        c.gridy = 6; add(priorityCombo, c);

        // Description field (multi-line area)
        c.gridy = 7; add(makeLabel("Description"), c);
        descriptionField = new JTextArea(5, 20);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setFont(sansPlain(13));
        descriptionField.setBorder(BorderFactory.createLineBorder(new Color(0xCBB89E)));
        JScrollPane descScroll = new JScrollPane(descriptionField);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        c.gridy = 8;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(descScroll, c);

        // Submit button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.gridy = 9;
        c.insets = new Insets(14, 4, 4, 4);
        JButton addBtn = new JButton("Add to Recipe Book");
        // Force Metal L&F just for this button so the terracotta colour is
        // actually applied -- Windows L&F repaints over setBackground.
        addBtn.setUI(new MetalButtonUI());
        addBtn.setBackground(BUTTON_BG);
        addBtn.setForeground(BUTTON_FG);
        addBtn.setOpaque(true);
        addBtn.setContentAreaFilled(true);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setFont(sansBold(14));
        addBtn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        addBtn.addActionListener(e -> handleAdd());
        add(addBtn, c);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sansBold(12));
        l.setForeground(LABEL_FG);
        return l;
    }

    private static Font serifBold(int size) {
        return new Font(Font.SERIF, Font.BOLD, size);
    }

    private static Font sansBold(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    private static Font sansPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    private void handleAdd() {
        String type = (String) typeCombo.getSelectedItem();
        String title = titleField.getText().trim();
        Integer priority = (Integer) priorityCombo.getSelectedItem();
        String description = descriptionField.getText().trim();

        try {
            manager.createRecipe(type, title, description, priority);
            titleField.setText("");
            descriptionField.setText("");
            onRecipeAdded.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Validation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setOnRecipeAdded(Runnable callback) {
        this.onRecipeAdded = callback == null ? () -> {} : callback;
    }
}
