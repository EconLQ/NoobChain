package com.liquiduspro.singleton;

import com.liquiduspro.domain.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

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
        return IntStream.range(1, chain.size())
                .parallel()
                .allMatch(i -> {
                    Block current = chain.get(i);
                    Block previous = chain.get(i - 1);
                    return current != null && previous != null && current.isValid(previous);
                });
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
