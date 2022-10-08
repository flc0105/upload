create table paste
(
	id integer not null
		constraint paste_pk
			primary key autoincrement,
	title text default 'Untitled' not null,
	text text not null,
	time datetime not null
);

create table share_code
(
	id integer not null
		constraint share_code_pk
			primary key autoincrement,
	code text not null,
	path text not null
);

create unique index share_code_code_uindex
	on share_code (code);

create table task
(
	id integer not null
		constraint task_pk
			primary key autoincrement,
	text text not null,
	checked boolean not null
);

create table bookmark
(
	id integer not null
		constraint bookmark_pk
			primary key autoincrement,
	url text not null,
	title text,
	icon text
);

