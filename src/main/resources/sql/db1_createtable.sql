-- TABLE
create table MYTBL (
    id  varchar(20) not null,
    name varchar(20),
    num number(3),
    constraint pk_mytbl PRIMARY KEY(id)
);

-- INDEX
create index idNum on MYTBL(num);
