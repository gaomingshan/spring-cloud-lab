create table if not exists reliable_message_outbox (
    id bigint not null auto_increment primary key,
    message_id varchar(64) not null,
    event_type varchar(512) not null,
    payload json not null,
    delivery_mode varchar(16) not null,
    status varchar(16) not null,
    attempt_count int not null,
    next_attempt_at timestamp(6) not null,
    locked_until timestamp(6) null,
    last_error text null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    sent_at timestamp(6) null,
    unique key uk_reliable_message_outbox_message_id (message_id),
    key idx_reliable_message_outbox_dispatch (status, next_attempt_at, id),
    key idx_reliable_message_outbox_lease (status, locked_until)
) engine=InnoDB;

create table if not exists reliable_message_processing (
    id bigint not null auto_increment primary key,
    consumer_scope varchar(256) not null,
    fingerprint varchar(256) not null,
    event_type varchar(512) not null,
    status varchar(16) not null,
    attempt_count int not null,
    lease_until timestamp(6) null,
    last_error text null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    completed_at timestamp(6) null,
    unique key uk_reliable_message_processing_fingerprint (consumer_scope, fingerprint),
    key idx_reliable_message_processing_lease (status, lease_until)
) engine=InnoDB;
