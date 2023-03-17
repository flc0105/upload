<template>
  <input class="form-control mb-2" placeholder="标题" v-model="title" />
  <textarea class="form-control mb-3" rows="10" placeholder="正文" v-model="text" @keyup.ctrl.enter="add()"></textarea>
  <div class="float-end mb-3 row g-3 align-middle">

    <div class="col-auto">
      <div class="form-check form-switch" style="padding: 0.375rem 0.75rem;">
        <input class="form-check-input" type="checkbox" role="switch" id="isPrivate" ref="isPrivate">
        <label class="form-check-label" for="isPrivate">私密</label>
      </div>
    </div>

    <div class="col-auto">
      <select class="form-select mb-2" ref="select">
        <option value="10" :selected="$root.noToken()">10分钟</option>
        <option value="60">1小时</option>
        <option value="1440">1天</option>
        <option value="10080">1周</option>
        <option value="0" :selected="!$root.noToken()">永不过期</option><!-- 只要有名为token的Cookie就默认选中永不过期，但不会去验证token -->
      </select>
    </div>

    <div class="col-auto">
      <button class="btn btn-outline-primary mb-2 col-auto" @click="add()">发布</button>
    </div>


  </div>
  <ul class="list-group clear-both">
    <li class="list-group-item list-group-item-action cursor-pointer" v-for="paste in pastes" :key="paste"
      @click="(event) => event.target.tagName !== 'A' && $router.push('/pastes/' + paste.id)">
      <div class="float-end">
        <a class="link-primary bi bi-clipboard" id="btnCopy" :data-clipboard-text="paste.text"></a>
        <a class="link-danger bi bi-trash ms-1" @click="$root.showConfirm(() => remove(paste.id))">
        </a>
      </div>
      <div>
        <p class="text-primary text-truncate mw-75">{{ paste.title }}</p>
        <p class="text-truncate mw-75">{{ paste.text }}</p>
        <p class="text-muted mb-0">发布于 {{ paste.time.slice(0, -3) }}<span class="text-danger" v-if="paste.expiredDate"
            style="font-size:0.875rem">&nbsp;&nbsp;Expired {{ getFromNow(paste.expiredDate) }}</span></p>
      </div>
    </li>
  </ul>
</template>

<script>
import axios from 'axios'
import Qs from 'qs'

import moment from 'moment'


export default {
  data() {
    return {
      pastes: [],
      title: "",
      text: "",
    };
  },
  methods: {
    getFromNow(date) {
      return moment(date).fromNow()
    },
    // 计算过期时间
    calcExpiredDate(minutes) {
      if (minutes == 0) {
        return undefined;
      }
      var dateObj = moment(new Date()).add(minutes, 'm').toDate()
      return moment(dateObj).format("YYYY-MM-DD HH:mm:ss")
    },
    // 获取文本
    list() {
      this.$root.loading = true;
      axios
        .post("/paste/list")
        .then((res) => {
          if (res.success) {
            this.pastes = res.detail;
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 添加文本
    add() {
      if (this.text.trim().length === 0) {
        this.$root.showModal("提示", "正文不能为空")
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/add",
          Qs.stringify({
            title: this.title.trim().length === 0 ? "未命名" : this.title,
            text: this.text,
            expiredDate: this.calcExpiredDate(this.$refs.select.value),
            isPrivate: this.$refs.isPrivate.checked,
          })
        )
        .then((res) => {
          if (res.success) {
            this.list();
            this.$root.showModal("发布成功", location.protocol + "//" + location.host + "/pastes/" + res.detail.id);
            this.text = ""
            this.title = ""
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
    // 删除文本
    remove(id) {
      if (!this.$root.hasToken(() => this.remove(id))) {
        return;
      }
      this.$root.loading = true;
      axios
        .post("/paste/delete", Qs.stringify({ id: id }))
        .then((res) => {
          if (res.success) {
            this.$root.showModal("成功", "删除成功");
            this.list();
          } else {
            this.$root.showModal("失败", res.msg);
          }
        })
        .catch((err) => {
          this.$root.showModal("错误", err.message);
        })
        .finally(() => {
          this.$root.loading = false;
        });
    },
  },
  created() {
    this.list();
  },
}
</script>