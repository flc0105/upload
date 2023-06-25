create table if not exists paste
(
    id          integer  not null
        constraint paste_pk
        primary key autoincrement,
    title       text     not null,
    text        text     not null,
    time        datetime not null,
    expiredDate datetime,
    isPrivate   boolean
);

create table if not exists share_code
(
    id   integer not null
        constraint share_code_pk
        primary key autoincrement,
    code text    not null,
    path text    not null
);

create unique index if not exists share_code_code_uindex
    on share_code (code);

create table if not exists bookmark
(
    id    integer not null
        constraint bookmark_pk
        primary key autoincrement,
    url   text    not null,
    title text,
    icon  text
);

create table if not exists tag
(
    id    integer not null
        constraint tag_pk
        primary key autoincrement,
    title text unique
);

PRAGMA foreign_keys = ON; -- 启用外键约束

CREATE TABLE if not exists bookmark_tag
(
    id          integer not null
        constraint bookmark_tag_pk
        primary key autoincrement,
    bookmark_id INTEGER,
    tag_id      INTEGER,
    CONSTRAINT unique_bookmark_tag UNIQUE (bookmark_id, tag_id), -- 添加唯一性约束 以防止重复插入相同的 bookmarkId 和 tagId 组合
    FOREIGN KEY (bookmark_id) REFERENCES bookmark (id),
    FOREIGN KEY (tag_id) REFERENCES tag (id)
);


insert or ignore into tag
values (null, "工作");
insert or ignore into tag
values (null, "学习");
insert or ignore into tag
values (null, "生活");
insert or ignore into tag
values (null, "娱乐");


create table if not exists permission
(
    id         integer not null
        primary key autoincrement
        unique,
    path       text    not null,
    isAdmin   integer not null,
    desc       text
);

/*
insert into permission values (null, "/paste/add", 0, "Paste_添加");
insert into permission values (null, "/paste/delete", 1, "Paste_删除");
insert into permission values (null, "/paste/update", 1, "Paste_修改");
insert into permission values (null, "/paste/list", 0, "Paste_查询所有");
insert into permission values (null, "/paste/get", 0, "Paste_根据id查询");
insert into permission values (null, "/paste/last", 0, "Paste_查询最后添加");
insert into permission values (null, "/paste/unlisted", 1, "Paste_查询私密");
insert into permission values (null, "/file/mkdir", 1, "文件_创建目录");
insert into permission values (null, "/file/delete", 1, "文件_删除");
insert into permission values (null, "/file/move", 1, "文件_移动");
insert into permission values (null, "/file/rename", 1, "文件_重命名");
insert into permission values (null, "/file/download", 0, "文件_下载");
insert into permission values (null, "/file/preview", 0, "文件_预览图片");
insert into permission values (null, "/file/read", 0, "文件_预览文本");
insert into permission values (null, "/file/info", 0, "文件_查询详情");
insert into permission values (null, "/file/zip", 0, "文件_压缩");
insert into permission values (null, "/file/zipAndDownload", 0, "文件_压缩并下载");
insert into permission values (null, "/file/link", 1, "文件_生成图片直链");
insert into permission values (null, "/token/deactivate", 1, "Token_停用");
insert into permission values (null, "/shareCode/add", 1, "文件分享码_添加");
insert into permission values (null, "/shareCode/delete", 1, "文件分享码_删除");
insert into permission values (null, "/shareCode/get", 0, "文件分享码_查询");
insert into permission values (null, "/shareCode/list", 1, "文件分享码_查询所有");
insert into permission values (null, "/permission/list", 1, "权限_查询");
insert into permission values (null, "/permission/update", 1, "权限_修改");
insert into permission values (null, "/config/list", 1, "配置_查询");
insert into permission values (null, "/permission/update", 1, "配置_修改");
insert into permission values (null, "/info", 1, "查询服务器信息");
insert into permission values (null, "/logs/delete", 1, "日志_删除");
insert into permission values (null, "/logs/list", 1, "日志_查询");
*/