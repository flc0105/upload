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
