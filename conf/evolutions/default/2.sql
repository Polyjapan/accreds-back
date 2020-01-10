-- !Ups

alter table accreds
    add stage_name varchar(200) null;

alter table accreds
    add must_contact_admin boolean default false not null;

-- !Downs

alter table accreds
    drop column stage_name;

alter table accreds
    drop column must_contact_admin;

