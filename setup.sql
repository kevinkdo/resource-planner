drop table reservations;
drop table resourcetags;
drop table resources;
drop table users;


create table users (
	user_id serial primary key not null,
	email varchar(255) unique not null,
	username varchar(255),
	passhash varchar(255) not null,
	should_email boolean not null
);

insert into users (email, username, passhash, should_email)
values ('admin@admin.com', 'admin', '1000:9816dd56235c68a566b1f50a1815ab96761ebf7ad33d84cd:5b209a5f9b1628fbd80cdffb0aa50b7ec58f07e93f9b18fc', false);

create table resources (
  resource_id serial primary key not null,
  name varchar(255) not null,
  description varchar(2000)
);

create table resourcetags (
  resource_id int not null references resources(resource_id) on delete cascade,
  tag varchar(255) not null
);

create table reservations (
  reservation_id serial primary key not null,
  user_id int not null references users(user_id) on delete cascade,
  resource_id int not null references resources(resource_id) on delete cascade,
  begin_time timestamp,
  end_time timestamp,
  should_email boolean not null
);

