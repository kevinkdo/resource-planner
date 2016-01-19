create table accounts (
	id bigserial primary key not null,
	email varchar(255) unique not null,
	passhash varchar(255) not null,
	timestamp timestamp default current_timestamp,
	should_email boolean,
	username varchar(255)
);

