<template>
  <input class="form-control mb-2" placeholder="标题" v-model="title" />
  <textarea class="form-control mb-2" rows="10" placeholder="正文" v-model="text"></textarea>
  <button class="btn btn-outline-primary float-end mb-2" @click="add()">发布</button>
  <ul class="list-group clear-both">
    <li class="list-group-item list-group-item-action cursor-pointer" v-for="paste in pastes" :key="paste"
      @click="(event) => event.target.tagName !== 'A' && $router.push('/pastes/' + paste.id)">
      <div class="float-end">
        <a class="link-primary bi bi-clipboard" id="btnCopy" :data-clipboard-text="paste.text"></a>
        <a class="link-danger bi bi-trash ms-1" @click="$root.showConfirm(() => remove(paste.id))">
        </a>
      </div>
      <div>
        <p class="text-primary text-truncate mw-75">{{ paste.title }}</p>
        <p class="text-truncate mw-75">{{ paste.text }}</p>
        <p class="text-muted mb-0">发布于 {{ paste.time.slice(0, -3) }}</p>
      </div>
    </li>
  </ul>
</template>

<script>
import axios from 'axios'
import Qs from 'qs'

export default {
  data() {
    return {
      pastes: [],
      title: "",
      text: "",
    };
  },
  methods: {
    // 获取文本
    list() {
      this.$root.loading = true;
      axios
        .post("/paste/list")
        .then((res) => {
          if (res.success) {
            this.pastes = res.detail;
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
    // 添加文本
    add() {
      if (this.text.trim().length === 0) {
        this.$root.showModal("提示", "正文不能为空")
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/add",
          Qs.stringify({
            title: this.title.trim().length === 0 ? "未命名" : this.title,
            text: this.text,
          })
        )
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal("发布成功", location.protocol + "//" + location.host + "/pastes/" + res.detail.id);
            this.text = ""
            this.title = ""
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
    remove(id) {
      if (!this.$root.hasToken(() => this.remove(id))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "删除成功");
            this.list();
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
  },
  created() {
    this.list();
  },
}
</script>