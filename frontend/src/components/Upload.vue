<template>
  <div
    :class="['wrapper', { active: active }]"
    v-cloak
    @drop.prevent="addFile"
    @dragenter.prevent="active = true"
    @dragover.prevent="active = true"
    @dragleave.prevent="active = false"
  >
    <input
      type="file"
      name="file"
      id="file"
      class="d-none"
      @change="(event) => addFromClick(event)"
      multiple
    />

    <div
      class="drag cursor-pointer"
      v-show="files.length == 0"
      onclick="document.getElementById('file').value = null; document.getElementById('file').click()"
    >
      <p class="title">未选择文件/文件夹</p>
      <p class="subtile">支持拖拽到此区域上传，支持选择多个文件/文件夹</p>
    </div>
    <div v-show="files.length != 0">
      <div class="table-wrapper">
        <table class="table table-sm">
          <thead>
            <tr>
              <th>#</th>
              <th>文件名</th>
              <th>大小</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(file, id) in files" :key="id">
              <td>{{ id + 1 }}</td>
              <td>
                <i>{{
                  file.webkitRelativePath ? file.webkitRelativePath : file.name
                }}</i>
              </td>
              <td>{{ $root.formatBytes(file.size) }}</td>
              <td>
                <a class="link-danger" @click="removeFile(file)"
                  ><i class="bi bi-trash"></i
                ></a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="text-center">
        <button class="btn btn-sm btn-outline-primary mt-2" @click="upload">
          上传文件
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrapper {
  height: 250px;
  border: 1px dashed #dedede;
  border-radius: 4px;
}

.table-wrapper {
  overflow: scroll;
  height: 200px;
}

.drag {
  height: 100%;
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
}

thead tr:nth-child(1) th {
  background: white;
  position: sticky;
  top: 0;
  z-index: 10;
}

.active {
  border: 1px dashed #2260ff;
}

.title {
  font-size: 14px;
}

.subtile {
  font-size: 12px;
  color: #999999;
  margin-top: 30px;
  text-align: center;
  line-height: unset;
}
</style>

<script>
import axios from "axios";
import "bootstrap/dist/js/bootstrap.bundle";

export default {
  data() {
    return {
      files: [],
      active: false,
    };
  },
  methods: {
    addFile(e) {
      this.active = false;
      // let files = e.dataTransfer.files;
      // [...files].forEach(file => {
      //   this.files.push(file);
      // });
      let files = e.dataTransfer.items;
      [...files].forEach((file) => {
        var entry = file.webkitGetAsEntry();
        if (entry) {
          this.traverseFileTree(entry);
        }
      });
    },
    addFromClick(e) {
      let files = Array.from(e.target.files);
      files.forEach((file) => {
        this.files.push(file);
      });
    },
    traverseFileTree(item, path) {
      path = path || "";
      if (item.isFile) {
        item.file((file) => {
          this.files.push(file);
        });
      } else if (item.isDirectory) {
        var dirReader = item.createReader();
        dirReader.readEntries((entries) => {
          for (var i = 0; i < entries.length; i++) {
            this.traverseFileTree(entries[i], path + item.name + "/");
          }
        });
      }
    },
    removeFile(file) {
      this.files = this.files.filter((f) => {
        return f != file;
      });
    },
    upload() {
      this.$root.message.title = "上传进度";
      const modal = new Modal(this.$root.$refs.progressModal);
      modal.show();
      const formData = new FormData();
      formData.append("currentDirectory", "/public/");
      this.files.forEach((file) => {
        formData.append("files", file);
      });
      var lastTime = new Date().getTime(); // 上次传输时的时间
      var lastBytes = 0; // 上次传输量
      axios({
        method: "post",
        url: "file/upload",
        data: formData,
        headers: {
          "Content-Type": "multipart/form-data",
        },
        onUploadProgress: (e) => {
          const current = e.loaded;
          const total = e.total;
          this.$root.progress = Math.round((current / total) * 100) + "%";
          var now = new Date().getTime(); //当前时间
          var amount_completed = current - lastBytes; // 从上次到这次的传输量
          var time_taken = (now - lastTime) / 1000; // 从上次到这次的传输所用秒数
          var speed = time_taken ? amount_completed / time_taken : 0;
          lastBytes = current;
          lastTime = now;
          this.$root.speed = this.$root.formatBytes(speed) + "/s";
        },
      })
        .then((res) => {
          if (res.success) {
            modal.hide();
            this.files = [];
            this.$root.showModal("成功", "上传成功");
            this.$root.progress = 0;
          } else {
            modal.hide();
            this.$root.showModal("失败", res.msg);
            this.$root.progress = 0;
          }
        })
        .catch((err) => {
          modal.hide();
          this.$root.showModal("错误", err.message);
          this.$root.progress = 0;
        });
    },
  },
};
</script>
