System.register(["./index-legacy-463bc73f.js"],(function(t,n){"use strict";var o,e,a,s,i,c,l,d,r,u,h,f,b=document.createElement("style");return b.textContent=".code[data-v-c966c5bd]{width:10%;min-width:80px}.filePath[data-v-c966c5bd]{max-width:200px}@media screen and (max-width: 768px){.status[data-v-c966c5bd]{display:none}}\n",document.head.appendChild(b),{setters:[function(t){o=t._,e=t.a,a=t.l,s=t.c,i=t.b,c=t.F,l=t.r,d=t.o,r=t.t,u=t.d,h=t.p,f=t.i}],execute:function(){var n={data:function(){return{codes:{}}},methods:{list:function(){var t=this;this.$root.loading=!0,e.post("/shareCode/list").then((function(n){n.success?t.codes=n.detail:t.$root.showModal("失败",n.msg)})).catch((function(n){t.$root.showModal("错误",n.message)})).finally((function(){t.$root.loading=!1}))},remove:function(t){var n=this;this.$root.hasToken((function(){return n.remove(t)}))&&(this.$root.loading=!0,e.post("/shareCode/delete",a.stringify({code:t})).then((function(t){t.success?(n.list(),n.$root.showModal("成功","删除成功")):n.$root.showModal("失败",t.msg)})).catch((function(t){n.$root.showModal("错误",t.message)})).finally((function(){n.$root.loading=!1})))},getLink:function(t){return window.location.protocol+"//"+window.location.host+"/files/"+t}},created:function(){this.list()}},b=function(t){return h("data-v-c966c5bd"),t=t(),f(),t},g={class:"table"},m=b((function(){return i("thead",null,[i("tr",null,[i("th",null,"分享码"),i("th",null,"文件路径"),i("th",{class:"status"},"状态"),i("th",null,"操作")])],-1)})),p={class:"code"},v={class:"text-truncate filePath"},w={class:"status"},y={key:0,class:"badge text-bg-success"},x={key:1,class:"badge text-bg-danger"},k=["data-clipboard-text"],$=[b((function(){return i("i",{class:"bi bi-clipboard"},null,-1)}))],C=["onClick"],M=[b((function(){return i("i",{class:"bi bi-trash"},null,-1)}))];t("default",o(n,[["render",function(t,n,o,e,a,h){return d(),s("table",g,[m,i("tbody",null,[(d(!0),s(c,null,l(a.codes,(function(n){return d(),s("tr",{key:n},[i("td",p,[i("i",null,r(n.code),1)]),i("td",v,[i("i",null,r(n.path),1)]),i("td",w,[n.valid?(d(),s("span",y,"有效")):(d(),s("span",x,"无效"))]),i("td",null,[n.valid?(d(),s("a",{key:0,class:"link-primary me-1",id:"btnCopy","data-clipboard-text":h.getLink(n.code)},$,8,k)):u("",!0),i("a",{class:"link-danger",onClick:function(o){return t.$root.showConfirm((function(){h.remove(n.code)}))}},M,8,C)])])})),128))])])}],["__scopeId","data-v-c966c5bd"]]))}}}));
