CREATE SCHEMA "admin";


-- domains

CREATE TABLE "admin"."domain" (
  "id"            INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 2, INCREMENT BY 1),
  "name"          VARCHAR(64) NOT NULL UNIQUE,
  "description"   VARCHAR(512),
  "system"        BOOLEAN DEFAULT FALSE
);

ALTER TABLE "admin"."domain" ADD CONSTRAINT domain_pk  PRIMARY KEY ("id");

-- users

CREATE TABLE "admin"."user"(
  "id"         INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 2, INCREMENT BY 1),
  "login"      VARCHAR(32) NOT NULL UNIQUE,
  "password"   VARCHAR(32) NOT NULL,
  "firstname"  VARCHAR(64) NOT NULL,
  "lastname"   VARCHAR(64) NOT NULL,
  "email"      VARCHAR(64) NOT NULL,
  "active"     BOOLEAN NOT NULL
);

ALTER TABLE "admin"."user" ADD CONSTRAINT user_pk PRIMARY KEY ("id");

CREATE TABLE "admin"."role"(
  "name"    VARCHAR(32) NOT NULL
);


ALTER TABLE "admin"."role" ADD CONSTRAINT role_pk PRIMARY KEY ("name");

CREATE TABLE "admin"."domainrole" (
  "id"           INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 2, INCREMENT BY 1),
  "name"         VARCHAR(32) NOT NULL UNIQUE,
  "description"  VARCHAR(512),
  "system"       BOOLEAN DEFAULT FALSE
);

ALTER TABLE "admin"."domainrole" ADD CONSTRAINT domainrole_pk PRIMARY KEY ("id");


CREATE TABLE "admin"."user_x_role"(
  "user_id"   INTEGER NOT NULL,
  "role"      VARCHAR(32) NOT NULL
);

ALTER TABLE "admin"."user_x_role" ADD CONSTRAINT user_x_role_pk PRIMARY KEY ("user_id", "role");
ALTER TABLE "admin"."user_x_role" ADD CONSTRAINT user_x_role_user_id_fk FOREIGN KEY ("user_id") REFERENCES "admin"."user"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."user_x_role" ADD CONSTRAINT user_x_role_role_fk FOREIGN KEY ("role") REFERENCES "admin"."role"("name")  ON DELETE CASCADE;

CREATE TABLE "admin"."permission" (
   "id"           INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
   "name" VARCHAR(32) NOT NULL,
   "description" VARCHAR(512) NOT NULL
);

ALTER TABLE "admin"."permission" ADD CONSTRAINT permission_pk PRIMARY KEY ("id");

CREATE TABLE "admin"."domainrole_x_permission" (
    "domainrole_id" INTEGER NOT NULL,
    "permission_id" INTEGER NOT NULL
);


ALTER TABLE "admin"."domainrole_x_permission" ADD CONSTRAINT domainrole_x_permission_pk PRIMARY KEY ("domainrole_id", "permission_id");
ALTER TABLE "admin"."domainrole_x_permission" ADD CONSTRAINT domainrole_x_permission_domainrole_id_fk FOREIGN KEY ("domainrole_id") REFERENCES "admin"."domainrole"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."domainrole_x_permission" ADD CONSTRAINT domainrole_x_permission_permission_id_fk FOREIGN KEY ("permission_id") REFERENCES "admin"."permission"("id") ON DELETE CASCADE;



CREATE TABLE "admin"."user_x_domain_x_domainrole"(
  "user_id"      INTEGER NOT NULL,
  "domain_id"    INTEGER NOT NULL,
  "domainrole_id"   INTEGER NOT NULL
);

ALTER TABLE "admin"."user_x_domain_x_domainrole" ADD CONSTRAINT user_x_domain_x_role_pk PRIMARY KEY ("user_id", "domain_id", "domainrole_id");
ALTER TABLE "admin"."user_x_domain_x_domainrole" ADD CONSTRAINT user_x_domain_x_role_user_id_fk FOREIGN KEY ("user_id") REFERENCES "admin"."user"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."user_x_domain_x_domainrole" ADD CONSTRAINT user_x_domain_x_role_domain_fk FOREIGN KEY ("domain_id") REFERENCES "admin"."domain"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."user_x_domain_x_domainrole" ADD CONSTRAINT user_x_domain_x_role_role_fk FOREIGN KEY ("domainrole_id") REFERENCES "admin"."domainrole"("id")  ON DELETE CASCADE;


