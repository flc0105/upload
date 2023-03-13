<template>
  <div class="container">
    <div class="header">
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link to="/files"
            :class="['nav-link', { active: ($router.currentRoute.value.path.startsWith('/file')) }]">文件列表</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/pastes"
            :class="['nav-link', { active: ($router.currentRoute.value.path.startsWith('/paste')) }]">文本共享</router-link>
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
          <button class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p style="white-space: pre-wrap">{{ message.text }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">关闭</button>
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
          <button class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p>确定要删除吗？</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline-danger" data-bs-dismiss="modal" @click="func()">确定</button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">关闭</button>
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
          <button class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <label for="password" class="form-label">请输入密码</label>
          <input type="password" class="form-control" ref="password" @keyup.enter="$refs.btnAuth.click()" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline-primary" data-bs-dismiss="modal" ref="btnAuth" @click="getToken()">确定</button>
          <button class="btn btn-outline-primary" data-bs-dismiss="modal">关闭</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 旋转器 -->
  <div v-show="loading" class="spinner-border text-primary" role="status">
    <span class="visually-hidden">Loading...</span>
  </div>
</template>

<script>

import axios from 'axios'
import Qs from 'qs'
import Cookies from 'js-cookie'
import ClipboardJS from 'clipboard'

export default {
  data() {
    return {
      // 消息框的标题和内容
      message: {
        title: '',
        text: '',
      },
      // 旋转器的状态
      loading: false,
      // 回调函数
      func: null,
    }
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
    // 检查token
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
        .post("token/get",
          Qs.stringify({ password: this.$refs.password.value })
        )
        .then((res) => {
          if (res.success) {
            this.func() // 如果通过验证，继续之前的操作
          } else {
            this.showModal("验证失败", res.msg);
          }
        })
        .catch((err) => {
          this.showModal("错误", err.message);
        });
    },
  },
  created() {
    new ClipboardJS("#btnCopy").on("success", () => {
      this.showModal("成功", "复制成功");
    });
  }
}
</script>