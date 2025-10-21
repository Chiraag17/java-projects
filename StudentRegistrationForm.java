import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class Student {
    private String name;
    private String email;
    private String phone;
    private String course;

    public Student(String name, String email, String phone, String course) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCourse() { return course; }
}

public class StudentRegistrationForm extends JFrame {

    private final JTextField nameField, emailField, phoneField;
    private final JComboBox<String> courseComboBox;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/university_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    public StudentRegistrationForm() {
        setTitle("Student Registration Form");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Course:"), gbc);

        gbc.gridx = 1;
        String[] courses = {"Computer Science", "Engineering", "Business", "Arts"};
        courseComboBox = new JComboBox<>(courses);
        panel.add(courseComboBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JButton registerButton = new JButton("Register");
        panel.add(registerButton, gbc);
        
        add(panel);

        registerButton.addActionListener(e -> registerStudent());
    }

    private void registerStudent() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String course = (String) courseComboBox.getSelectedItem();

        if (name.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student student = new Student(name, email, phone, course);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO students (name, email, phone, course) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getEmail());
                pstmt.setString(3, student.getPhone());
                pstmt.setString(4, student.getCourse());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student registered successfully!");
                clearFields();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        courseComboBox.setSelectedIndex(0);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRegistrationForm().setVisible(true));
    }
}


