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
 * This file contains generic SQL statements for a database topic map
 * used by Wandora application. Creation statements are used to
 * initialize a database before Wandora can use it as a database topic map.
 * 
 * The file uses field type 'longtext' that may be missing in some databases.
 * You can replace the 'longtext' type with any other type that supports lengthy
 * character arrays. Even 'varchar' may be viable alternative.
 * 
 * Table SUBJECTIDENTIFIER has a field SI that is typed as 'varchar(65535)' as
 * the field SI is a primary key and databases usually don't support 'longtext'
 * keys.
 */

create table TOPIC(
TOPICID varchar(255) primary key,
BASENAME longtext,
SUBJECTLOCATOR longtext
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
DATA longtext,
primary key(TOPIC,TYPE,VERSION)
);

create table VARIANT(
VARIANTID varchar(255) primary key,
TOPIC varchar(255) not null references TOPIC(TOPICID),
VALUE longtext
);

create table VARIANTSCOPE(
VARIANT varchar(255) not null references VARIANT(VARIANTID),
TOPIC varchar(255) not null references TOPIC(TOPICID),
primary key(VARIANT,TOPIC)
);

create table SUBJECTIDENTIFIER(
SI varchar(65535) primary key,
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
