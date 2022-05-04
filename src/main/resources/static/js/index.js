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
            currentDirectory: '/',
            directories: [],
            files: [],
            progress: 0,
            filesToDelete: [],
            filter: '',
            multiSelect: false,
            checkedFiles: [],
            previousAct: null
        }
    },
    methods: {
        showModal(title, text) {
            this.message.title = title
            this.message.text = text
            new bootstrap.Modal(this.$refs.messageModal).show()
        },
        formatBytes(bytes) {
            if (bytes === 0) {
                return '0 B'
            }
            const i = Math.floor(Math.log(bytes) / Math.log(1024))
            return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + ['B', 'kB', 'MB', 'GB'][i]
        },
        list() {
            this.checkedFiles = []
            axios.post('/file/list', Qs.stringify({ 'currentDirectory': this.currentDirectory }))
                .then((res) => {
                    if (res.success) {
                        this.files = res.detail
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        search(filter) {
            this.checkedFiles = []
            axios.post('/file/search', Qs.stringify({ 'filter': filter, 'currentDirectory': this.currentDirectory }))
                .then((res) => {
                    if (res.success) {
                        this.files = res.detail
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        changeDirectory(relativePath) {
            this.currentDirectory = relativePath
            this.list()
            this.getDirectoryHierachy()
        },
        getDirectoryHierachy() {
            this.directories = []
            const displayNames = this.currentDirectory.split('/');
            const relativePaths = [];
            for (let i = 0; i < displayNames.length; i++) {
                relativePaths.push(displayNames[i])
                const relativePath = relativePaths.join('/')
                this.directories.push({
                    'displayName': displayNames[i],
                    'relativePath': relativePath.length === 0 ? '/' : relativePath
                })
            }
            this.directories[0].displayName = 'Home'
        },
        upload(event) {
            if (!this.hasToken(() => this.upload(event))) {
                return
            }
            const modal = new bootstrap.Modal(this.$refs.progressModal)
            modal.show()
            const formData = new FormData()
            formData.append('currentDirectory', this.currentDirectory)
            const files = Array.from(event.target.files)
            files.forEach((file) => {
                formData.append('file', file)
            })
            axios({
                method: 'post',
                url: '/file/upload',
                data: formData,
                headers: {
                    'Content-Type': 'multipart/form-data'
                },
                onUploadProgress: (e) => {
                    if (e.lengthComputable) {
                        const current = e.loaded
                        const total = e.total
                        this.progress = Math.round((current / total) * 100) + '%'
                    }
                }
            }).then(res => {
                if (res.success) {
                    modal.hide()
                    this.list()
                    this.showModal('成功', '上传成功')
                    this.progress = 0
                } else {
                    modal.hide()
                    this.showModal('错误', res.msg)
                    this.progress = 0
                }
            }).catch(err => {
                modal.hide()
                this.showModal('错误', err.message)
                this.progress = 0
            })
        },
        download(relativePath) {
            location.href = '/file/download?relativePath=' + encodeURIComponent(relativePath)
        },
        createZip(relativePath) {
            location.href = '/file/createZip?relativePath=' + encodeURIComponent(relativePath)
        },
        bulkDownload() {
            location.href = '/file/bulkDownload?relativePath=' + encodeURIComponent(JSON.stringify(this.checkedFiles))
        },
        confirmDelete(files) {
            this.filesToDelete = files;
            new bootstrap.Modal(this.$refs.confirmModal).show()
        },
        deleteFile() {
            if (!this.hasToken(() => this.deleteFile())) {
                return
            }
            axios.post('/file/delete', Qs.stringify({ 'relativePath': JSON.stringify(this.filesToDelete) }))
                .then((res) => {
                    if (res.success) {
                        if (this.filter.length === 0) {
                            this.list()
                        } else {
                            this.search(this.filter)
                        }
                        this.showModal('成功', '删除成功')
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        inputDirectoryName() {
            this.$refs.directoryName.value = ''
            new bootstrap.Modal(this.$refs.inputModal).show()
            this.$refs.directoryName.focus()
        },
        createDirectory() {
            const directoryName = this.$refs.directoryName.value
            if (directoryName.trim().length === 0) {
                this.showModal('错误', '文件夹名不能为空')
                return
            }
            axios.post('/file/createDirectory', Qs.stringify({ 'relativePath': this.currentDirectory + '/' + directoryName }))
                .then((res) => {
                    if (res.success) {
                        this.list()
                        this.showModal('成功', '新建文件夹成功')
                    } else {
                        this.showModal('错误', res.msg)
                    }
                })
                .catch((err) => {
                    this.showModal('错误', err.message)
                })
        },
        checkAll(event) {
            if (event.target.checked) {
                this.files.folders.forEach((folder) => {
                    this.checkedFiles.push(folder.relativePath)
                })
                this.files.files.forEach((file) => {
                    this.checkedFiles.push(file.relativePath)
                })
            } else {
                this.checkedFiles = []
            }
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
    computed: {
        getParentDirectory() {
            currentDirectory = this.currentDirectory.split('/')
            currentDirectory.pop()
            parentDirectory = currentDirectory.join('/')
            return parentDirectory.length === 0 ? '/' : parentDirectory
        }
    },
    watch: {
        filter(newValue) {
            if (newValue.trim().length === 0) {
                this.list()
                return
            }
            this.search(newValue)
        }
    },
    mounted() {
        this.list()
        this.getDirectoryHierachy()
    }
})

app.config.compilerOptions.whitespace = 'preserve'
app.mount('body')