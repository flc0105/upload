<template>
  <div class="card">
    <div class="card-header">文件分享</div>
    <div class="card-body text-center">
      <table class="table table-borderless table-sm">
        <tbody>
          <tr>
            <td>文件名：</td>
            <td>{{ file.name }}</td>
          </tr>
          <tr>
            <td>文件大小：</td>
            <td>{{ $root.formatBytes(file.length) }}</td>
          </tr>
          <tr>
            <td>修改时间：</td>
            <td>{{ file.lastModified }}</td>
          </tr>
          <tr>
            <td>文件类型：</td>
            <td>{{ file.fileType }}</td>
          </tr>
        </tbody>
      </table>
      <button class="btn btn-sm btn-outline-primary mt-3" @click="download(file.relativePath)">
        下载
      </button>
    </div>
  </div>
</template>

<style scoped>
.table {
  margin: 0 auto;
  width: auto;
}

.table>tbody>tr>td {
  vertical-align: top;
}

tr :first-child {
  text-align: right;
}

tr :not(:first-child) {
  text-align: left;
  width: auto;
  font-style: italic;
  max-width: 200px;
  word-break: break-all;
}
</style>

<script>
import axios from 'axios'
import Qs from 'qs'

export default {
  data() {
    return {
      file: "",
    };
  },
  methods: {
    // 获取文件
    get(code) {
      this.$root.loading = true;
      axios
        .post("/shareCode/get", Qs.stringify({ code: code }))
        .then((res) => {
          if (res.success) {
            this.file = res.detail;
          } else {
            this.$router.push("/");
          }
        })
        .catch((err) => {
          this.$root.showModal('错误', err.message)
          this.$router.push("/");
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    download(relativePath) {
      location.href = axios.defaults.baseURL + "/file/download?relativePath=" + encodeURIComponent(relativePath);
    },
  },
  created() {
    if (this.$route.params.code) {
      this.get(this.$route.params.code);
    }
  },
};
</script>
