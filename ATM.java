import fi.iki.elonen.NanoHTTPD;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ATM extends NanoHTTPD {

    private BankAccount account = new BankAccount(1000);  // Initialize with a starting balance

    public ATM() throws IOException {
        super(8081);  // Use a port different from 8080
        start(SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started on http://localhost:8081/");
        
        // Automatically open the browser window to the ATM interface
        openBrowser();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><head><style>";
        msg += "body { font-family: Arial, sans-serif; background-color: #f0f8ff; margin: 50px; }";
        msg += "h1 { color: #4682b4; }";
        msg += "form { margin-top: 20px; }";
        msg += "input[type='text'], input[type='submit'] { padding: 10px; margin-top: 10px; }";
        msg += "input[type='radio'] { margin-top: 10px; }";
        msg += "p { color: #333; font-size: 18px; }";
        msg += "a { color: #4682b4; text-decoration: none; font-size: 18px; }";
        msg += "a:hover { text-decoration: underline; }";
        msg += "</style></head><body>";
        msg += "<h1>ATM Interface</h1>";

        Map<String, List<String>> parameters = session.getParameters();
        if (parameters.get("action") != null) {
            String action = parameters.get("action").get(0);

            if ("withdraw".equals(action)) {
                double amount = Double.parseDouble(parameters.get("amount").get(0));
                boolean success = account.withdraw(amount);
                msg += "<p>" + (success ? "Withdrawal successful!" : "Insufficient funds!") + "</p>";
            } else if ("deposit".equals(action)) {
                double amount = Double.parseDouble(parameters.get("amount").get(0));
                account.deposit(amount);
                msg += "<p>Deposit successful!</p>";
            } else if ("checkBalance".equals(action)) {
                double balance = account.checkBalance();
                msg += "<p>Current Balance: $" + balance + "</p>";
            }
        }

        // Display the form for ATM actions
        msg += "<form action='?' method='get'>";
        msg += "Select Action: <br>";
        msg += "<input type='radio' name='action' value='withdraw'> Withdraw<br>";
        msg += "<input type='radio' name='action' value='deposit'> Deposit<br>";
        msg += "<input type='radio' name='action' value='checkBalance'> Check Balance<br>";
        msg += "Amount: <input type='text' name='amount'><br>";
        msg += "<input type='submit' value='Submit'>";
        msg += "</form>";

        // Add a Home button
        msg += "<br><a href='?'>Home</a>";

        msg += "</body></html>";
        return newFixedLengthResponse(msg);
    }

    private class BankAccount {
        private double balance;

        public BankAccount(double initialBalance) {
            this.balance = initialBalance;
        }

        public synchronized boolean withdraw(double amount) {
            if (amount <= balance) {
                balance -= amount;
                return true;
            } else {
                return false;
            }
        }

        public synchronized void deposit(double amount) {
            balance += amount;
        }

        public synchronized double checkBalance() {
            return balance;
        }
    }

    private void openBrowser() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8081/"));
        } catch (Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            new ATM();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }
}
