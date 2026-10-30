'use strict';
const $ = id => document.getElementById(id);
let csrf = '', captureReady = false, imageUrl = null, busyScreen = false, generation = 0;
const today = () => new Date(Date.now() + 8 * 3600000).toISOString().slice(0, 10);
const dateText = value => { const n = Number(value); return Number.isFinite(n) && n > 0 ? new Date(n).toLocaleString('zh-CN', {timeZone:'Asia/Shanghai', hour12:false}) : '尚无记录'; };
const duration = value => { if (typeof value !== 'number' || !Number.isFinite(value) || value < 0) throw new Error('前台时长数据不可用'); const n=Math.floor(value); return n >= 3600 ? `${Math.floor(n/3600)} 小时 ${Math.floor(n%3600/60)} 分` : n >= 60 ? `${Math.floor(n/60)} 分 ${n%60} 秒` : `${n} 秒`; };
function connection(message, failed=false) { $('connection').textContent=message; $('connection').classList.toggle('error',failed); }
async function api(path, options={}) {
  const controller=new AbortController(), timeout=setTimeout(()=>controller.abort(),12000);
  try {
    const response=await fetch(path,{...options,credentials:'same-origin',cache:'no-store',signal:controller.signal});
    if(response.status===401) { clearImage(); window.location.assign('/login'); throw new Error('登录已失效'); }
    if(!response.ok) { const payload=await response.json().catch(()=>({})); const error=new Error(payload.error || `设备请求未完成（${response.status}）`); error.status=response.status; throw error; }
    return response;
  } catch(error) {
    if(error.name==='AbortError' || error instanceof TypeError) throw new Error('设备未连接或请求超时。请确认两台设备连接同一家庭 Wi-Fi，并保持采集服务运行。');
    throw error;
  } finally { clearTimeout(timeout); }
}
function clearImage() { if(imageUrl) URL.revokeObjectURL(imageUrl); imageUrl=null; $('screen-image').removeAttribute('src'); $('preview').hidden=true; $('clear-screen').hidden=true; }
function screenControls() { $('screen').disabled=!captureReady || !csrf || busyScreen; $('authorize').disabled=!csrf || busyScreen; $('authorize').hidden=captureReady; }
function renderStatus(status) {
  csrf=typeof status.csrf_token==='string' ? status.csrf_token : '';
  captureReady=status.capture_ready===true;
  $('device-detail').textContent=`${status.device_name || 'ThinkSmart 设备'} · ${status.local_ip || '地址暂不可用'}`;
  $('collection-start').textContent=`采集服务启动：${dateText(status.collector_started_at)}（北京时间）`;
  const last=Number(status.last_sample_at), stale=!Number.isFinite(last) || last<=0 || Date.now()-last>120000;
  if(status.usage_access!==true) connection('设备在线，但尚未获得使用情况访问权限。前台时长暂不可用。',true);
  else if(stale) connection(`设备在线，前台采集尚无近期样本。最近样本：${dateText(last)}。`,true);
  else connection(`设备在线 · 最近采样 ${dateText(last)} · 当前前台：${status.current_app || '未报告'} · ${status.keep_screen_on===true?'不自动息屏':'30秒无操作息屏'}`);
  $('screen-state').textContent=captureReady ? '屏幕查看已授权；只有点击下方按钮时才获取一张截图。' : '尚未授权或授权已结束。发起授权后，请在孩子的设备上确认系统提示。';
  const available=status.browser_control_available===true, until=Number(status.browser_setup_until);
  $('browser-state').textContent=!available ? '浏览器控制暂不可用。' : until>0 ? `维护开放至 ${dateText(until)}（北京时间）。` : '浏览器维护已关闭。';
  $('browser-start').disabled=!available || !csrf; $('browser-stop').disabled=!available || !csrf;
  screenControls();
}
function renderUsage(data, status) {
  if(!data || data.date!==$('date').value || !Array.isArray(data.apps) || data.timezone!=='Asia/Shanghai') throw new Error('前台时长数据格式不可用');
  const all=data.apps.map(app=>{ duration(app.seconds); return app; });
  const total=all.reduce((sum,app)=>sum+app.seconds,0);
  const chat=all.filter(app=>(app.package_name==='local.thinksmart.chatgpt' || app.package_name==='org.mozilla.firefov' || app.package_name==='com.openai.chatgpt')).reduce((sum,app)=>sum+app.seconds,0);
  if(status.usage_access!==true) throw new Error('尚未授予使用情况访问权限；无法读取前台时长');
  if(typeof data.observed_seconds!=='number' || !Number.isFinite(data.observed_seconds) || data.observed_seconds<0) throw new Error('观察时间尚不可用');
  if(data.observed_seconds===0 && all.length===0) { $('chat-seconds').textContent='暂无记录'; $('total-seconds').textContent='暂无记录'; }
  else { $('chat-seconds').textContent=duration(chat); $('total-seconds').textContent=duration(total); }
  $('usage-detail').textContent=`本日可观察记录起点：${dateText(data.since)}。仅覆盖实际观察到的时间段。`;
  $('apps').replaceChildren();
  all.sort((a,b)=>b.seconds-a.seconds).forEach(app=>{const row=document.createElement('div');row.className='app-row';const label=document.createElement('span');label.textContent=app.label || app.package_name || '未命名应用';const seconds=document.createElement('strong');seconds.textContent=duration(app.seconds);row.append(label,seconds);$('apps').append(row);});
}
const reasons={navigation:'页面导航被拦截',new_window:'新窗口入口被拦截',inbound_intent:'外部入口被拦截',location_guard:'页面地址变化被拦截',download:'文件下载被拦截',subframe:'自动子页面被拦截'};
function renderEvents(data) {
  if(!data || data.date!==$('date').value || !Array.isArray(data.events)) throw new Error('拦截日志数据不可用');
  $('events').replaceChildren();
  $('events-detail').textContent=data.events.length ? `本日 ${data.events.length} 条可用记录；部分自动跳转可能产生多条记录。` : '本日没有可用的拦截记录；这不表示设备全天均有采集。';
  [...data.events].sort((a,b)=>b.timestamp_ms-a.timestamp_ms).forEach(event=>{
    const li=document.createElement('li'), main=document.createElement('div'),host=document.createElement('span'),time=document.createElement('time'),meta=document.createElement('div'),gesture=document.createElement('span');
    main.className='event-main';host.className='event-host';time.className='event-time';meta.className='event-meta';gesture.className='gesture';
    host.textContent=event.target_host==='blocked-monitor-selftest.invalid' ? '安装自检（非孩子操作）' : event.target_host || '无可识别域名';time.textContent=dateText(event.timestamp_ms);
    gesture.textContent=event.target_host==='blocked-monitor-selftest.invalid' ? '安装验证记录 · 不计作孩子访问' : event.user_gesture===1 || event.user_gesture===true ? '有用户手势（不证明主动访问本站）' : event.user_gesture===0 || event.user_gesture===false ? '无用户手势 · 可能自动触发' : '手势未知 · 无法判断是否主动';
    meta.append(gesture,document.createTextNode(`${reasons[event.kind] || '受限制入口被拦截'} · 协议 ${event.target_scheme || '未知'}`));main.append(host,time);li.append(main,meta);$('events').append(li);
  });
}
async function refresh() {
  const current=++generation,date=$('date').value; $('refresh').disabled=true;
  $('chat-seconds').textContent='—';$('total-seconds').textContent='—';$('apps').replaceChildren();$('events').replaceChildren();
  $('usage-detail').textContent='正在读取前台时长…';$('events-detail').textContent='正在读取拦截日志…';
  try {
    const status=await (await api('/api/status')).json(); if(current!==generation)return; renderStatus(status);
    const result=await Promise.allSettled([api(`/api/usage?date=${date}`).then(r=>r.json()),api(`/api/events?date=${date}`).then(r=>r.json())]);
    if(current!==generation)return;
    try { if(result[0].status==='rejected') throw result[0].reason; renderUsage(result[0].value,status); }
    catch(error) {$('chat-seconds').textContent='不可用';$('total-seconds').textContent='不可用';$('usage-detail').textContent=error.message;}
    try { if(result[1].status==='rejected') throw result[1].reason; renderEvents(result[1].value); }
    catch(error) {$('events-detail').textContent=error.message;}
  } catch(error) { if(current!==generation)return; csrf='';captureReady=false;screenControls();$('browser-start').disabled=true;$('browser-stop').disabled=true;$('browser-state').textContent='设备未连接，浏览器控制不可用。';clearImage();connection(error.message,true);$('chat-seconds').textContent='不可用';$('total-seconds').textContent='不可用';$('usage-detail').textContent='无法获取设备数据，未将缺失时间计为零。';$('events-detail').textContent='无法获取拦截日志。';$('screen-state').textContent='设备未连接，屏幕查看不可用。'; }
  finally { if(current===generation)$('refresh').disabled=false; }
}
$('screen').addEventListener('click',async()=>{
  busyScreen=true;screenControls();clearImage();$('screen-message').textContent='正在获取这一张屏幕截图…';
  try { const response=await api('/api/screen',{method:'POST',headers:{'X-CSRF-Token':csrf}});const blob=await response.blob();if(blob.type!=='image/png')throw new Error('屏幕图片格式不可用');imageUrl=URL.createObjectURL(blob);$('screen-image').src=imageUrl;$('preview').hidden=false;$('clear-screen').hidden=false;$('screen-caption').textContent=`获取于 ${dateText(Date.now())} · 静态截图，非实时画面`;$('screen-message').textContent='已获取单次截图。'; }
  catch(error) { clearImage();$('screen-message').textContent=error.message;if(error.status===409){captureReady=false;$('screen-state').textContent='需要在孩子的设备上重新确认屏幕查看授权。';} }
  finally {setTimeout(()=>{busyScreen=false;screenControls();},2000);}
});
$('authorize').addEventListener('click',async()=>{busyScreen=true;screenControls();try{await api('/api/authorize-screen',{method:'POST',headers:{'X-CSRF-Token':csrf}});$('screen-message').textContent='已发起授权请求，请在孩子的设备上确认系统提示，然后刷新本页。';}catch(error){$('screen-message').textContent=error.message;}finally{busyScreen=false;screenControls();}});
for(const operation of ['start','stop']) $('browser-'+operation).addEventListener('click',async()=>{ $('browser-start').disabled=true;$('browser-stop').disabled=true;try{await api('/api/browser-setup/'+operation,{method:'POST',headers:{'X-CSRF-Token':csrf}});$('browser-message').textContent=operation==='start'?'维护已开启。':'维护已结束。';await refresh();}catch(error){$('browser-message').textContent=error.message;await refresh();}});
$('clear-screen').addEventListener('click',()=>{clearImage();$('screen-message').textContent='已清除本页预览。';});
$('logout').addEventListener('click',async()=>{try{await api('/api/logout',{method:'POST',headers:{'X-CSRF-Token':csrf}});clearImage();window.location.assign('/login');}catch(error){connection(error.message,true);}});
$('refresh').addEventListener('click',refresh);$('date').value=today();$('date').max=today();$('date').addEventListener('change',()=>{if($('date').value)refresh();});window.addEventListener('pagehide',clearImage);refresh();
