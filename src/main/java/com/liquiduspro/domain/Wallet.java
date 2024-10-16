package com.liquiduspro.domain;

import com.liquiduspro.NoobChain;
import com.liquiduspro.domain.transaction.Transaction;
import com.liquiduspro.domain.transaction.TransactionInput;
import com.liquiduspro.domain.transaction.TransactionOutput;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses <a href="https://en.wikipedia.org/wiki/Elliptic-curve_cryptography">Elliptic-curve cryptography</a> to Generate {@link KeyPair}.
 */
public final class Wallet {
    private static final Logger logger = LoggerFactory.getLogger(Wallet.class);
    private Map<String, TransactionOutput> UTXOs = new HashMap<>(); //list of all unspent transactions.>
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Wallet() {
        generateKeyPair();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // init the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            logger.warn("Error while generating key pair: {}", e.toString());
            throw new RuntimeException(e);
        }
    }

    // returns a balance and stores the UTXO's owned by THIS wallet in this.UTXOs
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : NoobChain.UTXOs.entrySet()) {
            final TransactionOutput UTXO = item.getValue();
            //if output belongs to me add it to the list of unspent transactions
            if (UTXO.isMine(this.publicKey)) {
                this.UTXOs.put(UTXO.getId(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey _recipient, float value) throws TransactionException {
        if (getBalance() < value) {
            throw new TransactionException(ErrorMessage.NO_ENOUGH_FUNDS + ". Current Balance: " + getBalance());
        }
        List<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : this.UTXOs.entrySet()) {
            final TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            final TransactionInput input = new TransactionInput(UTXO.getId());
//            input.setUTXO(UTXO);
            inputs.add(input);
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(this.publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(this.privateKey);
        for (TransactionInput input : inputs) {
//            this.UTXOs.remove(input.getUTXO().getId());
            this.UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }
}