<template id="share-list">
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
            <a href="#" @click="showDetails(item[key])">查看</a>
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
  created() {
    this.list();
  },
};
</script>
