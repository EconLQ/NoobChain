package com.liquiduspro;

import com.google.gson.GsonBuilder;
import com.liquiduspro.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Uses {@link java.util.concurrent.ExecutorService} and {@link java.util.concurrent.Future}
 * to parallelize the mining process.
 * Average performance gain is ~2.5x faster than sequential
 * <ul>Blockchain mining performance (Blocks #: 100. Difficulty: 5):
 *     <li>Sequential = Total mining execution time: 115 seconds</li>
 *     <li>Parallel = Total mining execution time: 51 seconds</li>
 * </ul>
 */
public class ParallelNoobChainApp {
    private static final Logger logger = LoggerFactory.getLogger(ParallelNoobChainApp.class);
    private static final List<Block> blockchain = new ArrayList<>();
    private static final ReentrantLock blockchainLock = new ReentrantLock();

    public static void main(String[] args) {
        System.out.println("Available Processors: " + Constants.AVAILABLE_PROCESSORS);
        System.out.println("Difficulty: " + Constants.DIFFICULTY);
        System.out.println("Number of Blocks: " + Constants.NUM_OF_BLOCKS);
        System.out.println("============================================");
        runParallelMining();
        printNoobChain();
    }

    private static void printNoobChain() {
        System.out.println("\n==================The Parallel NoobChain==================");
        String blockchainJSON = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainJSON);
    }

    // parallel blockchain mining
    public static void parallelMining(int numberOfThreads, int numOfBlocks) {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<Block>> futureList = new ArrayList<Future<Block>>();

        // Mine the first block separately
        Block genesisBlock = new Block("Im the genesis block", "0");
        genesisBlock.mineBlock(Constants.DIFFICULTY);
        blockchain.add(genesisBlock);

        for (int i = 1; i < numOfBlocks; i++) {
            final int index = i;
            futureList.add(executor.submit(() -> {
                String data = "Yo! Im a block #" + index;
                Block block;
                while (true) {
                    String latestHash = getLatestBlockHash();
                    block = new Block(data, latestHash);
                    block.mineBlock(Constants.DIFFICULTY);
                    // check if block is valid
                    if (addBlockToBlockchain(block)) break;
                    logger.info("Block #{} was not added to the blockchain. Retrying...", index);
                }
                return block;
            }));
        }
        for (Future<Block> future : futureList) {
            try {
                Block block = future.get();
                addBlockToBlockchain(block);
            } catch (ExecutionException | InterruptedException e) {
                logger.warn(e.toString());
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }

    private static boolean addBlockToBlockchain(Block block) {
        blockchainLock.lock();
        try {
            final int index = blockchain.size() - 1;
            final String latestBlockHash = getLatestBlockHash();
            if (blockchain.isEmpty() || block.getPreviousHash().equals(latestBlockHash)) {
                blockchain.add(block);
                logger.info("Added Block #{} to the blockchain. Hash: {}", index, block.getHash());
                return true;
            } else {
                logger.warn("Block #{} has an invalid previous hash: {} != {}", index, block.getPreviousHash(), latestBlockHash);
                return false;
            }
        } finally {
            blockchainLock.unlock();
        }
    }


    private static String getLatestBlockHash() {
        blockchainLock.lock();
        try {
            if (blockchain.isEmpty()) {
                // this is the genesis block
                return "0";
            } else {
                return blockchain.get(blockchain.size() - 1).getHash();
            }
        } finally {
            blockchainLock.unlock();
        }
    }

    private static boolean validateChain() {
        blockchainLock.lock();
        try {
            for (int i = 1; i < blockchain.size(); i++) {
                final Block currentBlock = blockchain.get(i);
                final String blockNumber = "Block #" + i;
                // check if hash is correct
                if (!currentBlock.isValid()) {
                    logger.warn("{} has an invalid hash", blockNumber);
                    return false;
                }

                // check if previous hash is equal for non-genesis blocks
                final Block previousBlock = blockchain.get(i - 1);
                if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                    logger.warn("{} has an invalid previous hash: {} != {}", blockNumber, currentBlock.getPreviousHash(), previousBlock.getHash());
                    return false;
                }

                // check if hash is less than target
                final String hashTarget = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
                if (!currentBlock.getHash().substring(0, Constants.DIFFICULTY).equals(hashTarget)) {
                    logger.warn("{} hasn't been mined properly", blockNumber);
                    return false;
                }
            }
            return true;
        } finally {
            blockchainLock.unlock();
        }
    }

    private static void runParallelMining() {
        System.out.println("===========================Parallel Mining===========================");
        long startTime = System.currentTimeMillis();
        parallelMining(Constants.AVAILABLE_PROCESSORS, Constants.NUM_OF_BLOCKS);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal mining execution time: " + ((endTime - startTime) / 1000) + " seconds");
        // validate blockchain
        System.out.println("\n==================Validate NoobChain==================");
        boolean isValid = validateChain();
        System.out.println("\nBlockchain is Valid: " + isValid);

    }
}
