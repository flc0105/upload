<template>
  <div class="card border-bottom-0">
    <div class="card-header">
      <ol class="breadcrumb float-start" style="margin: 0.5rem 0">
        <li
          class="filename text-truncate"
          v-for="directory in directories"
          :key="directory"
          :class="[
            'breadcrumb-item',
            { active: directory.relativePath == currentDirectory },
          ]"
        >
          <a v-if="directory.relativePath == currentDirectory">{{
            directory.displayName
          }}</a>
          <a
            v-else
            class="link-primary"
            @click="changeDirectory(directory.relativePath)"
            >{{ directory.displayName }}</a
          >
        </li>
      </ol>
    </div>
    <div class="card-body">
      <form
        id="form"
        action="upload"
        method="post"
        enctype="multipart/form-data"
      >
        <input
          type="file"
          name="file"
          id="file"
          class="d-none"
          @change="(event) => upload(event)"
          multiple
        />
        <input
          type="file"
          name="folder"
          id="folder"
          class="d-none"
          @change="(event) => upload(event)"
          multiple
          webkitdirectory
        />
      </form>
      <div class="files-left">
        <button
          class="btn btn-outline-primary"
          onclick="document.getElementById('file').value=null; document.getElementById('file').click()"
        >
          上传文件
        </button>
        <button
          class="btn btn-outline-primary ms-1 me-1"
          onclick="document.getElementById('folder').value=null; document.getElementById('folder').click()"
        >
          上传文件夹
        </button>
        <button
          class="btn btn-outline-primary me-1"
          @click="
            $root.inputValue = '';
            $root.showInput('新建文件夹', '输入文件夹名', createDirectory);
          "
        >
          新建文件夹
        </button>
        <button class="btn btn-outline-primary me-1" @click="list()">
          刷新
        </button>
        <button
          v-if="
            cutFiles.length !== 0 &&
            getParentDirectory(cutFiles[0]) !== currentDirectory &&
            !currentDirectory.startsWith(cutFiles[0])
          "
          class="btn btn-outline-primary me-1"
          @click="paste()"
        >
          粘贴
        </button>
        <div class="btn-group">
          <button
            class="btn btn-outline-primary"
            :disabled="files.length === 0"
            @click="
              multiSelect = !multiSelect;
              checkedFiles = [];
            "
          >
            多选
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="zipAndDownload(this.checkedFiles)"
          >
            下载
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="
              $root.showConfirm(function () {
                deleteFile(checkedFiles);
              })
            "
          >
            删除
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="cut(checkedFiles)"
          >
            剪切
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="zip(checkedFiles)"
          >
            打包
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="clientZip()"
          >
            客户端打包
          </button>
        </div>
      </div>
      <div class="files-right">
        <div class="w-100 d-flex">
          <input
            class="form-control"
            style="flex: 1 1 auto; width: 1%"
            placeholder="搜索"
            v-model="filter"
            @keyup.enter="search()"
          />
          <div class="dropdown" style="padding-left: 5px">
            <button
              class="btn btn-outline-primary dropdown-toggle"
              data-bs-toggle="dropdown"
              data-bs-auto-close="outside"
            >
              <i class="bi bi-grid-3x3-gap-fill"></i>
            </button>
            <ul class="dropdown-menu">
              <li class="dropdown-item">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="size"
                    :checked="columns.includes('size')"
                    @click="hideColumn('size')"
                  />
                  <label class="form-check-label" for="size">大小</label>
                </div>
              </li>
              <li class="dropdown-item">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="contentType"
                    :checked="columns.includes('contentType')"
                    @click="hideColumn('contentType')"
                  />
                  <label class="form-check-label" for="contentType">类型</label>
                </div>
              </li>
              <li class="dropdown-item">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="lastModified"
                    :checked="columns.includes('lastModified')"
                    @click="hideColumn('lastModified')"
                  />
                  <label class="form-check-label" for="lastModified"
                    >修改日期</label
                  >
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <table class="mb-0 table table-hover">
      <tbody>
        <tr style="user-select: none">
          <th
            class="checkbox"
            v-if="multiSelect"
            @click="
              (event) =>
                event.target === event.currentTarget &&
                event.target.querySelector('.form-check-input').click()
            "
          >
            <input
              type="checkbox"
              class="form-check-input"
              @click="(event) => checkAll(event)"
              :checked="
                Object.keys(files).length > 0 &&
                checkedFiles.length > 0 &&
                files.folders.length + files.files.length > 0
              "
              :indeterminate="
                Object.keys(files).length > 0 &&
                checkedFiles.length > 0 &&
                checkedFiles.length < files.folders.length + files.files.length
              "
              :disabled="
                Object.keys(files).length > 0 &&
                files.folders.length + files.files.length == 0
              "
            />
          </th>
          <th
            class="filename text-truncate cursor-pointer"
            @click="sortBy('name')"
          >
            文件名
            <div class="sort-by-filename">
              <i
                v-if="sort.key === 'name' && sort.direction === 'asc'"
                class="bi bi-sort-alpha-up text-muted"
              ></i>
              <i
                v-if="sort.key === 'name' && sort.direction === 'desc'"
                class="bi bi-sort-alpha-down-alt text-muted"
              ></i>
            </div>
          </th>
          <th class="size" v-if="columns.includes('size')">大小</th>
          <th class="contentType" v-if="columns.includes('contentType')">
            类型
          </th>
          <th
            class="lastModified cursor-pointer"
            v-if="columns.includes('lastModified')"
            @click="sortBy('time')"
          >
            修改时间
            <div class="sort-by-time">
              <i
                v-if="sort.key === 'time' && sort.direction === 'asc'"
                class="bi bi-sort-numeric-up text-muted"
              ></i>
              <i
                v-if="sort.key === 'time' && sort.direction === 'desc'"
                class="bi bi-sort-numeric-down-alt text-muted"
              ></i>
            </div>
          </th>
          <th class="action">操作</th>
        </tr>
        <tr v-if="currentDirectory != '/'">
          <td
            colspan="6"
            @click="changeDirectory(getParentDirectory(currentDirectory))"
          >
            <a class="link-primary">..</a>
          </td>
        </tr>
        <tr v-for="folder in files.folders" :key="folder">
          <td
            class="checkbox"
            v-show="multiSelect"
            @click="
              (event) =>
                event.target === event.currentTarget &&
                event.target.querySelector('.form-check-input').click()
            "
          >
            <input
              type="checkbox"
              class="form-check-input"
              :value="folder.relativePath"
              v-model="checkedFiles"
            />
          </td>
          <td
            class="filename text-truncate"
            @click="changeDirectory(folder.relativePath)"
          >
            <i class="bi bi-folder2"></i>&nbsp;
            <a class="link-primary">{{ folder.name }}</a>

            <!-- <span
              v-if="folder.relativePath == '/images'"
              class="badge text-bg-primary ms-2"
              >直链图片文件夹</span
            >
            <span
              v-if="folder.relativePath == '/public'"
              class="badge text-bg-primary ms-2"
              >公共文件夹</span
            >
            <span
              v-if="folder.relativePath == '/private'"
              class="badge text-bg-primary ms-2"
              >私密文件夹</span
            > -->
          </td>
          <td class="size" v-if="columns.includes('size')">-</td>
          <td class="contentType" v-if="columns.includes('contentType')">-</td>
          <td class="lastModified" v-if="columns.includes('lastModified')">
            {{ folder.lastModified }}
          </td>
          <td class="action">
            <a
              class="link-primary"
              @click="zipAndDownload([folder.relativePath])"
              ><i class="bi bi-cloud-download"></i
            ></a>
            <a
              class="link-danger ms-1"
              @click="
                $root.showConfirm(function () {
                  deleteFile([folder.relativePath]);
                })
              "
              ><i class="bi bi-trash"></i
            ></a>
            <div class="dropdown d-inline">
              <a
                class="link-primary ms-1"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-three-dots"></i>
              </a>
              <ul class="dropdown-menu">
                <li>
                  <a class="dropdown-item" @click="zip([folder.relativePath])"
                    >压缩</a
                  >
                </li>
                <li>
                  <a class="dropdown-item" @click="cut([folder.relativePath])"
                    >剪切</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.inputValue = folder.name;
                      $root.showInput('重命名', '输入新文件夹名', function () {
                        rename(folder.relativePath);
                      });
                    "
                    >重命名</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="directoryStats(folder.relativePath)"
                    >详细信息</a
                  >
                </li>
              </ul>
            </div>
          </td>
        </tr>
        <tr v-for="file in files.files" :key="file">
          <td
            class="checkbox"
            v-show="multiSelect"
            @click="
              (event) =>
                event.target === event.currentTarget &&
                event.target.querySelector('.form-check-input').click()
            "
          >
            <input
              type="checkbox"
              class="form-check-input"
              :value="file.relativePath"
              v-model="checkedFiles"
            />
          </td>
          <td class="filename text-truncate">
            <i class="bi" :class="getIcon(file.name, file.fileType)"></i>&nbsp;
            {{ file.name }}
          </td>
          <td class="size" v-if="columns.includes('size')">
            {{ $root.formatBytes(file.length) }}
          </td>
          <td class="contentType" v-if="columns.includes('contentType')">
            {{ file.fileType }}
          </td>
          <td class="lastModified" v-if="columns.includes('lastModified')">
            {{ file.lastModified }}
          </td>
          <td class="action">
            <a class="link-primary" @click="downloadFile(file.relativePath)"
              ><i class="bi bi-cloud-download"></i
            ></a>
            <a
              class="link-danger ms-1"
              @click="
                $root.showConfirm(function () {
                  deleteFile([file.relativePath]);
                })
              "
              ><i class="bi bi-trash"></i
            ></a>
            <div class="dropdown d-inline">
              <a
                class="link-primary ms-1"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-three-dots"></i>
              </a>
              <ul class="dropdown-menu">
                <li>
                  <a
                    class="dropdown-item"
                    v-if="
                      file.fileType.includes('image') ||
                      file.fileType.includes('text')
                    "
                    @click="preview(file.fileType, file.relativePath)"
                    >预览</a
                  >
                </li>

                <li>
                  <a
                    class="dropdown-item"
                    v-if="file.name.endsWith('.heic')"
                    @click="previewHeic(file.relativePath)"
                    >预览HEIC</a
                  >
                </li>

                <li>
                  <a class="dropdown-item" @click="share(file.relativePath)"
                    >分享</a
                  >
                </li>
                <li>
                  <a class="dropdown-item" @click="cut([file.relativePath])"
                    >剪切</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.inputValue = file.name;
                      $root.showInput('重命名', '输入新文件名', function () {
                        rename(file.relativePath);
                      });
                    "
                    >重命名</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="directoryStats(file.relativePath)"
                    >详细信息</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    v-if="file.fileType.includes('image')"
                    @click="generateDirectLink(file.relativePath)"
                    >生成图片直链</a
                  >
                </li>
              </ul>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.sort-by-filename,
