axios.interceptors.response.use(res => {
    if (res.status === 200) {
        return res.data
    }
})

const app = Vue.createApp({
    data() {
        return {
            message: {
                text: '',
                title: ''
            },
            tasks: {},
        }
    },
    methods: {
        showModal(title, text) {
            this.message.title = title
            this.message.text = text
            new bootstrap.Modal(this.$refs.messageModal).show()
        },
        list() {
            axios.post('/task/list')
                .then((res) => {
                    if (res.success) {
                        this.tasks = res.detail
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        _add(text) {
            axios.post('/task/add', Qs.stringify({ 'text': text }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        del(id) {
            axios.post('/task/delete', Qs.stringify({ 'id': id }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        add() {
            const editable = document.getElementById('editable');
            if (editable.innerText.trim().length !== 0) {
                this._add(editable.innerText)
            }
            editable.innerText = ''
        },
        update(id, checked) {
            axios.post('/task/check', Qs.stringify({ 'id': id, 'checked': checked }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
    },
    mounted() {
        const editable = document.getElementById('editable');
        editable.addEventListener('focusin', function () {
            editable.innerText = ''
        })
        editable.addEventListener('focusout', () => {
            this.add()
            editable.innerHTML = '<span style="opacity: .5;">添加新任务</span>'
        })
        this.list()
    }
})

app.config.compilerOptions.whitespace = 'preserve'
app.mount('body')