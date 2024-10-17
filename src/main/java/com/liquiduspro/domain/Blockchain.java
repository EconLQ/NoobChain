package com.liquiduspro.domain;

import com.liquiduspro.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstraction of the (in-memory) blockchain for the {@link com.liquiduspro.NoobChain} and {@link com.liquiduspro.ParallelNoobChain}
 *
 * @author econlq
 */
public class Blockchain {
    public static final Logger logger = LoggerFactory.getLogger(Blockchain.class);
    private static final Blockchain INSTANCE = new Blockchain();
    private final List<Block> chain = new ArrayList<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private Blockchain() {
    }

    public static Blockchain getInstance() {
        return INSTANCE;
    }

    public boolean validateChain() {
        rwLock.readLock().lock();
        try {
            for (int i = 1; i < chain.size(); i++) {
                final Block currentBlock = chain.get(i);
                final String blockNumber = "Block #" + i;
                // check if hash is correct
                if (!currentBlock.isValid()) {
                    logger.warn("{} has an invalid hash", blockNumber);
                    return false;
                }

                // check if previous hash is equal for non-genesis blocks
                final Block previousBlock = chain.get(i - 1);
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
            rwLock.readLock().unlock();
        }
    }

    public List<Block> getChain() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(chain);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void add(Block block) {
        rwLock.writeLock().lock();
        try {
            chain.add(block);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public boolean addBlock(Block block) {
        rwLock.writeLock().lock();
        try {
            final int index = chain.size() - 1;
            final String latestBlockHash = getLatestBlockHash();
            if (chain.isEmpty() || block.getPreviousHash().equals(latestBlockHash)) {
                chain.add(block);
                logger.info("Added Block #{} to the blockchain. Hash: {}", index, block.getHash());
                return true;
            } else {
                logger.warn("Block #{} has an invalid previous hash: {} != {}", index, block.getPreviousHash(), latestBlockHash);
                return false;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public String getLatestBlockHash() {
        rwLock.readLock().lock();
        try {
            if (chain.isEmpty()) {
                // this is the genesis block
                return "0";
            } else {
                return chain.get(chain.size() - 1).getHash();
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Block get(int index) {
        rwLock.readLock().lock();
        try {
            if (index < 0 || index >= chain.size()) {
                logger.error("Invalid index: {}", index);
                return null;
            }
            return chain.get(index);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int size() {
        rwLock.readLock().lock();
        try {
            return chain.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
