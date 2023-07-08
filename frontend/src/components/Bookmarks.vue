<template>
  <input
    type="file"
    name="file"
    id="file"
    ref="file"
    class="d-none"
    @change="(event) => importBookmarks(event)"
    multiple
  />

  <v-contextmenu ref="contextmenu">
    <v-contextmenu-item @click="refresh()">{{
      $t("refresh")
    }}</v-contextmenu-item>
    <v-contextmenu-item
      @click="
        $root.inputValue = '';
        $root.showInput($t('new_bookmark'), $t('enter_url'), function () {
          add('bookmark');
        });
      "
      >{{ $t("new_bookmark") }}</v-contextmenu-item
    >
    <v-contextmenu-item
      @click="
        $root.inputValue = '';
        $root.showInput($t('new_directory'), $t('enter_name'), function () {
          add('directory');
        });
      "
      >{{ $t("new_directory") }}</v-contextmenu-item
    >
    <v-contextmenu-item @click="exportBookmarks()">{{
      $t("export")
    }}</v-contextmenu-item>

    <v-contextmenu-item
      @click="
        $refs.file.value = null;
        $refs.file.click();
      "
      >{{ $t("import") }}</v-contextmenu-item
    >

    <v-contextmenu-item @click="excel">{{ $t("excel") }}</v-contextmenu-item>
  </v-contextmenu>

  <div v-contextmenu:contextmenu>
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item" v-for="(value, key) in mapping" :key="key">
          <a v-if="isActive(value[0])">{{ value[0] }}</a>
          <router-link
            v-else
            :to="`/bookmark${value[1]}`"
            class="link-primary"
            >{{ value[0] }}</router-link
          >
        </li>
      </ol>
    </nav>

    <table class="table table-hover table-borderless border shadow-sm">
      <tbody>
        <tr v-if="page.length == 0" class="text-muted text-center">
          {{
            $t("folder_is_empty")
          }}
        </tr>
        <tr v-for="bookmark in page" :key="bookmark">
          <td class="text-truncate" style="max-width: 200px">
            <img
              class="align-middle me-2"
              v-if="bookmark.type == 'bookmark' && bookmark.icon != null"
              v-bind:src="bookmark.icon"
              style="width: auto; height: 16px"
            /><!--v-bind:src="'data:image/jpeg;base64,' + bookmark.icon"-->
            <i
              v-if="bookmark.type == 'bookmark' && bookmark.icon == null"
              class="bi align-middle bi-globe-asia-australia me-2"
            ></i>
            <i
              v-if="bookmark.type == 'directory'"
              class="bi align-middle bi-folder2 me-2"
            ></i>
            <span v-if="bookmark.name"
              ><!--class="align-middle"-->
              <router-link
                :to="appendPath(bookmark.name)"
                v-if="bookmark.type == 'directory'"
                class="link-primary align-middle"
                >{{ bookmark.name }}</router-link
              >

              <span class="align-middle" v-else>{{ bookmark.name }}</span>
            </span>
            <span v-else class="align-middle text-muted"
              ><i>{{ extractDomain(bookmark.url) }}</i></span
            >
          </td>
          <td class="text-truncate url" style="max-width: 200px">
            <i class="text-muted">{{ bookmark.url }}</i>
          </td>
          <td class="text-end" style="width: 10%">
            <div class="dropdown d-inline">
              <i
                class="bi bi-three-dots-vertical link-primary"
                data-bs-toggle="dropdown"
              ></i>
              <ul class="dropdown-menu">
                <li>
                  <a
                    v-if="bookmark.type == 'bookmark'"
                    class="dropdown-item"
                    :href="bookmark.url"
                    target="_blank"
                    >{{ $t("open") }}</a
                  >
                </li>
                <li>
                  <a
                    v-if="bookmark.type == 'bookmark'"
                    class="dropdown-item"
                    id="btnCopy"
                    :data-clipboard-text="bookmark.url"
                    >{{ $t("copy") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.inputValue = bookmark.name;
                      $root.showInput(
                        $t('edit_name'),
                        $t('enter_name'),
                        function () {
                          update(bookmark.id);
                        }
                      );
                    "
                    >{{ $t("edit_name") }}</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    @click="
                      $root.showConfirm(function () {
                        deleteBookmark(bookmark.id);
                      })
                    "
                    >{{ $t("delete") }}</a
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
.icon {
  width: auto;
  height: 15px;
  margin-right: 10px;
  vertical-align: middle;
}

@media screen and (max-width: 768px) {
  .url {
    display: none;
  }
}
</style>

<script>
import axios from "axios";
import "bootstrap/dist/js/bootstrap.bundle";
import "file-saver";

import { sendRequest } from "../utils/utils.js";

import { directive, Contextmenu, ContextmenuItem } from "v-contextmenu";
import "v-contextmenu/dist/themes/default.css";
import Qs from "qs";
export default {
  directives: {
    contextmenu: directive,
  },
  components: {
    [Contextmenu.name]: Contextmenu,
    [ContextmenuItem.name]: ContextmenuItem,
  },
  name: "BookmarkList",
  data() {
    return {
      page: [],
      mapping: null,
      bookmarks: "",
      currentId: 0,
      url: "", // 输入的url
    };
  },
  methods: {
    exportBookmarks() {
      // this.$root.loading = true;
      // var bookmarks = [];
      // for (const [key, value] of Object.entries(this.bookmarks)) {
      //   var obj = new Object();
      //   obj.title = value.title;
      //   obj.url = value.url;
      //   bookmarks.push(obj);
      // }
      // var json = JSON.stringify(bookmarks, null, 2);
      // var blob = new Blob([json], { type: "text/plain;charset=utf-8" });
      // saveAs(blob, "bookmarks.json");
      // this.$root.loading = false;

      this.$root.loading = true;

      var json = JSON.stringify(this.bookmarks, null, 2);
      var blob = new Blob([json], { type: "text/plain;charset=utf-8" });
      saveAs(blob, "bookmarks.json");
      this.$root.loading = false;
    },

    excel() {
      axios
        .get("/bookmarks/excel", { responseType: "blob" })
        .then((response) => {
          const contentDisposition = response.headers["content-disposition"];
          const fileName = contentDisposition.split("filename=")[1];
          const blob = new Blob([response.data], {
            type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          });
          saveAs(blob, fileName);
        })
        .catch((error) => {
          console.error(error);
        });
    },

    importBookmarks(event) {
      this.$root.loading = true;
      var reader = new FileReader();
      reader.readAsText(event.target.files[0]);
      reader.onload = (e) => {
        var data = e.target.result;
        if (!this.isJSON(data)) {
          this.$root.showModal("错误", "不是合法的json数据");
          this.$root.loading = false;
          return;
        }
        axios
          .post("bookmarks/import", Qs.stringify({ data: data }))
          .then((res) => {
            if (res.success) {
              this.list().then(() => {
                var paths = this.$route.params.path;
                this.processPathArray(paths);
              });
              this.$root.showModal("成功", res.msg);
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
      };
    },

    // 添加
    add(bookmarkType) {
      if (this.$root.$refs.input.value.trim().length === 0) {
        this.$root.showModal(
          this.$t("alert"),
          this.$t("name_or_url_cannot_be_empty")
        );
        return;
      }
      var data;
      if (bookmarkType == "directory") {
        data = {
          bookmarkType: 0,
          parentId: this.currentId,
          name: this.$root.$refs.input.value.trim(),
        };
      } else {
        data = {
          bookmarkType: 1,
          parentId: this.currentId,
          url: this.$root.$refs.input.value.trim(),
        };
      }

      sendRequest.call(
        this,
        "post",
        "/bookmarks",
        data,
        (res) => {
          this.list().then(() => {
            var paths = this.$route.params.path;
            this.processPathArray(paths);
            this.$root.showModal(this.$t("success"), res.msg);
            if (data.bookmarkType == 1 && res.detail != null) {
              sendRequest.call(
                this,
                "post",
                "/bookmarks/" + res.detail,
                data,
                (res) => {
                  this.list().then(() => {
                    var paths = this.$route.params.path;
                    this.processPathArray(paths);
                  });
                },
                (err) => {
                  console.log(err);
                }
              );
            }
          });
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },
    // 修改名称
    update(bookmarkId) {
      if (this.$root.$refs.input.value.trim().length === 0) {
        this.$root.showModal(this.$t("alert"), this.$t("name_cannot_be_empty"));
        return;
      }
      sendRequest.call(
        this,
        "put",
        "/bookmarks/" + bookmarkId,
        {
          // bookmarId: bookmarkId,
          name: this.$root.$refs.input.value.trim(),
        },
        (res) => {
          this.list().then(() => {
            var paths = this.$route.params.path;
            this.processPathArray(paths);
            this.$root.showModal(this.$t("success"), res.msg);
          });
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },
    deleteBookmark(id) {
      sendRequest.call(
        this,
        "delete",
        "/bookmarks/" + id,
        null,
        (res) => {
          this.list().then(() => {
            var paths = this.$route.params.path;
            this.processPathArray(paths);
            this.$root.showModal(this.$t("success"), res.msg);
          });
        },
        (err) => {
          this.$root.showModal(this.$t("error"), err);
        }
      );
    },
    /**
     * breadcrumb显示的链接是否应该处于激活状态
     * 检查给定的键是否与当前路径匹配
     * @param {string} key - 要检查的键
     * @returns {boolean} - 如果匹配则为 true，否则为 false
     */
    isActive(key) {
      const path = this.$route.params.path;
      if (path.length === 0) {
        // 如果路径为空，则必须匹配 "Home"
        return key === "Home";
      } else {
        // 否则必须匹配路径中的最后一个元素
        return key === path[path.length - 1];
      }
    },
    /**
     * 在当前路径后追加新的路径
     * @param {string} path - 要追加的路径
     * @returns {string} - 追加后的新路径
     */
    appendPath(path) {
      // 获取当前路由的路径
      const currentPath = this.$route.path;
      // 判断当前路径是否已以斜杠结尾
      const newPath = currentPath.endsWith("/")
        ? currentPath + path
        : currentPath + "/" + path;
      return newPath;
    },
    /**
     * 创建路径映射Map
     * @param {string[]} pathArray - 路径数组
     * @returns {Map} - 路径映射Map
     */
    createPathMapping(pathArray) {
      const pathMapping = new Map();
      pathMapping.set("Home", "/");

      let path = "";
      for (const element of pathArray) {
        path += `/${element}`;
        pathMapping.set(element, path);
      }
      return pathMapping;
    },
    /**
     * 根据路径查找对应的书签
     * @param {Bookmark[]} bookmarks - 书签数组
     * @param {string[]} path - 路径数组
     * @returns {Bookmark[]} - 匹配路径的书签数组
     */
    findBookmarksByPath(bookmarks, path) {
      // 递归查找目录
      function find(bookmarks, pathArr) {
        if (pathArr.length === 0) {
          return JSON.parse(JSON.stringify(bookmarks));
        }

        const currentDir = pathArr[0];
        const nextPathArr = pathArr.slice(1);

        for (const bookmark of bookmarks) {
          if (bookmark.type === "directory" && bookmark.name === currentDir) {
            return JSON.parse(
              JSON.stringify(find(bookmark.children, nextPathArr))
            );
          }
        }

        // 目录不存在
        return [];
      }
      const result = find(bookmarks, path);
      return result;
    },

    getIdByPath(bookmarks, pathArr) {
      if (pathArr.length === 0) {
        return 0;
      }

      const currentDir = pathArr[0];
      const nextPathArr = pathArr.slice(1);

      for (const bookmark of bookmarks) {
        if (bookmark.type === "directory" && bookmark.name === currentDir) {
          if (nextPathArr.length === 0) {
            return bookmark.id; // 如果已经匹配到最后一个路径，则返回当前节点的 id
          }

          return this.getIdByPath(bookmark.children, nextPathArr); // 递归查找下一级目录
        }
      }

      return 0; // 目录不存在
    },

    /**
     * 根据给定路径数组创建路径映射并存储在 mapping 属性中，同时查找给定路径下的书签并存储在 page 属性中
     * @param {string[]} pathArray - 路径数组
     */
    processPathArray(pathArray) {
      this.mapping = this.createPathMapping(pathArray);
      console.log(this.mapping);
      this.page = this.findBookmarksByPath(this.bookmarks, pathArray);
    },
    refresh() {
      this.list().then(() => {
        var paths = this.$route.params.path;
        this.processPathArray(paths);
        this.currentId = this.getIdByPath(this.bookmarks, paths);
      });
    },

    // 判断是不是json数据
    isJSON(str) {
      try {
        return JSON.parse(str) && !!str;
      } catch (e) {
        return false;
      }
    },
    // 从url中提取域名
    extractDomain(url) {
      try {
        const { hostname } = new URL(url);
        return hostname;
      } catch (e) {
        return null;
      }
    },
    // 获取书签列表
    async list() {
      this.$root.loading = true;
      await axios
        .get("/bookmarks")
        .then((res) => {
          if (res.success) {
            this.bookmarks = res.detail;
          } else {
            this.$root.showModal(this.$t("success"), res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal(this.$t("error"), err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
  },
  mounted() {
    this.list().then(() => {
      var paths = this.$route.params.path;
      this.processPathArray(paths);
      this.currentId = this.getIdByPath(this.bookmarks, paths);
    });
  },
  beforeRouteUpdate(to, from, next) {
    var paths = to.params.path;
    this.processPathArray(paths);
    this.currentId = this.getIdByPath(this.bookmarks, paths);
    next();
  },
};
</script>
