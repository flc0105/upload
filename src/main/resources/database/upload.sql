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
