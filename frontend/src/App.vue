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
            >文件列表</router-link
          >
        </li>
        <li class="nav-item">
          <router-link
            to="/pastes"
            :class="[
              'nav-link',
              { active: $router.currentRoute.value.meta.title == '文本共享' },
            ]"
            >文本共享</router-link
          >
        </li>
        <li class="nav-item">
          <router-link
            to="/bookmarks"
            :class="[
              'nav-link',
              { active: $router.currentRoute.value.meta.title == '书签列表' },
            ]"
            >书签列表</router-link
          >
        </li>
        <li class="nav-item ms-auto dropdown">
          <a
            class="nav-link dropdown-toggle"
            data-bs-toggle="dropdown"
            href="#"
          ></a>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="/share">文件分享</a></li>
            <li><a class="dropdown-item" href="/upload">拖拽上传</a></li>
            <li><a class="dropdown-item" href="/log">操作日志</a></li>
            <li><a class="dropdown-item" href="/server">服务器信息</a></li>
            <li><a class="dropdown-item" href="/permissions">权限控制</a></li>
          </ul>
        </li>
      </ul>
    </div>

    <div v-if="this.$route.path == '/log'" style="overflow-x: scroll">
      <router-view></router-view>
    </div>
    <router-view v-else></router-view>
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
            关闭
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
          <h5 class="modal-title">删除</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <p>确定要删除吗？</p>
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-outline-danger"
            data-bs-dismiss="modal"
            @click="func()"
          >
            确定
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            关闭
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
          <h5 class="modal-title">验证</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <label for="password" class="form-label">请输入密码</label>
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
            data-bs-dismiss="modal"
            ref="btnAuth"
            @click="getToken()"
          >
            确定
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            关闭
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
            确定
          </button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">
            关闭
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
        <img src="..." class="rounded me-2" alt="..." />
        <strong class="me-auto">{{ message.title }}</strong>
        <small>{{ getCurrentTime() }}</small>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="toast"
          aria-label="Close"
        ></button>
      </div>
      <div class="toast-body">
        {{ message.text }}
      </div>
    </div>
  </div>
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

export default {
  data() {
    return {
      // 消息框的标题和内容
      message: {
        title: "",
        text: "",
      },
      // 旋转器的状态
      loading: false,
      // 回调函数
      func: null,
      // 上传进度
      progress: 0,
      // 传输实时速度
      speed: "",
      // 图片或视频链接
      src: "",
      // 输入框中的默认值
      inputValue: "",
    };
  },
  methods: {
    // 显示消息框
    showModal(title, text) {
      this.message.title = title;
      this.message.text = text;
      new Modal(this.$refs.messageModal).show();
    },
    // 显示确认框
    showConfirm(func) {
      new Modal(this.$refs.confirmModal).show();
      this.func = func;
    },
    // 显示输入框
    showInput(title, text, func) {
      this.func = func;
      this.message.title = title;
      this.message.text = text;
      //this.$refs.input.value = "";
      this.$refs.input.value = this.inputValue;
      new Modal(this.$refs.inputModal).show();
      this.$refs.input.focus();
    },
    // 检查token
    noToken() {
      return !Cookies.get("token");
    },
    // 授权
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
    // 获取token
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
            this.showModal("验证失败", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
    // 格式化文件大小
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
    // 关闭图片预览框后清除图片资源
    modalClose() {
      this.src = "";
    },
    showToast(title, text) {
      this.message.title = title;
      this.message.text = text;
      const toastLiveExample = document.getElementById("liveToast");
      const toastBootstrap = Toast.getOrCreateInstance(toastLiveExample);
      toastBootstrap.show();
    },
    getCurrentTime() {
      return new Date().toLocaleString("zh-CN", {
        hour12: false,
        year: "numeric",
        month: "numeric",
        day: "numeric",
        hour: "numeric",
        minute: "numeric",
        second: "2-digit",
      });
    },
  },
  created() {
    new ClipboardJS("#btnCopy").on("success", () => {
      this.showModal("成功", "复制成功");
    });
  },
  mounted() {
    this.$refs.imageModal.addEventListener("hidden.bs.modal", this.modalClose);
  },
};
</script>
