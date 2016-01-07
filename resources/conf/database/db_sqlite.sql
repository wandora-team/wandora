create table TOPIC(
TOPICID varchar(256) primary key,
BASENAME text ,
SUBJECTLOCATOR varchar(900) 
);

create table TOPICTYPE(
TOPIC varchar(256) not null,
TYPE varchar(256) not null,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create table DATA(
TOPIC varchar(256) not null,
TYPE varchar(256) not null,
VERSION varchar(256) not null,
DATA text,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
foreign key (VERSION) references TOPIC(TOPICID),
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(256) primary key,
TOPIC varchar(256) not null,
VALUE text,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create table VARIANTSCOPE(
VARIANT varchar(256) not null,
TOPIC varchar(256) not null,
foreign key (VARIANT) references VARIANT(VARIANTID),
foreign key (TOPIC) references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI varchar(65535) primary key,
TOPIC varchar(256) not null,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create table ASSOCIATION(
ASSOCIATIONID varchar(256) primary key,
TYPE varchar(256) not null,
foreign key (TYPE) references TOPIC(TOPICID)
);

create table MEMBER(
ASSOCIATION varchar(256) not null,
PLAYER varchar(256) not null,
ROLE varchar(256) not null,
foreign key (ASSOCIATION) references ASSOCIATION(ASSOCIATIONID),
foreign key (PLAYER) references TOPIC(TOPICID),
foreign key (ROLE) references TOPIC(TOPICID),
primary key(ASSOCIATION,ROLE)
);

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
