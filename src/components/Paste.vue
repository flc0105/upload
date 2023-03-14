<template>
  <div class="card">
    <div class="card-body border-bottom" style="padding: 0.781rem 1rem">
      <div class="d-flex justify-content-between flex-wrap align-items-center">
        <a class="link-primary" @click="$router.push('/pastes')">返回</a>
        <div class="paste-title text-muted text-truncate mw-50">{{ title }}</div>
        <div>
          <a class="btn btn-outline-primary btn-sm" id="btnCopy" data-clipboard-target="#textarea">复制</a>
          <a class="btn btn-outline-primary btn-sm ms-1" @click="download()">下载</a>
          <a class="btn btn-outline-primary btn-sm ms-1" @click="update()">提交修改</a>
          <a class="btn btn-outline-danger btn-sm ms-1" @click="$root.showConfirm(() => remove())">删除</a>
        </div>
      </div>
    </div>
    <textarea id="textarea" class="form-control border-0 min-vh-75" v-model="text"></textarea>
  </div>
</template>
  
<script>
import axios from 'axios'
import Qs from 'qs'
import 'file-saver'

export default {
  data() {
    return {
      title: "",
      text: "",
    };
  },
  methods: {
    //获取文本
    get(id) {
      this.title = "";
      this.text = "";
      this.$root.loading = true;
      axios
        .post("/paste/get", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
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
        this.$root.showModal("提示", "正文不能为空")
        return;
      }
      if (!this.$root.hasToken(() => this.update())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/update",
          Qs.stringify({
            id: this.$route.params.id,
            text: this.text,
          })
        )
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
    }
  },
  created() {
    if (this.$route.params.id) {
      this.get(this.$route.params.id);
    }
  }
};
</script>