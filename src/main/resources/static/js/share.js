const clipboard = new ClipboardJS('#copy');
clipboard.on('success', (ev) => {
    showMessage('复制成功')
});

function list() {
    $('#textlist').empty();
    $.post('/getText', function (text) {
        for (let i = 0; i < text.length; i++) {
            const aCopy = $('<a id="copy">复制</a>');
            aCopy.attr('data-clipboard-text', text[i].data);
            const aDelete = $('<span>&nbsp;&nbsp;</span><a data-toggle="modal" data-target="#confirmModal" data-id="' + text[i].id + '">删除</a>');
            const div = $('<div class="pull-right"></div>');
            div.append(aCopy);
            div.append(aDelete);
            const li = $('<li class="list-group-item"></li>');
            li.append(div);
            li.append($('<p class="text-primary">' + text[i].author + '</p><p style="white-space: pre-wrap">' + text[i].data + '</p><p class="text-muted">发布于 ' + text[i].time + '</p>'));
            $('#textlist').append(li);
        }
    })
}

list();

$('#post').click(function () {
    if ($.trim($('#author').val()).length == 0 || $.trim($('#data').val()).length == 0) {
        showMessage('不能为空');
        return;
    }
    $.post('/addText', {
        'author': $('#author').val(),
        'data': $('#data').val()
    }, function (data) {
        if (!data.success) {
            showMessage(data.msg);
            return;
        }
        list();
        $('#author').val('');
        $('#data').val('');
    })
});

function deleteText(id) {
    $.post('/deleteText', {id: id},
        function (data) {
            if (!data.success) {
                showMessage(data.msg);
                return;
            }
            list();
        });
}

$('#confirmModal').on('show.bs.modal', function (e) {
    $('.btn-danger', this).data('id', $(e.relatedTarget).data('id'));
});

$('#confirmModal').on('click', '.btn-danger', function (e) {
    if (!hasToken()) {
        const func = function () {
            deleteText($('#confirmModal .btn-danger').data('id'));
        };
        showAuth(func);
    } else {
        deleteText($(this).data('id'));
    }
    $(e.delegateTarget).modal('hide');
});

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