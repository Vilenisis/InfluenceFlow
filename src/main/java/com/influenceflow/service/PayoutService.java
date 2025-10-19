package com.influenceflow.service;

import com.influenceflow.dao.PayoutDao;
import com.influenceflow.model.Payout;
import com.influenceflow.model.PayoutItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PayoutService {
    private final PayoutDao payoutDao;

    public PayoutService(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public Payout createPayout(long creatorId, Map<Long, BigDecimal> submissionAmounts) {
        if (submissionAmounts.isEmpty()) {
            throw new IllegalArgumentException("Необходимо выбрать хотя бы одну утвержденную публикацию");
        }
        BigDecimal total = submissionAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Payout payout = new Payout();
        payout.setCreatorId(creatorId);
        payout.setTotalAmount(total);
        payout.setCreatedAt(LocalDateTime.now());
        payout.setStatus("PLANNED");
        payout = payoutDao.save(payout);

        List<PayoutItem> items = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : submissionAmounts.entrySet()) {
            PayoutItem item = new PayoutItem();
            item.setSubmissionId(entry.getKey());
            item.setAmount(entry.getValue());
            items.add(item);
        }
        payoutDao.saveItems(payout.getId(), items);
        return payout;
    }

    public List<Payout> getPlannedPayouts() {
        return payoutDao.findByStatus("PLANNED");
    }
}
