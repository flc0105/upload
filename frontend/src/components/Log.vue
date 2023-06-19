<template id="share-list">
  <div id="alert"></div>
  <table class="table table-center" style="white-space: nowrap">
    <thead>
      <tr>
        <th v-for="(value, key) in logs[0]" :key="key">{{ key }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, index) in logs" :key="index">
        <!-- <td v-for="(value, key) in item" :key="key">{{ value }}</td> -->

        <td v-for="(value, key) in item" :key="key" :title="item[key]">
          <template v-if="['返回结果', 'token'].includes(key)">
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
.table th,
td {
  text-align: center;
}

table {
  /* width: 100%;
  table-layout: fixed; */
}

td {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* .code {
  width: 10%;
  min-width: 80px;
}

.filePath {
  max-width: 200px;
}

@media screen and (max-width: 768px) {
  .status {
    display: none;
  }
} */
</style>

<script>
import axios from "axios";
import Qs from "qs";

import "bootstrap/dist/js/bootstrap.bundle";
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
        .post("/logs")
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
