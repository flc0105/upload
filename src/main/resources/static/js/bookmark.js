axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    return res.data;
  }
});

const app = Vue.createApp({
  data() {
    return {
      message: {
        text: "",
        title: "",
      },
      bookmarks: {},
      url: "",
    };
  },
  methods: {
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new bootstrap.Modal(this.$refs.messageModal).show();
    },
    list() {
      axios
        .post("/bookmark/list")
        .then((res) => {
          if (res.success) {
            this.bookmarks = res.detail;
          } else {
            this.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
    add() {
      if (this.url.trim().length === 0) {
        this.$refs.url.focus();
        return;
      }
      axios
        .post("/bookmark/add", Qs.stringify({ url: this.url }))
        .then((res) => {
          if (res.success) {
            this.list();
            axios
              .post("/bookmark/update", Qs.stringify({ id: res.detail }))
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
        });
    },
    del(id) {
      axios
        .post("/bookmark/delete", Qs.stringify({ id: id }))
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
    },
  },
  mounted() {
    new ClipboardJS("#btnCopy").on("success", () => {
      this.showModal("成功", "复制成功");
    });
    this.list();
  },
});

app.config.compilerOptions.whitespace = "preserve";
app.mount("body");
