axios.interceptors.response.use(res => {
    if (res.status === 200) {
        return res.data
    }
})


const routes = [
    {
        'path': '/p',
        component: {
            template: document.getElementById('home').innerHTML,
            data() {
                return {
                    pastes: [],
                    title: '',
                    text: '',
                }
            },
            methods: {
                list() {
                    this.$root.loading = true
                    axios.post('/paste/list')
                        .then((res) => {
                            if (res.success) {
                                this.pastes = res.detail
                            } else {
                                this.$root.showModal('错误', res.msg)
                            }
                        })
                        .catch((err) => {
                            this.$root.showModal('错误', err.message)
                        })
                        .finally(() => {
                            this.$root.loading = false
                        })
                },
                add() {
                    if (this.title.trim().length === 0) {
                        this.$refs.title.focus()
                        return
                    }
                    if (this.text.trim().length === 0) {
                        this.$refs.text.focus()
                        return
                    }
                    axios.post('/paste/add',
                        Qs.stringify({
                            'title': this.title,
                            'text': this.text
                        }))
                        .then((res) => {
                            if (res.success) {
                                this.list()
                                this.$root.showModal('成功', '发布成功\nhttp://' + document.domain + '/p/' + res.detail.id)
                                this.title = ''
                                this.text = ''
                            } else {
                                this.$root.showModal('错误', res.msg)
                            }
                        })
                        .catch((err) => {
                            this.$root.showModal('错误', err.message)
                        })
                },
            },
            created() {
                this.$root.list = this.list
                this.list()
            }
        }
    },
    {
        'path': '/p/:id(\\d+)',
        component: {
            template: document.getElementById('detail').innerHTML,
            data() {
                return {
                    title: '',
                    text: '',
                }
            },
            methods: {
                get(id) {
                    this.title = ''
                    this.text = ''
                    this.$root.loading = true
                    axios.post('/paste/get', Qs.stringify({ 'id': id }))
                        .then((res) => {
                            if (res.success) {
                                this.title = res.detail.title
                                this.text = res.detail.text
                            } else {
                                this.$router.push('/404')
                            }
                        })
                        .catch((err) => {
                            this.$router.push('/404')
                        })
                        .finally(() => {
                            this.$root.loading = false
                        })
                },
                update() {
                    if (this.text.trim().length === 0) {
                        this.$refs.text.focus()
                        return
                    }
                    if (!this.$root.hasToken(() => this.update())) {
                        return
                    }
                    axios.post('/paste/update', Qs.stringify({
                        'id': this.$route.params.id,
                        'text': this.text
                    }))
                        .then((res) => {
                            if (res.success) {
                                this.$root.showModal('成功', '修改成功')
                            } else {
                                this.$root.showModal('错误', res.msg)
                            }
                        })
                        .catch((err) => {
                            this.$root.showModal('错误', err.message)
                        })
                },

            },
            created() {
                if (this.$route.params.id) {
                    this.get(this.$route.params.id)
                }
            },
        }
    },
    {
        'path': '/:pathMatch(.*)*',
        component: {
            template: '当前页面不存在 <a class="text-decoration-none" href="/p">返回</a>'
        }
    }
]

const router = VueRouter.createRouter({
    history: VueRouter.createWebHistory(),
    routes
})

const app = Vue.createApp({
    data() {
        return {
            message: {
                text: '',
                title: ''
            },
            loading: false,
            list: '',
            confirmDelete: '',
            previousAct: '',
        }
    },
    methods: {
        showModal(title, text) {
            this.message.title = title
            this.message.text = text
            new bootstrap.Modal(this.$refs.messageModal).show()
        },
        showConfirm(func) {
            new bootstrap.Modal(this.$refs.confirmModal).show()
            this.confirmDelete = func
        },
        del(id) {
            if (!this.hasToken(() => this.del(id))) {
                return
            }
            axios.post('/paste/delete', Qs.stringify({ 'id': id }))
                .then((res) => {
                    if (res.success) {
                        this.showModal('成功', '删除成功')
                        if (this.$route.params.id) {
                            this.$router.push('/p')
                        }
                        this.list()
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        hasToken(func) {
            if (!Cookies.get('token')) {
                this.previousAct = func
                this.$refs.password.value = ''
                new bootstrap.Modal(this.$refs.authModal).show()
                this.$refs.password.focus()
                return false
            }
            return true
        },
        getToken() {
            axios.post('/token/get', Qs.stringify({ 'password': this.$refs.password.value }))
                .then((res) => {
                    if (res.success) {
                        this.previousAct()
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        }
    },
    mounted() {
        new ClipboardJS('#btnCopy').on('success', () => {
            this.$root.showModal('成功', '复制成功')
        })
    }
})

app.use(router)
app.config.compilerOptions.whitespace = 'preserve'
app.mount('body')