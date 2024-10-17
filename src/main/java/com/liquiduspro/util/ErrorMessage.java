package com.liquiduspro.util;

public final class ErrorMessage {

    public static final String TRANSACTION_ALREADY_EXISTS = "Transaction already exists";
    public static final String INVALID_TRANSACTION = "Transaction failed to process";
    public static final String INVALID_TRANSACTION_INPUT = "Invalid transaction input";
    public static final String INVALID_TRANSACTION_OUTPUT = "Invalid transaction output";
    public static final String SIGNATURE_ERROR = "Transaction Signature failed to verify";
    public static final String NO_ENOUGH_FUNDS = "Not enough funds to send transaction";
    public static final String INVALID_TRANSACTION_VALUE = "Invalid transaction value";
    public static final String UTXO_NOT_FOUND = "UTXO not found";

    private ErrorMessage() {

    }
}
