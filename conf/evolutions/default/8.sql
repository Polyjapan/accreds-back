-- !Ups

alter table physical_accred_types
    add event_id int default 1 not null;

alter table physical_accred_types
    add constraint physical_accred_types_events_event_id_fk
        foreign key (event_id) references events (event_id);

alter table accred_type_mappings drop foreign key accred_type_mappings_events_event_id_fk;

alter table accred_type_mappings drop primary key;

alter table accred_type_mappings drop column event_id;

alter table accred_type_mappings
    add primary key (accred_type_id);

-- !Downs

alter table physical_accred_types
    drop constraint physical_accred_types_events_event_id_fk;

alter table physical_accred_types
    drop event_id;


alter table accred_type_mappings
    drop constraint accred_type_mappings_accred_type_accred_type_id_fk;

alter table accred_type_mappings
    add event_id int default 1 not null;

alter table accred_type_mappings
    drop primary key;

alter table accred_type_mappings
    add primary key (event_id, accred_type_id);

alter table accred_type_mappings
    add constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade;

alter table accred_type_mappings
    add constraint accred_type_mappings_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade;