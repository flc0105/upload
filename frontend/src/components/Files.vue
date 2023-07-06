<template>
  <div class="card border-bottom-0">
    <div class="card-header">
      <ol class="breadcrumb float-start" style="margin: 0.5rem 0">
        <li
          class="filename text-truncate"
          v-for="directory in pathMapping"
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
          {{ $t("upload_files") }}
        </button>
        <button
          class="btn btn-outline-primary ms-1 me-1"
          onclick="document.getElementById('folder').value=null; document.getElementById('folder').click()"
        >
          {{ $t("upload_folder") }}
        </button>
        <button
          class="btn btn-outline-primary me-1"
          @click="
            $root.inputValue = '';
            $root.showInput(
              $t('new_folder'),
              $t('enter_a_new_folder_name'),
              createDirectory
            );
          "
        >
          {{ $t("new_folder") }}
        </button>
        <button class="btn btn-outline-primary me-1" @click="list()">
          {{ $t("refresh") }}
        </button>
        <button
          v-if="
            cutFiles.length !== 0 &&
            getParentDirectory(cutFiles[0]) !== currentDirectory &&
            !currentDirectory.startsWith(cutFiles[0])
          "
          class="btn btn-outline-primary me-1"
          @click="move()"
        >
          {{ $t("paste") }}
        </button>

        <button
          class="btn btn-outline-primary me-1"
          :disabled="files.length === 0"
          @click="
            multiSelect = !multiSelect;
            checkedFiles = [];
          "
        >
          {{ $t("multi_select") }}
        </button>

        <div class="btn-group">
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="zipAndDownload(checkedFiles)"
          >
            {{ $t("download") }}
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
            {{ $t("delete") }}
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="cut(checkedFiles)"
          >
            {{ $t("cut") }}
          </button>
          <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="zip(checkedFiles)"
          >
            {{ $t("zip") }}
          </button>
          <!-- <button
            v-if="multiSelect"
            :disabled="checkedFiles.length == 0"
            class="btn btn-outline-primary"
            @click="zipFiles()"
          >
            {{ $t("zip_files") }}
          </button> -->
        </div>
      </div>
      <div class="files-right">
        <div class="w-100 d-flex">
          <input
            class="form-control"
            style="flex: 1 1 auto; width: 1%"
            :placeholder="$t('search')"
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
                  <label class="form-check-label" for="size">
                    {{ $t("size") }}</label
                  >
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
                  <label class="form-check-label" for="contentType">
                    {{ $t("type") }}</label
                  >
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
                  <label class="form-check-label" for="lastModified">
                    {{ $t("last_modified") }}</label
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
              @click="(event) => handleSelectAll(event)"
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
            @click="toggleSortDirectionForKey('name')"
          >
            {{ $t("filename") }}
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
          <th class="size" v-if="columns.includes('size')">{{ $t("size") }}</th>
          <th class="contentType" v-if="columns.includes('contentType')">
            {{ $t("type") }}
          </th>
          <th
            class="lastModified cursor-pointer"
            v-if="columns.includes('lastModified')"
            @click="toggleSortDirectionForKey('time')"
          >
            {{ $t("last_modified") }}
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
          <th class="action">{{ $t("action") }}</th>
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
                  <a
                    class="dropdown-item"
                    @click="zip([folder.relativePath])"
                    >{{ $t("zip") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="cut([folder.relativePath])"
                    >{{ $t("cut") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.inputValue = folder.name;
                      $root.showInput(
                        $t('rename'),
                        $t('enter_a_new_folder_name'),
                        function () {
                          rename(folder.relativePath);
                        }
                      );
                    "
                    >{{ $t("rename") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="showFileStats(folder.relativePath)"
                    >{{ $t("view_details") }}</a
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
                  <a class="dropdown-item" @click="cut([file.relativePath])">{{
                    $t("cut")
                  }}</a>
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.inputValue = file.name;
                      $root.showInput(
                        $t('rename'),
                        $t('enter_a_new_file_name'),
                        function () {
                          rename(file.relativePath);
                        }
                      );
                    "
                    >{{ $t("rename") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="showFileStats(file.relativePath)"
                    >{{ $t("view_details") }}</a
                  >
                </li>
                <li>
                  <hr class="dropdown-divider" />
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    v-if="
                      file.fileType.includes('image') ||
                      file.fileType.includes('text')
                    "
                    @click="preview(file.fileType, file.relativePath)"
                    >{{ $t("preview") }}</a
                  >
                </li>
                <li>
                  <a class="dropdown-item" @click="share(file.relativePath)">{{
                    $t("share")
                  }}</a>
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    v-if="file.fileType.includes('image')"
                    @click="generateDirectLink(file.relativePath)"
                    >{{ $t("generate_direct_link") }}</a
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

@media screen and (max-width: 1200px) {
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
import { BlobWriter, HttpReader, TextReader, ZipWriter } from "@zip.js/zip.js";

import {
  sendRequest,
  removeOuterQuotes,
  loadHeic2Any,
} from "../utils/utils.js";

let cancel;
const CancelToken = axios.CancelToken;

export default {
  data() {
    return {
      /**
       * 当前路径
       * @type {string}
       */
      currentDirectory: "/",

      /**
       * 当前路径拆分的目录名和其相对路径映射
       * @type {Array<{ displayName: string, relativePath: string }>}
       */
      pathMapping: [],

      /**
       * 目录和文件
       * @type {{ folders: Array<{ name: string, lastModified: string, relativePath: string }>,
       *          files: Array<{ name: string, length: number, lastModified: string, relativePath: string, fileType: string }> }}
       */
      files: {},

      /**
       * 搜索的关键字
       * @type {string}
       */
      filter: "",

      /**
       * 是否多选
       * @type {boolean}
       */
      multiSelect: false,

      /**
       * 已选中的文件
       * @type {Array<string>}
       */
      checkedFiles: [],

      /**
       * 要移动的文件
       * @type {Array<string>}
       */
      cutFiles: [],

      /**
       * 显示的列
       * @type {Array<string>}
       */
      columns: ["size", "lastModified"],

      /**
       * 排序方式
       * @type {{ key: string, direction: string }}
       */
      sort: {
        key: "name",
        direction: "asc",
      },
    };
  },
  methods: {
    /**
     * 查询所有文件
     */
    list() {
      this.$root.loading = true;
      this.files = [];
      this.checkedFiles = []; // 清空选中文件
      if (cancel !== undefined) {
        cancel(); // 取消之前的操作
      }
      axios
        .post(
          "file/list",
          Qs.stringify({ currentDirectory: this.currentDirectory }),
          {
            cancelToken: new CancelToken(function executor(c) {
              cancel = c;
            }),
          }
        )
        .then((res) => {
          if (res.success) {
            this.files = res.detail;
            this.sortFilesByCriteria(this.sort);
          } else {
            if (res.msg === "没有权限" || res.msg === "No permission.") {
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

    /**
     * 搜索文件
     */
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
            this.sortFilesByCriteria(this.sort);
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

    /**
     * 上传文件
     * @param {Event} event
     */
    upload(event) {
      if (!this.$root.hasToken(() => this.upload(event))) {
        return;
      }
      this.$root.message.title = this.$t("upload_progress");
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
            this.$root.showModal(this.$t("success"), res.msg);
            this.$root.progress = 0;
          } else {
            modal.hide();
            this.$root.showModal(this.$t("error"), res.msg);
            this.$root.progress = 0;
          }
        })
        .catch((err) => {
          modal.hide();
          this.$root.showModal(this.$t("error"), err.message);
          this.$root.progress = 0;
        });
    },

    /**
     * 下载
     * @param {String|Array<string>} relativePath
     * @param {String} api
     */
    download(relativePath, api) {
      this.$root.message.title = this.$t("download_progress");
      const modal = new Modal(this.$root.$refs.progressModal);
      modal.show();
      var lastTime = new Date().getTime();
      var lastBytes = 0;
      var self = this;
      axios({
        method: "post",
        url: axios.defaults.baseURL + api,
        data: Qs.stringify({
          relativePath: relativePath,
        }),
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
          saveAs(response.data, removeOuterQuotes(filename));
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

    /**
     * 下载文件
     * @param {String} relativePath
     */
    downloadFile(relativePath) {
      this.download(relativePath, "file/download");
    },

    /**
     * 压缩并下载
     * @param {Array<string>} relativePath
     */
    zipAndDownload(relativePath) {
      this.download(relativePath, "file/zipAndDownload");
    },

    /**
     * 创建目录
     */
    createDirectory() {
      const directoryName = this.$root.$refs.input.value;
      if (directoryName.trim().length === 0) {
        this.$root.showModal(
          this.$t("alert"),
          this.$t("folder_name_cannot_be_empty")
        );
        return;
      }
      if (!this.$root.hasToken(() => this.createDirectory())) {
        return;
      }
      const data = Qs.stringify({
        relativePath: this.currentDirectory + "/" + directoryName,
      });
      sendRequest.call(
        this,
        "post",
        "file/mkdir",
        data,
        (res) => {
          this.$root.showModal(this.$t("success"), res.msg);
          this.list();
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 压缩
     * @param {Array<string>} relativePath
     */
    zip(relativePath) {
      const data = {
        relativePath: relativePath,
      };
      sendRequest.call(
        this,
        "post",
        "file/zip",
        data,
        (res) => {
          this.$root.showModal(this.$t("success"), res.msg);
          this.list();
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 删除文件
     * @param {Array<string>} files
     */
    deleteFile(files) {
      if (!this.$root.hasToken(() => this.deleteFile(files))) {
        return;
      }
      const data = {
        relativePath: files,
      };
      sendRequest.call(
        this,
        "post",
        "file/delete",
        data,
        (res) => {
          // 判断是不是搜索状态下删除的
          if (this.filter.length === 0) {
            this.list();
          } else {
            this.search(this.filter);
          }
          this.$root.showModal(this.$t("success"), res.msg);
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 重命名文件
     * @param {String} relativePath
     */
    rename(relativePath) {
      // 传入旧文件名
      const name = this.$root.$refs.input.value; // 从根组件获取输入的文件名
      if (name.trim().length === 0) {
        this.$root.showModal(
          this.$t("alert"),
          this.$t("folder_name_cannot_be_empty")
        );
        return;
      }
      if (!this.$root.hasToken(() => this.rename(relativePath))) {
        return;
      }
      const data = {
        relativePath: relativePath,
        target: this.currentDirectory + "/" + name,
      };
      sendRequest.call(
        this,
        "post",
        "file/rename",
        data,
        (res) => {
          this.$root.showModal(this.$t("success"), res.msg);
          this.list();
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 移动文件
     */
    move() {
      if (this.cutFiles.length === 0) return;
      if (!this.$root.hasToken(() => this.move())) {
        return;
      }
      const data = {
        relativePath: this.cutFiles,
        target: this.currentDirectory,
      };
      sendRequest.call(
        this,
        "post",
        "file/move",
        data,
        (res) => {
          this.$root.showModal(this.$t("success"), res.msg);
          this.cutFiles = [];
          this.list();
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
          this.cutFiles = [];
        }
      );
    },

    /**
     * 预览文件
     * @param {String} fileType 预览的文件类型
     * @param {String} relativePath 预览的文件名
     */
    preview(fileType, relativePath) {
      if (fileType.includes("text")) {
        this.$root.message.title = relativePath;
        this.$root.message.text = "";
        const data = Qs.stringify({ relativePath: relativePath });
        sendRequest.call(
          this,
          "post",
          "file/read",
          data,
          (res) => {
            this.$root.message.text = res.detail;
            new Modal(this.$root.$refs.textModal).show();
          },
          (err) => {
            this.$root.showModal(this.$t("error"), err);
          }
        );
      } else if (fileType.includes("image")) {
        if (relativePath.split(".").pop() == "heic") {
          this.previewHeic(relativePath);
        } else {
          this.$root.src = "";
          this.$root.src =
            axios.defaults.baseURL +
            "file/preview?relativePath=" +
            encodeURIComponent(relativePath);
          new Modal(this.$root.$refs.imageModal).show();
        }
      }
    },

    /**
     * 创建文件分享链接
     * @param {String} relativePath
     */
    share(relativePath) {
      sendRequest.call(
        this,
        "post",
        "shareCode/add",
        Qs.stringify({ path: relativePath }),
        (res) => {
          this.$root.showModal(
            this.$t("success"),
            location.protocol + "//" + location.host + "/files/" + res.detail
          );
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 查询文件详细信息
     * @param {String} relativePath
     */
    showFileStats(relativePath) {
      sendRequest.call(
        this,
        "post",
        "file/info",
        Qs.stringify({ relativePath: relativePath }),
        (res) => {
          this.$root.showModal(this.$t("file_info"), res.detail);
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 生成图片直链
     * @param {String} relativePath
     */
    generateDirectLink(relativePath) {
      sendRequest.call(
        this,
        "post",
        "file/link",
        Qs.stringify({ relativePath: relativePath }),
        (res) => {
          this.$root.showModal(
            this.$t("success"),
            location.protocol + "//" + location.host + res.detail
          );
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },

    /**
     * 客户端压缩文件
     */
    async zipFiles() {
      this.$root.loading = true;
      try {
        const zipWriter = new ZipWriter(new BlobWriter("application/zip"));
        const promises = this.checkedFiles.map(async (file) => {
          const url =
            axios.defaults.baseURL + "file/download?relativePath=" + file;
          try {
            await zipWriter.add(
              url.substring(url.lastIndexOf("/") + 1),
              new HttpReader(url)
            );
          } catch (error) {
            console.log("Error while adding file:", file, error.message);
          }
        });
        await zipWriter.add(
          "hello.txt",
          new TextReader("https://github.com/flc0105/upload")
        );
        await Promise.all(promises);
        const blob = await zipWriter.close();
        saveAs(blob, Date.now() + ".zip");
      } catch (error) {
        this.$root.showModal("错误", error.message);
      } finally {
        this.$root.loading = false;
      }
    },

    /**
     * 切换目录
     * @param {String} relativePath 要切换到的目录的相对路径
     */
    changeDirectory(relativePath) {
      this.currentDirectory = relativePath;
      this.list();
      this.getDirectoryHierachy();
    },
    /**
     * 将当前路径上的每一部分目录名拆分，保存显示名称和相对路径
     */
    getDirectoryHierachy() {
      this.pathMapping = [];
      const displayNames = this.currentDirectory.split("/");
      const relativePaths = [];
      for (let i = 0; i < displayNames.length; i++) {
        relativePaths.push(displayNames[i]);
        const relativePath = relativePaths.join("/");
        this.pathMapping.push({
          displayName: displayNames[i],
          relativePath: relativePath.length === 0 ? "/" : relativePath,
        });
      }
      this.pathMapping[0].displayName = "Home";
    },
    /**
     * 获取文件所在的父目录路径
     * @param {string} file - 文件路径
     * @returns {string} - 父目录路径
     */
    getParentDirectory(file) {
      let currentDirectory = file.split("/");
      currentDirectory.pop();
      let parentDirectory = currentDirectory.join("/");
      return parentDirectory.length === 0 ? "/" : parentDirectory;
    },

    /**
     * 剪切文件
     * @param {*} files 文件相对路径列表
     */
    cut(files) {
      this.cutFiles = files;
      this.$root.showToast(this.$t("cut_success"), this.cutFiles.join("\n"));
    },

    /**
     * 处理全选/全不选事件
     * @param {Event} event - 复选框事件对象
     */
    handleSelectAll(event) {
      if (event.target.checked) {
        // 全选
        this.checkedFiles = [
          ...this.files.folders.map((folder) => folder.relativePath),
          ...this.files.files.map((file) => file.relativePath),
        ];
      } else {
        // 全不选
        this.checkedFiles = [];
      }
    },
    /**
     * 切换列的显示状态
     * @param {string} column - 列名
     */
    hideColumn(column) {
      this.columns.includes(column)
        ? (this.columns = this.columns.filter((item) => item !== column))
        : this.columns.push(column);
    },

    /**
     * 按指定条件对文件进行排序
     * @param {Object} sort - 排序条件对象，包括 key（排序关键字）和 direction（排序方向）
     */
    sortFilesByCriteria(sort) {
      const sortDirection = sort.direction === "desc" ? 1 : -1;

      // 根据名称排序函数
      const sortByName = (a, b) => {
        const nameA = a.name.toUpperCase();
        const nameB = b.name.toUpperCase();
        if (nameA < nameB) return sortDirection;
        if (nameA > nameB) return -sortDirection;
        return 0;
      };

      // 根据时间排序函数
      const sortByTime = (a, b) => {
        const dateA = new Date(a.lastModified);
        const dateB = new Date(b.lastModified);
        const nameA = a.name.toUpperCase();
        const nameB = b.name.toUpperCase();
        if (dateA < dateB) return sortDirection;
        if (dateA > dateB) return -sortDirection;
        if (nameA < nameB) return -1;
        if (nameA > nameB) return 1;
        return 0;
      };

      if (sort.key === "time") {
        // 按时间排序
        this.files.folders.sort(sortByTime);
        this.files.files.sort(sortByTime);
      }

      if (sort.key === "name") {
        // 按名称排序
        this.files.folders.sort(sortByName);
        this.files.files.sort(sortByName);
      }
    },

    /**
     * 切换指定关键字的排序方向
     * @param {string} key - 排序关键字
     */
    toggleSortDirectionForKey(key) {
      // 如果指定关键字与当前排序关键字不同，则重置为指定关键字且升序排序
      if (key !== this.sort.key) {
        this.sort.key = key;
        this.sort.direction = "asc";
      } else {
        // 如果指定关键字与当前排序关键字相同，则切换排序方向
        if (this.sort.direction === "asc") {
          this.sort.direction = "desc"; // 切换为降序
        } else {
          this.sort.direction = "asc"; // 切换为降序
        }
      }
    },

    /**
     * 获取文件图标样式类名
     * @param {string} filename - 文件名
     * @param {string} fileType - 文件类型
     * @returns {string} - 文件图标样式类名
     */
    getIcon(filename, fileType) {
      const ext = filename.split(".").pop();
      const exts = {
        "bi-file-earmark-binary": ["exe"],
        "bi-file-earmark-zip": ["zip", "7z", "rar"],
        "bi-file-earmark-code": [
          "py",
          "java",
          "c",
          "cpp",
          "html",
          "js",
          "css",
          "json",
        ],
        "bi-file-earmark-pdf": ["pdf"],
        "bi-file-earmark-word": ["doc", "docx"],
        "bi-file-earmark-excel": ["xls", "xlsx"],
        "bi-file-earmark-ppt": ["ppt", "pptx"],
      };
      // 检查文件后缀是否匹配已知的扩展名
      for (const [icon, extensions] of Object.entries(exts)) {
        if (extensions.includes(ext)) {
          return icon;
        }
      }
      const fileTypeMapping = {
        text: "bi-file-earmark-text",
        image: "bi-file-earmark-image",
        audio: "bi-file-earmark-music",
        video: "bi-file-earmark-play",
      };
      // 根据文件类型映射获取对应的图标样式类名
      let icon = fileTypeMapping[fileType.split("/")[0]];
      if (icon) {
        return icon;
      }
      return "bi-file-earmark";
    },

    /**
     * 预览heic
     * @param {String} relativePath
     */
    async previewHeic(relativePath) {
      this.$root.loading = true;
      try {
        const heic2anyModule = await loadHeic2Any();
        const response = await fetch(
          axios.defaults.baseURL + "/file/download?relativePath=" + relativePath
        );
        if (!response.ok) {
          throw new Error(
            this.$t("network_request_error") + "：" + response.status
          );
        }
        const blob = await response.blob();
        const result = await heic2anyModule({
          blob,
          toType: "image/jpeg",
        });
        const convertedBlob = new Blob([result], {
          type: "image/jpeg",
        });
        const blobURL = URL.createObjectURL(convertedBlob);
        this.$root.src = "";
        this.$root.src = blobURL;
        new Modal(this.$root.$refs.imageModal).show();
      } catch (error) {
        this.$root.showModal(this.$t("error"), error.message);
      } finally {
        this.$root.loading = false;
      }

      // this.$root.loading = true;
      // try {
      //   const heic2anyModule = await loadHeic2Any();
      //   fetch(
      //     axios.defaults.baseURL + "/file/download?relativePath=" + relativePath
      //   )
      //     .then((res) => res.blob())
      //     .then((blob) =>
      //       heic2anyModule({
      //         blob,
      //         toType: "image/jpeg",
      //       })
      //     )
      //     .then((result) => {
      //       const blob = new Blob([result], { type: "image/jpeg" });
      //       const blobURL = URL.createObjectURL(blob);
      //       this.$root.src = "";
      //       this.$root.src = blobURL;
      //       new Modal(this.$root.$refs.imageModal).show();
      //     })
      //     .catch((e) => {
      //       this.$root.showModal("错误", e.message);
      //     })
      //     .finally(() => {
      //       this.$root.loading = false;
      //     });
      // } catch (error) {
      //   this.$root.showModal("错误", error.message);
      // }
    },
  },
  mounted() {
    this.list();
    this.getDirectoryHierachy();
  },
  watch: {
    sort: {
      handler(newValue, oldValue) {
        this.sortFilesByCriteria(newValue);
      },
      deep: true,
      flush: "post",
    },
  },
};
</script>