-- sensors

CREATE TABLE "admin"."sensor"(
  "id"          INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "identifier"  VARCHAR(512) NOT NULL UNIQUE,
  "type"        VARCHAR(64)  NOT NULL,
  "parent"      VARCHAR(512) NOT NULL,
  "owner"       VARCHAR(32),
  "metadata"    CLOB
);

ALTER TABLE "admin"."sensor" ADD CONSTRAINT sensor_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."sensor" ADD CONSTRAINT sensor_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


-- providers

CREATE TABLE "admin"."provider"(
  "id"          INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "identifier"  VARCHAR(512) NOT NULL UNIQUE,
  "parent"      VARCHAR(512),
  "type"        VARCHAR(8)  NOT NULL,
  "impl"        VARCHAR(32) NOT NULL,
  "config"      CLOB        NOT NULL,
  "owner"       VARCHAR(32),
  "metadata_id" VARCHAR(512),
  "metadata"    CLOB
);

ALTER TABLE "admin"."provider" ADD CONSTRAINT provider_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."provider" ADD CONSTRAINT provider_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


-- provider items

CREATE TABLE "admin"."style"(
  "id"          INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"        VARCHAR(512) NOT NULL,
  "provider"    INTEGER     NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "date"        BIGINT      NOT NULL,
  "body"        CLOB        NOT NULL,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."style" ADD CONSTRAINT style_pk          PRIMARY KEY ("id");
ALTER TABLE "admin"."style" ADD CONSTRAINT style_owner_fk    FOREIGN KEY ("owner")    REFERENCES "admin"."user"("login");
ALTER TABLE "admin"."style" ADD CONSTRAINT style_provider_fk FOREIGN KEY ("provider") REFERENCES "admin"."provider"("id") ON DELETE CASCADE;

CREATE TABLE "admin"."style_i18n" (
  "style_id"    INTEGER  NOT NULL,
  "lang"        CHAR(2)  NOT NULL,
  "title"       INTEGER  NOT NULL,
  "description" INTEGER  NOT NULL
);

ALTER TABLE "admin"."style_i18n" ADD CONSTRAINT style_i18n_pk          PRIMARY KEY ("style_id", "lang");
ALTER TABLE "admin"."style_i18n" ADD CONSTRAINT style_i18n_style_id_fk    FOREIGN KEY ("style_id")  REFERENCES "admin"."style"("id")  ON DELETE CASCADE;


-- Domain cross tables with for styles

CREATE TABLE "admin"."style_x_domain"(
  "style_id"   INTEGER NOT NULL,
  "domain_id"  INTEGER NOT NULL
);

ALTER TABLE "admin"."style_x_domain" ADD CONSTRAINT style_x_domain_pk PRIMARY KEY ("style_id", "domain_id");
ALTER TABLE "admin"."style_x_domain" ADD CONSTRAINT style_x_domain_style_id_fk FOREIGN KEY ("style_id") REFERENCES "admin"."style"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."style_x_domain" ADD CONSTRAINT style_x_domain_domain_id_fk FOREIGN KEY ("domain_id") REFERENCES "admin"."domain"("id")  ON DELETE CASCADE;




CREATE TABLE "admin"."data"(
  "id"            INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"          VARCHAR(512) NOT NULL,
  "namespace"     VARCHAR(256)NOT NULL,
  "provider"      INTEGER     NOT NULL,
  "type"          VARCHAR(32) NOT NULL,
  "subtype"       VARCHAR(32) NOT NULL DEFAULT '',
  "visible"       BOOLEAN     NOT NULL DEFAULT TRUE,
  "sensorable"    BOOLEAN     NOT NULL DEFAULT FALSE,
  "date"          BIGINT      NOT NULL,
  "owner"         VARCHAR(32),
  "metadata"      CLOB,
  "metadata_id"   VARCHAR(512),
  "iso_metadata"  CLOB
);

ALTER TABLE "admin"."data" ADD CONSTRAINT data_pk          PRIMARY KEY ("id");
ALTER TABLE "admin"."data" ADD CONSTRAINT data_owner_fk    FOREIGN KEY ("owner")    REFERENCES "admin"."user"("login");
ALTER TABLE "admin"."data" ADD CONSTRAINT data_provider_fk FOREIGN KEY ("provider") REFERENCES "admin"."provider"("id") ON DELETE CASCADE;


CREATE TABLE "admin"."data_i18n" (
  "data_id"    INTEGER  NOT NULL,
  "lang"        CHAR(2)  NOT NULL,
  "title"       INTEGER  NOT NULL,
  "description" INTEGER  NOT NULL
);

ALTER TABLE "admin"."data_i18n" ADD CONSTRAINT data_i18n_pk          PRIMARY KEY ("data_id", "lang");
ALTER TABLE "admin"."data_i18n" ADD CONSTRAINT data_i18n_data_id_fk    FOREIGN KEY ("data_id")  REFERENCES "admin"."data"("id")  ON DELETE CASCADE;



CREATE TABLE "admin"."sensored_data"(
  "sensor"      INTEGER     NOT NULL,
  "data"        INTEGER     NOT NULL
);

ALTER TABLE "admin"."sensored_data" ADD CONSTRAINT sensor_data_pk PRIMARY KEY ("sensor","data");
ALTER TABLE "admin"."sensored_data" ADD CONSTRAINT sensored_data_sensor_fk FOREIGN KEY ("sensor") REFERENCES "admin"."sensor"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."sensored_data" ADD CONSTRAINT sensored_data_data_fk   FOREIGN KEY ("data")   REFERENCES "admin"."data"("id")   ON DELETE CASCADE;


-- Domain cross tables with for datas

CREATE TABLE "admin"."data_x_domain"(
  "data_id"   INTEGER NOT NULL,
  "domain_id"  INTEGER NOT NULL
);

ALTER TABLE "admin"."data_x_domain" ADD CONSTRAINT data_x_domain_pk PRIMARY KEY ("data_id", "domain_id");
ALTER TABLE "admin"."data_x_domain" ADD CONSTRAINT data_x_domain_data_id_fk FOREIGN KEY ("data_id") REFERENCES "admin"."data"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."data_x_domain" ADD CONSTRAINT data_x_domain_domain_id_fk FOREIGN KEY ("domain_id") REFERENCES "admin"."domain"("id")  ON DELETE CASCADE;



CREATE TABLE "admin"."crs"(
  "dataid"  INTEGER NOT NULL,
  "crscode" VARCHAR(64) NOT NULL
);

ALTER TABLE "admin"."crs" ADD CONSTRAINT crs_pk      PRIMARY KEY ("dataid", "crscode");
ALTER TABLE "admin"."crs" ADD CONSTRAINT crs_data_fk FOREIGN KEY ("dataid") REFERENCES "admin"."data"("id") ON DELETE CASCADE;





-- services

CREATE TABLE "admin"."service"(
  "id"          INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "identifier"  VARCHAR(512) NOT NULL,
  "type"        VARCHAR(32)  NOT NULL,
  "date"        BIGINT      NOT NULL,
  "config"      CLOB,
  "owner"       VARCHAR(32),
  "metadata_id" VARCHAR(512),
  "metadata_iso"    CLOB,
  "status"		VARCHAR(32)	NOT NULL,
  "versions"	VARCHAR(32) NOT NULL
);

ALTER TABLE "admin"."service" ADD CONSTRAINT service_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."service" ADD CONSTRAINT service_uq       UNIQUE ("identifier","type");
ALTER TABLE "admin"."service" ADD CONSTRAINT service_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");

-- Domain cross tables with for services

CREATE TABLE "admin"."service_x_domain"(
  "service_id"   INTEGER NOT NULL,
  "domain_id"  INTEGER NOT NULL
);

ALTER TABLE "admin"."service_x_domain" ADD CONSTRAINT service_x_domain_pk PRIMARY KEY ("service_id", "domain_id");
ALTER TABLE "admin"."service_x_domain" ADD CONSTRAINT service_x_domain_layer_fk FOREIGN KEY ("service_id") REFERENCES "admin"."service"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."service_x_domain" ADD CONSTRAINT service_x_domain_domain_id_fk FOREIGN KEY ("domain_id") REFERENCES "admin"."domain"("id")  ON DELETE CASCADE;



CREATE TABLE "admin"."service_extra_config"(
  "id"          INTEGER     NOT NULL,
  "filename"    VARCHAR(32) NOT NULL,
  "content"     CLOB
);

ALTER TABLE "admin"."service_extra_config" ADD CONSTRAINT service_extra_config_pk  PRIMARY KEY ("id", "filename");
ALTER TABLE "admin"."service_extra_config" ADD CONSTRAINT service_extra_config_service_fk FOREIGN KEY ("id") REFERENCES "admin"."service"("id");

CREATE TABLE "admin"."service_details"(
  "id"            INTEGER     NOT NULL,
  "lang"          VARCHAR(3) NOT NULL,
  "content"       CLOB,
  "default_lang"  BOOLEAN

);

ALTER TABLE "admin"."service_details" ADD CONSTRAINT service_details_pk  PRIMARY KEY ("id", "lang");
ALTER TABLE "admin"."service_details" ADD CONSTRAINT service_details_service_fk FOREIGN KEY ("id") REFERENCES "admin"."service"("id") ON DELETE CASCADE;


-- service items

CREATE TABLE "admin"."layer"(
  "id"           INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"         VARCHAR(512) NOT NULL,
  "namespace"    VARCHAR(256),
  "alias"        VARCHAR(512),
  "service"      INTEGER     NOT NULL,
  "data"         INTEGER     NOT NULL,
  "date"         BIGINT      NOT NULL,
  "config"       CLOB,
  "owner"        VARCHAR(32)
);

ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_pk         PRIMARY KEY ("id");
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_uq         UNIQUE ("name","service");
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_service_fk FOREIGN KEY ("service") REFERENCES "admin"."service"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_data_fk    FOREIGN KEY ("data")  REFERENCES "admin"."data"("id")      ON DELETE CASCADE;
ALTER TABLE "admin"."layer" ADD CONSTRAINT layer_owner_fk   FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");

CREATE TABLE "admin"."layer_i18n" (
  "layer_id"    INTEGER  NOT NULL,
  "lang"        CHAR(2)  NOT NULL,
  "title"       INTEGER  NOT NULL,
  "description" INTEGER  NOT NULL
);

ALTER TABLE "admin"."layer_i18n" ADD CONSTRAINT layer_i18n_pk             PRIMARY KEY ("layer_id", "lang");
ALTER TABLE "admin"."layer_i18n" ADD CONSTRAINT layer_i18n_layer_id_fk    FOREIGN KEY ("layer_id")  REFERENCES "admin"."layer"("id") ON DELETE CASCADE;


-- Domain cross tables



CREATE TABLE "admin"."task"(
  "identifier"  VARCHAR(512) NOT NULL,
  "state"       VARCHAR(32) NOT NULL,
  "type"        VARCHAR(32) NOT NULL,
  "start"       BIGINT      NOT NULL,
  "end"         BIGINT,
  "owner"       VARCHAR(32)
);

ALTER TABLE "admin"."task" ADD CONSTRAINT task_pk       PRIMARY KEY ("identifier");
ALTER TABLE "admin"."task" ADD CONSTRAINT task_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


CREATE TABLE "admin"."task_i18n" (
  "task_identifier"  VARCHAR(512)  NOT NULL,
  "lang"             CHAR(2)  NOT NULL,
  "title"            INTEGER  NOT NULL,
  "description"      INTEGER  NOT NULL
);

ALTER TABLE "admin"."task_i18n" ADD CONSTRAINT task_i18n_pk            PRIMARY KEY ("task_identifier", "lang");
ALTER TABLE "admin"."task_i18n" ADD CONSTRAINT task_i18n_task_id_fk    FOREIGN KEY ("task_identifier")  REFERENCES "admin"."task"("identifier") ON DELETE CASCADE;



CREATE TABLE "admin"."property"(
  "key"    VARCHAR(32) NOT NULL,
  "value"  VARCHAR(64) NOT NULL
);
ALTER TABLE "admin"."property" ADD CONSTRAINT property_pk PRIMARY KEY ("key");

insert into "admin"."role" ("name") values('cstl-admin');

insert into "admin"."permission" ("id", "name", "description") values (1, 'SERVICE_READ_ACCESS', 'Création de service');
insert into "admin"."permission" ("id", "name", "description") values (2, 'SERVICE_WRITE_ACCESS', 'Création de service');
insert into "admin"."permission" ("id", "name", "description") values (3, 'SERVICE_CREATION', 'Création de service');
insert into "admin"."permission" ("id", "name", "description") values (4, 'DATA_CREATION', 'Création de data');

insert into "admin"."domainrole" ("id", "name", "system") values(0, 'default', TRUE);
insert into "admin"."domainrole" ("id", "name") values(1, 'manager');

-- Anonymous domain.
insert into "admin"."domain" ("id", "name", "description", "system") values(0, 'anonymous', 'anonymous users', TRUE);
-- Anonymous user can access to services linked with anonymous domain.
insert into "admin"."domainrole_x_permission" ("domainrole_id", "permission_id") values(0, 1);

insert into "admin"."domain" ("id", "name", "description", "system") values(1, 'default', 'default domain', TRUE);

insert into "admin"."domainrole_x_permission" ("domainrole_id", "permission_id") values(1, 1);
insert into "admin"."domainrole_x_permission" ("domainrole_id", "permission_id") values(1, 2);
insert into "admin"."domainrole_x_permission" ("domainrole_id", "permission_id") values(1, 3);
insert into "admin"."domainrole_x_permission" ("domainrole_id", "permission_id") values(1, 4);

insert into "admin"."user" ("id", "login", "password", "firstname", "lastname", "email", "active") values(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Frédéric', 'Houbie', 'frederic.houbie@geomatys.com', TRUE);
insert into "admin"."user_x_role" ("user_id", "role") values(1, 'cstl-admin');

insert into "admin"."user_x_domain_x_domainrole" ("user_id", "domainrole_id", "domain_id") values(1, 1, 1);


CREATE TABLE "admin"."styled_data"(
  "style"       INTEGER     NOT NULL,
  "data"        INTEGER     NOT NULL
);

ALTER TABLE "admin"."styled_data" ADD CONSTRAINT styled_data_pk PRIMARY KEY ("style","data");
ALTER TABLE "admin"."styled_data" ADD CONSTRAINT styled_data_style_fk      FOREIGN KEY ("style") REFERENCES "admin"."style"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."styled_data" ADD CONSTRAINT styled_data_data_fk       FOREIGN KEY ("data")  REFERENCES "admin"."data"("id")  ON DELETE CASCADE;


CREATE TABLE "admin"."styled_layer"(
  "style"       INTEGER     NOT NULL,
  "layer"       INTEGER     NOT NULL,
  "default"     BOOLEAN
);

ALTER TABLE "admin"."styled_layer" ADD CONSTRAINT styled_layer_pk PRIMARY KEY ("style","layer");
ALTER TABLE "admin"."styled_layer" ADD CONSTRAINT styled_layer_style_fk      FOREIGN KEY ("style") REFERENCES "admin"."style"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."styled_layer" ADD CONSTRAINT styled_layer_layer_fk       FOREIGN KEY ("layer")  REFERENCES "admin"."layer"("id")  ON DELETE CASCADE;



-- Map context

CREATE TABLE "admin"."mapcontext" (
  "id"            INTEGER     NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "name"          VARCHAR(512) NOT NULL,
  "owner"         VARCHAR(32),
  "description"   VARCHAR(512),
  "crs"           VARCHAR(32),
  "west"          DOUBLE,
  "north"         DOUBLE,
  "east"          DOUBLE,
  "south"         DOUBLE,
  "keywords"      VARCHAR(256)
);

ALTER TABLE "admin"."mapcontext" ADD CONSTRAINT mapcontext_pk       PRIMARY KEY ("id");
ALTER TABLE "admin"."mapcontext" ADD CONSTRAINT mapcontext_owner_fk FOREIGN KEY ("owner") REFERENCES "admin"."user"("login");


CREATE TABLE "admin"."mapcontext_styled_layer" (
  "mapcontext_id"  INTEGER     NOT NULL,
  "layer_id"       INTEGER     NOT NULL,
  "style_id"       INTEGER     NOT NULL,
  "order"          INTEGER     NOT NULL   DEFAULT 1,
  "opacity"        INTEGER     NOT NULL   DEFAULT 100
);

ALTER TABLE "admin"."mapcontext_styled_layer" ADD CONSTRAINT mapcontext_styled_layer_pk PRIMARY KEY ("mapcontext_id","layer_id","style_id");
ALTER TABLE "admin"."mapcontext_styled_layer" ADD CONSTRAINT mapcontext_styled_layer_context_fk   FOREIGN KEY ("mapcontext_id") REFERENCES "admin"."mapcontext"("id") ON DELETE CASCADE;
ALTER TABLE "admin"."mapcontext_styled_layer" ADD CONSTRAINT mapcontext_styled_layer_layer_fk     FOREIGN KEY ("layer_id")      REFERENCES "admin"."layer"("id")  ON DELETE CASCADE;
ALTER TABLE "admin"."mapcontext_styled_layer" ADD CONSTRAINT mapcontext_styled_layer_style_fk     FOREIGN KEY ("style_id")      REFERENCES "admin"."style"("id")  ON DELETE CASCADE;
