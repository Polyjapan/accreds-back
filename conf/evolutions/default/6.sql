-- !Ups

alter table staff_accounts change autored_by authored_by int null;

-- !Downs

alter table staff_accounts change authored_by autored_by int null;
