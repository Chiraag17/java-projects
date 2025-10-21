import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class CalculatorLogic {
    private double num1 = 0;
    private double result = 0;
    private char operator;
    private boolean isOperatorClicked = false;
    private String currentDisplayValue = "";

    public String handleInput(String command) {
        if (command.equals("C")) {
            num1 = 0;
            result = 0;
            currentDisplayValue = "";
            isOperatorClicked = false;
            return "0";
        }
        
        char cmdChar = command.charAt(0);

        if (Character.isDigit(cmdChar) || cmdChar == '.') {
            if ("0".equals(currentDisplayValue) && cmdChar != '.') {
                currentDisplayValue = "";
            }
            if (isOperatorClicked) {
                currentDisplayValue = "";
                isOperatorClicked = false;
            }
            if (cmdChar == '.' && currentDisplayValue.contains(".")) {
                return currentDisplayValue;
            }
            currentDisplayValue += command;
            return currentDisplayValue;
        } else if (command.equals("=")) {
            if (currentDisplayValue.isEmpty()) return String.valueOf(result);

            double num2 = Double.parseDouble(currentDisplayValue);
            switch (operator) {
                case '+': result = num1 + num2; break;
                case '-': result = num1 - num2; break;
                case '*': result = num1 * num2; break;
                case '/':
                    if (num2 != 0) {
                        result = num1 / num2;
                    } else {
                        return "Error";
                    }
                    break;
                default: result = num2;
            }
            currentDisplayValue = String.valueOf(result);
            num1 = result;
            isOperatorClicked = true;
            return currentDisplayValue;
        } else {
            if (!currentDisplayValue.isEmpty()) {
                num1 = Double.parseDouble(currentDisplayValue);
                operator = cmdChar;
                isOperatorClicked = true;
            }
            return currentDisplayValue;
        }
    }
}


public class CalculatorApp extends JFrame implements ActionListener {

    private final JTextField displayField;
    private final CalculatorLogic logic;

    public CalculatorApp() {
        logic = new CalculatorLogic();
        setTitle("Calculator");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        displayField = new JTextField("0");
        displayField.setEditable(false);
        displayField.setFont(new Font("Arial", Font.BOLD, 48));
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setBackground(new Color(30, 30, 30));
        displayField.setForeground(Color.WHITE);
        displayField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(displayField, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 4, 5, 5));
        panel.setBackground(new Color(30,30,30));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] buttons = {
            "C", " ", " ", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", " ", "="
        };

        for (String text : buttons) {
            if (text.trim().isEmpty()) {
                panel.add(new JLabel("")); // Placeholder
                continue;
            }
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 24));
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);

            if ("+-*/".contains(text)) {
                button.setBackground(new Color(255, 159, 10)); // Orange for operators
            } else if ("=".equals(text)) {
                button.setBackground(new Color(255, 159, 10)); // Orange for equals
            } else if ("C".equals(text)){
                button.setBackground(new Color(165, 165, 165)); // Light gray for clear
                button.setForeground(Color.BLACK);
            }
             else {
                button.setBackground(new Color(51, 51, 51)); // Dark gray for numbers
            }
            
            button.addActionListener(this);
            panel.add(button);
        }

        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        String newDisplayValue = logic.handleInput(command);
        displayField.setText(newDisplayValue);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalculatorApp().setVisible(true));
    }
}


