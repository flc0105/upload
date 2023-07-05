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