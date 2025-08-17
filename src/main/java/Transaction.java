import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_account_id", nullable = false)
    private BankAccount senderAccount;

    @ManyToOne
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private BankAccount receiverAccount;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime time;

    public Transaction() {
    }

    public Transaction(BankAccount senderAccount, BankAccount receiverAccount, Double amount) {
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = senderAccount.getCurrency();
        this.time = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BankAccount getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(BankAccount senderAccount) {
        this.senderAccount = senderAccount;
    }

    public BankAccount getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(BankAccount receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
