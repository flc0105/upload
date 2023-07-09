import { createRouter, createWebHistory } from "vue-router";
import i18n from "../i18n/i18n";

const routes = [
  {
    path: "/",
    redirect: "/files",
  },
  {
    path: "/files",
    meta: { title: "files" },
    component: () => import("../components/Files.vue"),
  },
  {
    path: "/pastes",
    meta: { title: "pastes" },
    component: () => import("../components/Pastes.vue"),
  },
  {
    path: "/pastes/:id(\\d+)",
    meta: { title: "pastes" },
    component: () => import("../components/Paste.vue"),
  },
  {
    path: "/share",
    meta: { title: "files" },
    component: () => import("../components/Share.vue"),
  },
  {
    path: "/files/:code",
    meta: { title: "files" },
    component: () => import("../components/File.vue"),
  },
  {
    path: "/bookmark",
    meta: { title: "bookmarks" },
    component: () => import("../components/Bookmarks.vue"),
  },
  {
    path: "/bookmark/:path*", // 定义动态路由参数:path
    meta: { title: "bookmarks" },
    component: () => import("../components/Bookmarks.vue"),
  },
  {
    path: "/upload",
    meta: { title: "files" },
    component: () => import("../components/Upload.vue"),
  },
  {
    path: "/log",
    meta: { title: "operation_logs" },
    component: () => import("../components/Log.vue"),
  },
  {
    path: "/server",
    meta: { title: "server_info" },
    component: () => import("../components/Info.vue"),
  },
  {
    path: "/permissions",
    meta: { title: "permission_control" },
    component: () => import("../components/Permission.vue"),
  },
  {
    path: "/gallery",
    meta: { title: "gallery" },
    component: () => import("../components/Gallery.vue"),
  },
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  // document.title = to.meta.title || "404";

  if (to.meta.title) {
    document.title = i18n.global.t(to.meta.title);
  } else {
    document.title = "404";
  }

  next();
});

export default router;
