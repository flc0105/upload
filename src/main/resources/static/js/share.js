axios.interceptors.response.use(res => {
    if (res.status === 200) {
        return res.data
    }
})


const app = Vue.createApp({
    data() {
        return {
            textList: [],
            message: {
                text: '',
                title: ''
            },
            text: {
                author: '',
                data: '',
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
            axios.post('/text/list')
                .then((res) => {
                    if (res.success) {
                        this.textList = res.detail
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        add() {
            if (this.text.author.trim().length === 0) {
                this.$refs.author.focus()
                return
            }
            if (this.text.data.trim().length === 0) {
                this.$refs.data.focus()
                return
            }
            axios.post('/text/add',
                Qs.stringify({
                    'author': this.text.author,
                    'data': this.text.data
                }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '发布成功')
                        this.text.author = ''
                        this.text.data = ''
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
        deleteText() {
            if (!this.hasToken(() => this.deleteText())) {
                return
            }
            axios.post('/text/delete', Qs.stringify({ 'id': this.textToDelete }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '删除成功')
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
        this.list()
        new ClipboardJS('#btnCopy').on('success', () => {
            this.showModal('成功', '复制成功')
        });
    }
})

app.config.compilerOptions.whitespace = 'preserve'
app.mount('body')