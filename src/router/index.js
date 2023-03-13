import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: '/',
        redirect: '/files'
    },
    {
        path: '/files',
        component: () => import('../components/Files.vue')
    },
    {
        path: '/pastes',
        component: () => import('../components/Pastes.vue')
    },
    {
        path: "/pastes/:id(\\d+)",
        component: () => import('../components/Paste.vue')
    }
]
const router = createRouter(
    {
        history: createWebHistory(),
        routes
    }
)

export default router