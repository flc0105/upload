<template>
  <input class="form-control mb-2" :placeholder="$t('title')" v-model="title" />
  <textarea
    class="form-control mb-3"
    rows="10"
    :placeholder="$t('text')"
    v-model="text"
    @keyup.ctrl.enter="add()"
  ></textarea>
  <div class="float-end mb-3 row g-3 align-middle">
    <div class="col-auto">
      <div class="form-check form-switch" style="padding: 0.375rem 0.75rem">
        <input
          class="form-check-input"
          type="checkbox"
          role="switch"
          id="isPrivate"
          ref="isPrivate"
        />
        <label class="form-check-label" for="isPrivate">{{
          $t("private")
        }}</label>
      </div>
    </div>

    <div class="col-auto">
      <select class="form-select mb-2" ref="select">
        <option value="10" :selected="$root.noToken()">
          10 {{ $t("minutes") }}
        </option>
        <option value="60">1 {{ $t("hours") }}</option>
        <option value="1440">1 {{ $t("days") }}</option>
        <option value="10080">1 {{ $t("weeks") }}</option>
        <option value="-1">{{ $t("burn_after_reading") }}</option>
        <option value="0" :selected="!$root.noToken()">
          {{ $t("never_expires") }}
        </option>
        <!-- 只要有名为token的Cookie就默认选中永不过期，但不会去验证token -->
      </select>
    </div>

    <div class="col-auto">
      <button class="btn btn-outline-primary mb-2 col-auto" @click="add()">
        {{ $t("post") }}
      </button>
    </div>
  </div>
  <ul class="list-group clear-both">
    <li
      class="list-group-item list-group-item-action cursor-pointer"
      v-for="paste in pastes"
      :key="paste"
      @click="
        (event) =>
          event.target.tagName !== 'A' && $router.push('/pastes/' + paste.id)
      "
    >
      <div class="float-end">
        <a
          class="link-primary bi bi-clipboard"
          id="btnCopy"
          :data-clipboard-text="paste.text"
        ></a>
        <a
          class="link-danger bi bi-trash ms-1"
          @click="$root.showConfirm(() => remove(paste.id))"
        >
        </a>
      </div>
      <div>
        <p class="text-primary text-truncate mw-75">{{ paste.title }}</p>
        <p class="text-truncate mw-75">
          <i>{{ paste.text }}</i>
        </p>
        <p class="text-muted mb-0">
          {{ $t("post_on") }} {{ paste.time.slice(0, -3) }}
          <span
            class="text-danger"
            v-if="paste.expiredDate"
            style="font-size: 0.875rem"
            >&nbsp;&nbsp;
            <span v-if="paste.expiredDate == -1">{{
              $t("burn_after_reading")
            }}</span>
            <span v-else>Expired {{ getFromNow(paste.expiredDate) }}</span>
          </span>
        </p>
      </div>
    </li>
  </ul>
</template>

<style scoped>
.list-group-item {
  word-break: break-all;
  padding: 1rem 1rem !important;
}
</style>

<script>
import axios from "axios";
import Qs from "qs";

import moment from "moment";

// const endpoints = Vue.prototype.$endpoints;

export default {
  data() {
    return {
      pastes: [],
      title: "",
      text: "",
      endpoints: [],
    };
  },
  methods: {
    fetchTokenProtectedEndpoints() {
      axios
        .get("/permission/protected") // 替换为实际的后端接口地址
        .then((response) => {
          this.endpoints = response.detail;
        })
        .catch((error) => {
          console.error("获取接口列表失败", error);
        });
    },

    getFromNow(date) {
      return moment(date).fromNow();
    },
    // 计算过期时间
    calcExpiredDate(minutes) {
      if (minutes == 0) {
        return undefined;
      }
      if (minutes == -1) {
        return -1;
      }
      var dateObj = moment(new Date()).add(minutes, "m").toDate();
      return moment(dateObj).format("YYYY-MM-DD HH:mm:ss");
    },
    // 获取文本
    list() {
      this.$root.loading = true;
      axios
        .get("/paste/list")
        .then((res) => {
          if (res.success) {
            this.pastes = res.detail;
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
    // 添加文本
    add() {
      if (this.text.trim().length === 0) {
        this.$root.showModal("提示", "正文不能为空");
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/add", {
          title: this.title.trim().length === 0 ? "未命名" : this.title,
          text: this.text,
          expiredDate: this.calcExpiredDate(this.$refs.select.value),
          private: this.$refs.isPrivate.checked,
        })
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal(
              "发布成功",
              location.protocol +
                "//" +
                location.host +
                "/pastes/" +
                res.detail.id
            );
            this.text = "";
            this.title = "";
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          console.log(err);
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 删除文本
    remove(id) {
      // if (!this.$root.hasToken(() => this.remove(id))) {
      //   return;
      // }
      this.$root.loading = true;
      axios
        .post("/paste/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "删除成功");
            this.list();
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          console.log(err);
          if (err != "cancel") {
            this.$root.showModal("错误", err.message);
          }
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    handleVerify() {
      return new Promise((resolve, reject) => {
        console.log("弹出密码框");
        this.$root.$refs.password.value = "";
        const modal = new Modal(this.$root.$refs.passwordModal);
        modal.show();
        this.$root.$refs.password.focus();

        this.$root.func = () => {
          this.$root.confirmed = true;
          modal.hide();
          this.getToken()
            .then(() => {
              console.log("token验证成功，继续发送请求");
              resolve();
            })
            .catch((error) => {
              console.log("token验证失败，中止请求");
              reject({ message: error });
            });
        };

        var hiddenEvent = () => {
          if (!this.$root.confirmed) {
            console.log("验证框关闭后，不执行请求，直接中止");
            reject("cancel");
          }
          this.$root.confirmed = false;
          this.$root.$refs.passwordModal.removeEventListener(
            "hidden.bs.modal",
            hiddenEvent
          );
        };

        this.$root.$refs.passwordModal.addEventListener(
          "hidden.bs.modal",
          hiddenEvent
        );
      });
    },
    getToken() {
      return new Promise((resolve, reject) => {
        axios
          .post(
            "/token/get",
            Qs.stringify({ password: this.$root.$refs.password.value })
          )
          .then((res) => {
            if (res.success) {
              resolve();
            } else {
              reject(res.msg);
            }
          })
          .catch((err) => {
            reject(err.message);
          });
      });
    },
  },
  created() {
    this.list();
    this.fetchTokenProtectedEndpoints();

    axios.interceptors.request.use(
      async (config) => {
        const isTokenProtected = this.endpoints.includes(config.url);
        console.log(
          "判断当前请求的 API 地址是否在权限接口列表中：" +
            config.url +
            " " +
            isTokenProtected
        );

        if (isTokenProtected) {
          await this.handleVerify();
          console.log("验证通过后，继续发送请求：" + config.url);
          return config;
        }
        console.log("不需要验证的接口直接发送请求：" + config.url);
        return config;
      },
      (error) => {
        console.log(error);
        // 处理请求错误
        return Promise.reject(error);
      }
    );
  },
};
</script>
