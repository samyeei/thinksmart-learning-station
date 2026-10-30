'use strict';
const form=document.getElementById('login-form');
const password=document.getElementById('password');
const button=document.getElementById('login-submit');
const message=document.getElementById('login-message');
if(window.location.hash==='#password-changed'){message.hidden=false;message.textContent='密码已修改，请使用新密码登录。';}
form.addEventListener('submit',async event=>{
  event.preventDefault();
  if(button.disabled)return;
  button.disabled=true;message.hidden=false;message.textContent='正在登录…';
  const controller=new AbortController(),timeout=setTimeout(()=>controller.abort(),12000);
  try {
    // A normal form navigation with no-referrer sends Origin:null. Fetch cors mode
    // preserves the browser Origin; this relative URL and CSP keep it same-origin.
    const response=await fetch('/login',{method:'POST',mode:'cors',credentials:'same-origin',
      redirect:'follow',cache:'no-store',signal:controller.signal,
      headers:{'Content-Type':'application/x-www-form-urlencoded'},
      body:new URLSearchParams({password:password.value}).toString()});
    if(response.status===401)throw new Error('密码错误，请重试。');
    if(response.status===429)throw new Error('尝试次数过多，请等待一分钟再登录。');
    if(response.status===403)throw new Error('浏览器来源验证未通过。请用手机浏览器直接打开设备的局域网地址后重试。');
    if(!response.ok)throw new Error('设备暂时无法完成登录，请稍后重试。');
    const destination=new URL(response.url);
    if(destination.origin!==window.location.origin || destination.pathname!=='/')throw new Error('登录 Cookie 未生效。请允许此站点的 Cookie 后重试。');
    password.value='';window.location.assign('/');
  } catch(error) {
    message.textContent=error.name==='AbortError' || error instanceof TypeError
      ? '设备未连接或登录超时。请确认手机与设备连接同一家庭 Wi-Fi 后重试。' : error.message;
  } finally {clearTimeout(timeout);password.value='';button.disabled=false;}
});
