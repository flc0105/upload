<template>
  <div id="alert"></div>
  <v-contextmenu ref="contextmenu">
    <v-contextmenu-item @click="page(currentPage)">{{
      $t("refresh")
    }}</v-contextmenu-item>
    <v-contextmenu-item @click="clearLogs()">{{
      $t("clear")
    }}</v-contextmenu-item>
    <v-contextmenu-item @click="exportLogs()">{{
      $t("export")
    }}</v-contextmenu-item>
  </v-contextmenu>

  <div
    style="overflow-x: scroll; height: 470px"
    v-contextmenu:contextmenu
    v-show="show"
  >
    <table class="table table-hover" style="white-space: nowrap">
      <thead>
        <tr>
          <th>操作时间</th>
          <th>接口名称</th>
          <th>请求地址</th>
          <th>请求方式</th>
          <th>IP地址</th>
          <th>浏览器</th>
          <th>操作系统</th>
          <th>是否成功</th>
          <th>具体消息</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="log in logs" :key="value">
          <td>{{ log.operationTime }}</td>
          <td>{{ log.apiName }}</td>
          <td>{{ log.requestUrl }}</td>
          <td>{{ log.requestMethod }}</td>
          <td>{{ log.ipAddress }}</td>
          <td>{{ log.browser }}</td>
          <td>{{ log.operatingSystem }}</td>
          <td>{{ log.success }}</td>
          <td>{{ log.message }}</td>
          <td>
            <a class="link-primary me-1" @click="showDetail(log)">
              <i class="bi bi-info-circle"></i>
            </a>
            <a
              class="link-danger"
              @click="
                $root.showConfirm(function () {
                  deleteLog(log.id);
                })
              "
            >
              <i class="bi bi-trash"></i>
            </a>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <nav aria-label="Page navigation example" v-show="show">
    <ul class="pagination justify-content-center mt-4">
      <li :class="['page-item', { disabled: currentPage == 1 }]">
        <a class="page-link" @click="goToPage(currentPage - 1)">Previous</a>
      </li>
      <template v-for="page in totalPages" :key="page">
        <li class="page-item">
          <a
            class="page-link"
            href="#"
            @click="goToPage(page)"
            :class="['page-link', { active: page == currentPage }]"
            >{{ page }}</a
          >
        </li>
      </template>
      <li :class="['page-item', { disabled: currentPage == totalPages }]">
        <a class="page-link" href="#" @click="goToPage(currentPage + 1)"
          >Next</a
        >
      </li>
    </ul>
  </nav>
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
  /* font-style: italic; */
}
</style>

<script>
import axios from "axios";
import Qs from "qs";

import "bootstrap/dist/js/bootstrap.bundle";
import "file-saver";
import { directive, Contextmenu, ContextmenuItem } from "v-contextmenu";
import "v-contextmenu/dist/themes/default.css";

export default {
  directives: {
    contextmenu: directive,
  },
  components: {
    [Contextmenu.name]: Contextmenu,
    [ContextmenuItem.name]: ContextmenuItem,
  },
  data() {
    return {
      logs: {},
      totalPages: 0, // 总页数
      currentPage: 1, // 当前页数
      show: false,
    };
  },
  methods: {
    goToPage(page) {
      if (page == this.currentPage) {
        return;
      }
      if (page >= 1 && page <= this.totalPages) {
        this.page(page);
      }
    },
    page(page) {
      this.$root.loading = true;
      axios
        .get("/log/page?current=" + page)
        .then((res) => {
          if (res.success) {
            var result = res.detail;
            this.totalPages = result.pages;
            this.currentPage = result.current;
            this.logs = result.records;
            console.log(this.logs);
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
    showDetail(value) {
      this.$root.showModal(this.$t("view"), value);
    },
    // 清空日志
    deleteLog(id) {
      if (!this.$root.hasToken(() => this.deleteLog(id))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/log/delete",  Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            if (this.currentPage) {
              this.page(this.currentPage);
            } else {
              this.page(1);
            }
   
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
    // 清空日志
    clearLogs() {
      if (!this.$root.hasToken(() => this.clearLogs())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/log/clear")
        .then((res) => {
          if (res.success) {
            this.page(1);
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
      this.$root.loading = true;
      axios
        .post("/log/list")
        .then((res) => {
          if (res.success) {
            var blob = new Blob([JSON.stringify(res.detail, null, 2)], {
              type: "text/plain;charset=utf-8",
            });
            saveAs(blob, new Date().getTime() + ".txt");
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

      // var blob = new Blob([JSON.stringify(this.logs, null, 2)], {
      //   type: "text/plain;charset=utf-8",
      // });
      // saveAs(blob, new Date().getTime() + ".txt");
    },
  },
  created() {},
  mounted() {
    if (
      !this.$root.hasToken(() => {
        this.show = true;
        document.getElementById("alert").innerHTML = "";
        this.page(1);
      })
    ) {
      document.getElementById("alert").innerHTML = [
        `<div class="alert alert-danger alert-dismissible" role="alert">`,
        `   <div>` + this.$t("access_denied") + `</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        "</div>",
      ].join("");
      this.show = false;
      return;
    }
    this.page(1);
    this.show = true;
  },
};
</script>
