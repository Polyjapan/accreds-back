-- !Ups

alter table accreds
    drop foreign key accreds_events_event_id_fk;
alter table physical_accred_types
    drop foreign key physical_accred_types_events_event_id_fk;
alter table staff_accounts
    drop foreign key staff_accounts_events_event_id_fk;

drop table events;


-- !Downs

create table events
(
    event_id int auto_increment
        primary key,
    event_name varchar(150) null
);

alter table accreds
    add constraint accreds_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade;

alter table physical_accred_types
    add constraint physical_accred_types_events_event_id_fk
        foreign key (event_id) references events (event_id);

alter table staff_accounts
    add constraint staff_accounts_events_event_id_fk
        foreign key (event_id) references events (event_id);

