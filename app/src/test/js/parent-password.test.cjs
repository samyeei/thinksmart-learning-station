const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const path='app/src/main/assets/parent/password.js';const source=fs.existsSync(path)?fs.readFileSync(path,'utf8'):'';
async function scenario({current='fixture-old-1234',next='fixture-new-5678',confirm=next,error,ack=true}={}){
 let handler,request,navigated,reset=0,cleared=0;
 const form={addEventListener(type,fn){assert.equal(type,'submit');handler=fn;},reset(){reset++;for(const id of ['current-password','new-password','confirm-password'])elements[id].value='';}};
 const elements={'password-change-form':form,'current-password':{value:current},'new-password':{value:next},'confirm-password':{value:confirm},'password-submit':{disabled:false},'password-message':{textContent:''}};
 vm.runInNewContext(source,{document:{getElementById(id){return elements[id];}},csrf:'csrf-fixture',URLSearchParams,clearImage(){cleared++;},window:{location:{assign(p){navigated=p;}}},api:async(path,options)=>{request={path,options};if(error)throw Error(error);return{json:async()=>({password_changed:ack})};}});
 assert.equal(typeof handler,'function','Password form must have a submit handler');
 await handler({preventDefault(){}});return{request,navigated,reset,cleared,elements};
}
(async()=>{
 const ok=await scenario();assert.equal(ok.request.path,'/api/password');assert.equal(ok.request.options.method,'POST');assert.equal(ok.request.options.headers['X-CSRF-Token'],'csrf-fixture');assert.equal(ok.navigated,'/login#password-changed');assert.equal(ok.reset,1);assert.equal(ok.cleared,1);
 const form=new URLSearchParams(ok.request.options.body);assert.equal(form.get('current_password'),'fixture-old-1234');assert.equal(form.get('new_password'),'fixture-new-5678');
 const mismatch=await scenario({confirm:'different-fixture'});assert.equal(mismatch.request,undefined);assert.match(mismatch.elements['password-message'].textContent,/不一致/);
 const short=await scenario({next:'short'});assert.equal(short.request,undefined);
 const wrong=await scenario({error:'当前密码不正确'});assert.equal(wrong.navigated,undefined);assert.equal(wrong.reset,1);assert.equal(wrong.elements['password-submit'].disabled,false);assert.match(wrong.elements['password-message'].textContent,/当前密码/);
 const uncertain=await scenario({ack:false});assert.equal(uncertain.navigated,undefined);
 console.log('Parent password browser logic: 5 scenarios passed');
})().catch(e=>{console.error(e);process.exitCode=1;});
