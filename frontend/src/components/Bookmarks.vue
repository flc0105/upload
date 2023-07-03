<template>
  <div>
    <ul>
      <bookmark-item
        v-for="bookmark in bookmarks"
        :bookmark="bookmark"
        :key="bookmark.name"
      />
    </ul>
  </div>

  <!-- <div class="input-group mb-2">
    <input
      class="form-control"
      v-model="url"
      @keyup.enter="add()"
      :disabled="$root.loading"
    />
    <button
      class="btn btn-outline-primary"
      @click="add()"
      :disabled="$root.loading"
    >
      添加
    </button>
    <button
      class="btn btn-outline-primary"
      data-bs-toggle="dropdown"
      :disabled="$root.loading"
    >
      <i class="bi bi-three-dots-vertical"></i>
      <ul class="dropdown-menu">
        <li>
          <a class="dropdown-item">...</a>
        </li>
      </ul>
    </button>
  </div> -->

  <nav aria-label="breadcrumb">
    <ol class="breadcrumb">
      <li class="breadcrumb-item" v-for="(value, key) in mapping" :key="key">
        <a v-if="isActive(key)">{{ key }}</a>
        <router-link v-else :to="`/bookmark${value}`" class="link-primary">{{
          key
        }}</router-link>
      </li>
    </ol>
  </nav>

  <table class="table table-hover table-borderless border shadow-sm">
    <tbody>
      <tr v-for="bookmark in page" :key="bookmark">
        <td class="text-truncate" style="max-width: 200px">
          <i
            v-if="bookmark.type == 'bookmark'"
            class="bi bi-file-earmark me-2"
          ></i>
          <i v-else class="bi bi-folder2 me-2"></i>
          <span v-if="bookmark.name"
            ><!--class="align-middle"-->
            <router-link
              :to="appendPath(bookmark.name)"
              v-if="bookmark.type == 'directory'"
              class="link-primary"
              >{{ bookmark.name }}</router-link
            >

            <a v-else>{{ bookmark.name }}</a>
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
                <a class="dropdown-item" :href="bookmark.url" target="_blank"
                  >打开</a
                >
              </li>
            </ul>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
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
import Qs from "qs";
import "bootstrap/dist/js/bootstrap.bundle";
import "file-saver";

export default {
  name: "BookmarkList",
  data() {
    return {
      page: [],
      mapping: {},
      bookmarks: "",
      currentId: 0,
      url: "", // 输入的url
    };
  },
  methods: {
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
     * 创建路径映射字典
     * @param {string[]} pathArray - 路径数组
     * @returns {Object} - 路径映射字典对象
     */
    createPathMapping(pathArray) {
      const pathMapping = {};

      let path = "";
      for (const element of pathArray) {
        path += `/${element}`;
        pathMapping[element] = path;
      }

      //return pathMapping;
      const baseMapping = { Home: "/" };
      return { ...baseMapping, ...pathMapping };
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

      return null; // 目录不存在
    },

    /**
     * 根据给定路径数组创建路径映射并存储在 mapping 属性中，同时查找给定路径下的书签并存储在 page 属性中
     * @param {string[]} pathArray - 路径数组
     */
    processPathArray(pathArray) {
      this.mapping = this.createPathMapping(pathArray);
      this.page = this.findBookmarksByPath(this.bookmarks, pathArray);
      console.log(this.page);
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
