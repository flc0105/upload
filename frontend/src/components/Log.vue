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

  <div style="overflow-x: scroll; height: 470px" v-contextmenu:contextmenu>
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

  <nav aria-label="Page navigation example">
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
  font-style: italic;
}
</style>

<script>
import axios from "axios";

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
    };
  },
  methods: {
    // list() {
    //   this.$root.loading = true;
    //   axios
    //     .post("/logs/list")
    //     .then((res) => {
    //       if (res.success) {
    //         this.logs = res.detail;
    //       } else {
    //         this.$root.showModal(this.$t("error"), res.msg);
    //       }
    //     })
    //     .catch((err) => {
    //       this.$root.showModal(this.$t("error"), err.message);
    //     })
    //     .finally(() => {
    //       this.$root.loading = false;
    //     });
    // },
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
        .get("/logs/list?page=" + page)
        .then((res) => {
          if (res.success) {
            var result = res.detail;
            this.totalPages = result.totalPages;
            this.currentPage = result.currentPage;
            this.logs = result.data;
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
        this.page(1);
        // this.list();
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

    // this.list();
    this.page(1);
  },
};
</script>
