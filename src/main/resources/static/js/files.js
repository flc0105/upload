axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    return res.data;
  }
});

let cancel;
const CancelToken = axios.CancelToken;

const app = Vue.createApp({
  data() {
    return {
      message: {
        text: "",
        title: "",
      },
      loading: false,
      currentDirectory: "/",
      directories: [],
      files: [],
      progress: 0,
      filesToDelete: [],
      filter: "",
      multiSelect: false,
      checkedFiles: [],
      previousAct: null,
      columns: ["size", "lastModified"],
    };
  },
  methods: {
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new bootstrap.Modal(this.$refs.messageModal).show();
    },
    formatBytes(bytes) {
      if (bytes === 0) {
        return "0 B";
      }
      const i = Math.floor(Math.log(bytes) / Math.log(1024));
      return (
        parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) +
        " " +
        ["B", "kB", "MB", "GB"][i]
      );
    },
    list() {
      this.loading = true;
      this.files = [];
      this.checkedFiles = [];
      if (cancel !== undefined) {
        cancel();
      }
      axios
        .post(
          "file/list",
          Qs.stringify({ currentDirectory: this.currentDirectory }),
          {
            cancelToken: new CancelToken(function executor(c) {
              cancel = c;
            }),
          }
        )
        .then((res) => {
          if (res.success) {
            this.files = res.detail;
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    search() {
      if (this.filter.trim().length === 0) {
        this.list();
        return;
      }
      this.loading = true;
      this.files = [];
      this.checkedFiles = [];
      if (cancel !== undefined) {
        cancel();
      }
      axios
        .post(
          "file/search",
          Qs.stringify({
            filter: this.filter,
            currentDirectory: this.currentDirectory,
          }),
          {
            cancelToken: new CancelToken(function executor(c) {
              cancel = c;
            }),
          }
        )
        .then((res) => {
          if (res.success) {
            this.files = res.detail;
          } else {
            this.showModal("错误", res.msg);
          }
          this.loading = false;
        })
        .catch((err) => {
          if (err.name === "CanceledError") {
            return;
          }
          this.loading = false;
          this.showModal("错误", err.message);
        });
    },
    changeDirectory(relativePath) {
      this.currentDirectory = relativePath;
      this.list();
      this.getDirectoryHierachy();
    },
    getDirectoryHierachy() {
      this.directories = [];
      const displayNames = this.currentDirectory.split("/");
      const relativePaths = [];
      for (let i = 0; i < displayNames.length; i++) {
        relativePaths.push(displayNames[i]);
        const relativePath = relativePaths.join("/");
        this.directories.push({
          displayName: displayNames[i],
          relativePath: relativePath.length === 0 ? "/" : relativePath,
        });
      }
      this.directories[0].displayName = "Home";
    },
    upload(event) {
      if (!this.hasToken(() => this.upload(event))) {
        return;
      }
      const modal = new bootstrap.Modal(this.$refs.progressModal);
      modal.show();
      const formData = new FormData();
      formData.append("currentDirectory", this.currentDirectory);
      const files = Array.from(event.target.files);
      files.forEach((file) => {
        formData.append("files", file);
      });
      axios({
        method: "post",
        url: "file/upload",
        data: formData,
        headers: {
          "Content-Type": "multipart/form-data",
        },
        onUploadProgress: (e) => {
          if (e.lengthComputable) {
            const current = e.loaded;
            const total = e.total;
            this.progress = Math.round((current / total) * 100) + "%";
          }
        },
      })
        .then((res) => {
          if (res.success) {
            modal.hide();
            this.list();
            this.showModal("成功", "上传成功");
            this.progress = 0;
          } else {
            modal.hide();
            this.showModal("错误", res.msg);
            this.progress = 0;
          }
        })
        .catch((err) => {
          modal.hide();
          this.showModal("错误", err.message);
          this.progress = 0;
        });
    },
    download(relativePath) {
      location.href =
        "file/download?relativePath=" + encodeURIComponent(relativePath);
    },
    createZip(relativePath) {
      location.href =
        "file/zip?relativePath=" + encodeURIComponent(relativePath);
    },
    bulkDownload() {
      location.href =
        "file/bulk?relativePath=" +
        encodeURIComponent(JSON.stringify(this.checkedFiles));
    },
    confirmDelete(files) {
      this.filesToDelete = files;
      new bootstrap.Modal(this.$refs.confirmModal).show();
    },
    deleteFile() {
      if (!this.hasToken(() => this.deleteFile())) {
        return;
      }
      axios
        .post(
          "file/delete",
          Qs.stringify({ relativePath: JSON.stringify(this.filesToDelete) })
        )
        .then((res) => {
          if (res.success) {
            if (this.filter.length === 0) {
              this.list();
            } else {
              this.search(this.filter);
            }
            this.showModal("成功", "删除成功");
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
    inputDirectoryName() {
      this.$refs.directoryName.value = "";
      new bootstrap.Modal(this.$refs.inputModal).show();
      this.$refs.directoryName.focus();
    },
    createDirectory() {
      const directoryName = this.$refs.directoryName.value;
      if (directoryName.trim().length === 0) {
        this.showModal("错误", "文件夹名不能为空");
        return;
      }
      axios
        .post(
          "file/mkdir",
          Qs.stringify({
            relativePath: this.currentDirectory + "/" + directoryName,
          })
        )
        .then((res) => {
          if (res.success) {
            this.list();
            this.showModal("成功", "新建文件夹成功");
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
    checkAll(event) {
      if (event.target.checked) {
        this.files.folders.forEach((folder) => {
          this.checkedFiles.push(folder.relativePath);
        });
        this.files.files.forEach((file) => {
          this.checkedFiles.push(file.relativePath);
        });
      } else {
        this.checkedFiles = [];
      }
    },
    hideColumn(column) {
      this.columns.includes(column)
        ? (this.columns = this.columns.filter((item) => item !== column))
        : this.columns.push(column);
    },
    previewFile(fileType, filename) {
      if (fileType.includes("image")) {
        let image = document.getElementById("image");
        image.removeAttribute("src");
        image.setAttribute(
          "src",
          "file/download?relativePath=" + encodeURIComponent(filename)
        );
        new bootstrap.Modal(this.$refs.imageModal).show();
      } else if (fileType.includes("text")) {
        let text = document.getElementById("text");
        text.innerText = "";
        axios
          .post("file/read", Qs.stringify({ relativePath: filename }))
          .then((res) => {
            if (res.success) {
              text.innerText = res.detail;
              new bootstrap.Modal(this.$refs.textModal).show();
            } else {
              this.showModal("错误", res.msg);
            }
          })
          .catch((err) => {
            this.showModal("错误", err.message);
          });
      }
    },
    getIcon(filename, fileType) {
      const ext = filename.split(".").pop();
      const exts = {
        "bi-file-earmark-binary": ["exe"],
        "bi-file-earmark-zip": ["zip", "7z", "rar"],
        "bi-file-earmark-code": ["py", "java", "c", "cpp", "html", "js", "css"],
        "bi-file-earmark-pdf": ["pdf"],
        "bi-file-earmark-word": ["doc", "docx"],
        "bi-file-earmark-excel": ["xls", "xlsx"],
        "bi-file-earmark-ppt": ["ppt", "pptx"],
      };
      for (const [key, value] of Object.entries(exts)) {
        if (value.includes(ext)) {
          return key;
        }
      }
      const type = {
        text: "bi-file-earmark-text",
        image: "bi-file-earmark-image",
        audio: "bi-file-earmark-music",
        video: "bi-file-earmark-play",
      };
      let icon = type[fileType.split("/")[0]];
      if (icon != null) {
        return icon;
      }
      return " bi-file-earmark";
    },
    share(path) {
      axios
        .post("shareCode/add", Qs.stringify({ path: path }))
        .then((res) => {
          if (res.success) {
            this.showModal(
              "分享成功",
              location.protocol + "//" + location.host + "/s/" + res.detail
            );
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
    hasToken(func) {
      if (!Cookies.get("token")) {
        this.previousAct = func;
        this.$refs.password.value = "";
        new bootstrap.Modal(this.$refs.authModal).show();
        this.$refs.password.focus();
        return false;
      }
      return true;
    },
    getToken() {
      axios
        .post(
          "token/get",
          Qs.stringify({ password: this.$refs.password.value })
        )
        .then((res) => {
          if (res.success) {
            this.previousAct();
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
  },
  computed: {
    getParentDirectory() {
      let currentDirectory = this.currentDirectory.split("/");
      currentDirectory.pop();
      let parentDirectory = currentDirectory.join("/");
      return parentDirectory.length === 0 ? "/" : parentDirectory;
    },
  },
  mounted() {
    this.list();
    this.getDirectoryHierachy();
  },
});

app.config.compilerOptions.whitespace = "preserve";
app.mount("body");
