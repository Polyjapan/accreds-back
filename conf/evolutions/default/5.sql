-- !Ups

alter table accreds
    add require_real_name_on_delivery boolean default false not null;

alter table accred_logs
    add accred_number VARCHAR(10) default NULL null;

alter table physical_accred_types
    add physical_accred_type_numbered boolean default false not null;


-- !Downs

alter table accreds
    drop column require_real_name_on_delivery;

alter table accred_logs
    drop column accred_number;

alter table physical_accred_types
    drop column physical_accred_type_numbered;
