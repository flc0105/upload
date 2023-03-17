import{_,a,l as h,c as n,b as s,w as f,v as w,e as $,F as g,r as b,o as r,t as m,p as k,q as v}from"./index-51afd596.js";import"./FileSaver.min-a4a8521e.js";const y="/vite.svg";const M={data(){return{bookmarks:{},url:""}},methods:{isJSON(t){try{return JSON.parse(t)&&!!t}catch{return!1}},extractDomain(t){const{hostname:o}=new URL(t);return o},list(){this.$root.loading=!0,a.post("bookmark/list").then(t=>{t.success?this.bookmarks=t.detail:this.$root.showModal("失败",t.msg)}).catch(t=>{this.$root.showModal("错误",t.message)}).finally(()=>{this.$root.loading=!1})},add(){if(this.url.trim().length===0){this.$root.showModal("提示","URL不能为空");return}this.$root.loading=!0,a.post("bookmark/add",h.stringify({url:this.url})).then(t=>{t.success?(this.list(),this.update(t.detail)):this.$root.showModal("添加失败",t.msg)}).catch(t=>{this.$root.showModal("错误",t.message)}).finally(()=>{this.url="",this.$root.loading=!1})},remove(t){this.$root.hasToken(()=>this.remove(t))&&(this.$root.loading=!0,a.post("bookmark/delete",h.stringify({id:t})).then(o=>{o.success?(this.$root.showModal("成功","删除成功"),this.list()):this.$root.showModal("失败","删除失败")}).catch(o=>{this.$root.showModal("错误",o.message)}).finally(()=>{this.$root.loading=!1}))},rename(t){if(this.$root.$refs.input.value.trim().length===0){this.$root.showModal("提示","标题不能为空");return}this.$root.loading=!0,a.post("bookmark/rename",h.stringify({id:t,title:this.$root.$refs.input.value})).then(o=>{o.success?(this.$root.showModal("成功","修改成功"),this.list()):this.$root.showModal("失败","修改失败")}).catch(o=>{this.$root.showModal("错误",o.message)}).finally(()=>{this.$root.loading=!1})},update(t){this.$root.loading=!0,a.post("bookmark/update",h.stringify({id:t})).then(o=>{o.success?(this.$root.showModal("成功","更新成功"),this.list()):this.$root.showModal("失败","更新失败")}).catch(o=>{this.$root.showModal("错误",o.message)}).finally(()=>{this.$root.loading=!1})},importBookmarks(t){this.$root.loading=!0;var o=new FileReader;o.readAsText(t.target.files[0]),o.onload=c=>{var d=c.target.result;if(!this.isJSON(d)){this.$root.showModal("错误","不是合法的json数据"),this.$root.loading=!1;return}a.post("bookmark/bulkAdd",h.stringify({data:d})).then(i=>{i.success?(this.list(),this.$root.showModal("成功",i.msg)):this.$root.showModal("失败",i.msg)}).catch(i=>{this.$root.showModal("错误",i.message)}).finally(()=>{this.$root.loading=!1})}},exportBookmarks(){this.$root.loading=!0;var t=[];for(const[i,l]of Object.entries(this.bookmarks)){var o=new Object;o.title=l.title,o.url=l.url,t.push(o)}var c=JSON.stringify(t,null,2),d=new Blob([c],{type:"text/plain;charset=utf-8"});saveAs(d,"bookmarks.json"),this.$root.loading=!1},updateAll(){this.$root.loading=!0,a.post("bookmark/updateAll").then(t=>{t.success?(this.list(),this.$root.showModal("成功",t.msg)):this.$root.showModal("失败",t.msg)}).catch(t=>{this.$root.showModal("错误",t.message)}).finally(()=>{this.$root.loading=!1})}},mounted(){this.list()}},u=t=>(k("data-v-b110260b"),t=t(),v(),t),x={class:"input-group mb-2"},B=["disabled"],C=["disabled"],S=["disabled"],I=u(()=>s("i",{class:"bi bi-three-dots-vertical"},null,-1)),A={class:"dropdown-menu"},O=u(()=>s("li",null,[s("a",{class:"dropdown-item",onclick:"document.getElementById('file').value=null; document.getElementById('file').click()"},"导入")],-1)),N={class:"table table-hover table-borderless border shadow-sm"},j={class:"text-truncate w-45"},D=["src"],J={key:1,class:"icon",src:y},E={key:2,class:"align-middle"},F={key:3,class:"align-middle text-muted"},L={class:"text-truncate w-45 url"},R={class:"text-muted"},T={class:"text-end",style:{width:"10%"}},U={class:"dropdown d-inline"},K=u(()=>s("i",{class:"bi bi-three-dots-vertical link-primary","data-bs-toggle":"dropdown"},null,-1)),V={class:"dropdown-menu"},q=["href"],z=["data-clipboard-text"],G=u(()=>s("li",null,[s("hr",{class:"dropdown-divider"})],-1)),H=["onClick"],P=["onClick"],Q=["onClick"];function W(t,o,c,d,i,l){return r(),n(g,null,[s("input",{type:"file",name:"file",id:"file",class:"d-none",onChange:o[0]||(o[0]=e=>l.importBookmarks(e)),multiple:""},null,32),s("div",x,[f(s("input",{class:"form-control","onUpdate:modelValue":o[1]||(o[1]=e=>i.url=e),onKeyup:o[2]||(o[2]=$(e=>l.add(),["enter"])),disabled:t.$root.loading},null,40,B),[[w,i.url]]),s("button",{class:"btn btn-outline-primary",onClick:o[3]||(o[3]=e=>l.add()),disabled:t.$root.loading},"添加",8,C),s("button",{class:"btn btn-outline-primary","data-bs-toggle":"dropdown",disabled:t.$root.loading},[I,s("ul",A,[O,s("li",null,[s("a",{class:"dropdown-item",onClick:o[4]||(o[4]=e=>l.exportBookmarks())},"导出")]),s("li",null,[s("a",{class:"dropdown-item",onClick:o[5]||(o[5]=e=>l.updateAll())},"更新")])])],8,S)]),s("table",N,[s("tbody",null,[(r(!0),n(g,null,b(i.bookmarks,e=>(r(),n("tr",{key:e},[s("td",j,[e.icon?(r(),n("img",{key:0,class:"icon",src:"data:image/jpeg;base64,"+e.icon},null,8,D)):(r(),n("img",J)),e.title?(r(),n("span",E,m(e.title),1)):(r(),n("span",F,[s("i",null,m(l.extractDomain(e.url)),1)]))]),s("td",L,[s("i",R,m(e.url),1)]),s("td",T,[s("div",U,[K,s("ul",V,[s("li",null,[s("a",{class:"dropdown-item",href:e.url,target:"_blank"},"打开",8,q)]),s("li",null,[s("a",{class:"dropdown-item",id:"btnCopy","data-clipboard-text":e.url},"复制",8,z)]),G,s("li",null,[s("a",{class:"dropdown-item",onClick:p=>t.$root.showConfirm(()=>l.remove(e.id))},"删除",8,H)]),s("li",null,[s("a",{class:"dropdown-item",onClick:p=>t.$root.showInput("修改标题","输入新标题",function(){l.rename(e.id)})},"修改",8,P)]),s("li",null,[s("a",{class:"dropdown-item",onClick:p=>l.update(e.id)},"更新",8,Q)])])])])]))),128))])])],64)}const Z=_(M,[["render",W],["__scopeId","data-v-b110260b"]]);export{Z as default};
