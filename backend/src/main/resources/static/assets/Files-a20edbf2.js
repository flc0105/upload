import{_ as P,a as h,l as m,c,b as o,F as v,r as C,d,w as y,v as S,e as z,o as a,n as M,t as p,f as F,g as D,h as x,p as I,i as N}from"./index-754c879a.js";import"./FileSaver.min-aa3b8c56.js";let k;const $=h.CancelToken,B={data(){return{currentDirectory:"/",directories:[],files:[],filter:"",multiSelect:!1,checkedFiles:[],cutFiles:[],columns:["size","lastModified"]}},methods:{list(){this.$root.loading=!0,this.files=[],this.checkedFiles=[],k!==void 0&&k(),h.post("file/list",m.stringify({currentDirectory:this.currentDirectory}),{cancelToken:new $(function(e){k=e})}).then(s=>{if(s.success)this.files=s.detail;else{if(s.msg==="没有权限"&&!this.$root.hasToken(()=>this.list()))return;this.$root.showModal("失败",s.msg)}}).catch(s=>{this.$root.showModal("错误",s.message)}).finally(()=>{this.$root.loading=!1})},search(){if(this.filter.trim().length===0){this.list();return}this.$root.loading=!0,this.files=[],this.checkedFiles=[],k!==void 0&&k(),h.post("file/search",m.stringify({filter:this.filter,currentDirectory:this.currentDirectory}),{cancelToken:new $(function(e){k=e})}).then(s=>{s.success?this.files=s.detail:this.$root.showModal("失败",s.msg)}).catch(s=>{s.name!=="CanceledError"&&this.$root.showModal("错误",s.message)}).finally(()=>{this.$root.loading=!1})},changeDirectory(s){this.currentDirectory=s,this.list(),this.getDirectoryHierachy()},getDirectoryHierachy(){this.directories=[];const s=this.currentDirectory.split("/"),e=[];for(let r=0;r<s.length;r++){e.push(s[r]);const f=e.join("/");this.directories.push({displayName:s[r],relativePath:f.length===0?"/":f})}this.directories[0].displayName="Home"},getParentDirectory(s){let e=s.split("/");e.pop();let r=e.join("/");return r.length===0?"/":r},upload(s){if(!this.$root.hasToken(()=>this.upload(s)))return;this.$root.message.title="上传进度";const e=new Modal(this.$root.$refs.progressModal);e.show();const r=new FormData;r.append("currentDirectory",this.currentDirectory),Array.from(s.target.files).forEach(t=>{console.log(t),r.append("files",t)});var i=new Date().getTime(),l=0;h({method:"post",url:"file/upload",data:r,headers:{"Content-Type":"multipart/form-data"},onUploadProgress:t=>{const n=t.loaded,g=t.total;this.$root.progress=Math.round(n/g*100)+"%";var _=new Date().getTime(),b=n-l,w=(_-i)/1e3,T=w?b/w:0;l=n,i=_,this.$root.speed=this.$root.formatBytes(T)+"/s"}}).then(t=>{t.success?(e.hide(),this.list(),this.$root.showModal("成功","上传成功"),this.$root.progress=0):(e.hide(),this.$root.showModal("失败",t.msg),this.$root.progress=0)}).catch(t=>{e.hide(),this.$root.showModal("错误",t.message),this.$root.progress=0})},createDirectory(){const s=this.$root.$refs.input.value;if(s.trim().length===0){this.$root.showModal("提示","文件夹名不能为空");return}this.$root.hasToken(()=>this.createDirectory())&&(this.$root.loading=!0,h.post("file/mkdir",m.stringify({relativePath:this.currentDirectory+"/"+s})).then(e=>{e.success?(this.list(),this.$root.showModal("成功","新建文件夹成功")):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1}))},download(s,e){this.$root.message.title="正在下载";const r=new Modal(this.$root.$refs.progressModal);r.show();var f=new Date().getTime(),i=0;h({method:"post",url:h.defaults.baseURL+e,data:m.stringify({relativePath:s}),responseType:"blob",onDownloadProgress:l=>{const t=l.loaded,n=l.total;this.$root.progress=Math.round(t*100/n)+"%";var g=new Date().getTime(),_=t-i,b=(g-f)/1e3,w=b?_/b:0;i=t,f=g,this.$root.speed=this.$root.formatBytes(w)+"/s"}}).then(l=>{let t=l.headers["content-disposition"].split("filename=")[1];t=decodeURIComponent(t),saveAs(l.data,t)}).catch(l=>{r.hide(),this.$root.showModal("错误",l.message)}).finally(()=>{this.$root.progress=0,r.hide()})},downloadFile(s){this.download(s,"file/download")},downloadFolder(s){this.download(s,"file/downloadFolder")},bulkDownload(){this.download(JSON.stringify(this.checkedFiles),"file/bulk")},deleteFile(s){this.$root.hasToken(()=>this.deleteFile(s))&&(this.$root.loading=!0,h.post("file/delete",m.stringify({relativePath:JSON.stringify(s)})).then(e=>{e.success?(this.filter.length===0?this.list():this.search(this.filter),this.$root.showModal("成功","删除成功")):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1}))},zipFolder(s){this.$root.loading=!0,h.post("file/zip",m.stringify({relativePath:s})).then(e=>{e.success?(this.$root.showModal("成功","压缩成功"),this.list()):this.$root.showModal("成功","压缩失败")}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1})},bulkZip(){this.$root.loading=!0,h.post("file/bulkZip",m.stringify({relativePath:JSON.stringify(this.checkedFiles)})).then(s=>{s.success?(this.$root.showModal("成功","压缩成功"),this.list()):this.$root.showModal("成功","压缩失败")}).catch(s=>{this.$root.showModal("错误",s.message)}).finally(()=>{this.$root.loading=!1})},rename(s){const e=this.$root.$refs.input.value;if(e.trim().length===0){this.$root.showModal("提示","新文件名不能为空");return}this.$root.hasToken(()=>this.rename(s))&&(this.$root.loading=!0,h.post("file/rename",m.stringify({src:s,dst:this.currentDirectory+"/"+e})).then(r=>{r.success?(this.$root.showModal("成功",r.msg),this.list()):this.$root.showModal("失败",r.msg)}).catch(r=>{this.$root.showModal("错误",r.message)}).finally(()=>{this.$root.loading=!1}))},cut(s){this.cutFiles=s,this.$root.showModal("剪切成功",this.cutFiles.join(`
`))},paste(){this.cutFiles.length!==0&&this.$root.hasToken(()=>this.paste())&&(this.$root.loading=!0,h.post("file/move",m.stringify({src:JSON.stringify(this.cutFiles),dst:this.currentDirectory})).then(s=>{s.success?(this.$root.showModal("成功",s.msg),this.cutFiles=[],this.list()):(this.$root.showModal("失败",s.msg),this.cutFiles=[])}).catch(s=>{this.$root.showModal("错误",s.message),this.cutFiles=[]}).finally(()=>{this.$root.loading=!1}))},preview(s,e){s.includes("text")?(this.$root.loading=!0,this.$root.message.title=e,this.$root.message.text="",h.post("file/read",m.stringify({relativePath:e})).then(r=>{r.success?(this.$root.message.text=r.detail,new Modal(this.$root.$refs.textModal).show()):this.$root.showModal("失败",r.msg)}).catch(r=>{this.$root.showModal("错误",r.message)}).finally(()=>{this.$root.loading=!1})):s.includes("image")?(this.$root.src=h.defaults.baseURL+"file/download?relativePath="+encodeURIComponent(e),new Modal(this.$root.$refs.imageModal).show()):s.includes("video")&&(this.$root.src=h.defaults.baseURL+"file/download?relativePath="+encodeURIComponent(e),new Modal(this.$root.$refs.videoModal).show())},share(s){this.$root.loading=!0,h.post("shareCode/add",m.stringify({path:s})).then(e=>{e.success?this.$root.showModal("分享成功",location.protocol+"//"+location.host+"/files/"+e.detail):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1})},checkAll(s){s.target.checked?(this.files.folders.forEach(e=>{this.checkedFiles.push(e.relativePath)}),this.files.files.forEach(e=>{this.checkedFiles.push(e.relativePath)})):this.checkedFiles=[]},hideColumn(s){this.columns.includes(s)?this.columns=this.columns.filter(e=>e!==s):this.columns.push(s)},getIcon(s,e){const r=s.split(".").pop(),f={"bi-file-earmark-binary":["exe"],"bi-file-earmark-zip":["zip","7z","rar"],"bi-file-earmark-code":["py","java","c","cpp","html","js","css"],"bi-file-earmark-pdf":["pdf"],"bi-file-earmark-word":["doc","docx"],"bi-file-earmark-excel":["xls","xlsx"],"bi-file-earmark-ppt":["ppt","pptx"]};for(const[t,n]of Object.entries(f))if(n.includes(r))return t;let l={text:"bi-file-earmark-text",image:"bi-file-earmark-image",audio:"bi-file-earmark-music",video:"bi-file-earmark-play"}[e.split("/")[0]];return l!=null?l:" bi-file-earmark"}},mounted(){this.list(),this.getDirectoryHierachy()}},u=s=>(I("data-v-a71bd3a2"),s=s(),N(),s),U={class:"card border-bottom-0"},j={class:"card-header"},E={class:"breadcrumb float-start",style:{margin:"0.5rem 0"}},O={key:0},R=["onClick"],V={class:"card-body"},A={id:"form",action:"upload",method:"post",enctype:"multipart/form-data"},H={class:"files-left"},J=u(()=>o("button",{class:"btn btn-outline-primary",onclick:"document.getElementById('file').value=null; document.getElementById('file').click()"}," 上传文件 ",-1)),L=u(()=>o("button",{class:"btn btn-outline-primary ms-1 me-1",onclick:"document.getElementById('folder').value=null; document.getElementById('folder').click()"}," 上传文件夹 ",-1)),q={class:"btn-group"},Z=["disabled"],K=["disabled"],W=["disabled"],G=["disabled"],Q=["disabled"],X={class:"files-right"},Y={class:"w-100 d-flex"},ee={class:"dropdown",style:{"padding-left":"5px"}},te=u(()=>o("button",{class:"btn btn-outline-primary dropdown-toggle","data-bs-toggle":"dropdown","data-bs-auto-close":"outside"},[o("i",{class:"bi bi-grid-3x3-gap-fill"})],-1)),se={class:"dropdown-menu"},oe={class:"dropdown-item"},ie={class:"form-check"},le=["checked"],ne=u(()=>o("label",{class:"form-check-label",for:"size"},"大小",-1)),re={class:"dropdown-item"},ce={class:"form-check"},ae=["checked"],de=u(()=>o("label",{class:"form-check-label",for:"contentType"},"类型",-1)),he={class:"dropdown-item"},ue={class:"form-check"},me=["checked"],fe=u(()=>o("label",{class:"form-check-label",for:"lastModified"},"修改日期",-1)),pe={class:"mb-0 table table-hover"},ke=["checked","indeterminate","disabled"],ye=u(()=>o("th",{class:"filename text-truncate"},"文件名",-1)),ge={key:1,class:"size"},_e={key:2,class:"contentType"},be={key:3,class:"lastModified"},we=u(()=>o("th",{class:"action"},"操作",-1)),ve={key:0},Ce=u(()=>o("a",{class:"link-primary"},"..",-1)),Me=[Ce],Fe=["value"],De=["onClick"],xe=u(()=>o("i",{class:"bi bi-folder2"},null,-1)),$e={class:"link-primary"},Te={key:0,class:"size"},Pe={key:1,class:"contentType"},Se={key:2,class:"lastModified"},ze={class:"action"},Ie=["onClick"],Ne=u(()=>o("i",{class:"bi bi-cloud-download"},null,-1)),Be=[Ne],Ue=["onClick"],je=u(()=>o("i",{class:"bi bi-trash"},null,-1)),Ee=[je],Oe={class:"dropdown d-inline"},Re=u(()=>o("a",{class:"link-primary ms-1","data-bs-toggle":"dropdown","aria-expanded":"false"},[o("i",{class:"bi bi-three-dots"})],-1)),Ve={class:"dropdown-menu"},Ae=["onClick"],He=["onClick"],Je=["onClick"],Le=["value"],qe={class:"filename text-truncate"},Ze={key:0,class:"size"},Ke={key:1,class:"contentType"},We={key:2,class:"lastModified"},Ge={class:"action"},Qe=["onClick"],Xe=u(()=>o("i",{class:"bi bi-cloud-download"},null,-1)),Ye=[Xe],et=["onClick"],tt=u(()=>o("i",{class:"bi bi-trash"},null,-1)),st=[tt],ot={class:"dropdown d-inline"},it=u(()=>o("a",{class:"link-primary ms-1","data-bs-toggle":"dropdown","aria-expanded":"false"},[o("i",{class:"bi bi-three-dots"})],-1)),lt={class:"dropdown-menu"},nt=["onClick"],rt=["onClick"],ct=["onClick"],at=["onClick"];function dt(s,e,r,f,i,l){return a(),c("div",U,[o("div",j,[o("ol",E,[(a(!0),c(v,null,C(i.directories,t=>(a(),c("li",{class:M(["filename text-truncate",["breadcrumb-item",{active:t.relativePath==i.currentDirectory}]]),key:t},[t.relativePath==i.currentDirectory?(a(),c("a",O,p(t.displayName),1)):(a(),c("a",{key:1,class:"link-primary",onClick:n=>l.changeDirectory(t.relativePath)},p(t.displayName),9,R))],2))),128))])]),o("div",V,[o("form",A,[o("input",{type:"file",name:"file",id:"file",class:"d-none",onChange:e[0]||(e[0]=t=>l.upload(t)),multiple:""},null,32),o("input",{type:"file",name:"folder",id:"folder",class:"d-none",onChange:e[1]||(e[1]=t=>l.upload(t)),multiple:"",webkitdirectory:""},null,32)]),o("div",H,[J,L,o("button",{class:"btn btn-outline-primary me-1",onClick:e[2]||(e[2]=t=>s.$root.showInput("新建文件夹","输入文件夹名",l.createDirectory))}," 新建文件夹 "),o("button",{class:"btn btn-outline-primary me-1",onClick:e[3]||(e[3]=t=>l.list())}," 刷新 "),i.cutFiles.length!==0&&l.getParentDirectory(i.cutFiles[0])!==i.currentDirectory&&!i.currentDirectory.startsWith(i.cutFiles[0])?(a(),c("button",{key:0,class:"btn btn-outline-primary me-1",onClick:e[4]||(e[4]=t=>l.paste())}," 粘贴 ")):d("",!0),o("div",q,[o("button",{class:"btn btn-outline-primary",disabled:i.files.length===0,onClick:e[5]||(e[5]=t=>{i.multiSelect=!i.multiSelect,i.checkedFiles=[]})}," 多选 ",8,Z),i.multiSelect?(a(),c("button",{key:0,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[6]||(e[6]=t=>l.bulkDownload())}," 下载 ",8,K)):d("",!0),i.multiSelect?(a(),c("button",{key:1,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[7]||(e[7]=t=>s.$root.showConfirm(function(){l.deleteFile(i.checkedFiles)}))}," 删除 ",8,W)):d("",!0),i.multiSelect?(a(),c("button",{key:2,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[8]||(e[8]=t=>l.cut(i.checkedFiles))}," 剪切 ",8,G)):d("",!0),i.multiSelect?(a(),c("button",{key:3,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[9]||(e[9]=t=>l.bulkZip())}," 打包 ",8,Q)):d("",!0)])]),o("div",X,[o("div",Y,[y(o("input",{class:"form-control",style:{flex:"1 1 auto",width:"1%"},placeholder:"搜索","onUpdate:modelValue":e[10]||(e[10]=t=>i.filter=t),onKeyup:e[11]||(e[11]=z(t=>l.search(),["enter"]))},null,544),[[S,i.filter]]),o("div",ee,[te,o("ul",se,[o("li",oe,[o("div",ie,[o("input",{class:"form-check-input",type:"checkbox",id:"size",checked:i.columns.includes("size"),onClick:e[12]||(e[12]=t=>l.hideColumn("size"))},null,8,le),ne])]),o("li",re,[o("div",ce,[o("input",{class:"form-check-input",type:"checkbox",id:"contentType",checked:i.columns.includes("contentType"),onClick:e[13]||(e[13]=t=>l.hideColumn("contentType"))},null,8,ae),de])]),o("li",he,[o("div",ue,[o("input",{class:"form-check-input",type:"checkbox",id:"lastModified",checked:i.columns.includes("lastModified"),onClick:e[14]||(e[14]=t=>l.hideColumn("lastModified"))},null,8,me),fe])])])])])])]),o("table",pe,[o("tbody",null,[o("tr",null,[i.multiSelect?(a(),c("th",{key:0,class:"checkbox",onClick:e[16]||(e[16]=t=>t.target===t.currentTarget&&t.target.querySelector(".form-check-input").click())},[o("input",{type:"checkbox",class:"form-check-input",onClick:e[15]||(e[15]=t=>l.checkAll(t)),checked:Object.keys(i.files).length>0&&i.checkedFiles.length>0&&i.files.folders.length+i.files.files.length>0,indeterminate:Object.keys(i.files).length>0&&i.checkedFiles.length>0&&i.checkedFiles.length<i.files.folders.length+i.files.files.length,disabled:Object.keys(i.files).length>0&&i.files.folders.length+i.files.files.length==0},null,8,ke)])):d("",!0),ye,i.columns.includes("size")?(a(),c("th",ge,"大小")):d("",!0),i.columns.includes("contentType")?(a(),c("th",_e,"类型")):d("",!0),i.columns.includes("lastModified")?(a(),c("th",be,"修改时间")):d("",!0),we]),i.currentDirectory!="/"?(a(),c("tr",ve,[o("td",{colspan:"6",onClick:e[17]||(e[17]=t=>l.changeDirectory(l.getParentDirectory(i.currentDirectory)))},Me)])):d("",!0),(a(!0),c(v,null,C(i.files.folders,t=>(a(),c("tr",{key:t},[y(o("td",{class:"checkbox",onClick:e[19]||(e[19]=n=>n.target===n.currentTarget&&n.target.querySelector(".form-check-input").click())},[y(o("input",{type:"checkbox",class:"form-check-input",value:t.relativePath,"onUpdate:modelValue":e[18]||(e[18]=n=>i.checkedFiles=n)},null,8,Fe),[[D,i.checkedFiles]])],512),[[F,i.multiSelect]]),o("td",{class:"filename text-truncate",onClick:n=>l.changeDirectory(t.relativePath)},[xe,x("  "),o("a",$e,p(t.name),1)],8,De),i.columns.includes("size")?(a(),c("td",Te,"-")):d("",!0),i.columns.includes("contentType")?(a(),c("td",Pe,"-")):d("",!0),i.columns.includes("lastModified")?(a(),c("td",Se,p(t.lastModified),1)):d("",!0),o("td",ze,[o("a",{class:"link-primary",onClick:n=>l.downloadFolder(t.relativePath)},Be,8,Ie),o("a",{class:"link-danger ms-1",onClick:n=>s.$root.showConfirm(function(){l.deleteFile([t.relativePath])})},Ee,8,Ue),o("div",Oe,[Re,o("ul",Ve,[o("li",null,[o("a",{class:"dropdown-item",onClick:n=>l.zipFolder(t.relativePath)},"压缩",8,Ae)]),o("li",null,[o("a",{class:"dropdown-item",onClick:n=>l.cut([t.relativePath])},"剪切",8,He)]),o("li",null,[o("a",{class:"dropdown-item",onClick:n=>s.$root.showInput("重命名","输入新文件名",function(){l.rename(t.relativePath)})},"重命名",8,Je)])])])])]))),128)),(a(!0),c(v,null,C(i.files.files,t=>(a(),c("tr",{key:t},[y(o("td",{class:"checkbox",onClick:e[21]||(e[21]=n=>n.target===n.currentTarget&&n.target.querySelector(".form-check-input").click())},[y(o("input",{type:"checkbox",class:"form-check-input",value:t.relativePath,"onUpdate:modelValue":e[20]||(e[20]=n=>i.checkedFiles=n)},null,8,Le),[[D,i.checkedFiles]])],512),[[F,i.multiSelect]]),o("td",qe,[o("i",{class:M(["bi",l.getIcon(t.name,t.fileType)])},null,2),x("  "+p(t.name),1)]),i.columns.includes("size")?(a(),c("td",Ze,p(s.$root.formatBytes(t.length)),1)):d("",!0),i.columns.includes("contentType")?(a(),c("td",Ke,p(t.fileType),1)):d("",!0),i.columns.includes("lastModified")?(a(),c("td",We,p(t.lastModified),1)):d("",!0),o("td",Ge,[o("a",{class:"link-primary",onClick:n=>l.downloadFile(t.relativePath)},Ye,8,Qe),o("a",{class:"link-danger ms-1",onClick:n=>s.$root.showConfirm(function(){l.deleteFile([t.relativePath])})},st,8,et),o("div",ot,[it,o("ul",lt,[o("li",null,[t.fileType.includes("image")||t.fileType.includes("text")||t.fileType.includes("video")?(a(),c("a",{key:0,class:"dropdown-item",onClick:n=>l.preview(t.fileType,t.relativePath)},"预览",8,nt)):d("",!0)]),o("li",null,[o("a",{class:"dropdown-item",onClick:n=>l.share(t.relativePath)},"分享",8,rt)]),o("li",null,[o("a",{class:"dropdown-item",onClick:n=>l.cut([t.relativePath])},"剪切",8,ct)]),o("li",null,[o("a",{class:"dropdown-item",onClick:n=>s.$root.showInput("重命名","输入新文件名",function(){l.rename(t.relativePath)})},"重命名",8,at)])])])])]))),128))])])])}const mt=P(B,[["render",dt],["__scopeId","data-v-a71bd3a2"]]);export{mt as default};