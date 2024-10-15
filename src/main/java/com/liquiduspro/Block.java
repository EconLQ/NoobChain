package com.liquiduspro;

import com.liquiduspro.util.StringUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public final class Block {
    private final String previousHash;
    private final long timeStamp;
    private final String data;
    private String hash;
    private final AtomicInteger nonce;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.nonce = new AtomicInteger(0);
        this.hash = calculateHash();
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

    public String getData() {
        return data;
    }

    // validate the block's hash
    public boolean isValid() {
        return hash.equals(calculateHash());
    }

    public String calculateHash() {
        final String hashToCalculate = previousHash + timeStamp + nonce + data;
        return StringUtil.applySha256(hashToCalculate);
    }

    /**
     * This function is used to mine the block.
     *
     * @param difficulty the number of 0's needed to mine the block
     */
    public void mineBlock(int difficulty) {
        // create a string with difficulty * "0"
        final String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce.getAndIncrement();
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
}
