let dir = '';
const files = [];

list();

function getFiles(action, param) {
    $('#cancel').trigger('click');
    $('#multiSelect').removeClass('disabled');
    $('#multiSelect').prop('disabled', false);
    $.post(action, param, function (data) {
        if (!data.success) {
            showMessage(data.msg);
            $('#multiSelect').addClass('disabled');
            $('#multiSelect').prop('disabled', true);
            return;
        }
        if (data.detail.folders.length + data.detail.files.length === 0) {
            $('#multiSelect').addClass('disabled');
            $('#multiSelect').prop('disabled', true);
        }
        let index = 0;
        $.each(data.detail.folders, function (i, folder) {
            const dropdown = '<div class="dropdown" style="display: inline">' +
                '<button type="button" class="btn dropdown-toggle btn-xs btn-default" id="dropdown" data-toggle="dropdown">...</button>' +
                '<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">' +
                '<li><a href="javascript:;" onclick="cut(\'' + folder.relativePath + '\')">剪切</a></li>' +
                '<li><a href="javascript:;" data-filename="' + folder.relativePath + '" data-toggle="modal" data-target="#renameModal">重命名</a></li>' +
                '</ul></div>';
            const tr = $('<tr data-name="' + folder.relativePath + '">').append(
                '<td>' + (i + 1) + '</td>' +
                '<td><a href="javascript:;">' + folder.name + '</a></td>' +
                '<td>-</td>' +
                '<td>' + folder.lastModified + '</td>' +
                '<td><button class="btn btn-default btn-xs" onclick="createZip(\'' + folder.relativePath + '\')">下载</button> <button class="btn btn-danger btn-xs" data-filename="' + folder.relativePath + '" data-toggle="modal" data-target="#confirmModal">删除</button> ' + dropdown + '</td>'
            ).appendTo('.table');
            tr.on('click', 'td:nth-child(2)', function () {
                dir = folder.relativePath;
                list();
            });
            index = i + 1;
        });
        $.each(data.detail.files, function (i, file) {
            const type = file.fileType.startsWith('image') ? 'image' : file.fileType.startsWith('text') ? 'text' : null;
            const preview = '<li><a href="javascript:;" onclick="preview(\'' + file.relativePath + '\', \'' + type + '\')">预览</a></li>';
            const dropdown = '<div class="dropdown" style="display: inline">' +
                '<button type="button" class="btn dropdown-toggle btn-xs btn-default" id="dropdown" data-toggle="dropdown">...</button>' +
                '<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">' + (type != null ? preview : '') +
                '<li><a href="javascript:;" onclick="cut(\'' + file.relativePath + '\')">剪切</a></li>' +
                '<li><a href="javascript:;" data-filename="' + file.relativePath + '" data-toggle="modal" data-target="#renameModal">重命名</a></li>' +
                '</ul></div>';
            $('<tr data-name="' + file.relativePath + '">').append(
                '<td>' + (index + i + 1) + '</td>' +
                '<td>' + file.name + '</td>' +
                '<td>' + readableSize(file.length) + '</td>' +
                '<td>' + file.lastModified + '</td>' +
                '<td><button class="btn btn-default btn-xs" onclick="downloadFile(\'' + file.relativePath + '\')">下载</button> <button class="btn btn-danger btn-xs" data-filename="' + file.relativePath + '" data-toggle="modal" data-target="#confirmModal">删除</button> ' + dropdown + '</td>'
            ).appendTo('.table');
        });
    }, 'json');
}

function list(dn) {
    if (dn !== undefined) dir = dn;
    $('#form .btn').slice(0, 3).removeClass('disabled');
    $('#form .btn').slice(0, 3).prop('disabled', false);
    const dirArr = dir.split('/');
    const dispArr = dirArr.slice();
    dispArr[0] = 'Home';
    $('.breadcrumb').empty();
    const destArr = [];
    for (let i = 0; i < dirArr.length; i++) {
        destArr.push(dirArr[i]);
        const dest = destArr.join('/');
        const link = $('<li><a href="javascript:;">' + dispArr[i] + '</a></li>');
        if (i !== dirArr.length - 1) link.on('click', {dest: dest}, function (e) {
            dir = e.data.dest;
            list();
        });
        $('.breadcrumb').append(link);
    }
    $(".breadcrumb>li:last").text($(".breadcrumb>li:last").children().filter('a').text());
    $(".breadcrumb>li:last").addClass('active');
    $('table tr:not(:first)').remove();
    if (dir !== '') {
        dirArr.pop();
        const parent = dirArr.join('/');
        const parentLink = $('<tr><td colspan="5"><a href="javascript:;">..</a></td></tr>');
        parentLink.on('click', function () {
            dir = parent;
            list();
        });
        $('.table').append(parentLink);
    }
    getFiles('list', {param: dir});
    if (files.length === 0) {
        $('#paste').addClass('hidden');
        return;
    }
    if (files[0].substr(0, files[0].lastIndexOf('/')) === dir || dir.startsWith(files[0])) {
        $('#paste').addClass('hidden');
    } else {
        $('#paste').removeClass('hidden');
    }
}

