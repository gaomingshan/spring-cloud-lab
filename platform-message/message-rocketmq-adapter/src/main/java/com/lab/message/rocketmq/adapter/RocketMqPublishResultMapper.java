package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.PublishResult;
import com.lab.message.contract.PublishStatus;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

public class RocketMqPublishResultMapper {
    public PublishResult sent(EventEnvelope<?> event, SendResult result) {
        if (result != null && result.getSendStatus() == SendStatus.SEND_OK) {
            return new PublishResult(event.eventId(), PublishStatus.SENT, result.getMsgId(), null);
        }
        String status = result == null || result.getSendStatus() == null ? "no send result" : result.getSendStatus().name();
        return failed(event, "ROCKETMQ_SEND_FAILED: " + status, null);
    }

    public PublishResult accepted(EventEnvelope<?> event, SendResult result) {
        if (result != null && result.getSendStatus() == SendStatus.SEND_OK) {
            return new PublishResult(event.eventId(), PublishStatus.ACCEPTED, result.getMsgId(), null);
        }
        String status = result == null || result.getSendStatus() == null ? "no send result" : result.getSendStatus().name();
        return failed(event, "ROCKETMQ_TRANSACTION_SUBMISSION_FAILED: " + status, null);
    }

    public PublishResult failed(EventEnvelope<?> event, String reason, Throwable cause) {
        String detail = cause == null ? null : cause.getMessage();
        return new PublishResult(event == null ? null : event.eventId(), PublishStatus.FAILED, null,
                detail == null || detail.isBlank() ? reason : reason + ": " + detail);
    }
}
