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
 *                       ...........................
 *
 *
 * This file contains Wandora's database creation statements for PostgreSQL 
 * database (http://www.postgresql.org/). Creation statements should be used to
 * initialize a database before Wandora can use it as a database topic map.
 *
 * JDBC jar (included in Wandora's lib folder):
 * postgresql-9.4.1207.jar
 *
 * Driver class: 
 * org.postgresql.Driver
 *
 * Connection string example:
 * jdbc:postgresql://localhost/wandora
 *
 */

create table TOPIC(
TOPICID varchar(255) primary key,
BASENAME text,
SUBJECTLOCATOR text
);

create table TOPICTYPE(
TOPIC varchar(255) not null references TOPIC(TOPICID),
TYPE varchar(255) not null references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create table DATA(
TOPIC varchar(255) not null references TOPIC(TOPICID),
TYPE varchar(255) not null references TOPIC(TOPICID),
VERSION varchar(255) not null references TOPIC(TOPICID),
DATA text,
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(255) primary key,
TOPIC varchar(255) not null references TOPIC(TOPICID),
VALUE text
);

create table VARIANTSCOPE(
VARIANT varchar(255) not null references VARIANT(VARIANTID),
TOPIC varchar(255) not null references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI text primary key,
TOPIC varchar(255) not null references TOPIC(TOPICID)
);

create table ASSOCIATION(
ASSOCIATIONID varchar(255) primary key,
TYPE varchar(255) not null references TOPIC(TOPICID)
);

create table MEMBER(
ASSOCIATION varchar(255) not null references ASSOCIATION(ASSOCIATIONID),
PLAYER varchar(255) not null references TOPIC(TOPICID),
ROLE varchar(255) not null references TOPIC(TOPICID),
primary key(ASSOCIATION,ROLE)
);

create index BASENAME_INDEX ON TOPIC((MD5(BASENAME)));
create index SUBJECTLOCATOR_INDEX ON TOPIC(SUBJECTLOCATOR);
create index SITOPIC_INDEX on SUBJECTIDENTIFIER(TOPIC);
create index ASSOCTYPE_INDEX on ASSOCIATION(TYPE);
create index MEMBERPLAYER_INDEX on MEMBER(PLAYER);
create index MEMBERROLE_INDEX on MEMBER(ROLE);
create index VARIANTTOPIC_INDEX on VARIANT(TOPIC);
create index DATATOPIC_INDEX on DATA(TOPIC);
create index DATATYPE_INDEX on DATA(TYPE);
create index DATAVERSION_INDEX on DATA(VERSION);
