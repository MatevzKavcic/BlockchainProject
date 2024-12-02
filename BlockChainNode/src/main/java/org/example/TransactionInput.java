package org.example;



public class TransactionInput {
    private String transactionOutputId; // Reference to the TransactionOutput
    private TransactionOutput UTXO;    // The actual unspent output this input points to

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    // Getters and setters

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }
}
