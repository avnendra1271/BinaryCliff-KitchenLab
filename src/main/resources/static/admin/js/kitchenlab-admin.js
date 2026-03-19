/* ══════════ DATA ══════════ */
let orders=[
  {id:'#2847',name:'Aryan Mehta',phone:'+91 98001 23456',items:3,type:'Delivery',amt:485,pay:'Online',status:'Pending',time:'3:02 PM',timer:'12:34',tc:'r'},
  {id:'#2846',name:'Sneha Patel',phone:'+91 97600 11223',items:2,type:'Dine-in',amt:620,pay:'Cash',status:'Preparing',time:'2:54 PM',timer:'07:15',tc:'a'},
  {id:'#2845',name:'Rohan Gupta',phone:'+91 99887 66554',items:1,type:'Pickup',amt:180,pay:'Online',status:'Ready',time:'2:50 PM',timer:'03:22',tc:'g'},
  {id:'#2841',name:'Priya Sharma',phone:'+91 98765 43210',items:2,type:'Delivery',amt:385,pay:'Online',status:'Delivered',time:'2:34 PM',timer:'Done',tc:'g'},
  {id:'#2838',name:'Vikram Nair',phone:'+91 96543 21098',items:5,type:'Dine-in',amt:1240,pay:'Card',status:'Cancelled',time:'2:12 PM',timer:'—',tc:''},
  {id:'#2835',name:'Meera Singh',phone:'+91 91234 56789',items:3,type:'Delivery',amt:670,pay:'Online',status:'Delivered',time:'1:55 PM',timer:'Done',tc:'g'},
];
let cats=[
  {id:1,emoji:'🍕',name:'Pizza',desc:'Italian pizzas & calzones',items:8,order:1,status:true},
  {id:2,emoji:'🍗',name:'Main Course',desc:'Indian & Continental mains',items:14,order:2,status:true},
  {id:3,emoji:'🥗',name:'Starters',desc:'Soups, salads & appetizers',items:6,order:3,status:true},
  {id:4,emoji:'🥤',name:'Beverages',desc:'Drinks, juices & lassi',items:10,order:4,status:true},
  {id:5,emoji:'🍰',name:'Desserts',desc:'Sweets & frozen desserts',items:5,order:5,status:false},
];
let items=[
  {id:1,emoji:'🍕',name:'Margherita Pizza',cat:'Pizza',price:280,disc:320,avail:true,tags:['veg','popular']},
  {id:2,emoji:'🍗',name:'Butter Chicken',cat:'Main Course',price:350,disc:null,avail:true,tags:['nonveg','chef']},
  {id:3,emoji:'🥤',name:'Mango Lassi',cat:'Beverages',price:60,disc:null,avail:true,tags:['veg']},
  {id:4,emoji:'🥗',name:'Paneer Tikka',cat:'Starters',price:220,disc:null,avail:false,tags:['veg','spicy']},
  {id:5,emoji:'🍰',name:'Gulab Jamun',cat:'Desserts',price:80,disc:120,avail:true,tags:['veg']},
  {id:6,emoji:'🍛',name:'Dal Makhani',cat:'Main Course',price:200,disc:null,avail:true,tags:['veg','popular']},
];
let coupons=[
  {id:1,code:'FIRST20',type:'Percentage',val:'20%',min:'₹200',max:'₹100',used:23,limit:100,exp:'Mar 31',status:'active'},
  {id:2,code:'SAVE50',type:'Flat',val:'₹50',min:'₹400',max:'—',used:67,limit:200,exp:'Apr 15',status:'active'},
  {id:3,code:'WKND15',type:'Percentage',val:'15%',min:'—',max:'₹150',used:8,limit:50,exp:'Apr 30',status:'active'},
  {id:4,code:'LAUNCH10',type:'Percentage',val:'10%',min:'—',max:'—',used:100,limit:100,exp:'Mar 1',status:'expired'},
];

let oTab='all',editCatId=null,editItemId=null,editCpId=null,delCb=null,riderSel=null;
let aInt=null,aSecs=20,sbColl=false,mode='';

/* ══════════ LAYOUT ENGINE ══════════ */
function applyLayout(){
  const w=window.innerWidth;
  const m=w<=768?'mobile':w<=1024?'tablet':'desktop';
  if(m===mode)return; mode=m;
  document.body.setAttribute('data-mode',m);
  const main=document.getElementById('main');
  if(m==='desktop') main.style.marginLeft=sbColl?'68px':'256px';
  else if(m==='tablet') main.style.marginLeft='68px';
  else{ main.style.marginLeft='0'; }
  const ca=document.getElementById('contentArea');
  const tb=document.getElementById('toastBox');
  if(m==='mobile'){ ca.style.paddingBottom='84px'; tb.style.bottom='76px'; }
  else{ ca.style.paddingBottom=''; tb.style.bottom='24px'; }
}
window.addEventListener('resize',applyLayout);

