package com.liquiduspro;

import com.liquiduspro.domain.Block;
import com.liquiduspro.domain.Wallet;
import com.liquiduspro.domain.transaction.Transaction;
import com.liquiduspro.domain.transaction.TransactionInput;
import com.liquiduspro.domain.transaction.TransactionOutput;
import com.liquiduspro.util.Constants;
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
    public static Map<String, TransactionOutput> UTXOs = new HashMap<>(); // <--- This is our global map of UTXOs>
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
        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        // create the rest of the blocks
        System.out.println("Creating and Mining Genesis block... ");
        final Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);

        Block block1 = new Block(genesisBlock.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        try {
            block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        addBlock(block1);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWallet A Attempting to send more funds (1000) than it has...");
        try {
            block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        addBlock(block2);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWallet B is Attempting to send funds (20) to Wallet A...");
        try {
            block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20));
        } catch (TransactionException e) {
            logger.warn(e.getMessage());
        }
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        System.out.println("Is chain valid: " + isChainValid());
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
        genesisTransaction.setTransactionId("0"); // manually set the transaction id
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
        return genesisTransaction;
    }

    private static Boolean isChainValid() {
        Block currentBlock, previousBlock;
        String hashTarget = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        Map<String, TransactionOutput> tempUTXOs = new HashMap<>();
        TransactionOutput genesisTransactionOutput = genesisTransaction.getOutputs().get(0);
        tempUTXOs.put(genesisTransactionOutput.getId(), genesisTransactionOutput);

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            final String calcCurrBlockHash = currentBlock.calculateHash();

            // check if hash is correct
            if (!currentBlock.getHash().equals(calcCurrBlockHash)) {
                System.out.println("Current Hashes not equal: " + currentBlock.getHash() + " " + calcCurrBlockHash);
                return false;
            }

            // check if previous hash is equal
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Previous Hashes not equal: " + currentBlock.getPreviousHash() + " != " + previousBlock.getHash());
                return false;
            }
            // check if hash is less than target
            if (!currentBlock.getHash().substring(0, Constants.DIFFICULTY).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
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
        // loop through block's transactions
        TransactionOutput tempOutput;
        for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
            String transactionHash = currentBlock.getTransactions().get(t).getTransactionId();
            final List<TransactionInput> tempInputs = currentBlock.getTransactions().get(t).getInputs();
            if (!tempInputs.isEmpty()) {
                for (TransactionInput tempInput : tempInputs) {
                    String tempInputTxId = tempInput.getTransactionOutputId();
                    tempOutput = tempInput.getUTXO();
                    if (!tempInputTxId.equals(transactionHash) || tempOutput == null) {
                        logger.warn("Referenced input isn't correct");
                        return false;
                    }
                    if (tempUTXOs.get(tempInputTxId) == null) {
                        logger.warn("Referenced output isn't correct");
                        return false;
                    }
                    tempUTXOs.remove(tempInputTxId);
                }
            }
            final List<TransactionOutput> tempOutputs = currentBlock.getTransactions().get(t).getOutputs();
            if (!tempOutputs.isEmpty()) {
                for (TransactionOutput output : tempOutputs) {
                    tempOutput = output;
                    tempOutput.setId(transactionHash);
                    tempUTXOs.put(transactionHash, tempOutput);
                }
            }
        }

        if (!tempUTXOs.isEmpty()) {
            logger.warn("Transaction(s) not signed correctly");
            return false;
        } else {
            logger.info("Blockchain is valid");
            for (Block block : blockchain) {
                logger.info("Block Hash: {}", block.getHash());
            }
            return true;
        }
    }
}