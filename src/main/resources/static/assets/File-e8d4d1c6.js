import{_ as i,a,l as r,c,b as t,t as d,o as _,p as u,m as h}from"./index-471c8892.js";const p={data(){return{file:""}},methods:{get(e){this.$root.loading=!0,a.post("/shareCode/get",r.stringify({code:e})).then(s=>{s.success?this.file=s.detail:this.$router.push("/")}).catch(s=>{this.$root.showModal("错误",s.message),this.$router.push("/")}).finally(()=>{this.$root.loading=!1})},download(e){location.href=a.defaults.baseURL+"/file/download?relativePath="+encodeURIComponent(e)}},created(){this.$route.params.code&&this.get(this.$route.params.code)}},l=e=>(u("data-v-29d9396e"),e=e(),h(),e),f={class:"card"},m=l(()=>t("div",{class:"card-header"},"文件分享",-1)),b={class:"card-body text-center"},g={class:"table table-borderless table-sm"},v=l(()=>t("td",null,"文件名：",-1)),y=l(()=>t("td",null,"文件大小：",-1)),$=l(()=>t("td",null,"修改时间：",-1)),w=l(()=>t("td",null,"文件类型：",-1));function x(e,s,I,B,o,n){return _(),c("div",f,[m,t("div",b,[t("table",g,[t("tbody",null,[t("tr",null,[v,t("td",null,d(o.file.name),1)]),t("tr",null,[y,t("td",null,d(e.$root.formatBytes(o.file.length)),1)]),t("tr",null,[$,t("td",null,d(o.file.lastModified),1)]),t("tr",null,[w,t("td",null,d(o.file.fileType),1)])])]),t("button",{class:"btn btn-sm btn-outline-primary mt-3",onClick:s[0]||(s[0]=S=>n.download(o.file.relativePath))}," 下载 ")])])}const C=i(p,[["render",x],["__scopeId","data-v-29d9396e"]]);export{C as default};
