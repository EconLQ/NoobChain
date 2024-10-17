package com.liquiduspro;

import com.liquiduspro.domain.Block;
import com.liquiduspro.domain.UTXOSet;
import com.liquiduspro.domain.Wallet;
import com.liquiduspro.domain.transaction.Transaction;
import com.liquiduspro.domain.transaction.TransactionInput;
import com.liquiduspro.domain.transaction.TransactionOutput;
import com.liquiduspro.util.Constants;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public class NoobChain {
    private static final Logger logger = LoggerFactory.getLogger(NoobChain.class);
    private final static List<Block> blockchain = new ArrayList<>(); // <--- This is our global list of blocks, like a global variable>
    public static UTXOSet UTXOs = UTXOSet.getInstance(); // <--- This is our global map of UTXOs>
    public static Wallet walletA;
    public static Wallet walletB;
    private static Transaction genesisTransaction;
    private static int blockCounter = 0;

    private static void addBlocksToChain() {
        // add genesis block
        if (blockchain.isEmpty()) {
            System.out.println("Creating and Mining Genesis block... ");
            final Block genesisBlock = new Block("0");
//            genesisBlock.mineBlock(Constants.DIFFICULTY);
            genesisBlock.addTransaction(genesisTransaction);
            blockchain.add(genesisBlock);
            blockCounter++;
        }

        // add rest of the blocks
        for (int i = 1; i < Constants.NUM_OF_BLOCKS; i++) {
            final String previousHash = blockchain.get(blockchain.size() - 1).getHash();
            final Block newBlock = new Block(previousHash);
            final float randomAmount = RandomGenerator.getDefault().nextFloat(100f);
            // send transactions
            newBlock.addTransaction(walletA.sendFunds(walletB.getPublicKey(), randomAmount));
            System.out.println("Wallet A balance: " + walletA.getBalance());
            if (i % 2 == 0) {
                newBlock.addTransaction(walletB.sendFunds(walletA.getPublicKey(), randomAmount));
                System.out.println("Wallet B balance: " + walletB.getBalance());
            }
            blockchain.add(newBlock); // <--- add the new block to our blockchain
            System.out.println("Trying to Mine block: #" + blockCounter);
//            blockchain.get(blockCounter++).mineBlock(Constants.DIFFICULTY);
        }
    }

    public static void testNoobChain() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet binance = new Wallet();

        // create genesis transaction, which sends 100 NoobCoin to walletA
        Transaction genesisTransaction = createGenesisTransaction(binance);

        // create the rest of the blocks
        logger.info("Creating and Mining Genesis block... ");
        final Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);

        Block block1 = new Block(genesisBlock.getHash());
        logger.info("Wallet A's balance is: {}", walletA.getBalance());
        logger.info("Wallet A is Attempting to send funds (40) to WalletB...");
        try {
            block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        addBlock(block1);
        logger.info("Wallet A's balance is: {}", walletA.getBalance());
        logger.info("Wallet B's balance is: {}", walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        logger.info("Wallet A Attempting to send more funds (1000) than it has...");
        try {
            block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        addBlock(block2);
        logger.info("Wallet A's balance is: {}", walletA.getBalance());
        logger.info("Wallet B's balance is: {}", walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        logger.info("Wallet B is Attempting to send funds (20) to Wallet A...");
        try {
            block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        logger.info("Wallet A's balance is: {}", walletA.getBalance());
        logger.info("Wallet B's balance is: {}", walletB.getBalance());

        logger.info("Is chain valid: {}", isChainValid());
    }

    private static void addBlock(Block block) {
        block.mineBlock(Constants.DIFFICULTY);
        blockchain.add(block);
        blockCounter++;
    }

    private static Transaction createGenesisTransaction(final Wallet from) {
        // create genesis transaction, which sends 100 NoobCoin to walletA
        TransactionInput genesisTransactionInput = new TransactionInput("0");
        genesisTransactionInput.setUTXO(new TransactionOutput(from.getPublicKey(), Constants.INITIAL_AMOUNT_OF_COINS, genesisTransactionInput.getTransactionOutputId()));
        genesisTransaction = new Transaction(from.getPublicKey(), walletA.getPublicKey(), Constants.INITIAL_AMOUNT_OF_COINS, List.of(genesisTransactionInput));
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