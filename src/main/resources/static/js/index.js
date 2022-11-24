axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    return res.data;
  }
});

const path = "/";

const pasteListComponent = {
  template: document.getElementById("paste-list").innerHTML,
  data() {
    return {
      pastes: [],
      title: "",
      text: "",
    };
  },
  methods: {
    list() {
      this.$root.loading = true;
      axios
        .post(path + "paste/list")
        .then((res) => {
          if (res.success) {
            this.pastes = res.detail;
          } else {
            this.$root.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    add() {
      if (this.text.trim().length === 0) {
        return;
      }
      axios
        .post(
          path + "paste/add",
          Qs.stringify({
            title: this.title.trim().length === 0 ? "Untitled" : this.title,
            text: this.text,
          })
        )
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal(
              "发布成功",
              location.protocol +
                "//" +
                location.host +
                path +
                "p/" +
                res.detail.id
            );
            this.title = "";
            this.text = "";
          } else {
            this.$root.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        });
    },
  },
  created() {
    this.$root.list = this.list;
    this.list();
  },
};

const pasteDetailComponent = {
  template: document.getElementById("paste-detail").innerHTML,
  data() {
    return {
      title: "",
      text: "",
    };
  },
  methods: {
    get(id) {
      this.title = "";
      this.text = "";
      this.$root.loading = true;
      axios
        .post(path + "paste/get", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.title = res.detail.title;
            this.text = res.detail.text;
          } else {
            this.$router.push("/p");
          }
        })
        .catch((err) => {
          this.$router.push("/p");
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    update() {
      if (this.text.trim().length === 0) {
        return;
      }
      if (!this.$root.hasToken(() => this.update())) {
        return;
      }
      axios
        .post(
          path + "paste/update",
          Qs.stringify({
            id: this.$route.params.id,
            text: this.text,
          })
        )
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "修改成功");
          } else {
            this.$root.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        });
    },
    download() {
      var blob = new Blob([this.text], { type: "text/plain;charset=utf-8" });
      saveAs(blob, this.$route.params.id + ".txt");
    }
  },
  created() {
    if (this.$route.params.id) {
      this.get(this.$route.params.id);
    }
  },
};

const shareListComponent = {
  template: document.getElementById("share-list").innerHTML,
  data() {
    return {
      shareCodeList: {},
    };
  },
  methods: {
    list() {
      this.$root.loading = true;
      axios
        .post(path + "shareCode/list")
        .then((res) => {
          if (res.success) {
            this.shareCodeList = res.detail;
          } else {
            this.$root.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    remove(code) {
      axios
        .post(path + "shareCode/delete", Qs.stringify({ code: code }))
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal("成功", "删除成功");
          } else {
            this.$root.showModal("错误", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        });
    },
    visit(code) {
      this.$router.push("/s/" + code);
    },
  },
  created() {
    this.list();
  },
};

const shareDetailComponent = {
  template: document.getElementById("share-detail").innerHTML,
  data() {
    return {
      file: "",
    };
  },
  methods: {
    get(code) {
      this.$root.loading = true;
      axios
        .post(path + "shareCode/get", Qs.stringify({ code: code }))
        .then((res) => {
          if (res.success) {
            this.file = res.detail;
          } else {
            this.$router.push("/404");
          }
        })
        .catch((err) => {
          this.$router.push("/404");
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    download(relativePath) {
      location.href =
        path + "file/download?relativePath=" + encodeURIComponent(relativePath);
    },
  },
  created() {
    if (this.$route.params.code) {
      this.get(this.$route.params.code);
    }
  },
};

const routes = [
  {
    name: "index",
    path: "/",
    component: {
      created() {
        location.href = path + "files.html";
      },
    },
  },
  {
    name: "pasteList",
    path: "/p",
    component: pasteListComponent,
  },
  {
    name: "pasteDetail",
    path: "/p/:id(\\d+)",
    component: pasteDetailComponent,
  },
  {
    name: "shareList",
    path: "/s",
    component: shareListComponent,
  },
  {
    name: "shareDetail",
    path: "/s/:code",
    component: shareDetailComponent,
  },
  {
    name: "404",
    path: "/:pathMatch(.*)*",
    component: {
      template: "当前页面不存在",
    },
  },
];

const router = VueRouter.createRouter({
  history: VueRouter.createWebHistory(path),
  routes,
});

const app = Vue.createApp({
  data() {
    return {
      message: {
        text: "",
        title: "",
      },
      loading: false,
      list: "",
      confirmDelete: "",
      previousAct: "",
      file: "",
    };
  },
  methods: {
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new bootstrap.Modal(this.$refs.messageModal).show();
    },
    showConfirm(func) {
      new bootstrap.Modal(this.$refs.confirmModal).show();
      this.confirmDelete = func;
    },
    remove(id) {
      if (!this.hasToken(() => this.remove(id))) {
        return;
      }
      axios
        .post(path + "paste/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.showModal("成功", "删除成功");
            if (this.$route.params.id) {
              this.$router.push("/p");
            }
            this.list();
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
          path + "token/get",
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
  mounted() {
    new ClipboardJS("#btn-copy").on("success", () => {
      this.$root.showModal("成功", "复制成功");
    });
  },
});

app.use(router);
app.config.compilerOptions.whitespace = "preserve";
app.mount("body");
