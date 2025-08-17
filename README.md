# MySQL Setup Instructions

CREATE TABLE clients (
    client_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance DOUBLE NOT NULL,
    currency VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT fk_bankaccount_client FOREIGN KEY (client_id) REFERENCES clients(client_id)
);

CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_account_id BIGINT NOT NULL,
    receiver_account_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    currency VARCHAR(255) NOT NULL,
    time DATETIME NOT NULL,
    CONSTRAINT fk_transaction_sender FOREIGN KEY (sender_account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_transaction_receiver FOREIGN KEY (receiver_account_id) REFERENCES bank_accounts(id)
);
