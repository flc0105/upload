axios.interceptors.response.use(res => {
    if (res.status === 200) {
        return res.data
    }
})

const routes = [
    {'path': '/:id(\\d+)', component: {template: ''}}
]

const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
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
            pasteList: [],
            pasteAdd: {
                title: '',
                text: '',
            },
            pasteShow: {
                title: '',
                text: '',
            },
            textToDelete: '',
            previousAct: ''
        }
    },
    methods: {
        showModal(title, text) {
            this.message.title = title
            this.message.text = text
            new bootstrap.Modal(this.$refs.messageModal).show()
        },
        list() {
            this.loading = true
            axios.post('/paste/list')
                .then((res) => {
                    if (res.success) {
                        this.pasteList = res.detail
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
                .finally(() => {
                    this.loading = false
                })
        },
        add() {
            if (this.pasteAdd.title.trim().length === 0) {
                this.$refs.pasteAddTitle.focus()
                return
            }
            if (this.pasteAdd.text.trim().length === 0) {
                this.$refs.pasteAddText.focus()
                return
            }
            axios.post('/paste/add',
                Qs.stringify({
                    'title': this.pasteAdd.title,
                    'text': this.pasteAdd.text
                }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '保存成功\n' + window.location.href + res.detail.id)
                        this.pasteAdd.title = ''
                        this.pasteAdd.text = ''
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        confirmDelete(id) {
            this.textToDelete = id
            new bootstrap.Modal(this.$refs.confirmModal).show()
        },
        deletePaste() {
            if (!this.hasToken(() => this.deletePaste())) {
                return
            }
            axios.post('/paste/delete', Qs.stringify({'id': this.textToDelete}))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '删除成功')
                        if (this.$route.params.id) {
                            this.$router.push('/')
                        }
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        get(id) {
            this.pasteShow.title = ''
            this.pasteShow.text = ''
            this.loading = true
            axios.post('/paste/get', Qs.stringify({'id': id}))
                .then((res) => {
                    if (res.success) {
                        this.pasteShow.title = res.detail.title
                        this.pasteShow.text = res.detail.text
                    } else {
                        this.$router.push('/')
                    }
                })
                .catch((err) => {
                    this.$router.push('/')
                })
                .finally(() => {
                    this.loading = false
                })
        },
        update() {
            if (this.pasteShow.text.trim().length === 0) {
                this.$refs.pasteShowText.focus()
                return
            }
            if (!this.hasToken(() => this.update())) {
                return
            }
            axios.post('/paste/update', Qs.stringify({'id': this.$route.params.id, 'text': this.pasteShow.text}))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '保存成功')
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
            axios.post('/token/get', Qs.stringify({'password': this.$refs.password.value}))
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
        this.list()
        new ClipboardJS('#btnCopy').on('success', () => {
            this.showModal('成功', '复制成功')
        });
    },
    created() {
        this.$watch(
            () => this.$route.params,
            (toParams) => {
                if (toParams.id) {
                    this.get(toParams.id)
                }
            }
        )
    }
})

app.use(router)
app.config.compilerOptions.whitespace = 'preserve'
app.mount('body')