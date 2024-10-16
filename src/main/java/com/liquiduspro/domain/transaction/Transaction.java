package com.liquiduspro.domain.transaction;

import com.liquiduspro.NoobChain;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.StringUtil;
import com.liquiduspro.util.TransactionException;

import java.io.Serial;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transaction in the NoobChain
 *
 * @author econlq
 */
public class Transaction implements Serializable {
    public static final float MINIMUM_TRANSACTION = 0.1f;
    @Serial
    private static final long serialVersionUID = 1L;
    private static int sequence = 0; // count of how many transactions have been processed
    private final PublicKey sender; // sender's address public key
    private final PublicKey recipient; // recipient's address public key
    private final float value; // amount to be sent
    private String transactionId; // hash of the transaction
    private byte[] signature; // digital signature of the transaction
    private final List<TransactionInput> inputs;
    private final List<TransactionOutput> outputs = new ArrayList<>();
    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public float getValue() {
        return value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    private String calculateHash() {
        sequence++; // increase sequence to avoid duplicate transactions
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)
                        + StringUtil.getStringFromKey(recipient)
                        + value
                        + sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        final String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        this.signature = StringUtil.applyECDSASignature(privateKey, data);
    }

    private boolean verifySignature() {
        final String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASignature(sender, data, signature);
    }

    public boolean processTransaction() throws TransactionException {
        if (!verifySignature()) {
            throw new TransactionException(ErrorMessage.SIGNATURE_ERROR);
        }
        // gather unspent outputs
        for (TransactionInput transactionInput : this.inputs) {
            transactionInput.setUTXO(NoobChain.UTXOs.get(transactionInput.getTransactionOutputId()));
        }

        // check if transaction is valid
        if (getInputsValue() < MINIMUM_TRANSACTION) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs
        float leftOver = getInputsValue() - value;
        this.transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // first output
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // second output

        // add outputs to UTXOs
        for (TransactionOutput transactionOutput : outputs) {
            NoobChain.UTXOs.put(transactionOutput.getId(), transactionOutput);
        }
        // remove transaction inputs from UTXO lists
        for (TransactionInput transactionInput : inputs) {
            if (transactionInput.getUTXO() != null) {
                NoobChain.UTXOs.remove(transactionInput.getUTXO().getId());
            }
        }
        return true;
    }

    // calculate sum of transaction inputs
    private float getInputsValue() {
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null) continue;
            total += input.getUTXO().getValue();
        }
        return total;
    }

    // calculate sum of transaction outputs
    private float getOutputsValue() {
        float total = 0;
        for (TransactionOutput output : outputs) {
            if (output != null) {
                total += output.getValue();
            }
        }
        return total;
    }
}