function find() {
    const find = $('#find').val();
    if (find.trim().length === 0) return;
    $('#form .btn').slice(0, 3).addClass('disabled');
    $('#form .btn').slice(0, 3).prop('disabled', true);
    $('.breadcrumb').empty();
    $('.breadcrumb').append('<li><a href="javascript:;" onclick="list(\'\')">Home</a></li><li class="active">搜索结果</li>')
    $('table tr:not(:first)').remove();
    getFiles('find', {param: find, dir: dir});
}

function upload(action) {
    if (!hasToken()) {
        const func = function () {
            uploadFile(action);
        };
        showAuth(func);
    } else {
        uploadFile(action);
    }
}

function uploadFile(action) {
    $('#progressModal').modal('show');
    const xhr = new XMLHttpRequest();
    xhr.open('post', action);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            list();
            $('#progressModal').modal('hide');
            const data = JSON.parse(xhr.responseText);
            if (!data.success) showMessage(data.msg);
        }
    };
    xhr.upload.onprogress = function (e) {
        if (e.lengthComputable) {
            const current = e.loaded;
            const total = e.total;
            const progress = Math.round((current / total) * 100) + '%';
            $('.progress-bar').css('width', progress);
        }
    };
    xhr.upload.onerror = function () {
        $('#progressModal').modal('hide');
        showMessage('上传失败');
    };
    const formData = new FormData(document.getElementById('form'));
    formData.append('dir', dir);
    xhr.send(formData);
}

function showCreateFolderModal() {
    $('#foldername').val('');
    $('#createFolderModal').modal('show');
}

$('#createFolderModal').on('shown.bs.modal', function (e) {
    $('#foldername').focus();
});

function createFolder() {
    $.post('createFolder', {
        'folderName': $('#foldername').val(),
        'dir': dir
    }, function (data) {
        $('#createFolderModal').modal('hide');
        if (!data.success) {
            showMessage(data.msg);
            return;
        }
        list();
    });
}

function downloadFile(fn) {
    location.href = 'download?fn=' + encodeURIComponent(fn);
}

function createZip(dn) {
    location.href = 'createZip?dir=' + dn;
}

$('#confirmModal').on('show.bs.modal', function (e) {
    $('.btn-danger', this).data('filename', $(e.relatedTarget).data('filename'));
});

function deleteFile(filename) {
    $.post('delete', {
        'fn': filename
    }, function (data) {
        if (!data.success) {
            showMessage(data.msg);
            return;
        }
        list();
    });
}

$('#confirmModal').on('click', '.btn-danger', function (e) {
    if (!hasToken()) {
        const func = function () {
            deleteFile($('#confirmModal .btn-danger').data('filename'));
        };
        showAuth(func);
    } else {
        deleteFile($(this).data('filename'));
    }
    $(e.delegateTarget).modal('hide');
});

$('#renameModal').on('show.bs.modal', function (e) {
    const filename = $(e.relatedTarget).data('filename');
    $('#name').val(filename.split('/').slice(-1));
    $('.btn-primary', this).data('filename', filename);
});

$('#renameModal').on('click', '.btn-primary', function (e) {
    var fn = $(this).data('filename');
    var newname = $(this).data('filename').substr(0, $(this).data('filename').lastIndexOf('/') + 1) + $('#name').val();
    if (fn === newname) return;
    $.post('rename', {
        'fn': fn,
        'newname': newname
    }, function (data) {
        if (!data.success) {
            showMessage(data.msg);
            return;
        }
        list();
    });
    $(e.delegateTarget).modal('hide');
});

function cut(fn) {
    files.length = 0;
    files[0] = fn;
    showMessage('已剪切文件<br>' + files.join('<br>'));
}

$('#paste').on('click', function () {
    if (files.length === 0) return;
    $.post('move', {
        'fn': JSON.stringify(files),
        'dir': dir
    }, function (data) {
        if (!data.success) {
            showMessage(data.msg);
            return;
        }
        files.length = 0;
        list();
    });
});

$('#multiSelect').on('click', function () {
    $(this).addClass('hidden');
    $('#hidden').removeClass('hidden');
    $('#hidden .btn').slice(0, 3).addClass('disabled');
    $('#hidden .btn').slice(0, 3).prop('disabled', true);
    const selectAll = $('<input type="checkbox" name="selectAll">');
    $('table tr th:first-child').html(selectAll);
    selectAll.on('click', function () {
        if (this.checked) {
            $('input[name=checkbox]:not(:checked)').trigger('click');
        } else {
            $('input[name=checkbox]:checked').trigger('click');
        }
    });
    for (let i = 0; i < $('table tr[data-name]').length; i++) {
        $('table tr[data-name]').eq(i).find('td:first-child').html('<input type="checkbox" name="checkbox" value="' + $('table tr[data-name]').eq(i).data('name') + '">');
    }
    $('table tr th:first-child').on('click', function (e) {
        const checkbox = $(this).parent().find('input[name=selectAll]');
        if (e.target !== checkbox.get(0)) checkbox.trigger('click');
    });
    $('table tr[data-name] td:first-child').on('click', function (e) {
        const checkbox = $(this).parent().find('input[name=checkbox]');
        if (e.target !== checkbox.get(0)) checkbox.trigger('click');
    })
});

