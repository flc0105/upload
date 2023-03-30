import{_ as f,a as n,l as d,c,b as e,t as a,d as p,o as h,p as _,i as m}from"./index-6e041f03.js";const b={data(){return{file:""}},methods:{get(s){this.$root.loading=!0,n.post("/shareCode/get",d.stringify({code:s})).then(t=>{t.success?this.file=t.detail:this.$router.push("/")}).catch(t=>{this.$root.showModal("错误",t.message),this.$router.push("/")}).finally(()=>{this.$root.loading=!1})},download(s){location.href=n.defaults.baseURL+"/file/download?relativePath="+encodeURIComponent(s)},preview(s,t){s.includes("text")?(this.$root.loading=!0,this.$root.message.title=t,this.$root.message.text="",n.post("/file/read",d.stringify({relativePath:t})).then(l=>{l.success?(this.$root.message.text=l.detail,new Modal(this.$root.$refs.textModal).show()):this.$root.showModal("失败",l.msg)}).catch(l=>{this.$root.showModal("错误",l.message)}).finally(()=>{this.$root.loading=!1})):s.includes("image")&&(this.$root.src="",this.$root.src=n.defaults.baseURL+"/file/download?relativePath="+encodeURIComponent(t),new Modal(this.$root.$refs.imageModal).show())}},created(){this.$route.params.code&&this.get(this.$route.params.code)}},i=s=>(_("data-v-c7b40173"),s=s(),m(),s),g={class:"card"},v=i(()=>e("div",{class:"card-header"},"文件分享",-1)),y={class:"card-body text-center"},$={class:"table table-borderless table-sm"},w=i(()=>e("td",null,"文件名：",-1)),x=i(()=>e("td",null,"文件大小：",-1)),M=i(()=>e("td",null,"修改时间：",-1)),C=i(()=>e("td",null,"文件类型：",-1));function I(s,t,l,k,o,r){return h(),c("div",g,[v,e("div",y,[e("table",$,[e("tbody",null,[e("tr",null,[w,e("td",null,a(o.file.name),1)]),e("tr",null,[x,e("td",null,a(s.$root.formatBytes(o.file.length)),1)]),e("tr",null,[M,e("td",null,a(o.file.lastModified),1)]),e("tr",null,[C,e("td",null,a(o.file.fileType),1)])])]),e("button",{class:"btn btn-sm btn-outline-primary mt-3",onClick:t[0]||(t[0]=u=>r.download(o.file.relativePath))}," 下载 "),o.file&&(o.file.fileType.includes("image")||o.file.fileType.includes("text"))?(h(),c("button",{key:0,class:"btn btn-sm btn-outline-primary mt-3 ms-1",onClick:t[1]||(t[1]=u=>r.preview(o.file.fileType,o.file.relativePath))}," 预览 ")):p("",!0)])])}const B=f(b,[["render",I],["__scopeId","data-v-c7b40173"]]);export{B as default};
