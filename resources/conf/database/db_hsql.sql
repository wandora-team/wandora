/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *
 *                       ...........................
 *
 *
 * This file contains Wandora's database creation statements for HyperSQL a.k.a 
 * HSQLDB database (http://hsqldb.org/). Creation statements should be used to
 * initialize a database before Wandora can use it as a database topic map.
 *
 * JDBC jar (included in Wandora's lib folder):
 * hsqldb.jar
 *
 * Driver class: 
 * org.hsqldb.jdbcDriver
 *
 * Connection string examples:
 * jdbc:hsqldb:mem:wandora
 * jdbc:hsqldb:file:C:\wandora\testdb
 *
 */


create cached table TOPIC(
TOPICID varchar(255) primary key,
BASENAME longvarchar,
SUBJECTLOCATOR longvarchar 
);

create cached table TOPICTYPE(
TOPIC varchar(255) not null,
TYPE varchar(255) not null,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create cached table DATA(
TOPIC varchar(255) not null,
TYPE varchar(255) not null,
VERSION varchar(255) not null,
DATA longvarchar,
foreign key (TOPIC) references TOPIC(TOPICID),
foreign key (TYPE) references TOPIC(TOPICID),
foreign key (VERSION) references TOPIC(TOPICID),
primary key(TOPIC,TYPE,VERSION)
);

create cached table VARIANT(
VARIANTID varchar(255) primary key,
TOPIC varchar(255) not null,
VALUE longvarchar,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create cached table VARIANTSCOPE(
VARIANT varchar(255) not null,
TOPIC varchar(255) not null,
foreign key (VARIANT) references VARIANT(VARIANTID),
foreign key (TOPIC) references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create cached table SUBJECTIDENTIFIER(
SI longvarchar primary key,
TOPIC varchar(255) not null,
foreign key (TOPIC) references TOPIC(TOPICID)
);

create cached table ASSOCIATION(
ASSOCIATIONID varchar(255) primary key,
TYPE varchar(255) not null,
foreign key (TYPE) references TOPIC(TOPICID)
);

create cached table MEMBER(
ASSOCIATION varchar(255) not null,
PLAYER varchar(255) not null,
ROLE varchar(255) not null,
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
