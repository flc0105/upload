axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    return res.data;
  }
});

function isJSON(str) {
  try {
    return JSON.parse(str) && !!str;
  } catch (e) {
    return false;
  }
}

const app = Vue.createApp({
  data() {
    return {
      message: {
        text: "",
        title: "",
      },
      bookmarks: {},
      url: "",
      loading: false,
    };
  },
  methods: {
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new bootstrap.Modal(this.$refs.messageModal).show();
    },
    list() {
      this.loading = true;
      axios
        .post("bookmark/list")
        .then((res) => {
          if (res.success) {
            this.bookmarks = res.detail;
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
    add() {
      if (this.url.trim().length === 0) {
        return;
      }
      this.loading = true;
      axios
        .post("bookmark/add", Qs.stringify({ url: this.url }))
        .then((res) => {
          if (res.success) {
            this.list();
            axios
              .post("bookmark/update", Qs.stringify({ id: res.detail }))
              .then((res) => {
                if (res.success) {
                  this.list();
                } else {
                  this.showModal("错误", res.msg);
                }
              })
              .catch((err) => {
                this.showModal("错误", err.message);
              });
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        })
        .finally(() => {
          this.url = "";
          this.loading = false;
        });
    },
    remove(id) {
      this.loading = true;
      axios
        .post("bookmark/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.list();
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
    importBookmarks(event) {
      this.loading = true;
      var reader = new FileReader();
      reader.readAsText(event.target.files[0]);
      reader.onload = (e) => {
        data = e.target.result;
        if (!isJSON(data)) {
          this.showModal("错误", "导入失败");
          this.loading = false;
          return;
        }
        axios
          .post("bookmark/bulkAdd", Qs.stringify({ data: data }))
          .then((res) => {
            if (res.success) {
              this.list();
              this.showModal("成功", res.msg);
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
      };
    },
    exportBookmarks() {
      this.loading = true;
      var bookmarks = [];
      for (const [key, value] of Object.entries(this.bookmarks)) {
        var obj = new Object();
        obj.title = value.title;
        obj.url = value.url;
        bookmarks.push(obj);
      }
      var json = JSON.stringify(bookmarks, null, 2);
      var blob = new Blob([json], { type: "text/plain;charset=utf-8" });
      saveAs(blob, "bookmarks.json");
      this.loading = false;
    },
    updateAll() {
      this.loading = true;
      axios
        .post("bookmark/updateAll")
        .then((res) => {
          if (res.success) {
            this.list();
            this.showModal("成功", res.msg);
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
  },
  mounted() {
    new ClipboardJS("#btn-copy").on("success", () => {
      this.showModal("成功", "复制成功");
    });
    this.list();
  },
});

app.config.compilerOptions.whitespace = "preserve";
app.mount("body");
