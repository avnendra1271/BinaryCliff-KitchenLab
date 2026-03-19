// ✨ Gemini API Integration Configuration
    const apiKey = "";

    let cursor; let lastOrderKitchenNote = "";
    let currentDiscountRate = 0; // Tracks applied promo codes

    // Real menu data from backend
    let categories = [];
    let menuItems = [];
    const RESTAURANT_ID = "550e8400-e29b-41d4-a716-446655440000"; // Default restaurant ID - should come from user context
    
    // WebSocket connection for real-time updates
    let menuWebSocket = null;
    
    // API Configuration
    const API_BASE_URL = '/api/menu';
    
    // Initialize menu data
    async function initializeMenuData() {
        try {
            await loadCompleteMenu();
            setupWebSocketConnection();
        } catch (error) {
            console.error('Failed to initialize menu data:', error);
            // Fallback to mock data if backend is not available
            initializeMockData();
        }
    }
    
    // Load complete menu from backend
    async function loadCompleteMenu() {
        const response = await fetch(`${API_BASE_URL}/complete?restaurantId=${RESTAURANT_ID}`);
        if (!response.ok) throw new Error('Failed to load menu');
        
        const menuData = await response.json();
        
        // Transform backend data to frontend format
        categories = menuData.map(cat => ({
            name: cat.name,
            description: cat.description,
            displayOrder: cat.displayOrder,
            isActive: cat.isActive,
            imageUrl: cat.imageUrl
        }));
        
        // Flatten all menu items
        menuItems = [];
        let idCounter = 1;
        menuData.forEach(category => {
            if (category.menuItems) {
                category.menuItems.forEach(item => {
                    menuItems.push({
                        id: idCounter++,
                        name: item.name,
                        category: category.name,
                        categoryId: category.id,
                        originalPrice: item.originalPrice,
                        price: item.price,
                        isVeg: item.isVegetarian,
                        popular: item.isPopular,
                        isSignature: item.isSignature,
                        img: item.imageUrl || `https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=400`,
                        desc: item.description || "Artisanal flavours crafted with heritage recipes and hand-ground spice blends for a refined experience.",
                        spiceLevel: item.spiceLevel,
                        calories: item.calories,
                        allergens: item.allergens,
                        customizable: item.customizable,
                        available: item.isAvailable
                    });
                });
            }
        });
        
        console.log('Loaded menu from backend:', { categories: categories.length, items: menuItems.length });
    }
    
    // Fallback mock data initialization
    function initializeMockData() {
        console.log('Using fallback mock data');
        categories = ["North Indian", "Indo-Chinese", "Veg Specialties", "Non-Veg Delights", "Tandoori Grill", "Wok Specials", "Street Chaat", "Fresh Salads", "Chef Specials", "Signature Bowls", "Seafood Treasures", "Lamb Selection", "Artisanal Breads", "Rice Creations", "Dessert Garden", "Beverages"];
        menuItems = []; let idCounter = 1;
        categories.forEach(cat => { for(let i = 1; i <= 8; i++) { const isOnSale = Math.random() > 0.7; const originalPrice = 14.95 + Math.floor(Math.random() * 10); const currentPrice = isOnSale ? originalPrice - 4.00 : originalPrice; menuItems.push({ id: idCounter++, name: `${cat} ${["Heritage", "Treasure", "Discovery", "Classic", "Deluxe", "Fusion", "Sizzle", "Elite"][i-1]}`, category: cat, originalPrice: isOnSale ? originalPrice : null, price: currentPrice, isVeg: Math.random() > 0.4, popular: i === 1, img: `https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=400`, desc: "Artisanal flavours crafted with heritage recipes and hand-ground spice blends for a refined experience." }); } });
    }
    
    // Setup WebSocket connection for real-time updates
    function setupWebSocketConnection() {
        if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
            const socket = new SockJS('/ws/menu');
            const stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                console.log('Connected to menu WebSocket');
                
                // Subscribe to general menu updates
                stompClient.subscribe('/topic/menu-updates', function(message) {
                    handleMenuUpdate(JSON.parse(message.body));
                });
                
                // Subscribe to restaurant-specific menu updates
                stompClient.subscribe(`/topic/restaurant/${RESTAURANT_ID}/menu`, function(message) {
                    handleMenuUpdate(JSON.parse(message.body));
                });
            });
            
            menuWebSocket = stompClient;
        }
    }
    
    // Handle real-time menu updates
    function handleMenuUpdate(message) {
        console.log('Received menu update:', message);
        
        switch (message.type) {
            case 'CATEGORY_CREATED':
            case 'CATEGORY_UPDATED':
                // Refresh categories
                loadCompleteMenu();
                break;
            case 'CATEGORY_DELETED':
                // Remove category and refresh
                loadCompleteMenu();
                break;
            case 'MENU_ITEM_CREATED':
            case 'MENU_ITEM_UPDATED':
                // Update menu item
                updateMenuItemInUI(message.data);
                break;
            case 'MENU_ITEM_DELETED':
                // Remove menu item
                removeMenuItemFromUI(message.itemId);
                break;
            case 'MENU_ITEM_AVAILABILITY_CHANGED':
                // Update item availability
                updateItemAvailability(message.data);
                break;
            case 'MENU_RELOAD':
                // Full menu reload
                loadCompleteMenu();
                if (typeof renderMenuPage === 'function') {
                    renderMenuPage();
                }
                break;
        }
    }
    
    // Update menu item in UI
    function updateMenuItemInUI(itemData) {
        const existingItem = menuItems.find(item => item.name === itemData.name);
        if (existingItem) {
            Object.assign(existingItem, {
                price: itemData.price,
                originalPrice: itemData.originalPrice,
                isAvailable: itemData.isAvailable,
                isPopular: itemData.isPopular,
                imageUrl: itemData.imageUrl
            });
            
            // Refresh menu if currently visible
            if (typeof renderMenuPage === 'function') {
                renderMenuPage();
            }
        }
    }
    
    // Remove menu item from UI
    function removeMenuItemFromUI(itemId) {
        menuItems = menuItems.filter(item => item.id != itemId);
        if (typeof renderMenuPage === 'function') {
            renderMenuPage();
        }
    }
    
    // Update item availability
    function updateItemAvailability(itemData) {
        const item = menuItems.find(item => item.name === itemData.name);
        if (item) {
            item.available = itemData.isAvailable;
            if (typeof renderMenuPage === 'function') {
                renderMenuPage();
            }
        }
    }
    
    // Track menu item views for analytics
    async function trackMenuItemView(itemId) {
        try {
            const item = menuItems.find(mi => mi.id === itemId);
            if (item && item.categoryId) {
                await fetch(`${API_BASE_URL}/items/${item.categoryId}/view?restaurantId=${RESTAURANT_ID}`, {
                    method: 'POST'
                });
            }
        } catch (error) {
            console.warn('Failed to track menu item view:', error);
        }
    }

    let cart = []; let currentDrawerItem = null; let currentDrawerQty = 1;

    // ✨ GEMINI API CORE LOGIC ✨
    // Standardized fetch wrapper with exponential backoff for the Gemini API
    async function callGemini(prompt, sysInstruction = "You are an elite, poetic culinary assistant and sommelier for 'Hakka Treasures', a premium Indo-Chinese restaurant. Your responses are very concise (1-2 sentences max), highly descriptive, and make the food sound absolutely irresistible. Do not use markdown.") {
        if (!apiKey) return "The AI Oracle is asleep. (API key missing)";

        const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=${apiKey}`;
        const payload = {
            contents: [{ parts: [{ text: prompt }] }],
            systemInstruction: { parts: [{ text: sysInstruction }] }
        };

        for (let attempt = 0; attempt < 5; attempt++) {
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                const data = await response.json();
                return data.candidates[0].content.parts[0].text;
            } catch (error) {
                if (attempt === 4) return "Our Master Chef is currently too busy cooking to answer. Please try again in a moment.";
                await new Promise(r => setTimeout(r, Math.pow(2, attempt) * 1000));
            }
        }
    }

    // ✨ FEATURE 1: AI Sommelier (Inside Item Drawer)
    window.getAIPairing = async function() {
        if (!currentDrawerItem) return;

        const textEl = document.getElementById('ai-pairing-text');
        const loaderEl = document.getElementById('ai-pairing-loader');

        // Show loading
        textEl.classList.add('hidden');
        loaderEl.classList.remove('hidden');

        const prompt = `A guest is looking at our dish: ${currentDrawerItem.name}. The description is: ${currentDrawerItem.desc}. Suggest one specific drink (cocktail or mocktail) or side dish that pairs perfectly with this flavor profile, and tell them why in 1 or 2 short sentences.`;

        const recommendation = await callGemini(prompt);

        // Hide loading, show result
        loaderEl.classList.add('hidden');
        textEl.innerText = `"${recommendation}"`;
        textEl.classList.remove('hidden');
    };

    // ✨ FEATURE 3: Smart Dietary Translator
    window.formatKitchenNote = async function() {
        const noteEl = document.getElementById('item-kitchen-note');
        const val = noteEl.value.trim();
        if(!val) return;

        const loader = document.getElementById('note-loader');
        loader.classList.remove('hidden');

        const prompt = `Rewrite this customer kitchen note into concise, professional culinary shorthand for expeditors (e.g., "ALLERGY ALERT: [x]", or "MODIFY: [y]"). Keep it extremely brief (under 15 words). Note: "${val}"`;
        const sys = "You are an expert expediter in a high-end professional kitchen. You speak in concise, uppercase culinary shorthand. No markdown.";

        const res = await callGemini(prompt, sys);
        noteEl.value = res.replace(/["*]/g, '');
        loader.classList.add('hidden');
    };

    // ✨ FEATURE 2: AI Taste Concierge (Menu Header)
    window.openAIConcierge = function() {
        const modal = document.getElementById('ai-concierge-overlay');
        const inner = document.getElementById('ai-concierge-modal');

        // Reset state
        document.getElementById('ai-concierge-input').value = "";
        document.getElementById('ai-concierge-result').classList.add('hidden');
        document.getElementById('ai-concierge-loader').classList.add('hidden');
        document.getElementById('ai-concierge-response').classList.add('hidden');

        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        // Small timeout for transition to apply
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            if (window.innerWidth >= 768) {
                inner.classList.remove('scale-95');
                inner.classList.add('scale-100');
            } else {
                inner.classList.add('is-open');
            }
        }, 10);
    };

    window.closeAIConcierge = function(e) {
        // If event is passed, ensure it's on the overlay, not inner modal
        if (e && e.target.id !== 'ai-concierge-overlay' && e.target.id !== 'close-concierge-btn') return;

        const modal = document.getElementById('ai-concierge-overlay');
        const inner = document.getElementById('ai-concierge-modal');

        modal.classList.add('opacity-0');
        if (window.innerWidth >= 768) {
            inner.classList.remove('scale-100');
            inner.classList.add('scale-95');
        } else {
            inner.classList.remove('is-open');
        }

        setTimeout(() => {
            modal.classList.add('hidden');
            document.body.style.overflow = '';
        }, 400); // Wait for transition
    };

    window.askAIConcierge = async function() {
        const input = document.getElementById('ai-concierge-input').value.trim();
        if (!input) return;

        const resultBox = document.getElementById('ai-concierge-result');
        const loader = document.getElementById('ai-concierge-loader');
        const responseText = document.getElementById('ai-concierge-response');

        // Show loading UI
        resultBox.classList.remove('hidden');
        loader.classList.remove('hidden');
        responseText.classList.add('hidden');

        // Build a small context map of the menu so Gemini knows what we actually serve
        // We'll pick 1 random item from each category to save tokens but give it context
        const sampledMenu = categories.map(cat => {
            const item = menuItems.find(m => m.category === cat);
            return item ? `${item.name} (${cat})` : '';
        }).filter(Boolean).join(', ');

        const prompt = `A guest at Hakka Treasures says: "${input}".
        Here is a sample of what we serve: ${sampledMenu}.
        Based on their craving, recommend exactly 1 or 2 specific dishes from our menu. Make the recommendation sound incredibly appetizing. Limit your response to 2 sentences.`;

        const recommendation = await callGemini(prompt);

        // Show result
        loader.classList.add('hidden');
        responseText.innerText = `Chef's Recommendation: ${recommendation}`;
        responseText.classList.remove('hidden');
    };

    // --- GLOBAL UI FUNCTIONS ---
    window.showPage = function(p) {
        const landing = document.getElementById('page-landing');
        const menu = document.getElementById('page-menu');
        if(landing) landing.classList.toggle('hidden', p !== 'landing');
        if(menu) menu.classList.toggle('hidden', p !== 'menu');
        const nl = document.getElementById('nav-links'); if(nl) nl.classList.toggle('invisible', p === 'menu');
        const nob = document.getElementById('nav-order-btn'); if(nob) nob.classList.toggle('hidden', p === 'menu');
        if (p === 'menu') renderMenuPage();
        window.scrollTo(0, 0); updateUI(); setupCursorHovers(); refreshReveal();
    };

    window.goToSection = function(id) {
        showPage('landing');
        setTimeout(() => {
            const el = document.getElementById(id);
            if (el) {
                const yOffset = -80;
                const y = el.getBoundingClientRect().top + window.pageYOffset + yOffset;
                window.scrollTo({top: y, behavior: 'smooth'});
            }
        }, 100);
    };

    window.toggleTheme = function() {
        const isLight = document.body.classList.toggle('light-mode');
        const icon = document.getElementById('theme-icon');
        if(icon) { icon.setAttribute('data-lucide', isLight ? 'moon' : 'sun'); lucide.createIcons(); }
        if (document.getElementById('page-menu') && !document.getElementById('page-menu').classList.contains('hidden')) renderMenuPage();
    };

    window.toggleCartDrawer = function() {
        const dr = document.getElementById('cart-drawer');
        const ov = document.getElementById('cart-drawer-overlay');
        if(!dr || !ov) return;
        const isOpen = dr.classList.contains('is-open');
        if(isOpen) { dr.classList.remove('is-open'); ov.classList.add('hidden'); document.body.style.overflow = ''; }
        else { dr.classList.add('is-open'); ov.classList.remove('hidden'); document.body.style.overflow = 'hidden'; }
    };

    window.openItemDrawer = function(id) {
        const i = menuItems.find(x => x.id === id); if (!i) return;
        
        // Track view for analytics
        trackMenuItemView(id);
        
        currentDrawerItem = i; currentDrawerQty = 1;
        document.getElementById('drawer-item-name').innerText = i.name;
        document.getElementById('drawer-item-price').innerText = `$${i.price.toFixed(2)}`;
        document.getElementById('drawer-qty').innerText = 1;
        document.getElementById('drawer-subtotal').innerText = `$${i.price.toFixed(2)}`;
        document.getElementById('item-kitchen-note').value = "";

        // ✨ Reset AI Pairing UI
        document.getElementById('ai-pairing-text').classList.add('hidden');
        document.getElementById('ai-pairing-loader').classList.add('hidden');
        document.getElementById('ai-pairing-text').innerText = "";

        document.getElementById('item-drawer-overlay').classList.remove('hidden');
        document.getElementById('item-drawer').classList.add('is-open');
        document.body.style.overflow = 'hidden';
        lucide.createIcons(); setupCursorHovers();
    };

    window.closeItemDrawer = function() {
        const ido = document.getElementById('item-drawer-overlay'); if(ido) ido.classList.add('hidden');
        const idr = document.getElementById('item-drawer'); if(idr) idr.classList.remove('is-open');
        document.body.style.overflow = ''; currentDrawerItem = null;
    };

    window.updateDrawerQty = function(d) {
        currentDrawerQty = Math.max(1, currentDrawerQty + d);
        document.getElementById('drawer-qty').innerText = currentDrawerQty;
        document.getElementById('drawer-subtotal').innerText = `$${(currentDrawerItem.price * currentDrawerQty).toFixed(2)}`;
    };

    window.confirmDrawerItem = function() { const ex = cart.find(x => x.id === currentDrawerItem.id); if (ex) ex.quantity += currentDrawerQty; else cart.push({ id: currentDrawerItem.id, quantity: currentDrawerQty }); updateUI(); closeItemDrawer(); };
    window.updateQuantity = function(id, d) { const i = cart.find(x => x.id === id); if (i) i.quantity += d; if (i && i.quantity <= 0) cart = cart.filter(x => x.id !== id); updateUI(); };

    window.goToCheckout = function() {
        if(cart.length === 0) {
            const notif = document.getElementById('empty-bag-notif');
            if(notif) {
                notif.classList.remove('hidden');
                setTimeout(() => notif.classList.add('hidden'), 3000);
            }
            return;
        }

        const bag = document.getElementById('bag-stage'); if(bag) bag.classList.add('hidden');
        const chk = document.getElementById('checkout-stage'); if(chk) chk.classList.remove('hidden');
        renderInvoice();
    };

    window.goToBag = function() {
        const chk = document.getElementById('checkout-stage'); if(chk) chk.classList.add('hidden');
        const bag = document.getElementById('bag-stage'); if(bag) bag.classList.remove('hidden');
    };

    window.getUserLocation = function() {
        const addrInput = document.getElementById('chk-address');
        if (!navigator.geolocation) { addrInput.value = "GPS not supported."; return; }
        addrInput.value = "Acquiring precision coordinates...";
        navigator.geolocation.getCurrentPosition(
            (pos) => { addrInput.value = `GPS: ${pos.coords.latitude.toFixed(4)}, ${pos.coords.longitude.toFixed(4)} (Verified)`; },
            () => { addrInput.value = "GPS Blocked. Enter address."; }
        );
    };

    window.applyPromoCode = function() {
        const input = document.getElementById('promo-input').value.trim().toUpperCase();
        const msg = document.getElementById('promo-message');
        msg.classList.remove('hidden');
        if(input === 'HAKKA15') {
            currentDiscountRate = 0.15;
            msg.innerText = "15% Welcome Reward Applied!";
            msg.className = "text-[10px] font-bold text-green-500 block uppercase tracking-widest mt-2";
        } else {
            currentDiscountRate = 0;
            msg.innerText = "Invalid or Expired Code.";
            msg.className = "text-[10px] font-bold text-red-500 block uppercase tracking-widest mt-2";
        }
        renderInvoice();
    };

    function renderInvoice() {
        const list = document.getElementById('invoice-items');
        if(!list) return;
        const rawSubtotal = cart.reduce((s, x) => s + (menuItems.find(m => m.id === x.id).price * x.quantity), 0);
        const discountAmt = rawSubtotal * currentDiscountRate;
        const subtotal = rawSubtotal - discountAmt;
        const tax = subtotal * 0.0825;
        const total = subtotal + tax;

        list.innerHTML = cart.map(ci => {
            const m = menuItems.find(x => x.id === ci.id);
            return `<div class="flex justify-between items-center text-xs text-left w-full gap-2" style="color: var(--text-main);"><span class="font-bold truncate flex-1">${ci.quantity}x ${m.name}</span><span class="font-mono shrink-0">$${(m.price * ci.quantity).toFixed(2)}</span></div>`;
        }).join('');

        const is = document.getElementById('inv-subtotal'); if(is) is.innerText = `$${rawSubtotal.toFixed(2)}`;

        const idr = document.getElementById('inv-discount-row');
        const idAmt = document.getElementById('inv-discount');
        if(currentDiscountRate > 0) {
            if(idr) idr.classList.remove('hidden');
            if(idAmt) idAmt.innerText = `-$${discountAmt.toFixed(2)}`;
        } else {
            if(idr) idr.classList.add('hidden');
        }

        const it = document.getElementById('inv-tax'); if(it) it.innerText = `$${tax.toFixed(2)}`;
        const itl = document.getElementById('inv-total'); if(itl) itl.innerText = `$${total.toFixed(2)}`;
    }

    window.confirmOrder = function() {
        const n = document.getElementById('chk-name');
        const e = document.getElementById('chk-email');
        const p = document.getElementById('chk-phone');
        const a = document.getElementById('chk-address');

        let notif = document.getElementById('chk-notif');

        if(!n || !e || !p || !a) return;

        if(!n.value || !e.value || !p.value || !a.value) {
            notif.innerText = 'Please complete all verification and address details to proceed.';
            notif.classList.remove('hidden');
            return;
        }
        notif.classList.add('hidden');

        const noteEl = document.getElementById('item-kitchen-note');
        if(noteEl) lastOrderKitchenNote = noteEl.value;
        document.getElementById('checkout-stage').classList.add('hidden');
        document.getElementById('tracking-stage').classList.remove('hidden');
        document.getElementById('home-status-banner').classList.remove('hidden');
        const readyList = document.getElementById('ready-items-list');

        const rawSubtotal = cart.reduce((s, x) => s + (menuItems.find(m => m.id === x.id).price * x.quantity), 0);
        const discountAmt = rawSubtotal * currentDiscountRate;
        const finalSubtotal = rawSubtotal - discountAmt;
        const tax = finalSubtotal * 0.0825;
        const finalTotal = finalSubtotal + tax;

        if(readyList) {
            readyList.innerHTML = cart.map(ci => {
                const m = menuItems.find(x => x.id === ci.id);
                return `<div class="flex justify-between items-center text-[11px] opacity-60 text-left w-full gap-2" style="color: var(--text-main);"><span class="font-bold truncate flex-1">${ci.quantity}x ${m.name}</span><span class="shrink-0">$${(m.price * ci.quantity).toFixed(2)}</span></div>`;
            }).join('');

            if(currentDiscountRate > 0) {
                readyList.innerHTML += `<div class="flex justify-between items-center text-[11px] text-green-500 font-bold text-left w-full gap-2 pt-2 border-t mt-2" style="border-color: var(--border-color);"><span class="truncate flex-1">Promo Applied (15%)</span><span class="shrink-0">-$${discountAmt.toFixed(2)}</span></div>`;
            }
        }
        const rtv = document.getElementById('ready-total-val'); if(rtv) rtv.innerText = `$${finalTotal.toFixed(2)}`;
        startLiveTracking();
    };

    function startLiveTracking() {
        const progress = document.getElementById('status-progress');
        const title = document.getElementById('status-title');
        const desc = document.getElementById('status-desc');
        const timeVal = document.getElementById('ready-time-val');
        const homeText = document.getElementById('home-status-text');
        if(progress) progress.style.width = '20%';
        setTimeout(() => {
            if(progress) progress.style.width = '100%';
            if(title) title.innerText = "Ready For Collection.";
            if(desc) desc.innerText = "Hurry, your food is getting cold!";
            if(timeVal) timeVal.innerText = "COLLECT NOW";
            if(homeText) homeText.innerText = "Hurry! Treasures ready for pickup!";
            const pd = document.getElementById('preparing-dots'); if(pd) pd.classList.add('hidden');
            const psb = document.getElementById('pickup-sim-btn'); if(psb) psb.classList.remove('hidden');
            lucide.createIcons();
        }, 6000);
    }

    window.orderPickedUp = function() {
        document.getElementById('status-title').innerText = "Discovery Collected.";
        document.getElementById('status-desc').innerText = "Enjoy your treasures.";
        document.getElementById('pickup-sim-btn').classList.add('hidden');
        document.getElementById('review-hub').classList.remove('hidden');
        document.getElementById('home-status-banner').classList.add('hidden');
        document.getElementById('ready-invoice-view').classList.add('hidden');
        document.getElementById('tracking-ui-box').classList.add('hidden');
        document.getElementById('discovery-hub-footer').classList.add('hidden');
        const noteMention = document.getElementById('feedback-note-mention');
        if(noteMention && lastOrderKitchenNote) { noteMention.innerText = `We ensured your request: "${lastOrderKitchenNote}" was handled precisely.`; }
        lucide.createIcons();
    };

    window.submitFeedback = async function() {
        const text = document.getElementById('feedback-text').value;
        const notif = document.getElementById('feedback-notif');

        if(!text) {
            notif.innerText = "Please provide your remarks.";
            notif.classList.remove('hidden');
            return;
        }
        notif.classList.add('hidden');

        // ✨ FEATURE 4: AI Chef's Sentiment Response
        const btn = document.getElementById('submit-review-btn');
        const origText = btn.innerHTML;
        btn.innerHTML = 'Transmitting... <i data-lucide="sparkles" class="w-4 h-4 animate-pulse"></i>';
        lucide.createIcons();

        const prompt = `A guest just left this review: "${text}". Write a short (2 sentences max), personalized, empathetic response thanking them or politely addressing their concern.`;
        const sys = "You are the Executive Chef of Hakka Treasures. You are gracious, professional, and deeply care about hospitality. Do not use markdown.";
        const response = await callGemini(prompt, sys);

        document.getElementById('review-hub').classList.add('hidden');
        document.getElementById('thank-you-stage').classList.remove('hidden');
        document.getElementById('status-title').classList.add('hidden');
        document.getElementById('status-desc').classList.add('hidden');
        document.getElementById('status-icon-box').classList.add('hidden');

        document.getElementById('ai-chef-response').innerText = `"${response}"`;
        document.getElementById('ai-chef-response-container').classList.remove('hidden');

        btn.innerHTML = origText;
        lucide.createIcons();
    };

    window.resetFlow = function() {
        cart = [];
        currentDiscountRate = 0;
        updateUI();
        const stages = ['bag-stage', 'checkout-stage', 'tracking-stage', 'review-hub', 'thank-you-stage'];
        stages.forEach(s => { const el = document.getElementById(s); if(el) el.classList.toggle('hidden', s !== 'bag-stage'); });
        const vis = ['status-title', 'status-desc', 'status-icon-box', 'ready-invoice-view', 'tracking-ui-box', 'discovery-hub-footer'];
        vis.forEach(v => { const el = document.getElementById(v); if(el) el.classList.remove('hidden'); });
        document.getElementById('home-status-banner').classList.add('hidden');

        const promoInput = document.getElementById('promo-input'); if(promoInput) promoInput.value = "";
        const promoMsg = document.getElementById('promo-message'); if(promoMsg) promoMsg.classList.add('hidden');

        showPage('landing');
    };

    window.claimReward = function() {
        const n = document.getElementById('vip-name').value;
        const e = document.getElementById('vip-email').value;
        const p = document.getElementById('vip-phone').value;
        const consent = document.getElementById('inner-consent').checked;
        const notif = document.getElementById('reward-notif');

        if(!n || !e || !p || !consent) {
            notif.innerText = "Please complete all fields and accept the terms.";
            notif.classList.remove('hidden');
            return;
        }
        notif.classList.add('hidden');

        document.getElementById('reward-form-container').classList.add('hidden');
        document.getElementById('reward-success-container').classList.remove('hidden');
        lucide.createIcons();
    };

    window.sendInquiry = function() {
        const n = document.getElementById('inq-name').value;
        const e = document.getElementById('inq-email').value;
        const m = document.getElementById('inq-message').value;
        const notif = document.getElementById('inq-notification');

        if(!n || !e || !m) {
            notif.innerHTML = '<span class="text-red-500">Please complete all fields to proceed.</span>';
            return;
        }
        notif.innerHTML = '<span class="text-green-500">Message successfully dispatched to our concierge.</span>';

        document.getElementById('inq-name').value = '';
        document.getElementById('inq-email').value = '';
        document.getElementById('inq-message').value = '';

        setTimeout(() => { notif.innerHTML = ''; }, 4000);
    };

    window.copyPromoCode = function() {
        const handle = "HAKKA15";
        const el = document.createElement('textarea');
        el.value = handle;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);

        const btn = document.getElementById('promo-code-display');
        if(btn) {
            const orig = btn.innerText;
            btn.innerText = 'COPIED!';
            setTimeout(() => { btn.innerText = orig; }, 2000);
        }

        const floatingText = document.getElementById('floating-banner-text');
        if(floatingText) {
            const origFloat = floatingText.innerText;
            floatingText.innerText = 'COPIED!';
            setTimeout(() => { floatingText.innerText = origFloat; }, 2000);
        }

        const menuBannerText = document.getElementById('menu-banner-text');
        if(menuBannerText) {
            const origMenu = menuBannerText.innerText;
            menuBannerText.innerText = 'CODE COPIED!';
            setTimeout(() => { menuBannerText.innerText = origMenu; }, 2000);
        }
    };

    window.copyInstaHandle = function() {
        const handle = "@hakka_treasures";
        const el = document.createElement('textarea');
        el.value = handle;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);

        const btn = document.getElementById('share-exp-btn');
        btn.innerHTML = 'COPIED! <i data-lucide="check" class="w-3 h-3 inline"></i>';
        lucide.createIcons();
        setTimeout(() => {
            btn.innerHTML = '@hakka_treasures <i data-lucide="copy" class="w-3 h-3 inline"></i>';
            lucide.createIcons();
        }, 2000);
    };

    window.manualScroll = function(id, dir) { const c = document.getElementById(id); const amt = 320; if(c) c.scrollBy({ left: dir === 'left' ? -amt : amt, behavior: 'smooth' }); };

    // --- Smart Scroll Handler ---
    window.addEventListener('scroll', () => {
        const nav = document.getElementById('menu-sticky-nav');
        const menuPage = document.getElementById('page-menu');
        if(nav && menuPage && !menuPage.classList.contains('hidden')) {
            if(window.scrollY > 80) {
                nav.classList.add('menu-scrolled');
            } else {
                nav.classList.remove('menu-scrolled');
            }
        }
    });

    // --- Mobile Auto-Scroll / Cycle Features ---
    function initAutoFeatures() {
        // Reviews Auto-Scroll
        const rContainer = document.getElementById('reviews-container');
        if (rContainer) {
            let rScrollAmount = 0;
            setInterval(() => {
                if (window.innerWidth < 768 && !document.getElementById('page-landing').classList.contains('hidden')) {
                    const cardWidth = rContainer.clientWidth;
                    rScrollAmount += cardWidth;
                    if (rScrollAmount >= rContainer.scrollWidth - cardWidth / 2) rScrollAmount = 0;
                    rContainer.scrollTo({ left: rScrollAmount, behavior: 'smooth' });
                }
            }, 3000);
        }

        // Stats Auto-Cycle (Fading)
        const statsSlides = document.querySelectorAll('.stat-slide');
        if (statsSlides.length) {
            let currentStat = 0;
            setInterval(() => {
                if (window.innerWidth < 768 && !document.getElementById('page-landing').classList.contains('hidden')) {
                    statsSlides.forEach((slide, index) => {
                        if (index === currentStat) {
                            slide.classList.remove('opacity-100', 'z-10');
                            slide.classList.add('opacity-0', 'z-0');
                        }
                    });
                    currentStat = (currentStat + 1) % statsSlides.length;
                    statsSlides[currentStat].classList.remove('opacity-0', 'z-0');
                    statsSlides[currentStat].classList.add('opacity-100', 'z-10');
                }
            }, 3500);
        }

        // Signature Collection Auto-Scroll
        const sigContainer = document.getElementById('favorites-scroll-container');
        if (sigContainer) {
            let sigScrollAmount = 0;
            setInterval(() => {
                if (window.innerWidth < 768 && !document.getElementById('page-landing').classList.contains('hidden')) {
                    // Card width (85vw) + gap (1.5rem = 24px)
                    const cardWidth = (window.innerWidth * 0.85) + 24;
                    sigScrollAmount += cardWidth;
                    // Reset if we reach the end
                    if (sigScrollAmount >= sigContainer.scrollWidth - window.innerWidth) {
                        sigScrollAmount = 0;
                    }
                    sigContainer.scrollTo({ left: sigScrollAmount, behavior: 'smooth' });
                }
            }, 4000);
        }
    }

    function renderMenuPage() {
        const catNav = document.getElementById('menu-sticky-categories');
        const menuCont = document.getElementById('continuous-menu-container');
        if(!catNav || !menuCont) return;
        const searchVal = document.getElementById('menu-search-full').value.toLowerCase();
        catNav.innerHTML = categories.map(cat => `<button data-cat-id="${cat}" onclick="scrollToCategory('${cat}')" class="category-btn whitespace-nowrap px-6 py-2 rounded-full text-[9px] font-black uppercase tracking-[0.2em] transition-all cursor-hover text-left">${cat}</button>`).join('');
        menuCont.innerHTML = categories.map(cat => {
            const items = menuItems.filter(i => (i.category === cat) && (i.name.toLowerCase().includes(searchVal) || i.desc.toLowerCase().includes(searchVal)));
            if (items.length === 0) return '';
            return `<div id="section-${cat}" class="menu-section reveal"><div class="flex items-center gap-6 mb-10 text-left"><h3 class="text-4xl md:text-5xl font-black uppercase tracking-tighter leading-none" style="color: var(--text-main);">${cat}</h3><div class="h-[1px] flex-grow opacity-20" style="background-color: var(--text-main);"></div></div>
                    <div class="grid grid-rows-3 grid-flow-col gap-4 md:gap-6 overflow-x-auto snap-x snap-mandatory no-scrollbar pb-8 auto-cols-[85vw] md:auto-cols-[calc(50%-12px)]">
                        ${items.map(item => {
                            const discountPerc = item.originalPrice ? Math.round(((item.originalPrice - item.price) / item.originalPrice) * 100) : 0;
                            return `<div class="menu-row snap-center flex flex-col justify-center gap-4 p-5 md:p-8 rounded-[2.5rem] cursor-hover group transition-all h-full w-full" onclick="openItemDrawer(${item.id})">
                                <div class="flex items-center gap-4 w-full">
                                    <div class="relative shrink-0 text-left"><img src="${item.img}" class="menu-item-img w-20 h-20 md:w-28 md:h-28 rounded-[2rem] object-cover transition-all duration-700 shadow-md border border-black/5"><div class="absolute -top-1 -left-1 bg-white p-1 rounded-lg shadow-md border border-black/5"><div class="${item.isVeg ? 'veg-tag' : 'nonveg-tag'} p-[0.5px]"><div class="${item.isVeg ? 'veg-dot' : 'nonveg-dot'} w-1 h-1 rounded-full text-left"></div></div></div></div>
                                    <div class="flex-grow min-w-0">
                                        <div class="flex justify-between items-start mb-2 gap-3">
                                            <div class="flex flex-col text-left min-w-0 flex-1">
                                                <h4 class="text-base md:text-xl font-black uppercase tracking-tighter truncate leading-tight text-left" style="color: var(--text-main);">${item.name}</h4>
                                                ${item.popular ? `<span class="text-[8px] font-black uppercase text-orange-600 tracking-widest mt-1 truncate">Hakka Signature</span>`:''}
                                            </div>
                                            <div class="flex items-center gap-3 sm:gap-5 text-left shrink-0">
                                                <div class="flex flex-col items-end text-left">
                                                    ${item.originalPrice ? `<span class="text-[10px] line-through opacity-40 font-bold">$${item.originalPrice.toFixed(2)}</span>` : ''}
                                                    <div class="flex items-center gap-1 sm:gap-2">
                                                        ${item.originalPrice ? `<span class="bg-red-500/10 text-red-500 border border-red-500/20 px-2 py-0.5 rounded text-[8px] font-black tracking-widest hidden sm:inline-block">-${discountPerc}% OFF</span>` : ''}
                                                        <span class="font-black text-lg md:text-3xl" style="color: var(--text-main);">$${item.price.toFixed(2)}</span>
                                                    </div>
                                                </div>
                                                <div class="add-btn-circle w-8 h-8 md:w-12 md:h-12 rounded-full border border-orange-600/30 flex items-center justify-center transition-all group-hover:shadow-lg group-hover:shadow-orange-600/20 shrink-0" style="color: var(--text-main);"><i data-lucide="plus" class="w-4 h-4 text-left"></i></div>
                                            </div>
                                        </div>
                                        <p class="text-slate-500 text-[10px] leading-relaxed line-clamp-2 italic font-medium opacity-60 group-hover:opacity-100 transition-colors text-left" style="color: var(--text-main);">${item.desc}</p>
                                    </div>
                                </div>
                            </div>`}).join('')}
                    </div></div>`;
        }).join('');
        initScrollSpy(); lucide.createIcons(); setupCursorHovers(); refreshReveal();
    }

    window.scrollToCategory = function(cat) { const el = document.getElementById(`section-${cat}`); if (el) window.scrollTo({ top: el.offsetTop - 180, behavior: 'smooth' }); };
    function initScrollSpy() { const obs = new IntersectionObserver(es => es.forEach(e => { if (e.isIntersecting) updateActiveCategoryTab(e.target.id.replace('section-', '')); }), { threshold: 0.1, rootMargin: "-180px 0px -50% 0px" }); document.querySelectorAll('.menu-section').forEach(s => obs.observe(s)); }
    function updateActiveCategoryTab(id) { const container = document.getElementById('menu-sticky-categories'); if(!container) return; document.querySelectorAll('.category-btn').forEach(btn => { if(btn.dataset.catId === id) { btn.classList.add('active'); container.scrollTo({ left: btn.offsetLeft - (container.clientWidth / 2) + (btn.clientWidth / 2), behavior: 'smooth' }); } else { btn.classList.remove('active'); } }); }

    function updateUI() {
        const totalItems = cart.reduce((s, x) => s + x.quantity, 0);
        const rawTotal = cart.reduce((s, x) => s + (menuItems.find(m => m.id === x.id).price * x.quantity), 0);
        const finalTotal = rawTotal - (rawTotal * currentDiscountRate);

        const trackingStage = document.getElementById('tracking-stage');
        const orderActive = trackingStage && !trackingStage.classList.contains('hidden');

        // Top Nav Badge
        const nb = document.getElementById('cart-badge-nav'); if (nb) { nb.innerText = totalItems; nb.classList.toggle('opacity-0', totalItems === 0); }

        // Floating Pill (Desktop & Mobile)
        const hub = document.getElementById('floating-cart-hub');
        if (hub) {
            if (totalItems === 0 || orderActive) {
                hub.classList.add('translate-y-20', 'opacity-0', 'pointer-events-none');
                hub.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
            } else {
                hub.classList.remove('translate-y-20', 'opacity-0', 'pointer-events-none');
                hub.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
            }
        }

        // Empty Bag Validation State
        const checkoutBtn = document.getElementById('checkout-btn');
        if (checkoutBtn) {
            if (totalItems === 0) {
                checkoutBtn.classList.add('opacity-50', 'pointer-events-none');
            } else {
                checkoutBtn.classList.remove('opacity-50', 'pointer-events-none');
            }
        }

        const hcc = document.getElementById('hub-cart-count'); if (hcc) hcc.innerText = totalItems;
        const hct = document.getElementById('hub-cart-total'); if (hct) hct.innerText = `$${finalTotal.toFixed(2)}`;
        const bt = document.getElementById('bag-total'); if (bt) bt.innerText = `$${finalTotal.toFixed(2)}`;
        renderCartLists();
    }

    function renderCartLists() {
        const html = cart.length === 0 ? `<div class="text-center py-20 opacity-20 italic text-sm text-slate-800" style="color: var(--text-main);">Bag is empty.</div>` : cart.map(ci => {
            const m = menuItems.find(x => x.id === ci.id);
            return `<div class="flex items-center justify-between gap-3 p-4 md:p-5 bg-white rounded-3xl border border-slate-100 shadow-sm transition-all hover:scale-[1.02] text-slate-900 w-full overflow-hidden">
                <div class="flex-grow min-w-0 text-left flex-1">
                    <h4 class="text-[10px] font-black uppercase text-slate-800 mb-1 leading-tight truncate text-left text-slate-900">${m.name}</h4>
                    <p class="text-[10px] font-bold text-orange-600 text-left">$${(m.price * ci.quantity).toFixed(2)}</p>
                </div>
                <div class="flex items-center gap-2 md:gap-3 bg-slate-50 p-1 rounded-xl shadow-inner border border-slate-100 text-slate-900 shrink-0">
                    <button onclick="updateQuantity(${m.id},-1)" class="w-7 h-7 md:w-8 md:h-8 flex items-center justify-center hover:bg-orange-600 rounded-lg group transition-all active:scale-90"><i data-lucide="minus" class="w-3.5 h-3.5 text-slate-400 group-hover:text-white transition-colors text-slate-400"></i></button>
                    <span class="w-4 text-center text-xs font-black text-slate-900">${ci.quantity}</span>
                    <button onclick="updateQuantity(${m.id},1)" class="w-7 h-7 md:w-8 md:h-8 flex items-center justify-center hover:bg-orange-600 rounded-lg group transition-all active:scale-90"><i data-lucide="plus" class="w-3.5 h-3.5 text-slate-400 group-hover:text-white transition-colors text-white"></i></button>
                </div>
            </div>`;
        }).join('');
        const bil = document.getElementById('bag-items-list'); if (bil) bil.innerHTML = html;
        lucide.createIcons(); setupCursorHovers();
    }

    const revealObserver = new IntersectionObserver(es => es.forEach(e => { if (e.isIntersecting) e.target.classList.add('active'); }), { threshold: 0.1 });
    function refreshReveal() { document.querySelectorAll('.reveal').forEach(el => revealObserver.observe(el)); }
    function setupCursorHovers() { if (!cursor) return; document.querySelectorAll('.cursor-hover, button, a, label, input, textarea').forEach(el => { el.onmouseenter = () => { cursor.style.transform = 'translate(-50%, -50%) scale(2.5)'; cursor.style.background = 'rgba(234, 88, 12, 0.1)'; }; el.onmouseleave = () => { cursor.style.transform = 'translate(-50%, -50%) scale(1)'; cursor.style.background = 'rgba(234, 88, 12, 0.4)'; }; }); }
    document.addEventListener('mousemove', e => { if (cursor) { cursor.style.left = e.clientX + 'px'; cursor.style.top = e.clientY + 'px'; cursor.style.display = 'block'; } });

    function renderFavorites() {
        const container = document.getElementById('favorites-scroll-container');
        if (!container) return;
        const popular = menuItems.filter(i => i.popular).slice(0, 8);

        container.innerHTML = popular.map((item, index) => {
            return `
            <div class="bento-card group rounded-[2.5rem] overflow-hidden relative snap-start cursor-hover text-left shrink-0 w-[85vw] sm:w-[320px] lg:w-[calc((100%-4rem)/3)] shadow-xl hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 border flex flex-col" style="background-color: var(--card-bg); border-color: var(--border-color);" onclick="openItemDrawer(${item.id})">

                <!-- ANIMATED LINE -->
                <div class="absolute bottom-0 left-0 h-1.5 bg-orange-600 w-0 group-hover:w-full transition-all duration-[600ms] ease-out z-[100]"></div>

                <div class="absolute -top-6 -right-6 font-black text-9xl italic opacity-5 pointer-events-none z-0 transition-transform duration-700 group-hover:scale-110" style="color: var(--text-main);">0${index + 1}</div>

                <div class="relative h-56 md:h-64 overflow-hidden border-b shrink-0 z-10" style="border-color: var(--border-color);">
                    <img src="${item.img}" class="absolute inset-0 w-full h-full object-cover transition-transform duration-[2s] group-hover:scale-105">

                    <div class="absolute top-5 left-5 bg-white/90 dark:bg-black/50 backdrop-blur-md p-1.5 rounded-lg shadow-md border border-black/10 z-20">
                        <div class="${item.isVeg ? 'border-green-600' : 'border-red-600'} border p-[1px] bg-white rounded-sm">
                            <div class="${item.isVeg ? 'bg-green-600' : 'bg-red-600'} w-1.5 h-1.5 rounded-full"></div>
                        </div>
                    </div>
                    <div class="absolute top-5 right-5 bg-orange-600 text-white text-[8px] font-black uppercase tracking-widest px-3 py-1.5 rounded-full z-20 shadow-lg">
                        ${item.category}
                    </div>
                </div>

                <div class="p-6 md:p-8 flex flex-col flex-grow relative z-10 text-left bg-transparent">
                    <div class="flex justify-between items-start mb-3 gap-4">
                        <h3 class="text-xl md:text-2xl font-black uppercase tracking-tighter leading-tight" style="color: var(--text-main);">${item.name}</h3>
                        <span class="text-orange-600 font-black text-xl md:text-2xl tracking-tighter shrink-0">$${item.price.toFixed(2)}</span>
                    </div>

                    <p class="text-slate-500 text-[11px] md:text-xs leading-relaxed italic font-medium line-clamp-2 mb-6 flex-grow">"${item.desc}"</p>

                    <div class="flex items-center justify-between mt-auto pt-6 border-t" style="border-color: var(--border-color);">
                        <span class="text-[9px] font-black uppercase tracking-widest text-slate-400 group-hover:text-orange-600 transition-colors">Hakka Signature</span>
                        <div class="w-10 h-10 md:w-12 md:h-12 rounded-full border flex items-center justify-center transition-all duration-300 group-hover:bg-orange-600 group-hover:border-orange-600 group-hover:shadow-lg shadow-orange-600/30 group-hover:text-white" style="border-color: var(--border-color); color: var(--text-main);">
                            <i data-lucide="plus" class="w-4 h-4 md:w-5 md:h-5 transition-transform duration-500 group-hover:rotate-90"></i>
                        </div>
                    </div>
                </div>
            </div>`;
        }).join('');
        lucide.createIcons(); setupCursorHovers(); refreshReveal();
    }

    // Initialize menu data when DOM is ready
    window.addEventListener('DOMContentLoaded', () => {
        cursor = document.getElementById('custom-cursor');
        
        // Initialize real menu data
        initializeMenuData().then(() => {
            renderFavorites();
            showPage('landing');
            updateUI();
            initAutoFeatures();
        });
    });
