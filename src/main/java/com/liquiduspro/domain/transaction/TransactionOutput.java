package com.liquiduspro.domain.transaction;

import com.liquiduspro.util.StringUtil;

import java.security.PublicKey;
import java.util.Objects;

public class TransactionOutput {
    private final float value; // the amount of coins they own
    private final String parentTransactionId; // the transaction that created these coins
    private final PublicKey recipient; // new owner of these coins
    private String id;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + value + parentTransactionId);
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public float getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMine(final PublicKey publicKey) {
        return publicKey == recipient;
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "value=" + value +
                ", parentTransactionId='" + parentTransactionId + '\'' +
                ", recipient=" + recipient +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionOutput that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
