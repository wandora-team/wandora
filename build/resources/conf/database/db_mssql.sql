create table TOPIC(
TOPICID varchar(50) collate SQL_Latin1_General_Cp1_CS_AS primary key,
BASENAME varchar(900) collate SQL_Latin1_General_Cp1_CS_AS ,
SUBJECTLOCATOR varchar(900) collate SQL_Latin1_General_Cp1_CS_AS 
);

create table TOPICTYPE(
TOPIC varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
TYPE varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create table DATA(
TOPIC varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
TYPE varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
VERSION varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
DATA text,
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(50) collate SQL_Latin1_General_Cp1_CS_AS primary key,
TOPIC varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
VALUE text
);

create table VARIANTSCOPE(
VARIANT varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references VARIANT(VARIANTID),
TOPIC varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI varchar(255) collate SQL_Latin1_General_Cp1_CS_AS primary key,
TOPIC varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID)
);

create table ASSOCIATION(
ASSOCIATIONID varchar(50) collate SQL_Latin1_General_Cp1_CS_AS primary key,
TYPE varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID)
);

create table MEMBER(
ASSOCIATION varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references ASSOCIATION(ASSOCIATIONID),
PLAYER varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
ROLE varchar(50) collate SQL_Latin1_General_Cp1_CS_AS not null references TOPIC(TOPICID),
primary key(ASSOCIATION,ROLE)
);

/* use these for mysql
create index BASENAME_INDEX ON TOPIC(BASENAME(255));
create index SUBJECTLOCATOR_INDEX ON TOPIC(SUBJECTLOCATOR(255));
*/
create index BASENAME_INDEX ON TOPIC(BASENAME);
create index SUBJECTLOCATOR_INDEX ON TOPIC(SUBJECTLOCATOR);
create index SITOPIC_INDEX on SUBJECTIDENTIFIER(TOPIC);
create index ASSOCTYPE_INDEX on ASSOCIATION(TYPE);
create index MEMBERPLAYER_INDEX on MEMBER(PLAYER);
create index MEMBERROLE_INDEX on MEMBER(ROLE);
create index VARIANTTOPIC_INDEX on VARIANT(TOPIC);
create index DATATOPIC_INDEX on DATA(TOPIC);
create index DATATYPE_INDEX on DATA(TYPE);
create index DATAVERSION_INDEX on DATA(VERSION);
create index TOPICTYPETOPIC_INDEX on TOPICTYPE(TOPIC);
create index TOPICTYPETYPE_INDEX on TOPICTYPE(TYPE);
