DROP TABLE IF EXISTS groupmembers;
DROP TABLE IF EXISTS groupResourcePermissions;
DROP TABLE IF EXISTS userResourcePermissions;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS resourcetags;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;


CREATE TABLE users (
  user_id       SERIAL PRIMARY KEY  NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  passhash      VARCHAR(255)        NOT NULL,
  username      VARCHAR(255) UNIQUE NOT NULL,
  super_p       BOOLEAN             NOT NULL DEFAULT FALSE,
  resource_p    BOOLEAN             NOT NULL DEFAULT FALSE,
  reservation_p BOOLEAN             NOT NULL DEFAULT FALSE,
  user_p        BOOLEAN             NOT NULL DEFAULT FALSE,
  should_email  BOOLEAN             NOT NULL
);

INSERT INTO users (email, username, passhash, resource_p, reservation_p, user_p, should_email)
VALUES ('admin@admin.com', 'admin',
        '1000:9816dd56235c68a566b1f50a1815ab96761ebf7ad33d84cd:5b209a5f9b1628fbd80cdffb0aa50b7ec58f07e93f9b18fc',
        TRUE, TRUE, TRUE, FALSE);


CREATE TABLE resources (
  resource_id     SERIAL PRIMARY KEY NOT NULL,
  name            VARCHAR(255)       NOT NULL,
  description     VARCHAR(2000)
);

CREATE TABLE resourcetags (
  resource_id INT          NOT NULL REFERENCES resources (resource_id) ON DELETE CASCADE,
  tag         VARCHAR(255) NOT NULL
);

CREATE TABLE reservations (
  reservation_id SERIAL PRIMARY KEY NOT NULL,
  user_id        INT                NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  resource_id    INT                NOT NULL REFERENCES resources (resource_id) ON DELETE CASCADE,
  begin_time     TIMESTAMP,
  end_time       TIMESTAMP,
  should_email   BOOLEAN            NOT NULL
);

CREATE TABLE groups (
  group_id      SERIAL PRIMARY KEY NOT NULL,
  group_name    VARCHAR(255)       NOT NULL,
  resource_p    BOOLEAN            NOT NULL DEFAULT FALSE,
  reservation_p BOOLEAN            NOT NULL DEFAULT FALSE,
  user_p        BOOLEAN            NOT NULL DEFAULT FALSE
);

CREATE TABLE groupmembers (
  group_id INT NOT NULL REFERENCES groups (group_id) ON DELETE CASCADE,
  user_id  INT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  UNIQUE (group_id, user_id)
);

CREATE TABLE groupresourcepermissions(
  group_id INT NOT NULL REFERENCES groups (group_id) ON DELETE CASCADE,
  resource_id INT NOT NULL REFERENCES resources (resource_id) ON DELETE CASCADE,
  permission_level INT NOT NULL,
  UNIQUE (group_id, resource_id)
);

CREATE TABLE userresourcepermissions(
  user_id INT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  resource_id INT NOT NULL REFERENCES resources (resource_id) ON DELETE CASCADE,
  permission_level INT NOT NULL,
  UNIQUE (user_id, resource_id)
);



