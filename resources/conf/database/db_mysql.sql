create table TOPIC(
TOPICID varchar(256) collate latin1_general_cs primary key ,
BASENAME text,
SUBJECTLOCATOR text 
);

create table TOPICTYPE(
TOPIC varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
TYPE varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create table DATA(
TOPIC varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
TYPE varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
VERSION varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
DATA text,
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(256) collate latin1_general_cs primary key,
TOPIC varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
VALUE text
);

create table VARIANTSCOPE(
VARIANT varchar(256) collate latin1_general_cs not null references VARIANT(VARIANTID),
TOPIC varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI varchar(65535) collate latin1_general_cs primary key,
TOPIC varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID)
);

create table ASSOCIATION(
ASSOCIATIONID varchar(256) collate latin1_general_cs primary key,
TYPE varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID)
);

create table MEMBER(
ASSOCIATION varchar(256) collate latin1_general_cs not null references ASSOCIATION(ASSOCIATIONID),
PLAYER varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
ROLE varchar(256) collate latin1_general_cs not null references TOPIC(TOPICID),
primary key(ASSOCIATION,ROLE)
);

create index BASENAME_INDEX ON TOPIC(BASENAME(255));
create index SUBJECTLOCATOR_INDEX ON TOPIC(SUBJECTLOCATOR(255));
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


