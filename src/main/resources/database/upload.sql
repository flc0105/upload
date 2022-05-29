create table paste
(
	id integer not null
		constraint paste_pk
			primary key autoincrement,
	title text default 'Untitled' not null,
	text text not null,
	time datetime not null
);

create table token
(
	value text not null
		constraint token_pk
			primary key
);