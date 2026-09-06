package com.lab.reliable.message.model;

public enum OutboxStatus {
    PENDING,
    SENDING,
    SENT,
    DEAD
}
