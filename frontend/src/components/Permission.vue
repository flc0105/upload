<template>
  <div>
    <table class="table table-hover">
      <thead>
        <tr>
          <th>操作</th>
          <th>接口地址</th>
          <th>管理员</th>
          <th>匿名用户</th>
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
    save(path, event) {
      axios
        .post("/permission/set", {
          path: path,
          isAdmin: event.target.checked ? 0 : 1,
        })
        .then((res) => {
          if (res.success) {
            this.$root.showToast(path, "修改权限成功");
          } else {
            this.$root.showModal(path, res.msg);
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
  created() {},
  mounted() {
    this.list();
  },
};
</script>
