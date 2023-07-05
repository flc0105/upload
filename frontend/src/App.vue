<template>
  <div class="container">
    <div class="header">
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link
            to="/files"
            :class="[
              'nav-link',
              { active: $router.currentRoute.value.meta.title == '文件列表' },
            ]"
            >{{ $t("files") }}</router-link
          >
        </li>
        <li class="nav-item">
          <router-link
            to="/pastes"
            :class="[
              'nav-link',
              { active: $router.currentRoute.value.meta.title == '文本共享' },
            ]"
            >{{ $t("pastes") }}</router-link
          >
        </li>
        <li class="nav-item">
          <router-link
            to="/bookmark"
            :class="[
              'nav-link',
              { active: $router.currentRoute.value.meta.title == '书签列表' },
            ]"
            >{{ $t("bookmarks") }}</router-link
          >
        </li>
        <li class="nav-item ms-auto dropdown">
          <a
            class="nav-link dropdown-toggle"
            data-bs-toggle="dropdown"
            href="#"
          ></a>
          <ul class="dropdown-menu">
            <li>
              <a class="dropdown-item" href="/upload">{{
                $t("drag_and_drop")
              }}</a>
            </li>
            <li>
              <a class="dropdown-item" href="/share">{{ $t("file_share") }}</a>
            </li>
            <li>
              <a class="dropdown-item" href="/log">{{
                $t("operation_logs")
              }}</a>
            </li>
            <li>
              <a class="dropdown-item" href="/permissions">{{
                $t("permission_control")
              }}</a>
            </li>
            <li>
              <a class="dropdown-item" href="/server">{{
                $t("server_info")
              }}</a>
            </li>
            <li>
              <a class="dropdown-item" @click="switchLanguage()">{{
                $t("change_language")
              }}</a>
            </li>
          </ul>
        </li>
      </ul>
    </div>
    <router-view></router-view>
  </div>

  <!-- 消息框 -->
  <div class="modal" tabindex="-1" ref="messageModal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ message.title }}</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p style="white-space: pre-wrap; word-break: break-all">
            {{ message.text }}
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            {{ $t("close") }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 确认框 -->
  <div class="modal" tabindex="-1" ref="confirmModal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ $t("delete") }}</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p>{{ $t("confirm_delete") }}</p>
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-outline-danger"
            data-bs-dismiss="modal"
            @click="func()"
          >
            {{ $t("ok") }}
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            {{ $t("close") }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 密码框 -->
  <div class="modal" tabindex="-1" ref="passwordModal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ $t("verification") }}</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <label for="password" class="form-label">{{
            $t("please_enter_the_password")
          }}</label>
          <input
            type="password"
            class="form-control"
            ref="password"
            @keyup.enter="$refs.btnAuth.click()"
          />
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-outline-primary"
            ref="btnAuth"
            @click="getToken()"
            data-bs-dismiss="modal"
          >
            {{ $t("ok") }}
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            {{ $t("close") }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 进度框 -->
  <div
    class="modal"
    tabindex="-1"
    ref="progressModal"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ message.title }}</h5>
        </div>
        <div class="modal-body">
          <div class="progress">
            <div class="progress-bar" :style="{ width: progress }">
              {{ progress }} ({{ speed }})
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 输入框 -->
  <div class="modal" tabindex="-1" ref="inputModal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ message.title }}</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <label for="name" class="form-label">{{ message.text }}</label>
          <input
            class="form-control"
            id="name"
            ref="input"
            @keyup.enter="$refs.btnInput.click()"
          />
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-outline-primary"
            data-bs-dismiss="modal"
            @click="func()"
            ref="btnInput"
          >
            {{ $t("ok") }}
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            {{ $t("close") }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 旋转器 -->
  <div v-show="loading" class="spinner-border text-primary" role="status">
    <span class="visually-hidden">Loading...</span>
  </div>

  <!-- 文本预览框 -->
  <div class="modal" ref="textModal">
    <div class="modal-dialog modal-dialog-scrollable">
      <div class="modal-content" style="min-height: 100% !important">
        <div class="modal-header border-0" style="font-size: 0.875rem">
          <i>{{ message.title }}</i>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <highlightjs
          autodetect
          :code="message.text"
          class="mh-100 pe-2 ps-2"
        ></highlightjs>
      </div>
    </div>
  </div>

  <!-- 图片预览框 -->
  <div class="modal" ref="imageModal">
    <div class="modal-dialog text-center mw-100 h-100">
      <img
        id="image"
        :src="src"
        class="shadow"
        onclick="document.getElementById('image').classList.toggle('mh-100')"
      />
    </div>
  </div>

  <!-- Toast -->
  <div class="toast-container position-fixed top-0 end-0 p-3">
    <div
      id="liveToast"
      class="toast"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
    >
      <div class="toast-header">
        <!-- <img src="..." class="rounded me-2" alt="..." /> -->
        <strong class="me-auto">{{ message.title }}</strong>
        <small>{{ getCurrentTime() }}</small>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="toast"
          aria-label="Close"
        ></button>
      </div>
      <div
        class="toast-body"
        style="white-space: pre-wrap; word-break: break-all"
      >
        {{ message.text }}
      </div>
    </div>
  </div>

  <Command ref="commandModal" />
</template>

<style scoped>
pre,
code,
highlightjs,
.hljs {
  white-space: pre-wrap !important;
  word-wrap: break-word !important;
}
</style>

<script>
import axios from "axios";
import Qs from "qs";
import Cookies from "js-cookie";
import ClipboardJS from "clipboard";
import { getCurrentTime } from "./utils/utils.js";

// import watermark from "./utils/watermark.js";

import Command from "./components/Command.vue";

export default {
  components: {
    // watermark,
    Command,
  },
  data() {
    return {
      /**
       * 消息框的标题和内容
       * @type {{title:String, text:String}}
       */
      message: {
        title: "",
        text: "",
      },

      /**
       * 旋转器的状态
       * @type {boolean}
       */
      loading: false,

      /**
       * 回调函数
       * @type {Function|null}
       */
      func: null,

      /**
       * 上传进度
       * @type {number}
       */
      progress: 0,

      /**
       * 传输实时速度
       * @type {string}
       */
      speed: "",

      /**
       * 图片链接
       * @type {string}
       */
      src: "",

      /**
       * 输入框中的默认值
       * @type {string}
       */
      inputValue: "",
    };
  },
  methods: {
    getCurrentTime,
    /**
     * 显示模态框
     * @param {string} title 模态框的标题
     * @param {string} text 模态框的内容
     */
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new Modal(this.$refs.messageModal).show();
    },

    /**
     * 显示确认模态框
     * @param {Function} func 确认按钮点击后执行的函数
     */
    showConfirm(func) {
      new Modal(this.$refs.confirmModal).show();
      this.func = func;
    },

    /**
     * 显示带有输入框的模态框
     * @param {string} title 模态框标题
     * @param {string} text 模态框内容
     * @param {Function} func 确认按钮点击后执行的函数
     */
    showInput(title, text, func) {
      this.func = func;
      this.message.title = title;
      this.message.text = text;
      this.$refs.input.value = this.inputValue;
      new Modal(this.$refs.inputModal).show();
      this.$refs.input.focus();
    },

    /**
     * 检查是否缺少 "token" cookie
     * @returns {boolean} 返回是否缺少 "token" cookie
     */
    isTokenMissing() {
      return !Cookies.get("token");
    },

    /**
     * 检查是否存在令牌
     * 如果不存在令牌，则显示密码框，以进行验证
     * @param {Function} func 需要进行的操作
     * @returns {boolean} 如果存在令牌，则返回 true；否则返回 false
     */
    hasToken(func) {
      if (!Cookies.get("token")) {
        this.func = func; // 保存当前进行的操作
        this.$refs.password.value = "";
        new Modal(this.$refs.passwordModal).show(); // 弹出密码框
        this.$refs.password.focus();
        return false;
      }
      return true;
    },

    /**
     * 获取令牌
     */
    getToken() {
      axios
        .post(
          "/token/get",
          Qs.stringify({ password: this.$refs.password.value })
        )
        .then((res) => {
          if (res.success) {
            this.func(); // 如果通过验证，继续之前的操作
          } else {
            this.showModal(this.$t("error"), res.msg);
          }
        })
        .catch((err) => {
          this.showModal(this.$t("error"), err.message);
        });
    },

    /**
     * 格式化字节大小
     * @param {number} bytes 字节数
     * @returns {string} 格式化后的字节大小
     */
    formatBytes(bytes) {
      if (bytes === 0) {
        return "0 B";
      }
      const i = Math.floor(Math.log(bytes) / Math.log(1024));
      return (
        parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) +
        " " +
        ["B", "kB", "MB", "GB"][i]
      );
    },

    /**
     * 显示 Toast 提示
     * @param {string} title 标题
     * @param {string} text 文本内容
     */
    showToast(title, text) {
      this.message.title = title;
      this.message.text = text;
      const toast = document.getElementById("liveToast");
      const toastBootstrap = Toast.getOrCreateInstance(toast);
      toastBootstrap.show();
    },

    /**
     * 处理按下组合键的事件
     * 如果按下了 Ctrl + Alt + C 键，则显示命令模态框
     * @param {KeyboardEvent} event 键盘事件对象
     */
    handleShortcutKeyPress(event) {
      if (event.ctrlKey && event.altKey && event.key === "c") {
        this.$refs.commandModal.show();
      }
    },

    /**
     * 执行命令
     * 发送命令请求，并在请求完成后更新执行结果
     */
    executeCommand() {
      this.$refs.command.disabled = true;
      this.$refs.btnExecute.disabled = true;
      this.$refs.btnExecute.innerHTML =
        "<span class='spinner-grow spinner-grow-sm' role='status' aria-hidden='true'></span>";
      axios
        .post("/shell", Qs.stringify({ command: this.$refs.command.value }))
        .then((res) => {
          if (res.success) {
            this.$refs.executionResult.value = res.detail;
          } else {
            this.$refs.executionResult.value = res.msg;
          }
        })
        .catch((err) => {
          this.$refs.executionResult.value = err.message;
        })
        .finally(() => {
          this.$refs.btnExecute.innerHTML = this.$t("execute");
          this.$refs.command.disabled = false;
          this.$refs.btnExecute.disabled = false;
          this.$refs.command.focus();
          this.$refs.command.select();
        });
    },
    switchLanguage() {
      const currentLanguage = this.$i18n.locale;
      const newLanguage = currentLanguage.startsWith("en") ? "zh" : "en";

      // 更新语言选项
      this.$i18n.locale = newLanguage;

      // 存储语言选项到 localStorage
      localStorage.setItem("language", newLanguage);

      window.location.reload();
    },
  },
  created() {
    new ClipboardJS("#btnCopy").on("success", () => {
      this.showModal(this.$t("success"), this.$t("copy_success"));
    });
  },
  mounted() {
    // 从 localStorage 中获取语言选项
    const storedLanguage = localStorage.getItem("language");

    // 如果存在存储的语言选项，则使用它来设置当前语言
    if (storedLanguage) {
      this.$i18n.locale = storedLanguage;
    }

    const language = this.$i18n.locale;

    axios.interceptors.request.use((config) => {
      config.headers["Accept-Language"] = language;
      return config;
    });

    // watermark.set("https://github.com/flc0105/upload");

    // 监听图片预览框的隐藏事件，并清除图片资源
    this.$refs.imageModal.addEventListener("hidden.bs.modal", () => {
      this.src = "";
    });
    // 注册快捷键事件的监听器
    window.addEventListener("keydown", this.handleShortcutKeyPress);
  },
  beforeDestroy() {
    //取消快捷键事件的监听器
    window.removeEventListener("keydown", this.handleShortcutKeyPress);
    // watermark.set("");
  },
};
</script>
