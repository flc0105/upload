import{_ as M,a as h,l as m,c,b as t,F as y,r as g,d,w as p,v as F,e as x,o as a,n as _,t as f,f as b,g as w,h as v}from"./index-471c8892.js";import"./bootstrap.bundle-35fa0768.js";let k;const C=h.CancelToken,D={data(){return{currentDirectory:"/",directories:[],files:[],filter:"",multiSelect:!1,checkedFiles:[],cutFiles:[],columns:["size","lastModified"]}},methods:{list(){this.$root.loading=!0,this.files=[],this.checkedFiles=[],k!==void 0&&k(),h.post("file/list",m.stringify({currentDirectory:this.currentDirectory}),{cancelToken:new C(function(e){k=e})}).then(s=>{if(s.success)this.files=s.detail;else{if(s.msg==="没有权限"&&!this.$root.hasToken(()=>this.list()))return;this.$root.showModal("失败",s.msg)}}).catch(s=>{this.$root.showModal("错误",s.message)}).finally(()=>{this.$root.loading=!1})},search(){if(this.filter.trim().length===0){this.list();return}this.$root.loading=!0,this.files=[],this.checkedFiles=[],k!==void 0&&k(),h.post("file/search",m.stringify({filter:this.filter,currentDirectory:this.currentDirectory}),{cancelToken:new C(function(e){k=e})}).then(s=>{s.success?this.files=s.detail:this.$root.showModal("失败",s.msg)}).catch(s=>{s.name!=="CanceledError"&&this.$root.showModal("错误",s.message)}).finally(()=>{this.$root.loading=!1})},changeDirectory(s){this.currentDirectory=s,this.list(),this.getDirectoryHierachy()},getDirectoryHierachy(){this.directories=[];const s=this.currentDirectory.split("/"),e=[];for(let n=0;n<s.length;n++){e.push(s[n]);const u=e.join("/");this.directories.push({displayName:s[n],relativePath:u.length===0?"/":u})}this.directories[0].displayName="Home"},getParentDirectory(s){let e=s.split("/");e.pop();let n=e.join("/");return n.length===0?"/":n},upload(s){if(!this.$root.hasToken(()=>this.upload(s)))return;const e=new Modal(this.$root.$refs.progressModal);e.show();const n=new FormData;n.append("currentDirectory",this.currentDirectory),Array.from(s.target.files).forEach(i=>{n.append("files",i)}),h({method:"post",url:"file/upload",data:n,headers:{"Content-Type":"multipart/form-data"},onUploadProgress:i=>{const l=i.loaded,o=i.total;this.$root.progress=Math.round(l/o*100)+"%"}}).then(i=>{i.success?(e.hide(),this.list(),this.$root.showModal("成功","上传成功"),this.$root.progress=0):(e.hide(),this.$root.showModal("失败",i.msg),this.$root.progress=0)}).catch(i=>{e.hide(),this.$root.showModal("错误",i.message),this.$root.progress=0})},createDirectory(){const s=this.$root.$refs.input.value;if(s.trim().length===0){this.$root.showModal("提示","文件夹名不能为空");return}this.$root.hasToken(()=>this.createDirectory())&&(this.$root.loading=!0,h.post("file/mkdir",m.stringify({relativePath:this.currentDirectory+"/"+s})).then(e=>{e.success?(this.list(),this.$root.showModal("成功","新建文件夹成功")):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1}))},download(s){location.href=h.defaults.baseURL+"file/download?relativePath="+encodeURIComponent(s)},downloadFolder(s){location.href=h.defaults.baseURL+"file/zip?relativePath="+encodeURIComponent(s)},bulkDownload(){location.href=h.defaults.baseURL+"file/bulk?relativePath="+encodeURIComponent(JSON.stringify(this.checkedFiles))},deleteFile(s){this.$root.hasToken(()=>this.deleteFile(s))&&(this.$root.loading=!0,h.post("file/delete",m.stringify({relativePath:JSON.stringify(s)})).then(e=>{e.success?(this.filter.length===0?this.list():this.search(this.filter),this.$root.showModal("成功","删除成功")):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1}))},rename(s){const e=this.$root.$refs.input.value;if(e.trim().length===0){this.$root.showModal("提示","新文件名不能为空");return}this.$root.hasToken(()=>this.rename(s))&&(this.$root.loading=!0,h.post("file/rename",m.stringify({src:s,dst:this.currentDirectory+"/"+e})).then(n=>{n.success?(this.$root.showModal("成功",n.msg),this.list()):this.$root.showModal("失败",n.msg)}).catch(n=>{this.$root.showModal("错误",n.message)}).finally(()=>{this.$root.loading=!1}))},cut(s){this.cutFiles=s,this.$root.showModal("剪切成功",this.cutFiles.join(`
`))},paste(){this.cutFiles.length!==0&&this.$root.hasToken(()=>this.paste())&&(this.$root.loading=!0,h.post("file/move",m.stringify({src:JSON.stringify(this.cutFiles),dst:this.currentDirectory})).then(s=>{s.success?(this.$root.showModal("成功",s.msg),this.cutFiles=[],this.list()):(this.$root.showModal("失败",s.msg),this.cutFiles=[])}).catch(s=>{this.$root.showModal("错误",s.message),this.cutFiles=[]}).finally(()=>{this.$root.loading=!1}))},preview(s,e){if(s.includes("image")){let n=document.getElementById("image");n.removeAttribute("src"),n.setAttribute("src",h.defaults.baseURL+"file/download?relativePath="+encodeURIComponent(e)),new Modal(this.$root.$refs.imageModal).show()}else if(s.includes("text")){let n=document.getElementById("text");this.$root.loading=!0,n.innerText="",h.post("file/read",m.stringify({relativePath:e})).then(u=>{u.success?(n.innerText=u.detail,new Modal(this.$root.$refs.textModal).show()):this.$root.showModal("失败",u.msg)}).catch(u=>{this.$root.showModal("错误",u.message)}).finally(()=>{this.$root.loading=!1})}},share(s){this.$root.loading=!0,h.post("shareCode/add",m.stringify({path:s})).then(e=>{e.success?this.$root.showModal("分享成功",location.protocol+"//"+location.host+"/files/"+e.detail):this.$root.showModal("失败",e.msg)}).catch(e=>{this.$root.showModal("错误",e.message)}).finally(()=>{this.$root.loading=!1})},checkAll(s){s.target.checked?(this.files.folders.forEach(e=>{this.checkedFiles.push(e.relativePath)}),this.files.files.forEach(e=>{this.checkedFiles.push(e.relativePath)})):this.checkedFiles=[]},hideColumn(s){this.columns.includes(s)?this.columns=this.columns.filter(e=>e!==s):this.columns.push(s)},getIcon(s,e){const n=s.split(".").pop(),u={"bi-file-earmark-binary":["exe"],"bi-file-earmark-zip":["zip","7z","rar"],"bi-file-earmark-code":["py","java","c","cpp","html","js","css"],"bi-file-earmark-pdf":["pdf"],"bi-file-earmark-word":["doc","docx"],"bi-file-earmark-excel":["xls","xlsx"],"bi-file-earmark-ppt":["ppt","pptx"]};for(const[o,r]of Object.entries(u))if(r.includes(n))return o;let l={text:"bi-file-earmark-text",image:"bi-file-earmark-image",audio:"bi-file-earmark-music",video:"bi-file-earmark-play"}[e.split("/")[0]];return l??" bi-file-earmark"}},mounted(){this.list(),this.getDirectoryHierachy()}},P={class:"card border-bottom-0"},T={class:"card-header"},S={class:"breadcrumb float-start",style:{margin:"0.5rem 0"}},z={key:0},I=["onClick"],N={class:"card-body"},U={id:"form",action:"upload",method:"post",enctype:"multipart/form-data"},$={class:"files-left"},E=t("button",{class:"btn btn-outline-primary",onclick:"document.getElementById('file').value=null; document.getElementById('file').click()"}," 上传文件 ",-1),B=t("button",{class:"btn btn-outline-primary ms-1",onclick:"document.getElementById('folder').value=null; document.getElementById('folder').click()"}," 上传文件夹 ",-1),j={class:"btn-group"},R=["disabled"],O=["disabled"],V=["disabled"],A=["disabled"],L={class:"files-right"},H={class:"w-100 d-flex"},q={class:"dropdown",style:{"padding-left":"5px"}},J=t("button",{class:"btn btn-outline-primary dropdown-toggle","data-bs-toggle":"dropdown","data-bs-auto-close":"outside"},[t("i",{class:"bi bi-grid-3x3-gap-fill"})],-1),K={class:"dropdown-menu"},W={class:"dropdown-item"},G={class:"form-check"},Q=["checked"],X=t("label",{class:"form-check-label",for:"size"},"大小",-1),Y={class:"dropdown-item"},Z={class:"form-check"},ee=["checked"],te=t("label",{class:"form-check-label",for:"contentType"},"类型",-1),se={class:"dropdown-item"},ie={class:"form-check"},oe=["checked"],le=t("label",{class:"form-check-label",for:"lastModified"},"修改日期",-1),ne={class:"mb-0 table table-hover"},re=["checked","indeterminate","disabled"],ce=t("th",{class:"filename text-truncate"},"文件名",-1),ae={key:1,class:"size"},de={key:2,class:"contentType"},he={key:3,class:"lastModified"},ue=t("th",{class:"action"},"操作",-1),me={key:0},fe=t("a",{class:"link-primary"},"..",-1),ke=[fe],pe=["value"],ye=["onClick"],ge=t("i",{class:"bi bi-folder2"},null,-1),_e={class:"link-primary"},be={key:0,class:"size"},we={key:1,class:"contentType"},ve={key:2,class:"lastModified"},Ce={class:"action"},Me=["onClick"],Fe=t("i",{class:"bi bi-cloud-download"},null,-1),xe=[Fe],De=["onClick"],Pe=t("i",{class:"bi bi-trash"},null,-1),Te=[Pe],Se={class:"dropdown d-inline"},ze=t("a",{class:"link-primary ms-1","data-bs-toggle":"dropdown","aria-expanded":"false"},[t("i",{class:"bi bi-three-dots"})],-1),Ie={class:"dropdown-menu"},Ne=["onClick"],Ue=["onClick"],$e=["value"],Ee={class:"filename text-truncate"},Be={key:0,class:"size"},je={key:1,class:"contentType"},Re={key:2,class:"lastModified"},Oe={class:"action"},Ve=["onClick"],Ae=t("i",{class:"bi bi-cloud-download"},null,-1),Le=[Ae],He=["onClick"],qe=t("i",{class:"bi bi-trash"},null,-1),Je=[qe],Ke={class:"dropdown d-inline"},We=t("a",{class:"link-primary ms-1","data-bs-toggle":"dropdown","aria-expanded":"false"},[t("i",{class:"bi bi-three-dots"})],-1),Ge={class:"dropdown-menu"},Qe=["onClick"],Xe=["onClick"],Ye=["onClick"],Ze=["onClick"];function et(s,e,n,u,i,l){return a(),c("div",P,[t("div",T,[t("ol",S,[(a(!0),c(y,null,g(i.directories,o=>(a(),c("li",{class:_(["filename text-truncate",["breadcrumb-item",{active:o.relativePath==i.currentDirectory}]]),key:o},[o.relativePath==i.currentDirectory?(a(),c("a",z,f(o.displayName),1)):(a(),c("a",{key:1,class:"link-primary",onClick:r=>l.changeDirectory(o.relativePath)},f(o.displayName),9,I))],2))),128))])]),t("div",N,[t("form",U,[t("input",{type:"file",name:"file",id:"file",class:"d-none",onChange:e[0]||(e[0]=o=>l.upload(o)),multiple:""},null,32),t("input",{type:"file",name:"folder",id:"folder",class:"d-none",onChange:e[1]||(e[1]=o=>l.upload(o)),multiple:"",webkitdirectory:""},null,32)]),t("div",$,[E,B,t("button",{class:"btn btn-outline-primary ms-1 me-1",onClick:e[2]||(e[2]=o=>s.$root.showInput("新建文件夹","输入文件夹名",l.createDirectory))}," 新建文件夹 "),t("button",{class:"btn btn-outline-primary me-1",onClick:e[3]||(e[3]=o=>l.list())}," 刷新 "),i.cutFiles.length!==0&&l.getParentDirectory(i.cutFiles[0])!==i.currentDirectory&&!i.currentDirectory.startsWith(i.cutFiles[0])?(a(),c("button",{key:0,class:"btn btn-outline-primary me-1",onClick:e[4]||(e[4]=o=>l.paste())}," 粘贴 ")):d("",!0),t("div",j,[t("button",{class:"btn btn-outline-primary",disabled:i.files.length===0,onClick:e[5]||(e[5]=o=>{i.multiSelect=!i.multiSelect,i.checkedFiles=[]})}," 多选 ",8,R),i.multiSelect?(a(),c("button",{key:0,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[6]||(e[6]=o=>l.bulkDownload())}," 下载 ",8,O)):d("",!0),i.multiSelect?(a(),c("button",{key:1,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[7]||(e[7]=o=>s.$root.showConfirm(function(){l.deleteFile(i.checkedFiles)}))}," 删除 ",8,V)):d("",!0),i.multiSelect?(a(),c("button",{key:2,disabled:i.checkedFiles.length==0,class:"btn btn-outline-primary",onClick:e[8]||(e[8]=o=>l.cut(i.checkedFiles))}," 剪切 ",8,A)):d("",!0)])]),t("div",L,[t("div",H,[p(t("input",{class:"form-control",style:{flex:"1 1 auto",width:"1%"},placeholder:"搜索","onUpdate:modelValue":e[9]||(e[9]=o=>i.filter=o),onKeyup:e[10]||(e[10]=x(o=>l.search(),["enter"]))},null,544),[[F,i.filter]]),t("div",q,[J,t("ul",K,[t("li",W,[t("div",G,[t("input",{class:"form-check-input",type:"checkbox",id:"size",checked:i.columns.includes("size"),onClick:e[11]||(e[11]=o=>l.hideColumn("size"))},null,8,Q),X])]),t("li",Y,[t("div",Z,[t("input",{class:"form-check-input",type:"checkbox",id:"contentType",checked:i.columns.includes("contentType"),onClick:e[12]||(e[12]=o=>l.hideColumn("contentType"))},null,8,ee),te])]),t("li",se,[t("div",ie,[t("input",{class:"form-check-input",type:"checkbox",id:"lastModified",checked:i.columns.includes("lastModified"),onClick:e[13]||(e[13]=o=>l.hideColumn("lastModified"))},null,8,oe),le])])])])])])]),t("table",ne,[t("tbody",null,[t("tr",null,[i.multiSelect?(a(),c("th",{key:0,class:"checkbox",onClick:e[15]||(e[15]=o=>o.target===o.currentTarget&&o.target.querySelector(".form-check-input").click())},[t("input",{type:"checkbox",class:"form-check-input",onClick:e[14]||(e[14]=o=>l.checkAll(o)),checked:Object.keys(i.files).length>0&&i.checkedFiles.length>0&&i.files.folders.length+i.files.files.length>0,indeterminate:Object.keys(i.files).length>0&&i.checkedFiles.length>0&&i.checkedFiles.length<i.files.folders.length+i.files.files.length,disabled:Object.keys(i.files).length>0&&i.files.folders.length+i.files.files.length==0},null,8,re)])):d("",!0),ce,i.columns.includes("size")?(a(),c("th",ae,"大小")):d("",!0),i.columns.includes("contentType")?(a(),c("th",de,"类型")):d("",!0),i.columns.includes("lastModified")?(a(),c("th",he,"修改时间")):d("",!0),ue]),i.currentDirectory!="/"?(a(),c("tr",me,[t("td",{colspan:"6",onClick:e[16]||(e[16]=o=>l.changeDirectory(l.getParentDirectory(i.currentDirectory)))},ke)])):d("",!0),(a(!0),c(y,null,g(i.files.folders,o=>(a(),c("tr",{key:o},[p(t("td",{class:"checkbox",onClick:e[18]||(e[18]=r=>r.target===r.currentTarget&&r.target.querySelector(".form-check-input").click())},[p(t("input",{type:"checkbox",class:"form-check-input",value:o.relativePath,"onUpdate:modelValue":e[17]||(e[17]=r=>i.checkedFiles=r)},null,8,pe),[[w,i.checkedFiles]])],512),[[b,i.multiSelect]]),t("td",{class:"filename text-truncate",onClick:r=>l.changeDirectory(o.relativePath)},[ge,v("  "),t("a",_e,f(o.name),1)],8,ye),i.columns.includes("size")?(a(),c("td",be,"-")):d("",!0),i.columns.includes("contentType")?(a(),c("td",we,"-")):d("",!0),i.columns.includes("lastModified")?(a(),c("td",ve,f(o.lastModified),1)):d("",!0),t("td",Ce,[t("a",{class:"link-primary",onClick:r=>l.downloadFolder(o.relativePath)},xe,8,Me),t("a",{class:"link-danger ms-1",onClick:r=>s.$root.showConfirm(function(){l.deleteFile([o.relativePath])})},Te,8,De),t("div",Se,[ze,t("ul",Ie,[t("li",null,[t("a",{class:"dropdown-item",onClick:r=>l.cut([o.relativePath])},"剪切",8,Ne)]),t("li",null,[t("a",{class:"dropdown-item",onClick:r=>s.$root.showInput("重命名","输入新文件名",function(){l.rename(o.relativePath)})},"重命名",8,Ue)])])])])]))),128)),(a(!0),c(y,null,g(i.files.files,o=>(a(),c("tr",{key:o},[p(t("td",{class:"checkbox",onClick:e[20]||(e[20]=r=>r.target===r.currentTarget&&r.target.querySelector(".form-check-input").click())},[p(t("input",{type:"checkbox",class:"form-check-input",value:o.relativePath,"onUpdate:modelValue":e[19]||(e[19]=r=>i.checkedFiles=r)},null,8,$e),[[w,i.checkedFiles]])],512),[[b,i.multiSelect]]),t("td",Ee,[t("i",{class:_(["bi",l.getIcon(o.name,o.fileType)])},null,2),v("  "+f(o.name),1)]),i.columns.includes("size")?(a(),c("td",Be,f(s.$root.formatBytes(o.length)),1)):d("",!0),i.columns.includes("contentType")?(a(),c("td",je,f(o.fileType),1)):d("",!0),i.columns.includes("lastModified")?(a(),c("td",Re,f(o.lastModified),1)):d("",!0),t("td",Oe,[t("a",{class:"link-primary",onClick:r=>l.download(o.relativePath)},Le,8,Ve),t("a",{class:"link-danger ms-1",onClick:r=>s.$root.showConfirm(function(){l.deleteFile([o.relativePath])})},Je,8,He),t("div",Ke,[We,t("ul",Ge,[t("li",null,[o.fileType.includes("image")||o.fileType.includes("text")?(a(),c("a",{key:0,class:"dropdown-item",onClick:r=>l.preview(o.fileType,o.relativePath)},"预览",8,Qe)):d("",!0)]),t("li",null,[t("a",{class:"dropdown-item",onClick:r=>l.share(o.relativePath)},"分享",8,Xe)]),t("li",null,[t("a",{class:"dropdown-item",onClick:r=>l.cut([o.relativePath])},"剪切",8,Ye)]),t("li",null,[t("a",{class:"dropdown-item",onClick:r=>s.$root.showInput("重命名","输入新文件名",function(){l.rename(o.relativePath)})},"重命名",8,Ze)])])])])]))),128))])])])}const it=M(D,[["render",et]]);export{it as default};
