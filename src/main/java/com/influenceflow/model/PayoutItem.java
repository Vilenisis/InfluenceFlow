package com.influenceflow.model;

import java.math.BigDecimal;

public class PayoutItem {
    private long id;
    private long payoutId;
    private long submissionId;
    private BigDecimal amount;

    public PayoutItem() {
    }

    public PayoutItem(long id, long payoutId, long submissionId, BigDecimal amount) {
        this.id = id;
        this.payoutId = payoutId;
        this.submissionId = submissionId;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(long payoutId) {
        this.payoutId = payoutId;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
