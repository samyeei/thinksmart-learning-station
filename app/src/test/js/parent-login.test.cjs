const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const source = fs.readFileSync('app/src/main/assets/parent/login.js', 'utf8');
async function scenario(status, url) {
  let handler, request, navigated, prevented=false;
  const password={value:'test-only-placeholder'}, button={disabled:false}, message={textContent:'',hidden:true};
  const form={addEventListener(type,fn){assert.equal(type,'submit');handler=fn;}};
  const elements={'login-form':form,password,'login-submit':button,'login-message':message};
  vm.runInNewContext(source, {document:{getElementById(id){return elements[id];}},window:{location:{origin:'http://192.168.1.3:8766',assign(path){navigated=path;}}},URL,URLSearchParams,AbortController,setTimeout,clearTimeout,
    fetch:async (path,options)=>{request={path,options};return {status,ok:status===200,url};}});
  assert.equal(typeof handler,'function','Login must intercept the basic form navigation');
  await handler({preventDefault(){prevented=true;}});
  assert.ok(prevented);
  assert.equal(request.path,'/login');
  assert.equal(request.options.method,'POST');
  assert.equal(request.options.mode,'cors','Fetch must preserve Origin despite no-referrer');
  assert.equal(request.options.credentials,'same-origin');
  assert.equal(request.options.body,'password=test-only-placeholder');
  assert.equal(password.value,'','Clear password field after submitting');
  assert.equal(button.disabled,false);
  return {message,navigated};
}
(async()=>{
  assert.equal((await scenario(200,'http://192.168.1.3:8766/')).navigated,'/');
  const wrong=await scenario(401,'http://192.168.1.3:8766/login');
  assert.equal(wrong.navigated,undefined);assert.match(wrong.message.textContent,/密码错误/);assert.equal(wrong.message.hidden,false);
  const limited=await scenario(429,'http://192.168.1.3:8766/login');assert.match(limited.message.textContent,/一分钟/);
  const rejected=await scenario(403,'http://192.168.1.3:8766/login');assert.match(rejected.message.textContent,/浏览器/);
  const noCookie=await scenario(200,'http://192.168.1.3:8766/login');assert.equal(noCookie.navigated,undefined);assert.match(noCookie.message.textContent,/Cookie/);
  console.log('Parent login browser logic: 5 scenarios passed');
})().catch(error=>{console.error(error);process.exitCode=1;});
