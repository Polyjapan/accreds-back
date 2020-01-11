-- !Ups

create table admin_one_time_delegations
(
    admin_user_id int null,
    grant_key varchar(16) not null
        primary key,
    creation_time timestamp default current_timestamp() null
);


-- !Downs

drop table admin_one_time_delegations;


