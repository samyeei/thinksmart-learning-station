'use strict';
(()=>{
 const form=document.getElementById('password-change-form');
 const current=document.getElementById('current-password'),next=document.getElementById('new-password'),confirm=document.getElementById('confirm-password');
 const button=document.getElementById('password-submit'),message=document.getElementById('password-message');
 let busy=false;
 form.addEventListener('submit',async event=>{
  event.preventDefault();if(busy)return;
  if(!csrf){message.textContent='请先登录并刷新页面后再修改密码。';return;}
  if(next.value.length<12||next.value.length>128){message.textContent='新密码须为 12–128 个字符。';return;}
  if(next.value!==confirm.value){message.textContent='两次输入的新密码不一致。';return;}
  if(next.value===current.value){message.textContent='新密码不能与当前密码相同。';return;}
  busy=true;button.disabled=true;message.textContent='正在修改密码…';
  try{
   const response=await api('/api/password',{method:'POST',mode:'cors',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8','X-CSRF-Token':csrf},body:new URLSearchParams({current_password:current.value,new_password:next.value,confirm_password:confirm.value}).toString()});
   const result=await response.json();
   if(result.password_changed!==true)throw new Error('设备未确认密码已修改，请重新登录后检查。');
   clearImage();message.textContent='密码已修改，请使用新密码重新登录。';
   window.location.assign('/login#password-changed');
  }catch(error){message.textContent=error.message||'修改未完成，请稍后重试。';}
  finally{form.reset();busy=false;button.disabled=false;}
 });
})();
