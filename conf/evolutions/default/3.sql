-- !Ups

alter table accreds
    add deleted boolean default false not null;

-- !Downs

alter table accreds
    drop column deleted;


