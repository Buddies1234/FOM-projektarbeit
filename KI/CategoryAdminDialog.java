import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public final class CategoryAdminDialog {
    private CategoryAdminDialog() {
    }

    public static GameCategories showDialog(
            Component parent, GameCategories current, List<Game> games) {
        GameCategories edited = new GameCategories(current);
        JComboBox<GameCategories.Category> categorySelector =
                new JComboBox<>(GameCategories.Category.values());
        DefaultListModel<GameCategories.Option> valuesModel = new DefaultListModel<>();
        JList<GameCategories.Option> valuesList = new JList<>(valuesModel);
        valuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = new JButton("Hinzufügen");
        JButton renameButton = new JButton("Umbenennen");
        JButton removeButton = new JButton("Löschen");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addButton);
        actions.add(renameButton);
        actions.add(removeButton);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(categorySelector, BorderLayout.NORTH);
        content.add(new JScrollPane(valuesList), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        content.setPreferredSize(new Dimension(420, 360));

        Runnable refresh = () -> {
            valuesModel.clear();
            GameCategories.Category category =
                    (GameCategories.Category) categorySelector.getSelectedItem();
            if (category != null) {
                for (GameCategories.Option option : edited.options(category)) {
                    valuesModel.addElement(option);
                }
            }
            valuesList.setSelectedIndex(valuesModel.isEmpty() ? -1 : 0);
            updateButtons(valuesList, renameButton, removeButton);
        };
        categorySelector.addActionListener(event -> refresh.run());
        valuesList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateButtons(valuesList, renameButton, removeButton);
            }
        });

        addButton.addActionListener(event -> {
            GameCategories.Category category =
                    (GameCategories.Category) categorySelector.getSelectedItem();
            String label = JOptionPane.showInputDialog(parent,
                    "Name des neuen Werts:", "Wert hinzufügen", JOptionPane.PLAIN_MESSAGE);
            if (label == null) {
                return;
            }
            try {
                edited.add(category, label);
                refresh.run();
                valuesList.setSelectedIndex(valuesModel.size() - 1);
            } catch (IllegalArgumentException e) {
                showValidationError(parent, e);
            }
        });

        renameButton.addActionListener(event -> {
            GameCategories.Category category =
                    (GameCategories.Category) categorySelector.getSelectedItem();
            GameCategories.Option selected = valuesList.getSelectedValue();
            if (selected == null) {
                return;
            }
            String label = (String) JOptionPane.showInputDialog(parent,
                    "Neuer Name:", "Wert umbenennen", JOptionPane.PLAIN_MESSAGE,
                    null, null, selected.getLabel());
            if (label == null) {
                return;
            }
            try {
                edited.rename(category, selected.getId(), label);
                refresh.run();
                selectValue(valuesList, selected.getId());
            } catch (IllegalArgumentException e) {
                showValidationError(parent, e);
            }
        });

        removeButton.addActionListener(event -> {
            GameCategories.Category category =
                    (GameCategories.Category) categorySelector.getSelectedItem();
            GameCategories.Option selected = valuesList.getSelectedValue();
            if (selected == null) {
                return;
            }
            if (GameCategories.isUsed(category, selected.getId(), games)) {
                JOptionPane.showMessageDialog(parent,
                        "Dieser Wert wird noch von mindestens einem Spiel verwendet. "
                                + "Ändere das Spiel zuerst, bevor du den Wert löschst.",
                        "Wert wird verwendet", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirmation = JOptionPane.showConfirmDialog(parent,
                    "Wert „" + selected.getLabel() + "“ wirklich löschen?",
                    "Wert löschen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                edited.remove(category, selected.getId());
                refresh.run();
            } catch (IllegalArgumentException e) {
                showValidationError(parent, e);
            }
        });

        refresh.run();
        int result = JOptionPane.showConfirmDialog(parent, content,
                "Admin-Modus – Kategorien verwalten",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION ? edited : null;
    }

    private static void selectValue(
            JList<GameCategories.Option> valuesList, String id) {
        for (int i = 0; i < valuesList.getModel().getSize(); i++) {
            if (valuesList.getModel().getElementAt(i).getId().equals(id)) {
                valuesList.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void updateButtons(
            JList<GameCategories.Option> valuesList,
            JButton renameButton,
            JButton removeButton) {
        boolean selected = valuesList.getSelectedValue() != null;
        renameButton.setEnabled(selected);
        removeButton.setEnabled(selected);
    }

    private static void showValidationError(Component parent, IllegalArgumentException error) {
        JOptionPane.showMessageDialog(parent, error.getMessage(),
                "Eingabe prüfen", JOptionPane.ERROR_MESSAGE);
    }
}
