import jakarta.persistence.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Random;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BankService {

    private EntityManager em;
    private CurrencyService currencyService;

    public BankService(CurrencyService currencyService, EntityManager em) {
        this.currencyService = currencyService;
        this.em = em;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("Bank");
            em = emf.createEntityManager();
            CurrencyService currencyService = new CurrencyService();
            BankService bankService = new BankService(currencyService, em);

            while (true) {
                System.out.println("1: add customer");
                System.out.println("2: create bank account");
                System.out.println("3: top up account");
                System.out.println("4: commit transaction");
                System.out.println("5: convert account currency");
                System.out.println("6: get total balance in UAH");
                System.out.println("7: show current currency rates");
                System.out.println("8: exit");
                System.out.print("-> ");

                String s = sc.nextLine();
                switch (s) {
                    case "1" -> bankService.addCustomer(sc);
                    case "2" -> bankService.addBankAccount(sc);
                    case "3" -> bankService.topUpAccountInteractive(sc);
                    case "4" -> bankService.commitTransaction(sc);
                    case "5" -> bankService.convertAccount(sc);
                    case "6" -> bankService.getTotalBalanceInUAH(sc);
                    case "7" -> bankService.showCurrentRates();
                    case "8" -> { return; }
                    default -> System.out.println("Invalid option!");
                }
            }

        } finally {
            sc.close();
            if (em != null) em.close();
            if (emf != null) emf.close();
        }
    }

    public void addCustomer(Scanner sc) {
        String firstName;
        do {
            System.out.print("First name (letters only): ");
            firstName = sc.nextLine();
        } while (!firstName.matches("[a-zA-Z]+"));

        String lastName;

        do {
            System.out.print("Last name (letters only): ");
            lastName = sc.nextLine();
        } while (!lastName.matches("[a-zA-Z]+"));

        System.out.print("Email: ");
        String email = sc.nextLine();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Client client = new Client(firstName, lastName, email);
            em.persist(client);
            tx.commit();
            System.out.println("Customer added.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public void addBankAccount(Scanner sc) {
        Long clientId = readLong(sc, "Enter client ID: ");
        Client client = em.find(Client.class, clientId);
        if (client == null) {
            System.out.println("Client not found.");
            return;
        }

        String accountNumber = generateRandomAccountNumber(16);
        BankAccount account = new BankAccount(accountNumber, 0.0, "UAH", client);

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(account);
            tx.commit();
            System.out.println("Bank account created. Account Number: " + accountNumber);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    private String generateRandomAccountNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public void topUpAccountInteractive(Scanner sc) {
        Long accountId = readLong(sc, "Enter account ID: ");
        BankAccount account = em.find(BankAccount.class, accountId);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        double amount = readDouble(sc, "Amount: ");
        System.out.print("Currency: ");
        String currency = sc.nextLine().toUpperCase();

        topUpAccount(account, amount, currency);
    }

    private Long readLong(Scanner sc, String prompt) {
        Long value = null;
        while (value == null) {
            System.out.print(prompt);
            String input = sc.nextLine();
            if (input.matches("\\d+")) {
                value = Long.parseLong(input);
            } else {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        return value;
    }

    private double readDouble(Scanner sc, String prompt) {
        Double value = null;
        while (value == null) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                value = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return value;
    }

    public void topUpAccount(BankAccount account, double amount, String currency) {
        if (amount < 0) {
            System.out.println("Amount cannot be negative. Please try again.");
            return;
        }

        try {
            if (!account.getCurrency().equalsIgnoreCase(currency)) {
                amount = currencyService.convert(currency, account.getCurrency(), amount);
            }

            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                account.setBalance(account.getBalance() + amount);
                em.merge(account);
                tx.commit();

                System.out.println("Account topped up successfully. New balance: "
                        + String.format("%.2f", account.getBalance()) + " " + account.getCurrency());
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                System.out.println("Error updating account. Please try again.");
            }

        } catch (Exception e) {
            System.out.println("Currency conversion error. Please try again.");
        }
    }


    public void commitTransaction(Scanner sc) {
        Long senderId = readLong(sc, "Sender Account ID: ");
        Long receiverId = readLong(sc, "Receiver Account ID: ");
        double amount = readDouble(sc, "Amount: ");
        System.out.print("Currency: ");
        String currency = sc.nextLine().toUpperCase();

        BankAccount sender = em.find(BankAccount.class, senderId);
        BankAccount receiver = em.find(BankAccount.class, receiverId);

        if (sender != null && receiver != null) {
            try {
                commitTransaction(sender, receiver, amount, currency);
                System.out.println("Transaction committed.");
            } catch (Exception e) {
                System.out.println("Transaction failed: " + e.getMessage());
            }
        } else {
            System.out.println("One or both accounts not found.");
        }
    }

    public void commitTransaction(BankAccount sender, BankAccount receiver, double amount, String currency) {
        double amountInSenderCurrency = currencyService.convert(currency, sender.getCurrency(), amount);

        if (sender.getBalance() < amountInSenderCurrency) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            sender.setBalance(BigDecimal.valueOf(sender.getBalance() - amountInSenderCurrency)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
            double amountInReceiverCurrency = currencyService.convert(currency, receiver.getCurrency(), amount);
            receiver.setBalance(BigDecimal.valueOf(receiver.getBalance() + amountInReceiverCurrency)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());

            em.merge(sender);
            em.merge(receiver);

            Transaction transaction = new Transaction(sender, receiver, amount);
            em.persist(transaction);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public void convertAccount(Scanner sc) {
        Long accountId = readLong(sc, "Account ID: ");
        System.out.print("Target Currency: ");
        String targetCurrency = sc.nextLine().toUpperCase();

        BankAccount account = em.find(BankAccount.class, accountId);
        if (account != null) {
            convertTo(account, targetCurrency);
            System.out.println("Account converted.");
        } else {
            System.out.println("Account not found.");
        }
    }

    public void convertTo(BankAccount account, String targetCurrency) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            double newBalance = currencyService.convert(account.getCurrency(), targetCurrency, account.getBalance());
            account.setBalance(BigDecimal.valueOf(newBalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            account.setCurrency(targetCurrency.toUpperCase());
            em.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public void getTotalBalanceInUAH(Scanner sc) {
        Long accountId = readLong(sc, "Account ID: ");
        BankAccount account = em.find(BankAccount.class, accountId);

        if (account != null) {
            double total = currencyService.convert(account.getCurrency(), "UAH", account.getBalance());
            System.out.println("Balance in UAH: " + String.format("%.2f", total));
        } else {
            System.out.println("Account not found.");
        }
    }

    public void showCurrentRates() {
        System.out.println("Current currency rates:");
        for (Map.Entry<String, Double> entry : currencyService.getRates().entrySet()) {
            System.out.println("1 " + entry.getKey() + " = " + entry.getValue() + " UAH");
        }
    }
}
