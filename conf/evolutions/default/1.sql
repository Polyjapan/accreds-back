-- !Ups

create table accred_types
(
    accred_type_id int auto_increment
        primary key,
    accred_type_name varchar(250) null,
    requires_signature tinyint(1) null,
    is_temporary tinyint(1) null
);

create table events
(
    event_id int auto_increment
        primary key,
    event_name varchar(150) null
);

create table physical_accred_types
(
    physical_accred_type_id int auto_increment
        primary key,
    physical_accred_type_name varchar(250) null
);

create table accred_type_mappings
(
    event_id int not null,
    accred_type_id int not null,
    physical_accred_type_id int not null,
    primary key (event_id, accred_type_id, physical_accred_type_id),
    constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade,
    constraint accred_type_mappings_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade,
    constraint accred_type_physical_fk
        foreign key (physical_accred_type_id) references physical_accred_types (physical_accred_type_id)
            on delete cascade
);

create table vip_desks
(
    vip_desk_id int auto_increment
        primary key,
    vip_desk_name varchar(250) null
);

create table accreds
(
    accred_id int auto_increment
        primary key,
    event_id int null,
    lastname varchar(100) null,
    firstname varchar(100) null,
    body_name varchar(200) null,
    details text null,
    authored_by int null,
    accred_type_id int null,
    status set('GRANTED', 'DELIVERED', 'RECOVERED') default 'GRANTED' null,
    prefered_vip_desk int null,
    constraint accreds_accred_types_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id),
    constraint accreds_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade,
    constraint accreds_vip_desks_vip_desk_id_fk
        foreign key (prefered_vip_desk) references vip_desks (vip_desk_id)
);

create table staff_accounts
(
    staff_account_id int auto_increment
        primary key,
    event_id int null,
    vip_desk_id int null,
    name varchar(250) null,
    autored_by int null,
    authored_at timestamp default current_timestamp() null,
    constraint staff_accounts_events_event_id_fk
        foreign key (event_id) references events (event_id),
    constraint staff_accounts_vip_desks_vip_desk_id_fk
        foreign key (vip_desk_id) references vip_desks (vip_desk_id)
);

create table accred_logs
(
    accred_log_id int auto_increment
        primary key,
    accred_log_time timestamp default current_timestamp() null,
    accred_id int null,
    authored_by_admin int null,
    authored_by_staff int null,
    source_state set('GRANTED', 'DELIVERED', 'RECOVERED') null,
    target_state set('GRANTED', 'DELIVERED', 'RECOVERED') null,
    remarks text null,
    constraint accred_logs_accreds_accred_id_fk
        foreign key (accred_id) references accreds (accred_id),
    constraint accred_logs_staff_accounts_staff_account_id_fk
        foreign key (authored_by_staff) references staff_accounts (staff_account_id)
);

-- !Downs