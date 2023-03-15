<template>
  <input type="file" name="file" id="file" class="d-none" @change="(event) => importBookmarks(event)" multiple />
  <div class="input-group mb-2">
    <input class="form-control" v-model="url" @keyup.enter="add()" :disabled="$root.loading" />
    <button class="btn btn-outline-primary" @click="add()" :disabled="$root.loading">添加</button>
    <button class="btn btn-outline-primary" data-bs-toggle="dropdown" :disabled="$root.loading">
      <i class="bi bi-three-dots-vertical"></i>
      <ul class="dropdown-menu">
        <li>
          <a class="dropdown-item"
            onclick="document.getElementById('file').value=null; document.getElementById('file').click()">导入</a>
        </li>
        <li>
          <a class="dropdown-item" @click="exportBookmarks()">导出</a>
        </li>
        <li>
          <a class="dropdown-item" @click="updateAll()">更新</a>
        </li>
      </ul>
    </button>
  </div>
  <table class="table table-hover table-borderless border shadow-sm">
    <tbody>
      <tr v-for="bookmark in bookmarks" :key="bookmark">
        <td class="text-truncate w-45">
          <img v-if="bookmark.icon" class="icon" v-bind:src="'data:image/jpeg;base64,' + bookmark.icon" />
          <img v-else class="icon" src="/vite.svg" />
          <span v-if="bookmark.title" class="align-middle">{{ bookmark.title }}</span>
          <span v-else class="align-middle text-muted"><i>{{ extractDomain(bookmark.url) }}</i></span>
        </td>
        <td class="text-truncate w-45 url"><i class="text-muted">{{ bookmark.url }}</i></td>
        <td class="text-end" style="width: 10%">
          <div class="dropdown d-inline">
            <i class="bi bi-three-dots-vertical link-primary" data-bs-toggle="dropdown"></i>
            <ul class="dropdown-menu">
              <li>
                <a class="dropdown-item" :href="bookmark.url" target="_blank">打开</a>
              </li>
              <li>
                <a class="dropdown-item" id="btnCopy" :data-clipboard-text="bookmark.url">复制</a>
              </li>
              <li>
                <hr class="dropdown-divider">
              </li>
              <li>
                <a class="dropdown-item" @click="$root.showConfirm(() => remove(bookmark.id))">删除</a>
              </li>
              <li>
                <a class="dropdown-item"
                  @click="$root.showInput('修改标题', '输入新标题', function () { rename(bookmark.id) })">修改</a>
              </li>
              <li>
                <a class="dropdown-item" @click="update(bookmark.id)">更新</a>
              </li>
            </ul>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>


<style scoped>
.w-45 {
  width: 45%;
}

.icon {
  width: auto;
  height: 15px;
  margin-right: 10px;
  vertical-align: middle;
}

@media screen and (max-width: 768px) {
  .url {
    display: none;
  }
}
</style>

<script>

import axios from 'axios'
import Qs from 'qs'
import 'bootstrap/dist/js/bootstrap.bundle'
import 'file-saver'

export default {
  data() {
    return {
      bookmarks: {}, // 书签
      url: "", // 输入的url
    };
  },
  methods: {
    // 判断是不是json数据
    isJSON(str) {
      try {
        return JSON.parse(str) && !!str;
      } catch (e) {
        return false;
      }
    },
    // 从url中提取域名
    extractDomain(url) {
      const { hostname } = new URL(url);
      return hostname;
    },
    // 获取书签列表
    list() {
      this.$root.loading = true;
      axios
        .post("bookmark/list")
        .then((res) => {
          if (res.success) {
            this.bookmarks = res.detail;
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
    // 添加书签
    add() {
      if (this.url.trim().length === 0) {
        this.$root.showModal("提示", "URL不能为空");
        return;
      }
      this.$root.loading = true;
      axios
        .post("bookmark/add", Qs.stringify({ url: this.url }))
        .then((res) => {
          if (res.success) {
            this.list();
            this.update(res.detail);
            // axios
            //   .post("bookmark/update", Qs.stringify({ id: res.detail }))
            //   .then((res) => {
            //     if (res.success) {
            //       this.list();
            //       this.$root.showModal("成功", "更新成功");
            //     } else {
            //       this.$root.showModal("失败", "更新失败");
            //     }
            //   })
            //   .catch((err) => {
            //     this.$root.showModal("错误", err.message);
            //   });
          } else {
            this.$root.showModal("添加失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.url = "";
          this.$root.loading = false;
        });
    },
    // 删除书签
    remove(id) {
      if (!this.$root.hasToken(() => this.remove(id))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("bookmark/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "删除成功");
            this.list();
          } else {
            this.$root.showModal("失败", "删除失败");
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 修改书签标题
    rename(id) {
      if (this.$root.$refs.input.value.trim().length === 0) {
        this.$root.showModal("提示", "标题不能为空");
        return;
      }
      this.$root.loading = true;
      axios
        .post("bookmark/rename", Qs.stringify({ id: id, title: this.$root.$refs.input.value }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "修改成功");
            this.list();
          } else {
            this.$root.showModal("失败", "修改失败");
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 更新书签
    update(id) {
      this.$root.loading = true;
      axios
        .post("bookmark/update", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "更新成功");
            this.list();
          } else {
            this.$root.showModal("失败", "更新失败");
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 导入书签
    importBookmarks(event) {
      this.$root.loading = true;
      var reader = new FileReader();
      reader.readAsText(event.target.files[0]);
      reader.onload = (e) => {
        var data = e.target.result;
        if (!this.isJSON(data)) {
          this.$root.showModal("错误", "不是合法的json数据");
          this.$root.loading = false;
          return;
        }
        axios
          .post("bookmark/bulkAdd", Qs.stringify({ data: data }))
          .then((res) => {
            if (res.success) {
              this.list();
              this.$root.showModal("成功", res.msg);
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
      };
    },
    // 导出书签
    exportBookmarks() {
      this.$root.loading = true;
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
      this.$root.loading = false;
    },
    // 更新全部书签
    updateAll() {
      this.$root.loading = true;
      axios
        .post("bookmark/updateAll")
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal("成功", res.msg);
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
  mounted() {
    this.list();
  },
}
</script>