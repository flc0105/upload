<template>
  <div id="alert"></div>
  <h2 class="pt-2 pb-2 border-bottom">{{ $t("server_info") }}</h2>

  <div class="row g-4 py-5 row-cols-1 row-cols-lg-4">
    <div class="col" v-for="(value, key) in info" :key="key">
      <div class="card rounded-3 shadow-sm" style="min-height: 10rem">
        <div class="card-body">
          <h5 class="card-title text-primary">{{ key }}</h5>
          <p class="card-text">
            {{ value }}
          </p>
        </div>
      </div>
    </div>
  </div>

  <h2 class="pt-2 pb-2 border-bottom">{{ $t("configuration") }}</h2>

  <div class="row g-4 py-5 row-cols-1 row-cols-lg-4">
    <div class="col" v-for="(value, key) in config" :key="key">
      <div class="card rounded-3 shadow-sm" style="min-height: 10rem">
        <div class="card-body">
          <h5 class="card-title text-primary">{{ key }}</h5>
          <p
            class="card-text"
            style="
              min-height: 3rem;
              max-height: 3rem;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            "
          >
            {{ value }}
          </p>
          <div class="d-flex justify-content-between align-items-center">
            <div class="btn-group">
              <button
                type="button"
                class="btn btn-sm btn-outline-primary"
                @click="showDetails(value)"
              >
                View
              </button>
              <button
                type="button"
                class="btn btn-sm btn-outline-primary"
                @click="
                  $root.inputValue = value;
                  $root.showInput(
                    $t('update_configuration'),
                    $t('enter_configuration_value', { item: key }),
                    function () {
                      update(key, parseValue($root.$refs.input.value));
                    }
                  );
                "
              >
                Edit
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>

<script>
import axios from "axios";

import "bootstrap/dist/js/bootstrap.bundle";
export default {
  data() {
    return {
      info: {},
      config: {},
    };
  },
  methods: {
    list() {
      this.$root.loading = true;
      axios
        .post("/info")
        .then((res) => {
          if (res.success) {
            this.info = res.detail;
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

    listConfig() {
      this.$root.loading = true;
      axios
        .post("/config/list")
        .then((res) => {
          if (res.success) {
            this.config = res.detail;
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
    update(key, value) {
      this.$root.loading = true;
      axios
        .post("/config/update", {
          key: key,
          value: value,
        })
        .then((res) => {
          if (res.success) {
            this.$root.showModal(this.$t("success"), res.msg);
            this.listConfig();
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
    parseValue(value) {
      // 尝试解析为布尔值
      if (value === "true" || value === "false") {
        return value === "true";
      }
      // 尝试解析为整数
      if (!isNaN(value)) {
        return parseInt(value);
      }

      if (value.includes(",")) {
        // 如果输入值包含逗号，则按逗号分割为一个列表
        var lst = value
          .split(",")
          .map((item) => this.parseValue(item.trim()))
          .filter(
            (item) =>
              item != null &&
              item !== undefined &&
              item.length != 0 &&
              item.length != undefined
          );
        return lst;
      }

      // 保持为字符串
      return value;
    },
  },
  created() {},
  mounted() {
    if (
      !this.$root.hasToken(() => {
        document.getElementById("alert").innerHTML = "";
        this.list();
        this.listConfig();
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
    this.listConfig();
  },
};
</script>
