package com.liquiduspro.singleton;

import com.liquiduspro.domain.transaction.TransactionOutput;
import com.liquiduspro.util.ErrorMessage;
import com.liquiduspro.util.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Global UTXOSet to store {@link TransactionOutput} for {@link com.liquiduspro.NoobChain}
 *
 * @author econlq
 */
public class UTXOSet {
    private static final Logger logger = LoggerFactory.getLogger(UTXOSet.class);
    private static final UTXOSet INSTANCE = new UTXOSet();
    private final Map<String, TransactionOutput> UTXOs = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private UTXOSet() {
    }

    public static UTXOSet getInstance() {
        return INSTANCE;
    }

    public Map<String, TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public void add(String id, TransactionOutput transactionOutput) {
        readWriteLock.writeLock().lock();
        try {
            UTXOs.put(id, transactionOutput);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public TransactionOutput get(String id) throws TransactionException {
        readWriteLock.readLock().lock();
        try {
            if (!UTXOs.containsKey(id)) {
                logger.error("Could not find UTXO: {}", id);
                throw new TransactionException(ErrorMessage.UTXO_NOT_FOUND);
            }
            return UTXOs.get(id);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public boolean contains(String id) {
        readWriteLock.readLock().lock();
        try {
            return UTXOs.containsKey(id);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Map<String, TransactionOutput> getAll() {
        readWriteLock.readLock().lock();
        try {
            return new HashMap<>(UTXOs);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public TransactionOutput remove(String id) {
        readWriteLock.writeLock().lock();
        try {
            if (UTXOs.containsKey(id)) {
                return UTXOs.remove(id);
            }
            return null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void clear() {
        readWriteLock.writeLock().lock();
        try {
            UTXOs.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
