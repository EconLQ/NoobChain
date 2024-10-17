package com.liquiduspro.domain;

import com.liquiduspro.domain.transaction.Transaction;
import com.liquiduspro.domain.transaction.TransactionInput;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.StringUtil;
import com.liquiduspro.util.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Block {
    private static final Logger logger = LoggerFactory.getLogger(Block.class);
    private final String previousHash;
    private final long timeStamp;
    private final AtomicInteger nonce;
    private final List<Transaction> transactions = new ArrayList<>(); // <--- CHANGE LATER>
    private String merkleRoot;
    private String hash;
    public Block(final String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.nonce = new AtomicInteger(0);
        this.hash = calculateHash();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    // validate the block's hash
    public boolean isValid() {
        return hash.equals(calculateHash());
    }

    public String calculateHash() {
        final String hashToCalculate = previousHash + timeStamp + nonce + merkleRoot;
        return StringUtil.applySha256(hashToCalculate);
    }

    /**
     * This function is used to mine the block.
     *
     * @param difficulty the number of 0's needed to mine the block
     */
    public void mineBlock(int difficulty) {
        // create a string with difficulty * "0"
        this.merkleRoot = StringUtil.getMerkleRoot(transactions);
        final String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce.getAndIncrement();
            hash = calculateHash();
        }
        logger.info("Block Mined. Hash: {}", hash);
    }

    public boolean addTransaction(final Transaction transaction) {
        if (transaction == null) return false;
        if (!Objects.equals(previousHash, "0")) {
            try {
                if (!transaction.processTransaction()) {
                    logger.warn(ErrorMessage.INVALID_TRANSACTION);
                    return false;
                }
            } catch (TransactionException e) {
                logger.warn(e.getMessage());
                return false;
            }
        }

        if (transaction.getValue() < 0) {
            logger.error(ErrorMessage.INVALID_TRANSACTION_VALUE);
            return false;
        }
        if (!transaction.getInputs().isEmpty()) {
            for (TransactionInput input : transaction.getInputs()) {
                UTXOSet.getInstance().remove(input.getUTXO().getId());
            }
        }
        transactions.add(transaction);
        logger.info("Transaction successfully added to Block");
        return true;
    }
}
