import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "./router";

import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

import * as bootstrap from "bootstrap";
window.Modal = bootstrap.Modal;

import axios from "axios";

// 拦截器
axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    return res.data;
  }
});

// 环境切换
if (process.env.NODE_ENV == "development") {
  axios.defaults.baseURL = "http://localhost/";
} else if (process.env.NODE_ENV == "debug") {
  axios.defaults.baseURL = "http://localhost/";
} else if (process.env.NODE_ENV == "production") {
  axios.defaults.baseURL = "";
}

createApp(App).use(router).mount("#app");
