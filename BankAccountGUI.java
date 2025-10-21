import javax.swing.*;
import java.awt.*;

class BankAccount {
    private String accountNumber;
    private String accountHolderName;
    private double balance;

    public BankAccount(String accountNumber, String accountHolderName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance() { return balance; }

    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;
        }
        return false;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

public class BankAccountGUI extends JFrame {
    private final BankAccount account;
    private final JLabel holderLabel;
    private final JLabel numberLabel;
    private final JLabel balanceLabel;

    public BankAccountGUI() {
        account = new BankAccount("987654321", "Jane Doe", 1000.00);

        setTitle("Bank Account Management");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(new JLabel("Account Holder:"));
        holderLabel = new JLabel(account.getAccountHolderName());
        infoPanel.add(holderLabel);

        infoPanel.add(new JLabel("Account Number:"));
        numberLabel = new JLabel(account.getAccountNumber());
        infoPanel.add(numberLabel);

        infoPanel.add(new JLabel("Current Balance:"));
        balanceLabel = new JLabel(String.format("$%.2f", account.getBalance()));
        infoPanel.add(balanceLabel);

        add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);

        add(buttonPanel, BorderLayout.SOUTH);

        depositButton.addActionListener(e -> performDeposit());
        withdrawButton.addActionListener(e -> performWithdrawal());
    }

    private void updateBalance() {
        balanceLabel.setText(String.format("$%.2f", account.getBalance()));
    }
    
    private void performDeposit() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount to deposit:", "Deposit", JOptionPane.PLAIN_MESSAGE);
        try {
            double amount = Double.parseDouble(amountStr);
            if (account.deposit(amount)) {
                JOptionPane.showMessageDialog(this, "Deposit successful.");
                updateBalance();
            } else {
                JOptionPane.showMessageDialog(this, "Deposit amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ignored) {
            // User cancelled
        }
    }
    
    private void performWithdrawal() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw:", "Withdrawal", JOptionPane.PLAIN_MESSAGE);
        try {
            double amount = Double.parseDouble(amountStr);
             if (account.withdraw(amount)) {
                JOptionPane.showMessageDialog(this, "Withdrawal successful.");
                updateBalance();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid amount or insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ignored) {
            // User cancelled
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankAccountGUI().setVisible(true));
    }
}

