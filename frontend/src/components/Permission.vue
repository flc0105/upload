<template>
  <div>
    <table class="table table-hover">
      <thead>
        <tr>
          <th>{{ $t("api_name") }}</th>
          <th>{{ $t("api_url") }}</th>
          <th>{{ $t("admin") }}</th>
          <th>{{ $t("anonymous") }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="permission in permissions" :key="permission.id">
          <td>{{ permission.desc }}</td>
          <td>{{ permission.path }}</td>
          <td>
            <input type="checkbox" class="form-check-input" checked disabled />
          </td>
          <td>
            <input
              type="checkbox"
              class="form-check-input"
              :checked="permission.isAdmin == 0"
              @change="save(permission.path, $event)"
            />
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped></style>

<script>
import axios from "axios";
import Qs from "qs";

import "bootstrap/dist/js/bootstrap.bundle";
export default {
  data() {
    return {
      permissions: [],
    };
  },
  methods: {
    list() {
      this.$root.loading = true;
      axios
        .get("/permission/list")
        .then((res) => {
          if (res.success) {
            this.permissions = res.detail;
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
    save(path, event) {
      axios
        .post("/permission/update", {
          path: path,
          isAdmin: event.target.checked ? 0 : 1,
        })
        .then((res) => {
          if (res.success) {
            this.$root.showToast(this.$t("success"), res.msg);
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
  },
  created() {},
  mounted() {
    this.list();
  },
};
</script>
