<template id="share-list">
  <div id="alert"></div>

  <div class="btn-group mb-3">
    <button class="btn btn-outline-primary" @click="list()">刷新</button>
    <button class="btn btn-outline-primary" @click="exportLogs()">导出</button>
    <button class="btn btn-outline-primary" @click="clearLogs()">清空</button>
  </div>

  <table class="table table-hover" style="white-space: nowrap">
    <thead>
      <tr>
        <th v-for="(value, key) in logs[0]" :key="key">{{ key }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, index) in logs" :key="index">
        <!-- <td v-for="(value, key) in item" :key="key">{{ value }}</td> -->

        <td v-for="(value, key) in item" :key="key" :title="item[key]">
          <template v-if="['Stack trace', '堆栈信息', 'Token'].includes(key)">
            <a href="#" @click="showDetails(item[key])" v-if="item[key] != null"
              >查看</a
            ><span v-else>null</span>
          </template>
          <template v-else>{{ value }}</template>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
/* .table th,
td {
  text-align: center;
} */

td {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<script>
import axios from "axios";
import Qs from "qs";

import "bootstrap/dist/js/bootstrap.bundle";
import "file-saver";
export default {
  data() {
    return {
      logs: {},
    };
  },
  methods: {
    list() {
      this.$root.loading = true;
      axios
        .post("/logs/list")
        .then((res) => {
          if (res.success) {
            this.logs = res.detail;
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
    showDetails(value) {
      this.$root.showModal("查看", value);
    },
    // 新建文件夹
    clearLogs() {
      if (!this.$root.hasToken(() => this.clearLogs())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("logs/delete")
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
    exportLogs() {
      var blob = new Blob([JSON.stringify(this.logs, null, 2)], {
        type: "text/plain;charset=utf-8",
      });
      saveAs(blob, new Date().getTime() + ".txt");
    },
  },
  created() {},
  mounted() {
    if (
      !this.$root.hasToken(() => {
        document.getElementById("alert").innerHTML = "";
        this.list();
      })
    ) {
      document.getElementById("alert").innerHTML = [
        `<div class="alert alert-danger alert-dismissible" role="alert">`,
        `   <div>拒绝访问</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        "</div>",
      ].join("");
      return;
    }

    this.list();
  },
};
</script>
