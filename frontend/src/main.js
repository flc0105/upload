import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "./router";

import "bootstrap/dist/js/bootstrap.bundle.min.js";

import "bootstrap/dist/js/bootstrap.bundle";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

import * as bootstrap from "bootstrap";
window.Modal = bootstrap.Modal;

window.Toast = bootstrap.Toast;

import axios from "axios";

// 拦截器
axios.interceptors.response.use((res) => {
  if (res.status === 200) {
    if (res.data instanceof Blob) {
      return res;
    }
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

// 每个请求默认携带Cookie信息
axios.defaults.withCredentials = true;

import "highlight.js/styles/github.css";
import "highlight.js/lib/common";
import hljsVuePlugin from "@highlightjs/vue-plugin";

import hljs from "highlight.js/lib/core";

import xml from "highlight.js/lib/languages/xml";
import bash from "highlight.js/lib/languages/bash";
import c from "highlight.js/lib/languages/c";
import cpp from "highlight.js/lib/languages/cpp";
import csharp from "highlight.js/lib/languages/csharp";
import css from "highlight.js/lib/languages/css";
import markdown from "highlight.js/lib/languages/markdown";
import java from "highlight.js/lib/languages/java";
import javascript from "highlight.js/lib/languages/javascript";
import json from "highlight.js/lib/languages/json";
import python from "highlight.js/lib/languages/python";
import shell from "highlight.js/lib/languages/shell";
import sql from "highlight.js/lib/languages/sql";
import yaml from "highlight.js/lib/languages/yaml";

hljs.registerLanguage("xml", xml);
hljs.registerLanguage("bash", bash);
hljs.registerLanguage("c", c);
hljs.registerLanguage("cpp", cpp);
hljs.registerLanguage("csharp", csharp);
hljs.registerLanguage("css", css);
hljs.registerLanguage("markdown", markdown);
hljs.registerLanguage("java", java);
hljs.registerLanguage("javascript", javascript);
hljs.registerLanguage("json", json);
hljs.registerLanguage("python", python);
hljs.registerLanguage("shell", shell);
hljs.registerLanguage("sql", sql);
hljs.registerLanguage("yaml", yaml);


console.log(` %c upload %c https://github.com/flc0105/upload `, "background: #35495e; padding: 1px; border-radius: 3px 0 0 3px; color: #fff", "background: #fadfa3; padding: 1px; border-radius: 0 3px 3px 0; color: #fff");


import { createI18n } from 'vue-i18n';

const browserLocale = navigator.language; // 获取浏览器默认语言设置
import zh from './locales/zh';
import en from './locales/en';

const i18n = createI18n({
  // locale: 'zh', // 默认语言设置为中文
  locale: browserLocale,
  fallbackLocale: 'en',
  messages: {
    zh,
    en,
  },
});




createApp(App).use(router).use(hljsVuePlugin).use(i18n).mount("#app");
