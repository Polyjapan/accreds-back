-- !Ups

alter table accred_type_mappings drop foreign key accred_type_mappings_accred_type_accred_type_id_fk;

alter table accred_type_mappings drop foreign key accred_type_mappings_events_event_id_fk;

alter table accred_type_mappings drop foreign key accred_type_physical_fk;

alter table accred_type_mappings
    drop primary key;

alter table accred_type_mappings
    add constraint accred_type_mappings_pk
        primary key (event_id, accred_type_id),
    add constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade,
    add constraint accred_type_mappings_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade,
    add constraint accred_type_physical_fk
        foreign key (physical_accred_type_id) references physical_accred_types (physical_accred_type_id)
            on delete cascade;

-- !Downs

alter table accred_type_mappings drop foreign key accred_type_mappings_accred_type_accred_type_id_fk;

alter table accred_type_mappings drop foreign key accred_type_mappings_events_event_id_fk;

alter table accred_type_mappings drop foreign key accred_type_physical_fk;

alter table accred_type_mappings drop primary key;

alter table accred_type_mappings
    add constraint accred_type_mappings_pk
        primary key (event_id, accred_type_id, physical_accred_type_id),
    add constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade,
    add constraint accred_type_mappings_events_event_id_fk
        foreign key (event_id) references events (event_id)
            on delete cascade,
    add constraint accred_type_physical_fk
        foreign key (physical_accred_type_id) references physical_accred_types (physical_accred_type_id)
            on delete cascade;

