package com.liquiduspro;

import com.google.gson.GsonBuilder;
import com.liquiduspro.domain.Block;
import com.liquiduspro.domain.Wallet;
import com.liquiduspro.domain.transaction.Transaction;
import com.liquiduspro.domain.transaction.TransactionInput;
import com.liquiduspro.domain.transaction.TransactionOutput;
import com.liquiduspro.singleton.Blockchain;
import com.liquiduspro.singleton.UTXOSet;
import com.liquiduspro.util.Constants;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public class NoobChain {
    private static final Logger logger = LoggerFactory.getLogger(NoobChain.class);
    private final static Blockchain blockchain = Blockchain.getInstance();
    public static UTXOSet UTXOs = UTXOSet.getInstance();
    public static Wallet walletA;
    public static Wallet walletB;

    public static void run() {
        simulateTransactions(Constants.NUM_OF_TRANSACTIONS);
        System.out.println("\n==================Validate NoobChain==================");
        System.out.println("Is chain valid: " + isChainValid());
        System.out.println("\n==================NoobChain==================");
        String blockchainJSON = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(blockchain.getChain());
        System.out.println(blockchainJSON);
    }

    private static void simulateTransactions(int numOfTransactions) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        final Wallet binance = new Wallet();

        // create genesis transaction, which sends 100 NoobCoin to walletA
        Transaction genesisTransaction = createGenesisTransaction(binance);

        // create the rest of the blocks
        logger.info("Creating and Mining Genesis block... ");
        final Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);

        RandomGenerator randomGenerator = RandomGenerator.getDefault();
        // generate transactions and add them to the blockchain
        for (int i = 0; i < numOfTransactions; i++) {
            final String previousHash = blockchain.get(blockchain.size() - 1).getHash();
            final Block newBlock = new Block(previousHash);
            final float randomAmount = randomGenerator.nextFloat(100f);
            // send transactions randomly
            if (i % 2 == 0) {
                try {
                    newBlock.addTransaction(walletA.sendFunds(walletB.getPublicKey(), randomAmount));
                    logger.info("Wallet A attempting to send {} NoobCoins to Wallet B", randomAmount);
                } catch (TransactionException e) {
                    logger.warn(e.getMessage());
                }
            } else {
                try {
                    newBlock.addTransaction(walletB.sendFunds(walletA.getPublicKey(), randomAmount));
                    logger.info("Wallet B attempting to send {} NoobCoins to Wallet A", randomAmount);
                } catch (TransactionException e) {
                    logger.warn(e.getMessage());
                }
            }
            logger.info("Wallet A balance: {}", walletA.getBalance());
            logger.info("Wallet B balance: {}", walletB.getBalance());
            addBlock(newBlock);
        }
    }

    private static void addBlock(Block block) {
        block.mineBlock(Constants.DIFFICULTY);
        blockchain.addBlock(block);
    }

    private static Transaction createGenesisTransaction(final Wallet from) {
        // create genesis transaction, which sends 100 NoobCoin to walletA
        TransactionInput genesisTransactionInput = new TransactionInput("0");
        genesisTransactionInput.setUTXO(new TransactionOutput(from.getPublicKey(), Constants.INITIAL_AMOUNT_OF_COINS, genesisTransactionInput.getTransactionOutputId()));
        Transaction genesisTransaction = new Transaction(from.getPublicKey(), walletA.getPublicKey(), Constants.INITIAL_AMOUNT_OF_COINS, List.of(genesisTransactionInput));
        genesisTransaction.generateSignature(from.getPrivateKey());
        genesisTransaction.setTransactionId(genesisTransactionInput.getTransactionOutputId()); // manually set the transaction id
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
        // manually add the output of the genesis transaction to the UTXO list
        TransactionOutput genesisTransactionOutput = genesisTransaction.getOutputs().get(0);
        UTXOs.add(genesisTransactionOutput.getId(), genesisTransactionOutput);
        return genesisTransaction;
    }

    private static Boolean isChainValid() {
        Block currentBlock, previousBlock;
        final String hashTarget = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        final Map<String, TransactionOutput> tempUTXOs = new HashMap<>(UTXOs.getUTXOs());

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            final String calcCurrBlockHash = currentBlock.calculateHash();

            // check if hash is correct
            if (!currentBlock.getHash().equals(calcCurrBlockHash)) {
                logger.warn("Current Hashes not equal: {} {}", currentBlock.getHash(), calcCurrBlockHash);
                return false;
            }

            // check if previous hash is equal
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                logger.warn("Previous Hashes not equal: {} != {}", currentBlock.getPreviousHash(), previousBlock.getHash());
                return false;
            }
            // check if hash is less than target
            if (!currentBlock.getHash().substring(0, Constants.DIFFICULTY).equals(hashTarget)) {
                logger.warn("This block hasn't been mined");
                return false;
            }
            if (validateTransactions(currentBlock, tempUTXOs)) {
                logger.info("Block {} is valid", i);
                tempUTXOs.clear();
            }
        }
        return true;
    }

    private static boolean validateTransactions(Block currentBlock, Map<String, TransactionOutput> tempUTXOs) {
        if (currentBlock.getTransactions().isEmpty()) {
            logger.warn("This block has no transactions");
            return false;
        }
        for (Transaction transaction : currentBlock.getTransactions()) {
            // verify transaction
            if (!transaction.verifySignature()) {
                logger.warn(ErrorMessage.SIGNATURE_ERROR);
                return false;
            }
            float inputValue = 0;
            // check if transaction is valid
            for (TransactionInput input : transaction.getInputs()) {
                TransactionOutput referencedUTXO = tempUTXOs.get(input.getTransactionOutputId());
                if (referencedUTXO == null) {
                    logger.warn("Referenced UTXO not found in tempUTXOs");
                    return false;
                }
                if (input.getUTXO().getValue() != referencedUTXO.getValue()) {
                    logger.warn("Referenced UTXO input value is incorrect");
                    return false;
                }
                tempUTXOs.remove(input.getTransactionOutputId());
                inputValue += input.getUTXO().getValue();
            }
            float outputValue = 0;
            for (TransactionOutput output : transaction.getOutputs()) {
                outputValue += output.getValue();
                tempUTXOs.put(output.getId(), output);
            }
            if (inputValue != outputValue) {
                logger.warn("Inputs total value does not equal outputs total value");
                return false;
            }
            for (TransactionOutput output : transaction.getOutputs()) {
                tempUTXOs.put(output.getId(), output);
            }
        }
        return true;
    }
}