.sort-by-time {
  display: none;
}

.filename:hover .sort-by-filename {
  display: inline;
}

.lastModified:hover .sort-by-time {
  display: inline;
}

.btn-group > .btn-group:not(:first-child),
.btn-group > :not(.btn-check:first-child) + .btn {
  margin-left: -0.9px !important;
}

.table > tbody > tr > td {
  vertical-align: middle;
}

.files-left {
  float: left;
}

.files-right {
  float: right;
}

.checkbox {
  width: 2%;
}

.filename {
  max-width: 200px;
}

@media screen and (max-width: 768px) {
  .size,
  .contentType,
  .lastModified {
    display: none;
  }

  .action {
    width: 80px;
  }
}

@media screen and (max-width: 1000px) {
  .files-left,
  .files-right {
    float: none;
  }

  .files-left .btn {
    margin-bottom: 5px;
  }

  .dropdown {
    display: none;
  }
}
</style>

<script>
import axios from "axios";
import Qs from "qs";

import "file-saver";
import "bootstrap/dist/js/bootstrap.bundle";

//import heic2any from "heic2any"; //TODO: 按需加载

import { BlobWriter, HttpReader, TextReader, ZipWriter } from "@zip.js/zip.js";

let cancel;
const CancelToken = axios.CancelToken;

