-- !Ups

alter table accred_type_mappings
    add day int default 0 not null;

alter table accred_type_mappings
    drop constraint accred_type_mappings_accred_type_accred_type_id_fk;

alter table accred_type_mappings drop primary key;

alter table accred_type_mappings
    add primary key (accred_type_id, day);

alter table accred_type_mappings
    add constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade;

-- !Downs

alter table accred_type_mappings
    drop constraint accred_type_mappings_accred_type_accred_type_id_fk;

alter table accred_type_mappings drop primary key;

alter table accred_type_mappings drop column day;

alter table accred_type_mappings
    add primary key (accred_type_id);

alter table accred_type_mappings
    add constraint accred_type_mappings_accred_type_accred_type_id_fk
        foreign key (accred_type_id) references accred_types (accred_type_id)
            on delete cascade;