$('#downloadSelected').on('click', function () {
    const checked = $('input[name="checkbox"]:checked');
    if (checked.length === 0) return;
    const files = new Array();
    checked.each(function () {
        files.push($(this).val());
    });
    location.href = '/multiDownload?files=' + encodeURIComponent(JSON.stringify(files));
});

$('#deleteSelected').on('click', function () {
    if (!hasToken()) {
        const func = function () {
            deleteSelected();
        };
        showAuth(func);
    } else {
        deleteSelected();
    }
});

function deleteSelected() {
    const checked = $('input[name="checkbox"]:checked');
    if (checked.length === 0) return;
    const files = new Array();
    checked.each(function () {
        files.push($(this).val());
    });
    const confirmModal = $('#confirmModal').clone();
    confirmModal.modal('show');
    confirmModal.on('click', '.btn-danger', function (e) {
        $.post('multiDelete', {'files': JSON.stringify(files)}, function (data) {
            if (!data.success) {
                showMessage(data.msg);
                return;
            }
            list();
        });
        $(e.delegateTarget).modal('hide');
    });
}

$('#cutSelected').on('click', function () {
    files.length = 0;
    const checked = $('input[name="checkbox"]:checked');
    if (checked.length === 0) return;
    checked.each(function () {
        files.push($(this).val());
    });
    showMessage('已剪切文件<br>' + files.join('<br>'));
});

$('#cancel').on('click', function () {
    $('#multiSelect').removeClass('hidden');
    $('#hidden').addClass('hidden');
    $('table tr th:first-child').html('序号');
    for (let i = 0; i < $('table tr[data-name]').length; i++) {
        $('table tr[data-name]').eq(i).find('td:first-child').text(i + 1);
    }
    $('table tr[data-name] td:first-child').off('click');
    $('table tr th:first-child').off('click');
});

$('body').on('click', "input[type=checkbox]", function () {
    const all = $('input[name=checkbox]');
    const checked = $('input[name=checkbox]:checked');
    const selectAll = $('input[name=selectAll]').get(0);
    if (checked.length === 0) {
        selectAll.checked = false;
        if ('indeterminate' in selectAll) {
            selectAll.indeterminate = false;
        }
    } else if (checked.length === all.length) {
        selectAll.checked = true;
        if ('indeterminate' in selectAll) {
            selectAll.indeterminate = false;
        }
    } else {
        selectAll.checked = true;
        if ('indeterminate' in selectAll) {
            selectAll.indeterminate = true;
        }
    }
    if (checked.length) {
        $('#hidden .btn').slice(0, 3).removeClass('disabled');
    } else {
        $('#hidden .btn').slice(0, 3).addClass('disabled');
    }
    $('#hidden .btn').slice(0, 3).prop('disabled', !checked.length);
});

function preview(fn, type) {
    if (type === 'image') {
        const image = $('<img class="image">');
        image.attr('src', '/previewImage?fn=' + encodeURIComponent(fn));
        $('#previewModal').html(image);
        $('#previewModal').modal('show');
    }
    if (type === 'text') {
        $.post('previewText', {fn: fn}, function (data) {
            if (data.length === 0) {
                showMessage('文件为空');
                return;
            }
            const text = $('<div class="text"></div>');
            text.text(data);
            $('#previewModal').html(text);
            $('#previewModal').modal('show');
        })
    }
}

function refresh() {
    files.length = 0;
    list();
}

function readableSize(bytes) {
    if (bytes === 0) {
        return '0.00 B';
    }
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + ['B', 'KB', 'MB', 'GB'][i];
}

function showMessage(message) {
    $('#messageModal .modal-body').html(message);
    $('#messageModal').modal('show');
}

function auth() {
    return new Promise(function (resolve, reject) {
        $.post('/getToken', {
            'password': $('#password').val()
        }, function (data) {
            $('#authModal').modal('hide');
            if (!data.success) {
                showMessage(data.msg);
                reject();
            } else {
                resolve();
            }
        })
    })
}

function hasToken() {
    return !!Cookies.get('token');
}

function showAuth(func) {
    $('#authModal').modal('show');
    $('#authModal .btn-ok').off('click');
    $('#authModal .btn-ok').on('click', function () {
        auth().then(function () {
            func();
        }).catch(function () {
        });
    })
}