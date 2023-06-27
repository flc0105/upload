<template>
  <div class="card">
    <div class="card-header">{{ $t("file_share") }}</div>
    <div class="card-body text-center">
      <table class="table table-borderless table-sm">
        <tbody>
          <tr>
            <td>{{ $t("filename") }}:&nbsp;</td>
            <td>{{ file.name }}</td>
          </tr>
          <tr>
            <td>{{ $t("file_size") }}:&nbsp;</td>
            <td>{{ $root.formatBytes(file.length) }}</td>
          </tr>
          <tr>
            <td>{{ $t("last_modified") }}:&nbsp;</td>
            <td>{{ file.lastModified }}</td>
          </tr>
          <tr>
            <td>{{ $t("file_type") }}:&nbsp;</td>
            <td>{{ file.fileType }}</td>
          </tr>
        </tbody>
      </table>
      <button
        class="btn btn-sm btn-outline-primary mt-3"
        @click="download(file.relativePath)"
      >
        {{ $t("download") }}
      </button>
      <button
        v-if="
          file &&
          (file.fileType.includes('image') || file.fileType.includes('text'))
        "
        class="btn btn-sm btn-outline-primary mt-3 ms-1"
        @click="preview(file.fileType, file.relativePath)"
      >
        {{ $t("preview") }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.table {
  margin: 0 auto;
  width: auto;
}

.table > tbody > tr > td {
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
import axios from "axios";
import Qs from "qs";

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
          this.$root.showModal(this.$t("error"), err.message);
          this.$router.push("/");
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    download(relativePath) {
      location.href =
        axios.defaults.baseURL +
        "/file/download?relativePath=" +
        encodeURIComponent(relativePath);
    },
    // 预览文件
    preview(fileType, filename) {
      if (fileType.includes("text")) {
        this.$root.loading = true;
        this.$root.message.title = filename;
        this.$root.message.text = "";
        axios
          .post("/file/read", Qs.stringify({ relativePath: filename }))
          .then((res) => {
            if (res.success) {
              this.$root.message.text = res.detail;
              new Modal(this.$root.$refs.textModal).show();
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
      } else if (fileType.includes("image")) {
        this.$root.src = "";
        this.$root.src =
          axios.defaults.baseURL +
          "/file/preview?relativePath=" +
          encodeURIComponent(filename);
        new Modal(this.$root.$refs.imageModal).show();
      }
    },
  },
  created() {
    if (this.$route.params.code) {
      this.get(this.$route.params.code);
    }
  },
};
</script>
