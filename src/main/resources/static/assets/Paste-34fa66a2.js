import{_ as u,k as f,a as n,l as d,c as r,b as s,h,t as c,d as g,w as p,g as b,m as x,v as k,q as _,o as a,p as y,i as v}from"./index-bd898a27.js";import"./FileSaver.min-8708b00c.js";import{h as w}from"./moment-fbc5633a.js";const $={components:{highlightjs:f.component},data(){return{title:"",text:"",checked:!1}},methods:{getFromNow(t){return w(t).fromNow()},get(t){this.title="",this.text="",this.$root.loading=!0,n.post("/paste/get",d.stringify({id:t})).then(e=>{e.success?(this.expiredDate=e.detail.expiredDate,this.title=e.detail.title,this.text=e.detail.text):this.$router.push("/pastes")}).catch(e=>{this.$root.showModal("错误",e.message),this.$router.push("/pastes")}).finally(()=>{this.$root.loading=!1})},update(){if(this.text.trim().length===0){this.$root.showModal("提示","正文不能为空");return}this.$root.hasToken(()=>this.update())&&(this.$root.loading=!0,n.post("/paste/update",d.stringify({id:this.$route.params.id,text:this.text})).then(t=>{t.success?this.$root.showModal("成功","修改成功"):this.$root.showModal("失败",t.msg)}).catch(t=>{this.$root.showModal("错误",t.message)}).finally(()=>{this.$root.loading=!1}))},remove(){this.$root.hasToken(()=>this.remove())&&(this.$root.loading=!0,n.post("/paste/delete",d.stringify({id:this.$route.params.id})).then(t=>{t.success?(this.$root.showModal("成功","删除成功"),this.$router.push("/pastes")):this.$root.showModal("失败",t.msg)}).catch(t=>{this.$root.showModal("错误",t.message)}).finally(()=>{this.$root.loading=!1}))},download(){var t=new Blob([this.text],{type:"text/plain;charset=utf-8"});saveAs(t,this.$route.params.id+".txt")}},created(){this.$route.params.id&&this.get(this.$route.params.id)}},M=t=>(y("data-v-d857052c"),t=t(),v(),t),C={class:"card"},D={class:"card-body border-bottom",style:{padding:"0.781rem 1rem"}},N={class:"d-flex justify-content-between flex-wrap align-items-center"},B={class:"paste-title text-muted text-truncate mw-50"},V={key:0,class:"text-danger",style:{"font-size":"0.875rem"}},j={key:0},I={key:1},S={class:"form-check form-switch d-inline-block align-middle me-2"},T=M(()=>s("label",{class:"form-check-label",for:"highlight",style:{"user-select":"none"}},"代码高亮",-1)),E=["data-clipboard-text"],F=["disabled"];function P(t,e,U,q,o,l){const m=_("highlightjs");return a(),r("div",C,[s("div",D,[s("div",N,[s("a",{class:"link-primary",onClick:e[0]||(e[0]=i=>t.$router.push("/pastes"))},"返回"),s("div",B,[h(c(o.title)+" ",1),t.expiredDate?(a(),r("span",V,[h("  "),t.expiredDate==-1?(a(),r("span",j,"阅后即焚")):(a(),r("span",I,"Expired "+c(l.getFromNow(t.expiredDate)),1))])):g("",!0)]),s("div",null,[s("form",S,[p(s("input",{class:"form-check-input",type:"checkbox",role:"switch",id:"highlight","onUpdate:modelValue":e[1]||(e[1]=i=>o.checked=i)},null,512),[[b,o.checked]]),T]),s("a",{class:"btn btn-outline-primary btn-sm",id:"btnCopy","data-clipboard-text":o.text},"复制",8,E),s("a",{class:"btn btn-outline-primary btn-sm ms-1",onClick:e[2]||(e[2]=i=>l.download())},"下载"),s("button",{disabled:o.checked,class:"btn btn-outline-primary btn-sm ms-1",onClick:e[3]||(e[3]=i=>l.update())},"提交修改",8,F),s("a",{class:"btn btn-outline-danger btn-sm ms-1",onClick:e[4]||(e[4]=i=>t.$root.showConfirm(()=>l.remove()))},"删除")])])]),o.checked?(a(),x(m,{key:0,autodetect:"",code:o.text,class:"mb-0 min-vh-75"},null,8,["code"])):p((a(),r("textarea",{key:1,id:"textarea",class:"form-control border-0 min-vh-75","onUpdate:modelValue":e[5]||(e[5]=i=>o.text=i)},null,512)),[[k,o.text]])])}const H=u($,[["render",P],["__scopeId","data-v-d857052c"]]);export{H as default};
