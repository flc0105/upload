import moment from "moment";
import axios from "axios";

export function greet() {
  console.log("Hello!");
}

// 日期相对格式化
export function calculateFromNow(date) {
  return moment(date).fromNow();
}

// 根据分钟数计算过期时间
export function calculateExpiresAt(minutes) {
  if (minutes == 0) {
    return undefined;
  }
  if (minutes == -1) {
    return -1;
  }
  var date = moment(new Date()).add(minutes, "m").toDate();
  return moment(date).format("YYYY-MM-DD HH:mm:ss");
}

export function removeOuterQuotes(str) {
  if (str.startsWith('"') && str.endsWith('"')) {
    return str.slice(1, -1);
  }
  return str;
}

// 公共方法：发送请求
export function sendRequest(method, url, data, successCallback, errorCallback) {
  let requestConfig = {
    method,
    url,
    data,
  };
  if (method.toLowerCase() === "get") {
    requestConfig.params = data;
  }
  this.$root.loading = true;
  axios(requestConfig)
    .then((res) => {
      if (res.success) {
        successCallback(res);
      } else {
        console.log(res);
        errorCallback(res.msg);
      }
    })
    .catch((err) => {
      errorCallback(err.message);
    })
    .finally(() => {
      this.$root.loading = false;
    });
}

//import heic2any from "heic2any";
let heic2any = null; // 用于存储模块的变量

// 在需要的地方调用该方法，实现按需加载
export function loadHeic2Any() {
  if (heic2any) {
    // 如果模块已经加载过，直接返回Promise.resolve
    return Promise.resolve(heic2any);
  }
  // 如果模块还没有加载，使用动态导入加载它
  return import("heic2any")
    .then((module) => {
      // 保存模块到变量
      heic2any = module.default || module;
      return heic2any;
    })
    .catch((error) => {
      // 加载失败的处理
      console.error("Failed to load heic2any module.", error);
      throw error;
    });
}

/**
 * 获取当前时间，格式为年月日时分秒
 * @returns {string} 当前时间的字符串表示
 */
export function getCurrentTime() {
  return new Date().toLocaleString("zh-CN", {
    hour12: false,
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "numeric",
    second: "numeric",
  });
}
