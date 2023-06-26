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
