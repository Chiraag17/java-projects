import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LibraryManagementSystemGUI extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "root";
    private static final String PASS = "password";
    private DefaultTableModel tableModel;
    private JTable bookTable;

    public LibraryManagementSystemGUI() {
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Title", "Author", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Book");
        JButton issueButton = new JButton("Issue Book");
        JButton returnButton = new JButton("Return Book");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(issueButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addBook());
        issueButton.addActionListener(e -> issueBook());
        returnButton.addActionListener(e -> returnBook());
        refreshButton.addActionListener(e -> refreshTable());

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            createTables(conn);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        refreshTable();
    }
    
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


    private void refreshTable() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean isIssued = rs.getBoolean("is_issued");
                String status = isIssued ? "Issued" : "Available";
                tableModel.addRow(new Object[]{id, title, author, status});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBook() {
        String title = JOptionPane.showInputDialog(this, "Enter book title:");
        String author = JOptionPane.showInputDialog(this, "Enter book author:");
        if (title != null && !title.trim().isEmpty() && author != null && !author.trim().isEmpty()) {
            String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title.trim());
                pstmt.setString(2, author.trim());
                pstmt.executeUpdate();
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void issueBook() {
        String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID to issue:");
        if (bookIdStr != null) {
            try {
                int bookId = Integer.parseInt(bookIdStr.trim());
                String sql = "UPDATE books SET is_issued = TRUE WHERE id = ? AND is_issued = FALSE";
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, bookId);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Book issued successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Book not available or ID is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshTable();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Book ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void returnBook() {
        String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID to return:");
        if (bookIdStr != null) {
            try {
                int bookId = Integer.parseInt(bookIdStr.trim());
                String sql = "UPDATE books SET is_issued = FALSE WHERE id = ? AND is_issued = TRUE";
                 try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, bookId);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Book returned successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Book was not issued or ID is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshTable();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Book ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSystemGUI().setVisible(true));
    }
}
