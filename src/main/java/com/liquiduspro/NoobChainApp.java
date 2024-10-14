package com.liquiduspro;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class NoobChainApp {
    public static final int numOfBlocks = 50;
    private final static List<Block> blockchain = new ArrayList<>(); // <--- This is our global list of blocks, like a global variable>
    private final static int difficulty = 5;
    private static int blockCounter = 0;

    private static void addBlocksToChain(int numOfBlocks) {
        // add genesis block
        Block genesisBlock = new Block("I'm the genesis block", "0");
        blockchain.add(genesisBlock);
        blockCounter++;
        // add other block
        for (int i = 1; i < numOfBlocks; i++) {
            final String data = "Yo! I'm a block #" + blockCounter;
            final String previousHash = blockchain.get(blockchain.size() - 1).getHash();
            final Block newBlock = new Block(data, previousHash);
            blockchain.add(newBlock); // <--- add the new block to our blockchain
            System.out.println("Trying to Mine block: #" + blockCounter);
            blockchain.get(blockCounter++).mineBlock(difficulty);
        }
    }

    public static void main(String[] args) {
        System.out.println("Difficulty is: " + difficulty);
        System.out.println("\n==================Mine the NoobChain==================");
        long startTime = System.currentTimeMillis();
        addBlocksToChain(numOfBlocks);
        long endTime = System.currentTimeMillis();
        System.out.println("Total mining execution time: " + ((endTime - startTime) / 1000) + " seconds");
        // validate blockchain
        System.out.println("\n==================Validate NoobChain==================");
        System.out.println("\nBlockchain is Valid: " + isChainValid());

        // print blockchain
        String blockchainJSON = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\n==================The NoobChain==================");
        System.out.println(blockchainJSON);
    }

    private static Boolean isChainValid() {
        Block currentBlock, previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

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
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
            return true;
        }
        return true;
    }
}