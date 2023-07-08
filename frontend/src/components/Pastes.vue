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
        <option value="10" :selected="$root.isTokenMissing()">
          10 {{ $t("minutes") }}
        </option>
        <option value="60">1 {{ $t("hours") }}</option>
        <option value="1440">1 {{ $t("days") }}</option>
        <option value="10080">1 {{ $t("weeks") }}</option>
        <option value="-1">{{ $t("burn_after_reading") }}</option>
        <option value="0" :selected="!$root.isTokenMissing()">
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
            <span v-else
              >Expired {{ calculateFromNow(paste.expiredDate) }}</span
            >
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
import Qs from "qs";

import {
  calculateFromNow,
  calculateExpiresAt,
  sendRequest,
} from "../utils/utils.js";

export default {
  data() {
    return {
      pastes: [], //获取到的paste列表
      title: "", //标题输入框
      text: "", //正文输入框
    };
  },
  methods: {
    calculateFromNow,

    // 查询所有
    list() {
      sendRequest.call(
        this,
        "get",
        "/paste/list",
        null,
        (res) => {
          this.pastes = res.detail;
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    // 添加
    add() {
      if (this.text.trim().length === 0) {
        this.$root.showModal(this.$t("alert"), this.$t("text_cannot_be_empty"));
        return;
      }
      const data = {
        title: this.title.trim().length === 0 ? "未命名" : this.title,
        text: this.text,
        expiredDate: calculateExpiresAt(this.$refs.select.value),
        private: this.$refs.isPrivate.checked,
      };
      sendRequest.call(
        this,
        "post",
        "/paste/add",
        data,
        (res) => {
          this.list();
          this.$root.showModal(
            this.$t("success"),
            location.protocol +
              "//" +
              location.host +
              "/pastes/" +
              res.detail.id
          );
          this.text = "";
          this.title = "";
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    // 删除
    remove(id) {
      if (!this.$root.hasToken(() => this.remove(id))) {
        return;
      }
      const data = Qs.stringify({ id: id });
      sendRequest.call(
        this,
        "post",
        "/paste/delete",
        data,
        (res) => {
          this.$root.showModal(this.$t("success"), res.msg);
          this.list();
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },
  },
  created() {
    this.list();
  },
};
</script>