function toggleSidebar(){
  if(mode!=='desktop')return;
  sbColl=!sbColl;
  document.getElementById('sidebar').classList.toggle('collapsed',sbColl);
  document.getElementById('main').style.marginLeft=sbColl?'68px':'256px';
  document.getElementById('chevI').style.transform=sbColl?'rotate(180deg)':'';
}
function openMobSidebar(){document.getElementById('sidebar').classList.add('open');document.getElementById('overlay').classList.add('show');}
function closeMobSidebar(){document.getElementById('sidebar').classList.remove('open');document.getElementById('overlay').classList.remove('show');}

function toggleSub(id,el){
  const sub=document.getElementById(id);sub.classList.toggle('open');
  const ar=el?el.querySelector('.sub-arrow'):null;
  if(ar)ar.style.transform=sub.classList.contains('open')?'rotate(180deg)':'';
}

/* ══════════ NAVIGATION ══════════ */
function showPage(id){
  document.querySelectorAll('.page').forEach(p=>p.classList.remove('on'));
  const pg=document.getElementById('page-'+id);if(pg)pg.classList.add('on');
  if(id==='orders')renderOrders();
  if(id==='categories')renderCats();
  if(id==='menuItems')renderItems();
  if(id==='coupons')renderCoupons();
}
function navTo(id,el){
  showPage(id);closeMobSidebar();
  document.querySelectorAll('.sb-row').forEach(r=>r.classList.remove('on'));
  if(el&&el.classList.contains('sb-row'))el.classList.add('on');
  document.querySelectorAll('.mn').forEach(b=>b.classList.toggle('on',b.dataset.page===id));
}

/* ══════════ ALERT ══════════ */
function showAlert(){
  document.getElementById('alertWrap').classList.add('show');
  aSecs=20; document.getElementById('alertCD').textContent=aSecs;
  const bar=document.getElementById('alertBar');
  bar.style.transition='none'; bar.style.width='100%';
  clearInterval(aInt);
  setTimeout(()=>{bar.style.transition='width 1s linear';},30);
  aInt=setInterval(()=>{aSecs--;document.getElementById('alertCD').textContent=aSecs;bar.style.width=(aSecs/20*100)+'%';if(aSecs<=0){clearInterval(aInt);closeAlert();toast('⏰ Order #2847 auto-expired — moved to missed queue','warning');}},1000);
}
function closeAlert(){document.getElementById('alertWrap').classList.remove('show');clearInterval(aInt);}
function acceptAlert(){closeAlert();toast('✅ Order #2847 accepted — Chef notified!','success');}
function rejectAlert(){closeAlert();toast('❌ Order #2847 rejected — Customer notified.','error');}

/* ══════════ DRAWER ══════════ */
function openDrawer(){document.getElementById('drawerBg').classList.add('show');document.getElementById('drawer').classList.add('show');document.body.style.overflow='hidden';}
function closeDrawer(){document.getElementById('drawerBg').classList.remove('show');document.getElementById('drawer').classList.remove('show');document.body.style.overflow='';}
function setStatus(label,cls){const s=document.getElementById('drawerStatus');s.className='badge '+cls;s.textContent='● '+label;toast(`Status updated → ${label}`,'success');}

/* ══════════ MODALS ══════════ */
function openModal(id){const el=document.getElementById(id);el.classList.add('show');document.body.style.overflow='hidden';lucide.createIcons();}
function closeModal(id){const el=document.getElementById(id);el.classList.remove('show');document.body.style.overflow='';}
document.querySelectorAll('.modal-bg').forEach(m=>m.addEventListener('click',function(e){if(e.target===this)closeModal(this.id);}));

