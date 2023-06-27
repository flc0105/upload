<template id="share-list">
  <table class="table">
    <thead>
      <tr>
        <th>{{ $t("share_code") }}</th>
        <th>{{ $t("relative_path") }}</th>
        <th class="status">{{ $t("status") }}</th>
        <th>{{ $t("action") }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="code in codes" :key="code">
        <td class="code">
          <i>{{ code.code }}</i>
        </td>
        <td class="text-truncate filePath">
          <i>{{ code.path }}</i>
        </td>
        <td class="status">
          <span class="badge text-bg-success" v-if="code.valid">{{
            $t("valid")
          }}</span>
          <span class="badge text-bg-danger" v-else>{{ $t("invalid") }}</span>
        </td>
        <td>
          <a
            v-if="code.valid"
            class="link-primary me-1"
            id="btnCopy"
            :data-clipboard-text="getLink(code.code)"
          >
            <i class="bi bi-clipboard"></i>
          </a>
          <a
            class="link-danger"
            @click="
              $root.showConfirm(function () {
                remove(code.code);
              })
            "
          >
            <i class="bi bi-trash"></i>
          </a>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.code {
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
}
</style>

<script>
import axios from "axios";
import Qs from "qs";

import "bootstrap/dist/js/bootstrap.bundle";
export default {
  data() {
    return {
      codes: {},
    };
  },
  methods: {
    // 获取分享码列表
    list() {
      this.$root.loading = true;
      axios
        .post("/shareCode/list")
        .then((res) => {
          if (res.success) {
            this.codes = res.detail;
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
    // 删除分享码
    remove(code) {
      if (!this.$root.hasToken(() => this.remove(code))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/shareCode/delete", Qs.stringify({ code: code }))
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
    getLink(code) {
      return (
        window.location.protocol +
        "//" +
        window.location.host +
        "/files/" +
        code
      );
    },
  },
  created() {
    this.list();
  },
};
</script>
