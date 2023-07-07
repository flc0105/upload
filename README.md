# upload

A file listing program developed using `Vue.js` and `Spring Boot`.

## Features

### File

- Supports basic file operations: uploading, downloading, deleting, renaming, moving, creating folders, etc.
- Supports bulk operations on files: batch downloading, batch deleting, batch moving.
- Supports upload and download progress bars and real-time speed display.
- Supports downloading files/folders as a compressed package.
- Supports fuzzy search for quick file and directory lookup.
- Supports viewing detailed information of files and folders.
- Supports previewing image and text files (with code highlighting).
- Allows setting private directories, accessible only by administrator.
- Supports drag and drop file uploads (allowing guest uploads).
- Supports generating file sharing links and image direct links.
- Supports WebDAV.

### Paste

- Supports posting, deleting, modifying and viewing pastes.
- Supports sharing Paste as individual links.
- Allows setting pastes as private, which will not be displayed in the listing.
- Supports setting expiration time for pastes, which will be automatically deleted after expiration.
- Supports setting pastes to be self-destructing after viewing.
- Supports code highlighting for viewing pastes.

### Bookmark

- Supports adding, deleting, modifying and viewing bookmarks.
- Supports directory structure.
- Supports automatically retrieving website titles and icons.
- Supports importing and exporting bookmarks in JSON format.

### Other

- Supports internationalization.
- Supports logging of operation history, detailed request logs for each API.
- Supports simple authentication: administrator and guest.
- Allows assigning permissions for each API and operation.
- Supports viewing server status and information.
- Supports modifying and viewing configuration settings.

## Deployment

### Download

```shell
# Create directory
mkdir /root/upload/

# Change directory
cd /root/upload/

# Download
wget https://github.com/flc0105/upload/releases/download/v0.x.x/upload-0.x.x-SNAPSHOT.jar
```

### Start

```java
java -jar upload-0.x.x-SNAPSHOT.jar
```

Parameters:

- `--spring.config.location=application.properties` External configuration file
- `--server.port=8080` Port to start the server
- `--upload.path`File storage path
- `--password` Administrator password
- `--webdav.username`/`--webdav.password` WebDAV username/password

### Supervisor

#### CentOS

```shell
# Install supervisor
yum install -y supervisor

# Install openjdk
yum install -y java-1.8.0-openjdk

# Add configuration
vim /etc/supervisord.d/upload.ini

[program:upload]
command=/usr/bin/java -jar /root/upload/upload-0.x.x-SNAPSHOT.jar
redirect_stderr=true
stdout_logfile=/root/upload/upload.stdout.log

# Start supervisor service
systemctl start supervisord.service
systemctl enable supervisord.service

# Start
supervisorctl start upload
```

#### Ubuntu

```shell
# Install openjdk
apt-get update
apt install openjdk-8-jdk

# Install supervisor
apt install supervisor

# Add configuration
vim /etc/supervisor/conf.d/upload.conf

[program:upload]
command=/usr/bin/java -jar /root/upload/upload-0.x.x-SNAPSHOT.jar
redirect_stderr=true
stdout_logfile=/root/upload/upload.stdout.log

# Apply the configuration
supervisorctl reread && supervisorctl update
```

### Docker

```dockerfile
# Create Dockerfile
vim Dockerfile

FROM openjdk:8-jdk-alpine
WORKDIR /root/upload
COPY *.jar app.jar
ENV PORT 80
ENV PASSWORD flc
ENTRYPOINT java -jar app.jar --server.port=$PORT --password=$PASSWORD

# Build the image
docker build -t flc/upload .

# Run the container
docker run --restart=always -p <local_port>:80 -v <local_directory>:/root/upload/files --env PASSWORD=<password> --env PORT=80 -d flc/upload
```