/* ══════════ ORDERS ══════════ */
function renderOrders(){
  const s=(document.getElementById('oSearch')||{value:''}).value.toLowerCase();
  const t=(document.getElementById('oType')||{value:''}).value;
  const p=(document.getElementById('oPay')||{value:''}).value;
  const fil=orders.filter(o=>{
    if(oTab!=='all'&&o.status!==oTab)return false;
    if(s&&!o.id.toLowerCase().includes(s)&&!o.name.toLowerCase().includes(s))return false;
    if(t&&o.type!==t)return false;
    if(p&&o.pay!==p)return false;
    return true;
  });
  const tb=document.getElementById('oTbody'),em=document.getElementById('oEmpty'),cnt=document.getElementById('oCount');
  if(!tb)return;
  if(!fil.length){tb.innerHTML='';em.classList.remove('hidden');cnt.textContent='No results';lucide.createIcons();return;}
  em.classList.add('hidden');cnt.textContent=`Showing ${fil.length} of ${orders.length} orders`;
  const sm={Pending:'bp',Accepted:'ba',Preparing:'bpr',Ready:'brd',Delivered:'bdl',Cancelled:'bc'};
  const tm={Delivery:'btype-d',Pickup:'btype-p','Dine-in':'btype-i'};
  const tclr={r:'background:var(--red2);color:#f87171',a:'background:var(--amber2);color:#fcd34d',g:'background:var(--green2);color:#34d399','':`color:var(--muted2)`};
  const te={Delivery:'🛵',Pickup:'🛍️','Dine-in':'🍽️'};
  tb.innerHTML=fil.map(o=>`<tr class="tbl-row">
    <td class="font-black" style="color:var(--text)">${o.id}</td>
    <td><div class="font-bold text-sm" style="color:var(--text)">${o.name}</div><div class="text-xs" style="color:var(--muted)">${o.phone}</div></td>
    <td class="hidden md:table-cell"><span class="badge ${tm[o.type]||''}">${te[o.type]||''} ${o.type}</span></td>
    <td class="font-bold" style="color:var(--text)">₹${o.amt}</td>
    <td class="text-xs hidden lg:table-cell">${o.pay}</td>
    <td><span class="badge ${sm[o.status]||''}">● ${o.status}</span></td>
    <td class="hidden sm:table-cell"><span class="text-xs font-black px-2 py-0.5 rounded-xl" style="${tclr[o.tc]||''}">${o.timer}</span></td>
    <td><div class="flex gap-1">
      <button onclick="openDrawer()" class="btn btn-g btn-xs" title="View Details"><i data-lucide="eye" style="width:11px;height:11px"></i></button>
      ${o.status==='Pending'?`<button onclick="qA('${o.id}')" class="btn btn-xs" style="background:var(--green2);color:#34d399;border:1px solid rgba(16,185,129,.2)"><i data-lucide="check" style="width:11px;height:11px"></i></button><button onclick="qR('${o.id}')" class="btn btn-d btn-xs"><i data-lucide="x" style="width:11px;height:11px"></i></button>`:''}
      <button onclick="printInv()" class="btn btn-g btn-xs" title="Print Invoice"><i data-lucide="printer" style="width:11px;height:11px"></i></button>
      <button onclick="downloadInvoice()" class="btn btn-g btn-xs" title="Download Invoice"><i data-lucide="download" style="width:11px;height:11px"></i></button>
    </div></td>
  </tr>`).join('');
  lucide.createIcons();
}
function setTab(el,f){document.querySelectorAll('.tab').forEach(t=>t.classList.remove('on'));el.classList.add('on');oTab=f;renderOrders();}
function clearOF(){['oSearch','oType','oPay','oDate'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';});oTab='all';document.querySelectorAll('.tab').forEach((t,i)=>t.classList.toggle('on',i===0));renderOrders();toast('All filters cleared','info');}
function qA(id){const o=orders.find(x=>x.id===id);if(o){o.status='Accepted';renderOrders();toast(`${id} accepted!`,'success');}}
function qR(id){const o=orders.find(x=>x.id===id);if(o){o.status='Cancelled';renderOrders();toast(`${id} rejected.`,'error');}}
function exportOrders(){
  const csv=['ID,Customer,Items,Type,Amount,Payment,Status'].concat(orders.map(o=>`${o.id},${o.name},${o.items},${o.type},₹${o.amt},${o.pay},${o.status}`)).join('\n');
  const a=document.createElement('a');a.href=URL.createObjectURL(new Blob([csv],{type:'text/csv'}));a.download='BinarCliff-orders.csv';a.click();
  toast('Orders exported as CSV!','success');
}

/* ══════════ CATEGORIES ══════════ */
function renderCats(){
  const c=document.getElementById('catList');if(!c)return;
  c.innerHTML=cats.map((cat,i)=>`
    <div class="drag-row flex items-center gap-3 px-4 py-3.5" style="border-bottom:1px solid var(--border)" draggable="true" data-id="${cat.id}" ondragstart="dStart(event,${i})" ondragover="dOver(event)" ondrop="dDrop(event,${i})" ondragleave="dLeave(event)">
      <div class="p-2 rounded-xl cursor-grab flex-shrink-0 transition-all" style="background:var(--border2);color:var(--muted2)" title="Drag to reorder"><i data-lucide="grip-vertical" style="width:14px;height:14px"></i></div>
      <div class="w-11 h-11 rounded-2xl flex items-center justify-center text-2xl flex-shrink-0" style="background:var(--orange3)">${cat.emoji}</div>
      <div class="flex-1 min-w-0"><div class="font-bold text-sm" style="color:var(--text)">${cat.name}</div><div class="text-xs" style="color:var(--muted)">${cat.desc}</div></div>
      <div class="text-xs hidden sm:block w-16" style="color:var(--muted)">${cat.items} items</div>
      <div class="text-[10px] font-black hidden sm:block w-8 text-center px-2 py-1 rounded-lg" style="background:var(--border2);color:var(--muted2)">#${cat.order}</div>
      <span class="badge ${cat.status?'bact':'binact'}">● ${cat.status?'Active':'Inactive'}</span>
      <div class="flex gap-1.5">
        <button onclick="editCat(${cat.id})" class="btn btn-g btn-xs flex items-center gap-1"><i data-lucide="pencil" style="width:11px;height:11px"></i></button>
        <button onclick="confirmDel('${cat.name}',()=>delCat(${cat.id}))" class="btn btn-d btn-xs"><i data-lucide="trash-2" style="width:11px;height:11px"></i></button>
      </div>
    </div>`).join('');
  lucide.createIcons();
}
let dragIdx=null;
function dStart(e,i){dragIdx=i;e.currentTarget.classList.add('dragging');e.dataTransfer.effectAllowed='move';}
function dOver(e){e.preventDefault();e.currentTarget.classList.add('drag-over');}
function dLeave(e){e.currentTarget.classList.remove('drag-over');}
function dDrop(e,toIdx){
  e.preventDefault();e.currentTarget.classList.remove('drag-over');
  if(dragIdx===null||dragIdx===toIdx)return;
  const m=cats.splice(dragIdx,1)[0];cats.splice(toIdx,0,m);
  cats.forEach((c,i)=>c.order=i+1);dragIdx=null;renderCats();
  toast('Category order updated!','success');
}
document.addEventListener('dragend',()=>{document.querySelectorAll('.drag-row').forEach(d=>d.classList.remove('dragging','drag-over'));dragIdx=null;});

function openAddCat(){editCatId=null;document.getElementById('catModalTitle').textContent='Add Category';['cName','cDesc','cEmoji'].forEach(id=>document.getElementById(id).value='');document.getElementById('cOrder').value=cats.length+1;document.getElementById('catTog').classList.add('on');document.getElementById('catPrev').classList.add('hidden');openModal('catModal');}
function editCat(id){editCatId=id;const c=cats.find(x=>x.id===id);document.getElementById('catModalTitle').textContent='Edit Category';document.getElementById('cName').value=c.name;document.getElementById('cDesc').value=c.desc;document.getElementById('cEmoji').value=c.emoji;document.getElementById('cOrder').value=c.order;document.getElementById('catTog').classList.toggle('on',c.status);openModal('catModal');}
function saveCat(){
  const name=document.getElementById('cName').value.trim();if(!name){toast('Category name is required','error');return;}
  if(editCatId){const c=cats.find(x=>x.id===editCatId);c.name=name;c.desc=document.getElementById('cDesc').value;c.emoji=document.getElementById('cEmoji').value||c.emoji;c.order=parseInt(document.getElementById('cOrder').value)||c.order;c.status=document.getElementById('catTog').classList.contains('on');toast(`"${name}" updated!`,'success');}
  else{cats.push({id:Date.now(),emoji:document.getElementById('cEmoji').value||'🍽️',name,desc:document.getElementById('cDesc').value,items:0,order:cats.length+1,status:true});toast(`"${name}" created!`,'success');}
  closeModal('catModal');renderCats();
}
function delCat(id){cats=cats.filter(c=>c.id!==id);cats.forEach((c,i)=>c.order=i+1);closeModal('delModal');renderCats();toast('Category deleted','success');}

/* ══════════ MENU ITEMS ══════════ */
function renderItems(){
  const s=(document.getElementById('miSearch')||{value:''}).value.toLowerCase();
  const cf=(document.getElementById('miCat')||{value:''}).value;
  const af=(document.getElementById('miAvail')||{value:''}).value;
  const fil=items.filter(i=>{
    if(s&&!i.name.toLowerCase().includes(s)&&!i.cat.toLowerCase().includes(s))return false;
    if(cf&&i.cat!==cf)return false;
    if(af!==''&&(af==='1')!==i.avail)return false;
    return true;
  });
  const grid=document.getElementById('miGrid'),empty=document.getElementById('miEmpty');
  if(!grid)return;
  const cnt=document.getElementById('miCount');if(cnt)cnt.textContent=`${fil.length} item${fil.length!==1?'s':''} across ${cats.length} categories`;
  if(!fil.length){grid.innerHTML='';empty.classList.remove('hidden');return;}
  empty.classList.add('hidden');
  grid.innerHTML=fil.map(item=>`
    <div class="mi-card">
      <div class="relative h-32 flex items-center justify-center text-5xl" style="background:linear-gradient(135deg,var(--orange3),rgba(249,115,22,.05))">
        ${item.emoji}
        ${item.tags.includes('veg')?`<span style="position:absolute;top:8px;left:8px;font-size:9px;font-weight:700;background:var(--green2);color:#34d399;border:1px solid rgba(16,185,129,.25);padding:2px 8px;border-radius:20px">🟢 Veg</span>`:item.tags.includes('nonveg')?`<span style="position:absolute;top:8px;left:8px;font-size:9px;font-weight:700;background:var(--red2);color:#f87171;border:1px solid rgba(239,68,68,.25);padding:2px 8px;border-radius:20px">🔴 Non-Veg</span>`:''}
        ${item.tags.includes('popular')?`<span style="position:absolute;top:8px;right:8px;font-size:9px;font-weight:700;background:var(--amber2);color:#fcd34d;border:1px solid rgba(245,158,11,.25);padding:2px 6px;border-radius:20px">⭐</span>`:''}
        ${!item.avail?`<div style="position:absolute;inset:0;background:rgba(0,0,0,.6);display:flex;align-items:center;justify-content:center"><span style="font-size:10px;font-weight:800;color:var(--muted);background:var(--card);border:1px solid var(--border);padding:3px 10px;border-radius:20px;text-transform:uppercase;letter-spacing:.05em">Unavailable</span></div>`:''}
      </div>
      <div class="p-4">
        <div class="font-bold text-sm truncate mb-0.5" style="color:var(--text)">${item.name}</div>
        <div class="text-xs mb-3" style="color:var(--muted)">${item.cat}${item.tags.includes('spicy')?' · 🌶️':''}</div>
        <div class="flex items-center justify-between">
          <div class="font-black text-base" style="color:var(--orange)">${item.disc?`<span style="text-decoration:line-through;color:var(--muted2);font-size:11px;font-weight:400;margin-right:3px">₹${item.disc}</span>`:''}₹${item.price}</div>
          <div class="flex items-center gap-1.5">
            <button onclick="editItem(${item.id})" class="btn btn-g btn-xs"><i data-lucide="pencil" style="width:11px;height:11px"></i></button>
            <button onclick="confirmDel('${item.name}',()=>delItem(${item.id}))" class="btn btn-d btn-xs"><i data-lucide="trash-2" style="width:11px;height:11px"></i></button>
            <div class="tog ${item.avail?'on':''}" style="transform:scale(.82);transform-origin:center" onclick="toggleAvail(${item.id},this)" title="Toggle availability"></div>
          </div>
        </div>
      </div>
    </div>`).join('')+`
    <div class="mi-card flex flex-col items-center justify-center p-8 cursor-pointer" style="border-style:dashed;min-height:200px" onclick="openAddItem()" onmouseenter="this.style.borderColor='var(--orange)'" onmouseleave="this.style.borderColor=''">
      <div class="w-12 h-12 rounded-2xl flex items-center justify-center mb-3" style="background:var(--border2)"><i data-lucide="plus" style="width:22px;height:22px;color:var(--muted)"></i></div>
      <div class="font-bold text-sm" style="color:var(--muted)">Add New Item</div>
      <div class="text-xs mt-0.5" style="color:var(--muted2)">Click to create</div>
    </div>`;
  lucide.createIcons();
}
function openAddItem(){editItemId=null;document.getElementById('itemModalTitle').textContent='Add Menu Item';['iName','iDesc','iPrice','iDisc','iPrep'].forEach(id=>document.getElementById(id).value='');document.getElementById('iCat').value='';document.querySelectorAll('.tag-btn').forEach(t=>{t.style.background='var(--border2)';t.style.color='var(--muted)';t.style.borderColor='var(--border)';});document.getElementById('varList').innerHTML='';document.getElementById('addList').innerHTML='';document.getElementById('itemTog').classList.add('on');document.getElementById('itemPrev').classList.add('hidden');openModal('itemModal');}
function editItem(id){editItemId=id;const item=items.find(i=>i.id===id);document.getElementById('itemModalTitle').textContent='Edit Item';document.getElementById('iName').value=item.name;document.getElementById('iCat').value=item.cat;document.getElementById('iPrice').value=item.price;document.getElementById('iDisc').value=item.disc||'';document.querySelectorAll('.tag-btn').forEach(t=>{const a=item.tags.includes(t.dataset.tag);t.style.background=a?'var(--orange2)':'var(--border2)';t.style.color=a?'var(--orange)':'var(--muted)';t.style.borderColor=a?'var(--orange)':'var(--border)';});openModal('itemModal');}
function saveItem(addAnother){
  const name=document.getElementById('iName').value.trim(),cat=document.getElementById('iCat').value,price=parseFloat(document.getElementById('iPrice').value);
  if(!name||!cat||!price){toast('Please fill all required fields','error');return;}
  const tags=[...document.querySelectorAll('.tag-btn')].filter(t=>t.style.color==='var(--orange)').map(t=>t.dataset.tag);
  const disc=parseFloat(document.getElementById('iDisc').value)||null;
  const em={Pizza:'🍕','Main Course':'🍗',Starters:'🥗',Beverages:'🥤',Desserts:'🍰'};
  if(editItemId){const item=items.find(i=>i.id===editItemId);item.name=name;item.cat=cat;item.price=price;item.disc=disc;item.tags=tags;toast(`"${name}" updated!`,'success');}
  else{items.push({id:Date.now(),emoji:em[cat]||'🍽️',name,cat,price,disc,avail:true,tags});toast(`"${name}" added to menu!`,'success');}
  if(!addAnother){closeModal('itemModal');renderItems();}else{openAddItem();}
}
function delItem(id){items=items.filter(i=>i.id!==id);closeModal('delModal');renderItems();toast('Item deleted','success');}
function toggleAvail(id,tog){const item=items.find(i=>i.id===id);if(item){item.avail=!item.avail;tog.classList.toggle('on',item.avail);toast(`${item.name}: ${item.avail?'Now Available':'Set Unavailable'}`,item.avail?'success':'warning');renderItems();}}
function clearMIF(){['miSearch','miCat','miAvail'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';});renderItems();toast('Filters cleared','info');}

/* ══════════ COUPONS ══════════ */
function renderCoupons(){
  const grid=document.getElementById('cpGrid');if(!grid)return;
  grid.innerHTML=coupons.map(c=>{
    const pct=Math.round(c.used/c.limit*100),exp=c.status==='expired';
    return `<div class="card card-interactive p-5">
      <div class="flex items-start justify-between mb-1">
        <div class="text-[9px] font-black uppercase tracking-widest inline-block px-3 py-1 rounded-full" style="background:${exp?'var(--border2)':'var(--orange2)'};color:${exp?'var(--muted)':'var(--orange)'}">● ${c.type}</div>
        ${!exp?`<span class="badge bact text-[9px]">Live</span>`:`<span class="badge binact text-[9px]">Expired</span>`}
      </div>
      <div class="font-black text-2xl tracking-wider my-2 serif" style="color:${exp?'var(--muted)':'var(--text)'};letter-spacing:.12em">${c.code}</div>
      <div class="text-sm mb-3" style="color:var(--muted)">${c.val} off · Min ${c.min} · Max discount ${c.max}</div>
      <div class="mb-3">
        <div class="flex justify-between text-xs mb-1.5" style="color:var(--muted2)"><span>Usage progress</span><span class="font-bold" style="color:var(--text)">${c.used} / ${c.limit}</span></div>
        <div class="progress-track"><div class="h-full rounded-full transition-all" style="width:${pct}%;background:${exp?'var(--muted2)':'var(--orange)'}"></div></div>
      </div>
      <div class="flex items-center justify-between pt-1">
        <span class="text-xs flex items-center gap-1" style="color:var(--muted2)"><i data-lucide="calendar" style="width:10px;height:10px"></i>Expires ${c.exp}</span>
        <div class="flex gap-1.5">
          ${!exp?`<button onclick="editCpFn(${c.id})" class="btn btn-g btn-xs"><i data-lucide="pencil" style="width:11px;height:11px"></i></button><button onclick="pauseCoupon(${c.id})" class="btn btn-g btn-xs"><i data-lucide="pause" style="width:11px;height:11px"></i></button>`:''}
          <button onclick="confirmDel('${c.code} coupon',()=>delCoupon(${c.id}))" class="btn btn-d btn-xs"><i data-lucide="trash-2" style="width:11px;height:11px"></i></button>
        </div>
      </div>
    </div>`;
  }).join('');
  lucide.createIcons();
  const a=coupons.filter(c=>c.status==='active').length,e=coupons.filter(c=>c.status==='expired').length;
  const el=document.getElementById('cpCount');if(el)el.textContent=`${a} active · ${e} expired`;
}
function openAddCoupon(){editCpId=null;document.getElementById('couponTitle').textContent='Create Coupon';['cpCode','cpVal','cpMin','cpMax','cpLimit','cpUser','cpStart','cpEnd'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';});openModal('couponModal');}
function editCpFn(id){editCpId=id;const c=coupons.find(x=>x.id===id);document.getElementById('couponTitle').textContent='Edit Coupon';document.getElementById('cpCode').value=c.code;openModal('couponModal');}
function saveCoupon(){
  const code=document.getElementById('cpCode').value.trim().toUpperCase(),val=document.getElementById('cpVal').value;
  if(!code||!val){toast('Code and value are required','error');return;}
  const type=document.getElementById('cpType').value.includes('%')?'Percentage':'Flat';
  if(editCpId){const c=coupons.find(x=>x.id===editCpId);c.code=code;toast(`"${code}" updated!`,'success');}
  else{coupons.push({id:Date.now(),code,type,val:type==='Percentage'?`${val}%`:`₹${val}`,min:document.getElementById('cpMin').value||'—',max:document.getElementById('cpMax').value||'—',used:0,limit:parseInt(document.getElementById('cpLimit').value)||100,exp:document.getElementById('cpEnd').value||'TBD',status:'active'});toast(`Coupon "${code}" created!`,'success');}
  closeModal('couponModal');renderCoupons();
}
function delCoupon(id){coupons=coupons.filter(c=>c.id!==id);closeModal('delModal');renderCoupons();toast('Coupon deleted','success');}
function pauseCoupon(id){const c=coupons.find(x=>x.id===id);if(c){c.status=c.status==='active'?'paused':'active';renderCoupons();toast(`${c.code} ${c.status==='active'?'resumed':'paused'}`,'warning');}}
function filterCoupons(q){renderCoupons();}// could filter visually

/* ══════════ CONFIRM DELETE ══════════ */
function confirmDel(name,cb){document.getElementById('delName').textContent=name;delCb=cb;document.getElementById('delBtn').onclick=()=>{if(delCb)delCb();else closeModal('delModal');};openModal('delModal');}

/* ══════════ RIDER ══════════ */
function pickRider(el,name){
  document.querySelectorAll('.rider-opt').forEach(r=>{r.style.borderColor='var(--border)';r.style.background='';});
  el.style.borderColor='var(--orange)';el.style.background='var(--orange3)';
  riderSel=name;
  const btn=document.getElementById('assignBtn');btn.disabled=false;btn.classList.remove('opacity-40','cursor-not-allowed');
}
function confirmRider(){closeModal('riderModal');toast(`${riderSel} assigned to order!`,'success');riderSel=null;}

/* ══════════ INVOICE ══════════ */
function printInv(){window.print();}
function downloadInvoice(){
  const html=`<!DOCTYPE html><html><head><title>Invoice - BinarCliff</title></head><body>${document.getElementById('invoicePrint').innerHTML}</body></html>`;
  const a=document.createElement('a');a.href=URL.createObjectURL(new Blob([html],{type:'text/html'}));a.download='BinarCliff-invoice.html';a.click();
  toast('Invoice downloaded!','success');
}

/* ══════════ ONLINE TOGGLE ══════════ */
function toggleOnline(el){
  const isOn=el.dataset.state!=='offline';el.dataset.state=isOn?'offline':'online';
  const dot=document.getElementById('onDot'),txt=document.getElementById('onTxt');
  if(isOn){el.style.background='var(--red2)';el.style.borderColor='rgba(239,68,68,.2)';el.style.color='#f87171';dot.style.background='var(--red)';dot.classList.remove('ping');txt.textContent='Offline';}
  else{el.style.background='var(--green2)';el.style.borderColor='rgba(16,185,129,.2)';el.style.color='#34d399';dot.style.background='var(--green)';dot.classList.add('ping');txt.textContent='Online';}
  toast(isOn?'Restaurant set to Offline':'Restaurant is now Online',isOn?'warning':'success');
}

/* ══════════ THEME ══════════ */
function toggleTheme(){
  document.body.classList.toggle('light');
  const icon=document.getElementById('themeIcon');
  icon.setAttribute('data-lucide',document.body.classList.contains('light')?'moon':'sun');
  lucide.createIcons();
}

/* ══════════ PROFILE ══════════ */
function switchPTab(el,tabId){
  document.querySelectorAll('.ptab').forEach(t=>t.classList.remove('on'));
  document.querySelectorAll('.ppane').forEach(t=>t.classList.remove('on'));
  el.classList.add('on');document.getElementById(tabId).classList.add('on');
  lucide.createIcons();
}
function saveProfile(){
  const name=document.getElementById('pRestName').value.trim();
  document.getElementById('sbName').textContent=name;
  document.getElementById('topbarName').textContent=name;
  document.getElementById('pRestDisp').textContent=name;
  closeModal('profileModal');toast('Profile saved successfully!','success');
}
function changeAvatar(input){
  if(input.files&&input.files[0]){const r=new FileReader();r.onload=e=>{const av=document.getElementById('pAvatar');av.style.background=`url(${e.target.result}) center/cover`;av.textContent='';};r.readAsDataURL(input.files[0]);}
}

/* ══════════ HELPERS ══════════ */
function toggleTag(el){
  const a=el.style.color==='var(--orange)';
  el.style.background=a?'var(--border2)':'var(--orange2)';
  el.style.color=a?'var(--muted)':'var(--orange)';
  el.style.borderColor=a?'var(--border)':'var(--orange)';
}
function addRow(listId,ph){
  const list=document.getElementById(listId);
  const d=document.createElement('div');d.style.cssText='display:flex;align-items:center;gap:8px';
  d.innerHTML=`<input class="inp flex-1 h-9" style="font-size:12px" placeholder="${ph}"/><input class="inp h-9" style="width:90px;font-size:12px" placeholder="+₹0"/><button onclick="this.parentElement.remove()" class="btn btn-d btn-xs" style="flex-shrink:0;padding:6px"><i data-lucide="x" style="width:11px;height:11px"></i></button>`;
  list.appendChild(d);lucide.createIcons();
}
function prevImg(input,id){
  if(input.files&&input.files[0]){const r=new FileReader();r.onload=e=>{const img=document.getElementById(id);img.src=e.target.result;img.classList.remove('hidden');};r.readAsDataURL(input.files[0]);}
}
function gSearch(q){if(!q)return;if(q.startsWith('#')||/\d{4}/.test(q))navTo('orders',null);else if(['pizza','lassi','chicken','paneer','dal','butter'].some(k=>q.toLowerCase().includes(k)))navTo('menuItems',null);}

/* ══════════ TOAST ══════════ */
function toast(msg,type='success'){
  const cfg={
    success:{bg:'var(--green2)',bd:'rgba(16,185,129,.3)',c:'#34d399',ic:'check-circle'},
    error:{bg:'var(--red2)',bd:'rgba(239,68,68,.3)',c:'#f87171',ic:'x-circle'},
    warning:{bg:'var(--amber2)',bd:'rgba(245,158,11,.3)',c:'#fcd34d',ic:'alert-triangle'},
    info:{bg:'var(--blue2)',bd:'rgba(59,130,246,.3)',c:'#60a5fa',ic:'info'},
  };
  const {bg,bd,c,ic}=cfg[type]||cfg.success;
  const t=document.createElement('div');
  t.className='toast-in pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-bold max-w-xs';
  t.style.cssText=`background:${bg};border:1px solid ${bd};color:${c};box-shadow:0 8px 28px rgba(0,0,0,.5)`;
  t.innerHTML=`<i data-lucide="${ic}" style="width:14px;height:14px;flex-shrink:0"></i><span>${msg}</span>`;
  document.getElementById('toastBox').appendChild(t);lucide.createIcons({nodes:[t]});
  setTimeout(()=>{t.style.opacity='0';t.style.transform='translateX(12px)';t.style.transition='all .28s ease';setTimeout(()=>t.remove(),300);},3400);
}

/* ══════════ KEYBOARD ══════════ */
document.addEventListener('keydown',e=>{
  if(e.key==='Escape'){closeAlert();closeDrawer();document.querySelectorAll('.modal-bg').forEach(m=>m.classList.remove('show'));document.body.style.overflow='';}
  if((e.key==='n'||e.key==='N')&&document.activeElement.tagName!=='INPUT'&&document.activeElement.tagName!=='TEXTAREA'&&document.activeElement.tagName!=='SELECT')showAlert();
});

/* ══════════ INIT ══════════ */
document.addEventListener('DOMContentLoaded',()=>{
  lucide.createIcons();
  applyLayout();
  document.getElementById('menuSub').classList.add('open');
  // stagger stat card entrance
  document.querySelectorAll('.sg').forEach((c,i)=>{
    c.style.opacity='0';c.style.transform='translateY(12px)';
    setTimeout(()=>{c.style.transition='opacity .35s ease, transform .35s ease';c.style.opacity='1';c.style.transform='';},100+i*80);
  });
});