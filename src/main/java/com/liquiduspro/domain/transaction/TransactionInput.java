package com.liquiduspro.domain.transaction;

/**
 * This class will be used to reference TransactionOutputs that have not yet been spent.
 * <br>
 * The transactionOutputId will be used to find the relevant TransactionOutput, allowing miners to check your ownership.
 */
public class TransactionInput {
    private final String transactionOutputId; // the id of the output that's being spent
    private TransactionOutput UTXO; // contains the unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }
}
