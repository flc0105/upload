<template id="share-list">
  <div id="alert"></div>

  <!-- <div class="btn-group mb-3">
    <button class="btn btn-outline-primary" @click="list()">
      {{ $t("refresh") }}
    </button>
    <button class="btn btn-outline-primary" @click="exportLogs()">
      {{ $t("export") }}
    </button>
    <button class="btn btn-outline-primary" @click="clearLogs()">
      {{ $t("clear") }}
    </button>
  </div> -->

  <div style="overflow-x: scroll; height: 460px">
    <table class="table table-hover" style="white-space: nowrap">
      <thead>
        <tr>
          <th v-for="(value, key) in logs[0]" :key="key">{{ key }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in logs" :key="index">
          <td v-for="(value, key) in item" :key="key" :title="item[key]">
            <template
              v-if="
                [
                  'Token',
                  'Referer',
                  'Parameter type',
                  '访问来源',
                  '参数类型',
                ].includes(key)
              "
            >
              <a
                href="#"
                class="link-primary"
                @click="showDetails(item[key])"
                v-if="item[key] != null && item[key] != '{ }'"
                >{{ $t("view") }}</a
              ><span v-else>null</span>
            </template>
            <template v-else>{{ value }}</template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
th {
  text-align: center;
}
td {
  text-align: center;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-style: italic;
}
</style>

<script>
import axios from "axios";

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
            this.$root.showModal(this.$t("error"), res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal(this.$t("error"), err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    showDetails(value) {
      this.$root.showModal(this.$t("view"), value);
    },
    // 清空日志
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
            this.$root.showModal(this.$t("success"), res.msg);
          } else {
            this.$root.showModal(this.$t("error"), res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal(this.$t("error"), err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 导出日志
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
