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
 *                      ...........................
 *
 *
 * This file contains Wandora's database creation statements for MariaDB
 * database (http://mariadb.org/). Creation statements should be used to
 * initialize a database before Wandora can use it as a database topic map.
 *
 * JDBC jar (included in Wandora's lib folder):
 * mariadb-java-client-1.3.3.jar
 *
 * Driver class: 
 * com.mysql.jdbc.Driver
 *
 * Connection string example:
 * jdbc:mariadb://localhost:3306/wandora?useOldAliasMetadataBehavior=true
 *
 * The connection string *must* set the URL parameter 'useOldAliasMetadataBehavior'
 * true. Without the parameter Wandora fails to query data out of the database.
 *
 * Database table SUBJECTIDENTIFIER uses a key created out of 767 first characters 
 * of table field SI.
 */

create table TOPIC(
TOPICID varchar(255) collate utf8_general_ci primary key ,
BASENAME longtext collate utf8_general_ci,
SUBJECTLOCATOR longtext collate utf8_general_ci 
);

create table TOPICTYPE(
TOPIC varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
TYPE varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
primary key(TOPIC,TYPE)
);

create table DATA(
TOPIC varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
TYPE varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
VERSION varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
DATA longtext collate utf8_general_ci,
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(255) collate utf8_general_ci primary key,
TOPIC varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
VALUE longtext collate utf8_general_ci
);

create table VARIANTSCOPE(
VARIANT varchar(255) collate utf8_general_ci not null references VARIANT(VARIANTID),
TOPIC varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI longtext not null collate utf8_general_ci,
key SI_KEY (SI(767)),
TOPIC varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID)
);

create table ASSOCIATION(
ASSOCIATIONID varchar(255) collate utf8_general_ci primary key,
TYPE varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID)
);

create table MEMBER(
ASSOCIATION varchar(255) collate utf8_general_ci not null references ASSOCIATION(ASSOCIATIONID),
PLAYER varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
ROLE varchar(255) collate utf8_general_ci not null references TOPIC(TOPICID),
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


