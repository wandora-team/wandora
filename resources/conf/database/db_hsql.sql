create cached table TOPIC(
TOPICID varchar(256) primary key,
BASENAME longvarchar,
SUBJECTLOCATOR longvarchar 
);

create cached table TOPICTYPE(
TOPIC varchar(256) not null,
TYPE varchar(256) not null,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create cached table DATA(
TOPIC varchar(256) not null,
TYPE varchar(256) not null,
VERSION varchar(256) not null,
DATA longvarchar,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
foreign key (VERSION) references TOPIC(TOPICID),
primary key(TOPIC,TYPE,VERSION)
);

create cached table VARIANT(
VARIANTID varchar(256) primary key,
TOPIC varchar(256) not null,
VALUE longvarchar,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create cached table VARIANTSCOPE(
VARIANT varchar(256) not null,
TOPIC varchar(256) not null,
foreign key (VARIANT) references VARIANT(VARIANTID),
foreign key (TOPIC) references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create cached table SUBJECTIDENTIFIER(
SI varchar(1000000) primary key,
TOPIC varchar(256) not null,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create cached table ASSOCIATION(
ASSOCIATIONID varchar(256) primary key,
TYPE varchar(256) not null,
foreign key (TYPE) references TOPIC(TOPICID)
);

create cached table MEMBER(
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
