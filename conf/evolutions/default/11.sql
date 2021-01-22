-- !Ups

alter table accred_types
    add event_id int null after accred_type_id;

update accred_types set accred_types.event_id = 3 where 1 = 1; -- When creating, all accreds are in event id 3

alter table accred_types modify event_id int not null;

alter table vip_desks
    add event_id int null after vip_desk_id;

update vip_desks set vip_desks.event_id = 3 where 1 = 1; -- When creating, all accreds are in event id 3

alter table vip_desks modify event_id int not null;

-- !Downs

alter table accred_types
    drop event_id;

alter table vip_desks
    drop event_id;