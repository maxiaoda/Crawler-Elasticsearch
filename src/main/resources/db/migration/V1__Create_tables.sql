create table LINKS_TO_BE_PROCESSED(
link varchar(2000)
);

create table LINKS_ALREADY_PROCESSED(
link varchar(2000)
);

create table NEWS(
ID    bigint primary key auto_increment,
TITLE text,
CONTENT text,
URL varchar(2000),
CREATED_AT timestamp default now(),
MODIFIED_AT timestamp default now()
);