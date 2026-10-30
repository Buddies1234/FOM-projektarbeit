import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class GameLibraryFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color SIDEBAR = new Color(25, 32, 45);
    private static final Color SIDEBAR_MUTED = new Color(170, 181, 198);
    private static final Color ACCENT = new Color(93, 120, 255);
    private static final Color TEXT = new Color(35, 44, 58);
    private static final Color MUTED = new Color(112, 122, 138);
    private static final Color CARD = Color.WHITE;

    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public GameLibraryFrame(List<Game> games) {
        super("Spielebibliothek");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        DefaultListModel<Game> gamesModel = new DefaultListModel<>();
        for (Game game : games) {
            gamesModel.addElement(game);
        }

        JList<Game> gameList = new JList<>(gamesModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setCellRenderer(new GameListCellRenderer());
        gameList.setBackground(SIDEBAR);
        gameList.setBorder(new EmptyBorder(8, 10, 12, 10));
        gameList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && gameList.getSelectedValue() != null) {
                showGameDetails(gameList.getSelectedValue());
            }
        });

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(290, 0));
        sidebar.add(createSidebarHeader(games.size()), BorderLayout.NORTH);
        JScrollPane gameListScrollPane = new JScrollPane(gameList);
        gameListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gameListScrollPane.getViewport().setBackground(SIDEBAR);
        sidebar.add(gameListScrollPane, BorderLayout.CENTER);

        detailsPanel.setBackground(BACKGROUND);
        detailsPanel.setBorder(new EmptyBorder(30, 34, 30, 34));
        showSelectionPrompt();

        add(sidebar, BorderLayout.WEST);
        add(detailsPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebarHeader(int gameCount) {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(SIDEBAR);
        header.setBorder(new EmptyBorder(28, 22, 20, 18));

        JLabel title = new JLabel("Meine Spiele");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitle = new JLabel(gameCount + (gameCount == 1 ? " Spiel" : " Spiele"));
        subtitle.setForeground(SIDEBAR_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private void showSelectionPrompt() {
        JPanel prompt = new JPanel(new BorderLayout(0, 10));
        prompt.setOpaque(false);

        JLabel title = new JLabel("Wähle ein Spiel aus");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Die Details werden hier angezeigt.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JPanel message = new JPanel(new BorderLayout(0, 8));
        message.setOpaque(false);
        message.add(title, BorderLayout.NORTH);
        message.add(subtitle, BorderLayout.CENTER);
        prompt.add(message, BorderLayout.CENTER);

        detailsPanel.removeAll();
        detailsPanel.add(prompt, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void showGameDetails(Game game) {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);

        JPanel heading = new JPanel(new BorderLayout(0, 8));
        heading.setOpaque(false);

        JLabel title = new JLabel(game.getTitle() + " (" + game.getReleaseYear() + ")");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel(valueOrDash(game.getDeveloper())
                + "  ·  " + valueOrDash(game.getPlatform()));
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);

        JPanel sections = new JPanel(new GridBagLayout());
        sections.setOpaque(false);
        ((GridBagLayout) sections.getLayout()).columnWeights = new double[] {1, 1};
        sections.setBorder(new EmptyBorder(2, 0, 0, 0));

        int row = 0;
        addDetailCard(sections, "SPIEL", row, 0,
                detail("Entwickler", valueOrDash(game.getDeveloper())),
                detail("Reihe", valueOrDash(game.getSeries())),
                detail("Genres", game.getGenres() == null || game.getGenres().isEmpty()
                        ? "-" : String.join(", ", game.getGenres())));

        String physicalOrDigital = game.getDistributionType() == DistributionType.DIGITAL
                ? "Digital" : "Physisch";
        String distributionDetail = game.getDistributionType() == DistributionType.DIGITAL
                ? valueOrDash(game.getStorefront()) : valueOrDash(game.getCondition());
        addDetailCard(sections, "AUSGABE", row++, 1,
                detail("Plattform", valueOrDash(game.getPlatform())),
                detail("Format", physicalOrDigital),
                detail(game.getDistributionType() == DistributionType.DIGITAL ? "Store" : "Zustand",
                        distributionDetail));

        addDetailCard(sections, "FORTSCHRITT", row, 0,
                detail("Status", valueOrDash(game.getStatus())),
                detail("Beendet am", game.getCompletionDate() == null
                        ? "-" : game.getCompletionDate().toString()),
                detail("Bewertung", game.getRating() > 0 ? game.getRating() + " / 10" : "Keine"));

        addDetailCard(sections, "BIBLIOTHEK", row++, 1,
                detail("Spiel-ID", valueOrDash(game.getId())));

        JPanel allContent = new JPanel(new BorderLayout(0, 22));
        allContent.setOpaque(false);
        allContent.add(heading, BorderLayout.NORTH);
        allContent.add(sections, BorderLayout.CENTER);

        if (game.getNotes() != null && !game.getNotes().isBlank()) {
            allContent.add(createNotesCard(game.getNotes()), BorderLayout.SOUTH);
        }

        JScrollPane scrollPane = new JScrollPane(allContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        detailsPanel.removeAll();
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetailCard(JPanel parent, String title, int row, int column, Detail... details) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 19, 18, 19));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        JLabel cardTitle = new JLabel(title);
        cardTitle.setForeground(ACCENT);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        constraints.insets = new Insets(0, 0, 14, 0);
        card.add(cardTitle, constraints);

        for (Detail detail : details) {
            constraints.gridy++;
            constraints.insets = new Insets(0, 0, 10, 0);
            card.add(createDetailRow(detail), constraints);
        }

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx = column;
        cardConstraints.gridy = row;
        cardConstraints.weightx = 1;
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        cardConstraints.anchor = GridBagConstraints.NORTHWEST;
        cardConstraints.insets = new Insets(0, column == 0 ? 0 : 10, 12, column == 0 ? 10 : 0);
        parent.add(card, cardConstraints);
    }

    private JPanel createDetailRow(Detail detail) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);

        JLabel label = new JLabel(detail.label);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel value = new JLabel(detail.value);
        value.setForeground(TEXT);
        value.setFont(new Font("SansSerif", Font.PLAIN, 14));

        row.add(label, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JPanel createNotesCard(String notes) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 19, 18, 19));

        JLabel title = new JLabel("NOTIZEN");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));

        JTextArea text = new JTextArea(notes);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setForeground(TEXT);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        text.setBorder(null);

        card.add(title, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private Detail detail(String label, String value) {
        return new Detail(label, value);
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private static class Detail {
        private final String label;
        private final String value;

        private Detail(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private static class GameListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            Game game = (Game) value;
            setText("<html><div style='padding: 3px 0;'>"
                    + "<b>" + escapeHtml(valueOrDash(game.getTitle())) + "</b><br>"
                    + "<span style='color:#aab5c6;'>"
                    + escapeHtml(valueOrDash(game.getPlatform())) + " · "
                    + game.getReleaseYear() + "</span></div></html>");
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setOpaque(true);
            setBorder(new EmptyBorder(10, 12, 10, 10));
            setBackground(isSelected ? new Color(52, 65, 91) : SIDEBAR);
            setForeground(Color.WHITE);
            return this;
        }

        private String valueOrDash(Object value) {
            return value == null || value.toString().isBlank() ? "-" : value.toString();
        }

        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }
    }
}
