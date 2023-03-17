import{_,a as m,c,w as v,f,b as e,F as g,r as b,n as w,i as n,o as h,t as p,p as y,q as F}from"./index-51afd596.js";const k={data(){return{files:[],active:!1}},methods:{addFile(s){this.active=!1,[...s.dataTransfer.items].forEach(o=>{var a=o.webkitGetAsEntry();a&&this.traverseFileTree(a)})},traverseFileTree(s,t){if(t=t||"",s.isFile)s.file(a=>{this.files.push(a)});else if(s.isDirectory){var o=s.createReader();o.readEntries(a=>{for(var r=0;r<a.length;r++)this.traverseFileTree(a[r],t+s.name+"/")})}},removeFile(s){this.files=this.files.filter(t=>t!=s)},upload(){const s=new Modal(this.$root.$refs.progressModal);s.show();const t=new FormData;t.append("currentDirectory","/public/"),this.files.forEach(o=>{t.append("files",o)}),m({method:"post",url:"file/upload",data:t,headers:{"Content-Type":"multipart/form-data"},onUploadProgress:o=>{const a=o.loaded,r=o.total;this.$root.progress=Math.round(a/r*100)+"%"}}).then(o=>{o.success?(s.hide(),this.files=[],this.$root.showModal("成功","上传成功"),this.$root.progress=0):(s.hide(),this.$root.showModal("失败",o.msg),this.$root.progress=0)}).catch(o=>{s.hide(),this.$root.showModal("错误",o.message),this.$root.progress=0})}}},d=s=>(y("data-v-0e3ae284"),s=s(),F(),s),D={class:"drag"},M=d(()=>e("p",{class:"title"},"未选择文件/文件夹",-1)),$=d(()=>e("p",{class:"subtile"},"支持拖拽到此区域上传，支持选择多个文件/文件夹",-1)),x=[M,$],C={class:"table-wrapper"},E={class:"table table-sm"},S=d(()=>e("thead",null,[e("tr",null,[e("th",null,"#"),e("th",null,"文件名"),e("th",null,"大小"),e("th",null,"操作")])],-1)),T=["onClick"],B=d(()=>e("i",{class:"bi bi-trash"},null,-1)),I=[B],P={class:"text-center"};function R(s,t,o,a,r,i){return h(),c("div",{class:w(["wrapper",{active:r.active}]),onDrop:t[1]||(t[1]=n((...l)=>i.addFile&&i.addFile(...l),["prevent"])),onDragenter:t[2]||(t[2]=n(l=>r.active=!0,["prevent"])),onDragover:t[3]||(t[3]=n(l=>r.active=!0,["prevent"])),onDragleave:t[4]||(t[4]=n(l=>r.active=!1,["prevent"]))},[v(e("div",D,x,512),[[f,r.files.length==0]]),v(e("div",null,[e("div",C,[e("table",E,[S,e("tbody",null,[(h(!0),c(g,null,b(r.files,(l,u)=>(h(),c("tr",{key:u},[e("td",null,p(u+1),1),e("td",null,[e("i",null,p(l.webkitRelativePath?l.webkitRelativePath:l.name),1)]),e("td",null,p(s.$root.formatBytes(l.size)),1),e("td",null,[e("a",{class:"link-danger",onClick:U=>i.removeFile(l)},I,8,T)])]))),128))])])]),e("div",P,[e("button",{class:"btn btn-sm btn-outline-primary mt-2",onClick:t[0]||(t[0]=(...l)=>i.upload&&i.upload(...l))},"上传文件")])],512),[[f,r.files.length!=0]])],34)}const q=_(k,[["render",R],["__scopeId","data-v-0e3ae284"]]);export{q as default};
