# upload

## 部署

### 下载

```shell
# 下载
wget https://github.com/flc0105/upload/releases/download/v0.0.11/upload.zip

# 解压
unzip upload.zip
```

### supervisor

```shell
# 安装supervisor
yum install -y supervisor

# 安装openjdk
yum install -y java-1.8.0-openjdk

# 添加配置
vim /etc/supervisord.d/upload.ini

[program:upload]
command=/usr/bin/java -jar /root/upload/upload-0.0.10-SNAPSHOT.jar
redirect_stderr=true
stdout_logfile=/root/upload/upload.stdout.log

# 启动supervisor服务
systemctl start supervisord.service
systemctl enable supervisord.service

# 启动
supervisorctl start upload
```

### docker

```shell
cd upload/

# 构建镜像
docker build -t flc/upload .

# 启动容器
docker run --restart=always -p <本地端口>:80 -v <本地目录>:/root/upload/files --env PASSWORD=<密码> --env PORT=80 -d flc/upload
```

