import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LibraryManagementSystemGUI extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "root";
    private static final String PASS = "password";
    
    // Replaced JTable/TableModel with JList/ListModel
    private DefaultListModel<Book> listModel;
    private JList<Book> bookList;

    /**
     * Data class to hold book information.
     */
    private static class Book {
        private final int id;
        private final String title;
        private final String author;
        private final boolean isIssued;

        public Book(int id, String title, String author, boolean isIssued) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isIssued = isIssued;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isIssued() { return isIssued; }
        
        @Override
        public String toString() {
            return title + " by " + author;
        }
    }
    
    /**
     * Custom renderer to display a Book object as an "icon" card.
     */
    private static class BookCellRenderer extends JPanel implements ListCellRenderer<Book> {
        private final JLabel iconLabel;
        private final JLabel titleLabel;
        private final JLabel authorLabel;
        private final JLabel statusLabel;
        private final Border selectedBorder = BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 2);

        public BookCellRenderer() {
            // Set up the card panel
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 5, 5, 5) // Internal padding
            ));
            setOpaque(true);

            // Icon in the center (using a book emoji)
            iconLabel = new JLabel("📖");
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(iconLabel, BorderLayout.CENTER);

            // Panel for text info at the bottom
            JPanel textPanel = new JPanel(new GridLayout(3, 1));
            textPanel.setOpaque(false); // Make it transparent
            
            titleLabel = new JLabel();
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            authorLabel = new JLabel();
            authorLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
            authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            statusLabel = new JLabel();
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            textPanel.add(titleLabel);
            textPanel.add(authorLabel);
            textPanel.add(statusLabel);
            add(textPanel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Book> list, 
                                                      Book book, 
                                                      int index, 
                                                      boolean isSelected, 
                                                      boolean cellHasFocus) {
            
            // Use HTML to wrap long text within the card
            titleLabel.setText("<html><div style='text-align: center; width: 100px;'>" + book.getTitle() + "</div></html>");
            authorLabel.setText("<html><div style='text-align: center; width: 100px;'>" + book.getAuthor() + "</div></html>");

            // Update status and colors
            if (book.isIssued()) {
                statusLabel.setText("Issued");
                statusLabel.setForeground(Color.RED);
                iconLabel.setForeground(Color.LIGHT_GRAY); // Dim the icon
            } else {
                statusLabel.setText("Available");
                statusLabel.setForeground(new Color(0, 128, 0)); // Dark Green
                iconLabel.setForeground(Color.BLACK);
            }

            // Update background and border based on selection
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setBorder(BorderFactory.createCompoundBorder(
                    selectedBorder,
                    new EmptyBorder(3, 3, 3, 3) // Inner padding when selected
                ));
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                 setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    new EmptyBorder(5, 5, 5, 5) // Default inner padding
                ));
            }
            
            return this;
        }
    }


    public LibraryManagementSystemGUI() {
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set overall padding for the main window
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- List Setup (Replaces Table Setup) ---
        listModel = new DefaultListModel<>();
        bookList = new JList<>(listModel);
        bookList.setCellRenderer(new BookCellRenderer());
        bookList.setLayoutOrientation(JList.HORIZONTAL_WRAP); // Arrange in a grid
        bookList.setVisibleRowCount(-1); // Let it flow
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set fixed cell sizes for a uniform grid
        bookList.setFixedCellWidth(130);
        bookList.setFixedCellHeight(160);
        
        // Style selection
        bookList.setSelectionBackground(new Color(220, 235, 255)); // Light blue selection
        bookList.setSelectionForeground(Color.BLACK);

        // Add double-click listener to issue/return
        bookList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Check for double click
                if (evt.getClickCount() == 2) {
                    Book selectedBook = bookList.getSelectedValue();
                    if (selectedBook != null) {
                        // If book is issued, return it. Otherwise, issue it.
                        if (selectedBook.isIssued()) {
                            returnBook();
                        } else {
                            issueBook();
                        }
                    }
                }
            }
        });


        JScrollPane scrollPane = new JScrollPane(bookList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Only vertical scroll
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel Setup ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); 

        JButton addButton = createStyledButton("Add Book");
        JButton issueButton = createStyledButton("Issue Book");
        JButton returnButton = createStyledButton("Return Book");
        JButton refreshButton = createStyledButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(issueButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addBook());
        issueButton.addActionListener(e -> issueBook());
        returnButton.addActionListener(e -> returnBook());
        refreshButton.addActionListener(e -> refreshBookList()); // Renamed

        // --- Database Initialization ---
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            createTables(conn);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        refreshBookList(); // Renamed
    }

    /**
     * Helper method to create a consistently styled JButton.
     * (Unchanged from previous version)
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 12);
        button.setFont(buttonFont);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show hand cursor on hover
        button.setFocusPainted(false); // Remove distracting focus border
        button.setMargin(new Insets(8, 20, 8, 20)); // Add internal padding
        return button;
    }
    
    // --- Database Methods ---

    /**
     * Creates the 'books' table if it doesn't exist.
     * (Unchanged from previous version)
     */
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS books (" +
                         "id INT PRIMARY KEY AUTO_INCREMENT, " +
                         "title VARCHAR(255) NOT NULL, " +
                         "author VARCHAR(255) NOT NULL, " +
                         "is_issued BOOLEAN DEFAULT FALSE)";
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Refreshes the list of books from the database.
     */
    private void refreshBookList() {
        // Store selected index to re-select after refresh
        int selectedIndex = bookList.getSelectedIndex();
        
        listModel.clear(); // Clear existing items
        String sql = "SELECT * FROM books ORDER BY title";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Create Book objects and add them to the list model
                listModel.addElement(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getBoolean("is_issued")
                ));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Re-select the previously selected item if possible
        if (selectedIndex >= 0 && selectedIndex < listModel.getSize()) {
            bookList.setSelectedIndex(selectedIndex);
        }
    }

    /**
     * Shows a dialog to add a new book.
     * (Unchanged, but calls refreshBookList)
     */
    private void addBook() {
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Book", 
                                                 JOptionPane.OK_CANCEL_OPTION, 
                                                 JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String author = authorField.getText();
            
            if (title != null && !title.trim().isEmpty() && author != null && !author.trim().isEmpty()) {
                String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, title.trim());
                    pstmt.setString(2, author.trim());
                    pstmt.executeUpdate();
                    refreshBookList(); // Refresh the grid
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                 JOptionPane.showMessageDialog(this, "Title and Author cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Issues the currently selected book.
     */
    private void issueBook() {
        // Get the selected Book object from the list
        Book selectedBook = bookList.getSelectedValue();

        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "Please select a book to issue.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check status before hitting the database
        if (selectedBook.isIssued()) {
            JOptionPane.showMessageDialog(this, "'" + selectedBook.getTitle() + "' is already issued.", "Book Not Available", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE books SET is_issued = TRUE WHERE id = ? AND is_issued = FALSE";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, selectedBook.getId()); // Use the ID from the Book object
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "'" + selectedBook.getTitle() + "' issued successfully!");
            } else {
                // This might happen if two people try at the same time
                JOptionPane.showMessageDialog(this, "Book could not be issued. It might be already issued by someone else.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshBookList(); // Refresh the grid to show new status
        } catch (SQLException ex) {
             JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns the currently selected book.
     */
    private void returnBook() {
        Book selectedBook = bookList.getSelectedValue();

        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "Please select a book to return.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!selectedBook.isIssued()) {
            JOptionPane.showMessageDialog(this, "'" + selectedBook.getTitle() + "' is already available.", "Book Not Issued", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String sql = "UPDATE books SET is_issued = FALSE WHERE id = ? AND is_issued = TRUE";
         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, selectedBook.getId());
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "'" + selectedBook.getTitle() + "' returned successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Book could not be returned.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshBookList(); // Refresh the grid
        } catch (SQLException ex) {
             JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            // Set the native system Look and Feel for a modern appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to the cross-platform "Metal" look if the system one fails
             try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LibraryManagementSystemGUI().setVisible(true));
    }
}



