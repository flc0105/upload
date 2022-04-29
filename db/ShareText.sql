create table text
(
	id integer not null
		constraint text_pk
			primary key autoincrement,
	author text not null,
	data text not null,
	time datetime not null
);