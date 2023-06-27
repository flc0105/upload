<template>
  <!-- 命令模态框 -->
  <div class="modal fade" tabindex="-1" ref="commandModal">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ $t("command") }}</h5>
          <button
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <div class="input-group">
              <input
                class="form-control monospace"
                :placeholder="this.$t('command')"
                ref="command"
                @keyup.enter="$refs.btnExecute.click()"
              />
              <button
                type="submit"
                class="btn btn-outline-primary"
                ref="btnExecute"
                @click="executeCommand"
              >
                {{ $t("execute") }}
              </button>
            </div>
          </div>

          <div class="mb-3">
            <textarea
              class="form-control monospace"
              rows="12"
              ref="executionResult"
            ></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import Qs from "qs";

export default {
  data() {
    return {};
  },
  provide() {
    return {
      show: this.show, // 提供 show 方法
    };
  },
  methods: {
    show() {
      this.$refs.command.value = "";
      this.$refs.executionResult.value = "";
      new Modal(this.$refs.commandModal).show();
      this.$refs.command.focus();
    },
    /**
     * 执行命令
     * 发送命令请求，并在请求完成后更新执行结果
     */
    executeCommand() {
      this.$refs.command.disabled = true;
      this.$refs.btnExecute.disabled = true;
      this.$refs.btnExecute.innerHTML =
        "<span class='spinner-grow spinner-grow-sm' role='status' aria-hidden='true'></span>";
      axios
        .post("/shell", Qs.stringify({ command: this.$refs.command.value }))
        .then((res) => {
          if (res.success) {
            this.$refs.executionResult.value = res.detail;
          } else {
            this.$refs.executionResult.value = res.msg;
          }
        })
        .catch((err) => {
          this.$refs.executionResult.value = err.message;
        })
        .finally(() => {
          this.$refs.btnExecute.innerHTML = this.$t("execute");
          this.$refs.command.disabled = false;
          this.$refs.btnExecute.disabled = false;
          this.$refs.command.focus();
          this.$refs.command.select();
        });
    },
  },
  mounted() {
    this.$refs.commandModal.addEventListener("shown.bs.modal", () => {
      this.$refs.command.focus();
    });
  },
};
</script>
