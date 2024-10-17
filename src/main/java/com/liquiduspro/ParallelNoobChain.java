package com.liquiduspro;

import com.google.gson.GsonBuilder;
import com.liquiduspro.domain.Block;
import com.liquiduspro.domain.Blockchain;
import com.liquiduspro.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Uses {@link java.util.concurrent.ExecutorService} and {@link java.util.concurrent.Future}
 * to parallelize the mining process.
 * Average performance gain is ~2.5x faster than sequential
 * <ul>Blockchain mining performance (Blocks #: 100. Difficulty: 5):
 *     <li>Sequential = Total mining execution time: 115 seconds</li>
 *     <li>Parallel = Total mining execution time: 51 seconds</li>
 * </ul>
 *
 * @author econlq
 */
public class ParallelNoobChain {
    private static final Logger logger = LoggerFactory.getLogger(ParallelNoobChain.class);
    private static final Blockchain BLOCKCHAIN = Blockchain.getInstance();

    public static void run() {
        printConstants();
        runParallelMining();
//        printNoobChain();
    }

    private static void printConstants() {
        System.out.println("Available Processors: " + Constants.AVAILABLE_PROCESSORS);
        System.out.println("Difficulty: " + Constants.DIFFICULTY);
        System.out.println("Number of Blocks: " + Constants.NUM_OF_BLOCKS);
    }

    private static void printNoobChain() {
        System.out.println("\n==================The Parallel NoobChain==================");
        String blockchainJSON = new GsonBuilder().setPrettyPrinting().create().toJson(BLOCKCHAIN.getChain());
        System.out.println(blockchainJSON);
    }

    // parallel blockchain mining
    private static void parallelMining(int numberOfThreads, int numOfBlocks) {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<Block>> futureList = new ArrayList<Future<Block>>();

        // Mine the first block separately
        if (BLOCKCHAIN.size() == 0) {
            Block genesisBlock = new Block("0");
            genesisBlock.mineBlock(Constants.DIFFICULTY);
            BLOCKCHAIN.getChain().add(genesisBlock);
        }

        for (int i = 1; i < numOfBlocks; i++) {
            final int index = i;
            futureList.add(executor.submit(() -> {
                String data = "Yo! Im a block #" + index;
                Block block;
                while (true) {
                    String latestHash = BLOCKCHAIN.getLatestBlockHash();
                    block = new Block(latestHash);
                    block.mineBlock(Constants.DIFFICULTY);
                    // check if block is valid
                    if (BLOCKCHAIN.addBlock(block)) break;
                    logger.info("Block #{} was not added to the blockchain. Retrying...", index);
                }
                return block;
            }));
        }
        for (Future<Block> future : futureList) {
            try {
                Block block = future.get();
                BLOCKCHAIN.getChain().add(block); // add verified block to the blockchain
            } catch (ExecutionException | InterruptedException e) {
                logger.warn(e.toString());
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }

    private static void runParallelMining() {
        System.out.println("===========================Parallel Mining===========================");
        long startTime = System.currentTimeMillis();
        parallelMining(Constants.AVAILABLE_PROCESSORS, Constants.NUM_OF_BLOCKS);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTotal mining execution time: " + ((endTime - startTime) / 1000) + " seconds");
        // validate blockchain
        System.out.println("\n==================Validate NoobChain==================");
        boolean isValid = BLOCKCHAIN.validateChain();
        System.out.println("\nBlockchain is Valid: " + isValid);

    }
}
