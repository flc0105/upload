<template>
  <input
    type="file"
    name="file"
    id="file"
    ref="file"
    class="d-none"
    @change="(event) => importPastes(event)"
    multiple
  />

  <v-contextmenu ref="contextmenu">
    <v-contextmenu-item @click="getTextFromClipboard()"
      >Quick paste</v-contextmenu-item
    >

    <v-contextmenu-item
      @click="
        $refs.file.value = null;
        $refs.file.click();
      "
      >{{ $t("import") }}</v-contextmenu-item
    >

    <v-contextmenu-item @click="exportPastes()">{{
      $t("export")
    }}</v-contextmenu-item>
  </v-contextmenu>

  <div v-contextmenu:contextmenu>
    <input
      class="form-control mb-2"
      :placeholder="$t('title')"
      v-model="title"
    />
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
  </div>
</template>

<style scoped>
.list-group-item {
  word-break: break-all;
  padding: 1rem 1rem !important;
}
</style>

<script>
import Qs from "qs";
import "file-saver";

import {
  calculateFromNow,
  calculateExpiresAt,
  sendRequest,
} from "../utils/utils.js";
import { directive, Contextmenu, ContextmenuItem } from "v-contextmenu";
import "v-contextmenu/dist/themes/default.css";

import axios from "axios";
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
      pastes: [], //获取到的paste列表
      title: "", //标题输入框
      text: "", //正文输入框
    };
  },
  methods: {
    async getTextFromClipboard() {
      try {
        const text = await navigator.clipboard.readText();
        // 在这里使用获取到的剪贴板文字进行进一步操作
        console.log("剪贴板的文字:", text);
        sendRequest.call(
          this,
          "post",
          "/paste/add",
          {
            title: "From clipboard",
            text: text,
            expiredDate: null,
            private: false,
          },
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
          },
          (err) => {
            this.$root.showModal(this.$t("error"), err);
          }
        );
      } catch (error) {
        this.$root.showModal(this.$t("error"), error.message);
      }
    },

    exportPastes() {
      // this.$root.loading = true;
      // var json = JSON.stringify(this.pastes, null, 2);
      // var blob = new Blob([json], { type: "text/plain;charset=utf-8" });
      // saveAs(blob, "pastes.json");
      // this.$root.loading = false;

      var filteredPastes = this.pastes.map(function (paste) {
        return {
          title: paste.title,
          text: paste.text,
          time: paste.time,
        };
      });

      var json = JSON.stringify(filteredPastes, null, 2);
      var blob = new Blob([json], { type: "text/plain;charset=utf-8" });
      saveAs(blob, "pastes.json");
    },

    importPastes(event) {
      this.$root.loading = true;
      var reader = new FileReader();
      reader.readAsText(event.target.files[0]);
      reader.onload = (e) => {
        var data = e.target.result;
        if (!this.isJSON(data)) {
          this.$root.showModal(
            this.$t("error"),
            this.$t("not_valid_json_data")
          );
          this.$root.loading = false;
          return;
        }
        axios
          .post("paste/import", Qs.stringify({ json: data }))
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
      };
    },

    calculateFromNow,
    isJSON(str) {
      try {
        return JSON.parse(str) && !!str;
      } catch (e) {
        return false;
      }
    },

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
  mounted() {
    window.onbeforeunload = (e) => {
      if (this.text.trim().length != 0) {
        if (e) {
          e.returnValue = "关闭提示";
        }
        return "关闭提示";
      }
    };
  },
  beforeRouteLeave(to, from, next) {
    if (this.text.trim().length != 0) {
      if (!window.confirm("Leave without saving?")) {
        return;
      }
    }
    next();
  },
};
</script>