let heic2any = null; // 用于存储模块的变量

// 在需要的地方调用该方法，实现按需加载
function loadHeic2Any() {
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

export default {
  data() {
    return {
      // 当前路径
      currentDirectory: "/",
      // 当前路径拆分的目录名和其相对路径
      directories: [],
      // 目录和文件
      files: [],
      // 搜索的关键字
      filter: "",
      // 是否多选
      multiSelect: false,
      // 已选中的文件
      checkedFiles: [],
      // 要移动的文件
      cutFiles: [],
      // 显示的列
      columns: ["size", "lastModified"],
      // 排序方式
      sort: {
        key: "name",
        direction: "asc",
      },
    };
  },
  methods: {
    // 排序
    sortBy(key) {
      if (key !== this.sort.key) {
        this.sort.key = key;
        this.sort.direction = "asc";
      } else {
        if (this.sort.direction === "asc") {
          this.sort.direction = "desc";
        } else {
          this.sort.direction = "asc";
        }
      }
    },
    // 获取文件列表
    list() {
      this.$root.loading = true;
      this.files = [];
      this.checkedFiles = []; // 清空选中文件
      if (cancel !== undefined) {
        cancel(); // 取消之前的操作
      }
      axios
        .get(
          "file/list",

          {
            params: { currentDirectory: this.currentDirectory },
            cancelToken: new CancelToken(function executor(c) {
              cancel = c;
            }),
          }
        )
        .then((res) => {
          if (res.success) {
            this.files = res.detail;
            this.doSort(this.sort);
          } else {
            if (res.msg === "没有权限") {
              if (!this.$root.hasToken(() => this.list())) {
                return;
              }
            }
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 搜索文件
    search() {
      if (this.filter.trim().length === 0) {
        this.list();
        return;
      }
      this.$root.loading = true;
      this.files = [];
      this.checkedFiles = [];
      if (cancel !== undefined) {
        cancel();
      }
      axios
        .post(
          "file/search",
          Qs.stringify({
            filter: this.filter,
            currentDirectory: this.currentDirectory,
          }),
          {
            cancelToken: new CancelToken(function executor(c) {
              cancel = c;
            }),
          }
        )
        .then((res) => {
          if (res.success) {
            this.files = res.detail;
            this.doSort(this.sort);
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          if (err.name === "CanceledError") {
            return;
          }
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 切换目录
    changeDirectory(relativePath) {
      this.currentDirectory = relativePath;
      this.list();
      this.getDirectoryHierachy();
    },
    // 将当前路径上的每一部分目录名拆分，保存显示名称和相对路径
    getDirectoryHierachy() {
      this.directories = [];
      const displayNames = this.currentDirectory.split("/");
      const relativePaths = [];
      for (let i = 0; i < displayNames.length; i++) {
        relativePaths.push(displayNames[i]);
        const relativePath = relativePaths.join("/");
        this.directories.push({
          displayName: displayNames[i],
          relativePath: relativePath.length === 0 ? "/" : relativePath,
        });
      }
      this.directories[0].displayName = "Home";
    },
    // 获取文件所在目录
    getParentDirectory(file) {
      let currentDirectory = file.split("/");
      currentDirectory.pop();
      let parentDirectory = currentDirectory.join("/");
      return parentDirectory.length === 0 ? "/" : parentDirectory;
    },
    // 上传文件
    upload(event) {
      if (!this.$root.hasToken(() => this.upload(event))) {
        return;
      }
      this.$root.message.title = "上传进度";
      const modal = new Modal(this.$root.$refs.progressModal);
      modal.show();
      const formData = new FormData();
      formData.append("currentDirectory", this.currentDirectory);
      const files = Array.from(event.target.files);
      files.forEach((file) => {
        console.log(file);
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
            this.list();
            this.$root.showModal("成功", res.msg);
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
    // 新建文件夹
    createDirectory() {
      const directoryName = this.$root.$refs.input.value;
      if (directoryName.trim().length === 0) {
        this.$root.showModal("提示", "文件夹名不能为空");
        return;
      }
      if (!this.$root.hasToken(() => this.createDirectory())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post(
          "file/mkdir",
          Qs.stringify({
            relativePath: this.currentDirectory + "/" + directoryName,
          })
        )
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal("成功", "新建文件夹成功");
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    removeOuterQuotes(str) {
      if (str.startsWith('"') && str.endsWith('"')) {
        return str.slice(1, -1);
      }
      return str;
    },
    // 下载文件
    download(payload, api_url) {
      this.$root.message.title = "正在下载";
      const modal = new Modal(this.$root.$refs.progressModal);
      modal.show();
      var lastTime = new Date().getTime();
      var lastBytes = 0;
      var self = this;
      axios({
        method: payload.params ? "get" : "post",
        url: axios.defaults.baseURL + api_url,
        ...(payload.params
          ? { params: payload.params }
          : { data: payload.data }),

        // params: {
        //   relativePath: relativePath,
        // },
        // // data: Qs.stringify({
        //   relativePath: relativePath,
        // }),
        // data: {
        //   relativePath: relativePath,
        // },
        responseType: "blob",
        onDownloadProgress: (e) => {
          const current = e.loaded;
          const total = e.total;
          this.$root.progress = Math.round((current * 100) / total) + "%";
          var now = new Date().getTime();
          var amount_completed = current - lastBytes;
          var time_taken = (now - lastTime) / 1000;
          var speed = time_taken ? amount_completed / time_taken : 0;
          lastBytes = current;
          lastTime = now;
          this.$root.speed = this.$root.formatBytes(speed) + "/s";
        },
      })
        .then((response) => {
          const contentType = response.headers["content-type"];

          if (contentType === "application/json") {
            const reader = new FileReader(); // 创建 FileReader 对象
            reader.onload = (event) => {
              // 注册 FileReader 的 load 事件回调
              const jsonData = event.target.result; // 在回调中可以获取到 Blob 对象的数据内容
              console.log(jsonData); // 输出 JSON 数据内容
              self.$root.showModal("错误", jsonData);
            };
            reader.readAsText(response.data);
            return;
          }
          let filename =
            response.headers["content-disposition"].split("filename=")[1];
          filename = decodeURIComponent(filename);
          saveAs(response.data, this.removeOuterQuotes(filename));
        })
        .catch((error) => {
          modal.hide();
          this.$root.showModal("错误", error.message);
        })
        .finally(() => {
          this.$root.progress = 0;
          modal.hide();
        });
    },
    async clientZip() {
      this.$root.loading = true;
      const zipWriter = new ZipWriter(new BlobWriter("application/zip"));
      this.checkedFiles.forEach(async (f) => {
        var url = axios.defaults.baseURL + "file/download?relativePath=" + f;
        await zipWriter.add(
          url.substring(url.lastIndexOf("/") + 1),
          new HttpReader(url)
        );
      });
      var blob = await zipWriter.close();
      saveAs(blob, "客户端打包.zip");
      this.$root.loading = false;
    },
    downloadFile(relativePath) {
      const payload = {
        params: {
          relativePath: relativePath,
        },
      };
      this.download(payload, "file/download");
    },
    zipAndDownload(relativePath) {
      const payload = {
        data: {
          relativePath: relativePath,
        },
      };
      this.download(payload, "file/zipAndDownload");
    },
    zip(relativePath) {
      this.$root.loading = true;
      axios
        .post(
          "file/zip",
          {
            relativePath: relativePath,
          }
          // Qs.stringify({ relativePath: relativePath })
          // Qs.stringify({ relativePath: JSON.stringify(this.checkedFiles) })
        )
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "压缩成功");
            this.list();
          } else {
            this.$root.showModal("成功", "压缩失败");
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },

    // 压缩文件夹
    // zipFolder(folderPath) {
    //   this.$root.loading = true;
    //   axios
    //     .post("file/zip", Qs.stringify({ relativePath: folderPath }))
    //     .then((res) => {
    //       if (res.success) {
    //         this.$root.showModal("成功", "压缩成功");
    //         this.list();
    //       } else {
    //         this.$root.showModal("成功", "压缩失败");
    //       }
    //     })
    //     .catch((err) => {
    //       this.$root.showModal("错误", err.message);
    //     })
    //     .finally(() => {
    //       this.$root.loading = false;
    //     });
    // },
    // 批量打包

    // // 下载文件夹
    // downloadFolder(relativePath) {
    //   this.download(relativePath, "file/downloadFolder");
    // },
    // // 批量下载
    // bulkDownload() {
    //   this.download(this.checkedFiles.toString(), "file/bulk");
    //   // this.download(JSON.stringify(this.checkedFiles), "file/bulk");
    // },
    // 删除文件
    deleteFile(files) {
      if (!this.$root.hasToken(() => this.deleteFile(files))) {
        return;
      }
      this.$root.loading = true;
      // const params = new URLSearchParams();
      // params.append("relativePath", files);

      axios
        // .post("file/delete", Qs.stringify({ relativePath: files.toString() }))
        .post("file/delete", { relativePath: files })
        .then((res) => {
          if (res.success) {
            // 判断是不是搜索状态下删除的
            if (this.filter.length === 0) {
              this.list();
            } else {
              this.search(this.filter);
            }
            this.$root.showModal("成功", "删除成功");
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },

    // 重命名
    rename(oldName) {
      // 传入旧文件名
      const newName = this.$root.$refs.input.value; // 从根组件获取输入的文件名
      if (newName.trim().length === 0) {
        this.$root.showModal("提示", "新文件名不能为空");
        return;
      }
      if (!this.$root.hasToken(() => this.rename(oldName))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post(
          "file/rename",
          {
            relativePath: oldName,
            target: newName,
          }
          // Qs.stringify({
          //   src: oldName,
          //   dst: this.currentDirectory + "/" + newName, // 新文件名：当前路径+新文件名
          // })
        )
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", res.msg);
            this.list();
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 剪切文件
    cut(files) {
      this.cutFiles = files;
      this.$root.showModal("剪切成功", this.cutFiles.join("\n"));
    },
    // 粘贴文件
    paste() {
      if (this.cutFiles.length === 0) return;
      if (!this.$root.hasToken(() => this.paste())) {
        return;
      }
      this.$root.loading = true;
      axios
        .post(
          "file/move",
          {
            relativePath: this.cutFiles,
            target: this.currentDirectory,
          }
          // Qs.stringify({
          //   src: JSON.stringify(this.cutFiles),
          //   dst: this.currentDirectory,
          // })
        )
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", res.msg);
            this.cutFiles = [];
            this.list();
          } else {
            this.$root.showModal("失败", res.msg);
            this.cutFiles = [];
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
          this.cutFiles = [];
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 预览文件
    preview(fileType, filename) {
      if (fileType.includes("text")) {
        this.$root.loading = true;
        this.$root.message.title = filename;
        this.$root.message.text = "";
        axios
          .post("file/read", Qs.stringify({ relativePath: filename }))
          .then((res) => {
            if (res.success) {
              this.$root.message.text = res.detail;
              new Modal(this.$root.$refs.textModal).show();
            } else {
              this.$root.showModal("失败", res.msg);
            }
          })
          .catch((err) => {
            this.$root.showModal("错误", err.message);
          })
          .finally(() => {
            this.$root.loading = false;
          });
      } else if (fileType.includes("image")) {
        this.$root.src = "";
        this.$root.src =
          axios.defaults.baseURL +
          "file/previewImage?relativePath=" +
          encodeURIComponent(filename);
        new Modal(this.$root.$refs.imageModal).show();
      }
    },
    // 创建文件分享链接
    share(path) {
      this.$root.loading = true;
      axios
        .post("shareCode/add", Qs.stringify({ path: path }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal(
              "分享成功",
              location.protocol + "//" + location.host + "/files/" + res.detail
            );
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    directoryStats(relativePath) {
      this.$root.loading = true;
      axios
        .post("file/info", Qs.stringify({ relativePath: relativePath }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("详细信息", res.detail);
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 全选
    checkAll(event) {
      if (event.target.checked) {
        this.files.folders.forEach((folder) => {
          this.checkedFiles.push(folder.relativePath);
        });
        this.files.files.forEach((file) => {
          this.checkedFiles.push(file.relativePath);
        });
      } else {
        this.checkedFiles = [];
      }
    },
    // 隐藏列
    hideColumn(column) {
      this.columns.includes(column)
        ? (this.columns = this.columns.filter((item) => item !== column))
        : this.columns.push(column);
    },
    // 根据文件后缀名获取相应图标
    getIcon(filename, fileType) {
      const ext = filename.split(".").pop();
      const exts = {
        "bi-file-earmark-binary": ["exe"],
        "bi-file-earmark-zip": ["zip", "7z", "rar"],
        "bi-file-earmark-code": ["py", "java", "c", "cpp", "html", "js", "css"],
        "bi-file-earmark-pdf": ["pdf"],
        "bi-file-earmark-word": ["doc", "docx"],
        "bi-file-earmark-excel": ["xls", "xlsx"],
        "bi-file-earmark-ppt": ["ppt", "pptx"],
      };
      for (const [key, value] of Object.entries(exts)) {
        if (value.includes(ext)) {
          return key;
        }
      }
      const type = {
        text: "bi-file-earmark-text",
        image: "bi-file-earmark-image",
        audio: "bi-file-earmark-music",
        video: "bi-file-earmark-play",
      };
      let icon = type[fileType.split("/")[0]];
      if (icon != null) {
        return icon;
      }
      return " bi-file-earmark";
    },
    // 排序
    doSort(sort) {
      if (sort.key === "time") {
        // 按时间排序
        if (sort.direction === "desc") {
          // 降序
          this.files.folders.sort((a, b) => {
            const dateA = new Date(a.lastModified);
            const dateB = new Date(b.lastModified);
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            // 如果日期不同，按日期排序
            if (dateA < dateB) return 1;
            if (dateA > dateB) return -1;
            // 如果日期相同，按名称排序
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
          this.files.files.sort((a, b) => {
            const dateA = new Date(a.lastModified);
            const dateB = new Date(b.lastModified);
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (dateA < dateB) return 1;
            if (dateA > dateB) return -1;
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
          // this.files.folders.sort((a, b) => new Date(b.lastModified) - new Date(a.lastModified));
          // this.files.files.sort((a, b) => new Date(b.lastModified) - new Date(a.lastModified));
        } else {
          // 升序
          this.files.folders.sort((a, b) => {
            const dateA = new Date(a.lastModified);
            const dateB = new Date(b.lastModified);
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (dateA < dateB) return -1;
            if (dateA > dateB) return 1;
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
          this.files.files.sort((a, b) => {
            const dateA = new Date(a.lastModified);
            const dateB = new Date(b.lastModified);
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (dateA < dateB) return -1;
            if (dateA > dateB) return 1;
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
          // this.files.folders.sort((a, b) => new Date(a.lastModified) - new Date(b.lastModified));
          // this.files.files.sort((a, b) => new Date(a.lastModified) - new Date(b.lastModified));
        }
      }
      if (sort.key == "name") {
        // 按名称排序
        if (sort.direction === "desc") {
          // 降序
          this.files.files.sort((a, b) => {
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (nameA < nameB) return 1;
            if (nameA > nameB) return -1;
            return 0;
          });
          this.files.folders.sort((a, b) => {
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (nameA < nameB) return 1;
            if (nameA > nameB) return -1;
            return 0;
          });
        } else {
          // 升序
          this.files.files.sort((a, b) => {
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
          this.files.folders.sort((a, b) => {
            const nameA = a.name.toUpperCase();
            const nameB = b.name.toUpperCase();
            if (nameA < nameB) return -1;
            if (nameA > nameB) return 1;
            return 0;
          });
        }
      }
    },
    generateDirectLink(relativePath) {
      this.$root.loading = true;
      axios
        .post(
          "file/generateDirectLink",
          Qs.stringify({ relativePath: relativePath })
        )
        .then((res) => {
          if (res.success) {
            this.$root.showModal(
              "生成成功",
              location.protocol + "//" + location.host + res.detail
            );
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    async previewHeic(relativePath) {
      this.$root.loading = true;
      try {
        const heic2anyModule = await loadHeic2Any();
        fetch(
          axios.defaults.baseURL + "/file/download?relativePath=" + relativePath
        )
          .then((res) => res.blob())
          .then((blob) =>
            heic2anyModule({
              blob,
              toType: "image/jpeg",
              //quality: 0.5,
            })
          )
          .then((conversionResult) => {
            const blob = new Blob([conversionResult], { type: "image/jpeg" });
            const blobURL = URL.createObjectURL(blob);
            this.$root.src = "";
            this.$root.src = blobURL;
            new Modal(this.$root.$refs.imageModal).show();
          })
          .catch((e) => {
            this.$root.showModal("错误", e.message);
          })
          .finally(() => {
            this.$root.loading = false;
          });
      } catch (error) {
        this.$root.showModal("错误", error.message);
      }

      // fetch(
      //   axios.defaults.baseURL + "/file/download?relativePath=" + relativePath
      // )
      //   .then((res) => res.blob())
      //   .then((blob) =>
      //     heic2any({
      //       blob,
      //       toType: "image/jpeg",
      //       //quality: 0.5,
      //     })
      //   )
      //   .then((conversionResult) => {
      //     const blob = new Blob([conversionResult], { type: "image/jpeg" });
      //     const blobURL = URL.createObjectURL(blob);
      //     this.$root.src = "";
      //     this.$root.src = blobURL;
      //     new Modal(this.$root.$refs.imageModal).show();
      //   })
      //   .catch((e) => {
      //     // see error handling section
      //   })
      //   .finally(() => {
      //     this.$root.loading = false;
      //   });
    },
  },
  mounted() {
    this.list();
    this.getDirectoryHierachy();
  },
  watch: {
    sort: {
      handler(newValue, oldValue) {
        this.doSort(newValue);
      },
      deep: true,
      flush: "post",
    },
  },
};
</script>
