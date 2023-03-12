create table if not exists paste
(
	id integer not null
		constraint paste_pk
			primary key autoincrement,
	title text not null,
	text text not null,
	time datetime not null
);

create table if not exists share_code
(
	id integer not null
		constraint share_code_pk
			primary key autoincrement,
	code text not null,
	path text not null
);

create unique index if not exists share_code_code_uindex
	on share_code (code);

create table if not exists bookmark
(
	id integer not null
		constraint bookmark_pk
			primary key autoincrement,
	url text not null,
	title text,
	icon text
);

