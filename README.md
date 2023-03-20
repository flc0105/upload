# upload

## 部署

### 下载

```shell
# 创建目录
mkdir /root/upload/

# 切换目录
cd /root/upload/

# 下载
wget https://github.com/flc0105/upload/releases/download/v0.1.4/upload-0.1.4-SNAPSHOT.jar
```

### 启动

```java
java -jar upload-0.1.4-SNAPSHOT.jar
```

参数：

`--spring.config.location=application.properties` 外部配置文件

`--server.port=8080` 启动端口

`--upload.path` 文件存储路径

`--password` 密码

`--webdav.username`/`--webdav.password` webdav用户名/密码

`--private.directories` 私密目录

### supervisor

#### CentOS

```shell
# 安装supervisor
yum install -y supervisor

# 安装openjdk
yum install -y java-1.8.0-openjdk

# 添加配置
vim /etc/supervisord.d/upload.ini

[program:upload]
command=/usr/bin/java -jar /root/upload/upload-0.1.4-SNAPSHOT.jar
redirect_stderr=true
stdout_logfile=/root/upload/upload.stdout.log

# 启动supervisor服务
systemctl start supervisord.service
systemctl enable supervisord.service

# 启动
supervisorctl start upload
```

#### Ubuntu

```shell
# 安装openjdk
apt-get update
apt install openjdk-8-jdk

# 安装supervisor
apt install supervisor

# 添加配置
vim /etc/supervisor/conf.d/upload.conf

[program:upload]
command=/usr/bin/java -jar /root/upload/upload-0.1.4-SNAPSHOT.jar
redirect_stderr=true
stdout_logfile=/root/upload/upload.stdout.log

# 使配置生效
supervisorctl reread && supervisorctl update
```

### docker

```shell
# 创建Dockerfile
vim Dockerfile
```

```dockerfile
FROM openjdk:8-jdk-alpine
WORKDIR /root/upload
COPY *.jar app.jar
ENV PORT 80
ENV PASSWORD flc
ENTRYPOINT java -jar app.jar --server.port=$PORT --password=$PASSWORD
```

```shell
# 构建镜像
docker build -t flc/upload .

# 启动容器
docker run --restart=always -p <本地端口>:80 -v <本地目录>:/root/upload/files --env PASSWORD=<密码> --env PORT=80 -d flc/upload
```

