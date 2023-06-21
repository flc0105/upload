import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    redirect: "/files",
  },
  {
    path: "/files",
    meta: { title: "文件列表" },
    component: () => import("../components/Files.vue"),
  },
  {
    path: "/pastes",
    meta: { title: "文本共享" },
    component: () => import("../components/Pastes.vue"),
  },
  {
    path: "/pastes/:id(\\d+)",
    meta: { title: "文本共享" },
    component: () => import("../components/Paste.vue"),
  },
  {
    path: "/share",
    meta: { title: "文件列表" },
    component: () => import("../components/Share.vue"),
  },
  {
    path: "/files/:code",
    meta: { title: "文件列表" },
    component: () => import("../components/File.vue"),
  },
  {
    path: "/bookmarks",
    meta: { title: "书签列表" },
    component: () => import("../components/Bookmarks.vue"),
  },
  {
    path: "/upload",
    meta: { title: "文件列表" },
    component: () => import("../components/Upload.vue"),
  },
  {
    path: "/log",
    meta: { title: "操作日志" },
    component: () => import("../components/Log.vue"),
  },
  {
    path: "/server",
    meta: { title: "服务器信息" },
    component: () => import("../components/Info.vue"),
  },
  {
    path: "/permissions",
    meta: { title: "权限控制" },
    component: () => import("../components/Permission.vue"),
  },
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || "404";
  next();
});

export default router;
