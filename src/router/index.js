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
    meta: { title: "文件分享" },
    component: () => import("../components/Share.vue"),
  },
  {
    path: "/file/:code",
    meta: { title: "文件分享" },
    component: () => import("../components/File.vue"),
  },
  {
    path: "/bookmarks",
    meta: { title: "书签列表" },
    component: () => import("../components/Bookmarks.vue"),
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
