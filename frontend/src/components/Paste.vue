<template>
  <div class="card">
    <div class="card-body border-bottom" style="padding: 0.781rem 1rem">
      <div class="d-flex justify-content-between flex-wrap align-items-center">
        <a class="link-primary" @click="$router.push('/pastes')">返回</a>
        <div class="paste-title text-muted text-truncate mw-50">
          {{ title }}
          <span
            class="text-danger"
            v-if="expiredDate"
            style="font-size: 0.875rem"
            >&nbsp;
            <span v-if="expiredDate == -1">阅后即焚</span>
            <span v-else>Expired {{ getFromNow(expiredDate) }}</span>
          </span>
        </div>
        <div>
          <form class="form-check form-switch d-inline-block align-middle me-2">
            <input
              class="form-check-input"
              type="checkbox"
              role="switch"
              id="highlight"
              v-model="checked"
            />
            <label
              class="form-check-label"
              for="highlight"
              style="user-select: none"
              >代码高亮</label
            >
          </form>
          <a
            class="btn btn-outline-primary btn-sm"
            id="btnCopy"
            :data-clipboard-text="text"
            >复制</a
          >
          <!-- <a class="btn btn-outline-primary btn-sm" id="btnCopy" data-clipboard-target="#textarea">复制</a> -->
          <a class="btn btn-outline-primary btn-sm ms-1" @click="download()"
            >下载</a
          >
          <button
            :disabled="checked"
            class="btn btn-outline-primary btn-sm ms-1"
            @click="update()"
          >
            提交修改
          </button>
          <a
            class="btn btn-outline-danger btn-sm ms-1"
            @click="$root.showConfirm(() => remove())"
            >删除</a
          >
        </div>
      </div>
    </div>
    <highlightjs
      v-if="checked"
      autodetect
      :code="text"
      class="mb-0 min-vh-75"
    ></highlightjs>
    <textarea
      v-else
      id="textarea"
      class="form-control border-0 min-vh-75 monospace"
      v-model="text"
    ></textarea>
  </div>
</template>

<style scoped>
.form-check {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

@media screen and (max-width: 768px) {
  .paste-title {
    display: none;
  }

  .form-check {
    display: none !important;
  }
}

/* 自动换行 */
pre,
code,
highlightjs,
.hljs {
  white-space: pre-wrap !important;
  word-wrap: break-word !important;
}
</style>

<script>
import axios from "axios";
import Qs from "qs";
import "file-saver";
import moment from "moment";
import hljsVuePlugin from "@highlightjs/vue-plugin";

export default {
  components: {
    highlightjs: hljsVuePlugin.component,
  },
  data() {
    return {
      title: "",
      text: "",
      checked: false,
    };
  },
  methods: {
    getFromNow(date) {
      return moment(date).fromNow();
    },
    //获取文本
    get(id) {
      this.title = "";
      this.text = "";
      this.$root.loading = true;
      axios
        .post("/paste/get", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.expiredDate = res.detail.expiredDate;
            this.title = res.detail.title;
            this.text = res.detail.text;
          } else {
            this.$router.push("/pastes");
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
          this.$router.push("/pastes");
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    //修改文本
    update() {
      if (this.text.trim().length === 0) {
        this.$root.showModal("提示", "正文不能为空");
        return;
      }
      if (!this.$root.hasToken(() => this.update())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/update", {
          id: this.$route.params.id,
          text: this.text,
        })
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "修改成功");
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 删除文本
    remove() {
      if (!this.$root.hasToken(() => this.remove())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/delete", Qs.stringify({ id: this.$route.params.id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "删除成功");
            this.$router.push("/pastes");
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    //下载文本
    download() {
      var blob = new Blob([this.text], { type: "text/plain;charset=utf-8" });
      saveAs(blob, this.$route.params.id + ".txt");
    },
  },
  created() {
    if (this.$route.params.id) {
      this.get(this.$route.params.id);
      if (this.$route.query.highlight == "true") {
        // 如果url带有highlight=true的参数默认开启代码高亮
        this.checked = true;
      }
    }
  },
};
</script>
