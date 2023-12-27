CREATE TABLE
    IF
    NOT EXISTS paste (
                         id INTEGER NOT NULL CONSTRAINT paste_pk PRIMARY KEY autoincrement,
                         title text NOT NULL,
                         text text NOT NULL,
                         time datetime NOT NULL,
                         expiredDate datetime,
                         isPrivate boolean
);
CREATE TABLE
    IF
    NOT EXISTS share_code ( id INTEGER NOT NULL CONSTRAINT share_code_pk PRIMARY KEY autoincrement, CODE text NOT NULL, path text NOT NULL );
CREATE UNIQUE INDEX
    IF
    NOT EXISTS share_code_code_uindex ON share_code ( CODE );

CREATE TABLE
    IF
    NOT EXISTS permission ( id INTEGER NOT NULL PRIMARY KEY autoincrement UNIQUE, path text NOT NULL, isAdmin INTEGER NOT NULL, DESC text );

/*
insert into permission values (null, "/file/upload", 0, "上传文件");
insert into permission values (null, "/file/mkdir", 1, "创建目录");
insert into permission values (null, "/file/delete", 1, "删除文件");
insert into permission values (null, "/file/move", 1, "移动文件");
insert into permission values (null, "/file/rename", 1, "重命名文件");
insert into permission values (null, "/file/list", 0, "获取文件列表");
insert into permission values (null, "/file/search", 0, "搜索文件");
insert into permission values (null, "/file/download", 0, "下载文件");
insert into permission values (null, "/file/preview", 0, "预览图片");
insert into permission values (null, "/file/read", 0, "预览文本文件");
insert into permission values (null, "/file/info", 0, "获取文件详情");
insert into permission values (null, "/file/zip", 0, "压缩文件");
insert into permission values (null, "/file/zipAndDownload", 0, "压缩并下载文件");
insert into permission values (null, "/file/link", 1, "生成图片直链");
insert into permission values (null, "/file/gallery", 0, "获取图片列表");

insert into permission values (null, "/paste/add", 0, "添加Paste");
insert into permission values (null, "/paste/delete", 1, "删除Paste");
insert into permission values (null, "/paste/update", 1, "修改Paste");
insert into permission values (null, "/paste/list", 0, "获取Paste列表");
insert into permission values (null, "/paste/get", 0, "查询Paste");
insert into permission values (null, "/paste/last", 0, "查询最新Paste");
insert into permission values (null, "/paste/unlisted", 1, "查询私密Paste列表");
insert into permission values (null, "/paste/import", 1, "导入Paste");

insert into permission values (null, "/log/list", 1, "获取日志列表");
insert into permission values (null, "/log/page", 1, "分页查询日志");
insert into permission values (null, "/log/delete", 1, "删除日志");
insert into permission values (null, "/log/clear", 1, "清空日志");

insert into permission values (null, "/shareCode/add", 1, "添加分享码");
insert into permission values (null, "/shareCode/delete", 1, "删除分享码");
insert into permission values (null, "/shareCode/list", 1, "获取分享码列表");
insert into permission values (null, "/shareCode/get", 0, "查询分享码");
*/

CREATE TABLE
    IF
    NOT EXISTS bookmark (
                            id INTEGER NOT NULL CONSTRAINT bookmark_pk PRIMARY KEY autoincrement,
                            name text,
                            url text,
                            icon text,
                            parentId INTEGER NOT NULL,
                            bookmarkType INTEGER NOT NULL
);

/*
insert into bookmark values (0, "root", null, null, -1, 0);
*/