// Menu Management JavaScript for Kitchen Admin
// Real backend integration with WebSocket support

const RESTAURANT_ID = "550e8400-e29b-41d4-a716-446655440000"; // Default restaurant ID
const API_BASE_URL = '/api/menu';

// WebSocket connection for real-time updates
let menuWebSocket = null;

// Data storage
let categories = [];
let menuItems = [];

// Initialize menu management
document.addEventListener('DOMContentLoaded', function() {
    setupWebSocketConnection();
    loadCategories();
    loadMenuItems();
});

// Setup WebSocket connection
function setupWebSocketConnection() {
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        const socket = new SockJS('/ws/admin/menu');
        const stompClient = Stomp.over(socket);
        
        stompClient.connect({}, function(frame) {
            console.log('Connected to admin menu WebSocket');
            
            // Subscribe to menu updates
            stompClient.subscribe('/topic/menu-updates', function(message) {
                handleMenuUpdate(JSON.parse(message.body));
            });
            
            // Subscribe to restaurant-specific menu updates
            stompClient.subscribe(`/topic/restaurant/${RESTAURANT_ID}/admin/menu`, function(message) {
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
            loadCategories();
            break;
        case 'CATEGORY_DELETED':
            loadCategories();
            loadMenuItems();
            break;
        case 'MENU_ITEM_CREATED':
        case 'MENU_ITEM_UPDATED':
            loadMenuItems();
            break;
        case 'MENU_ITEM_DELETED':
            loadMenuItems();
            break;
        case 'MENU_ITEM_AVAILABILITY_CHANGED':
            loadMenuItems();
            break;
        case 'MENU_STATISTICS':
            updateStatistics(message.data);
            break;
    }
}

// Load categories from backend
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/categories?restaurantId=${RESTAURANT_ID}`);
        if (!response.ok) throw new Error('Failed to load categories');
        
        categories = await response.json();
        renderCategories();
        updateCategoryFilter();
    } catch (error) {
        console.error('Failed to load categories:', error);
        toast('Failed to load categories', 'error');
    }
}

// Load menu items from backend
async function loadMenuItems() {
    try {
        const response = await fetch(`${API_BASE_URL}/items?restaurantId=${RESTAURANT_ID}`);
        if (!response.ok) throw new Error('Failed to load menu items');
        
        menuItems = await response.json();
        renderMenuItems();
        updateMenuItemsCount();
    } catch (error) {
        console.error('Failed to load menu items:', error);
        toast('Failed to load menu items', 'error');
    }
}

// Render categories table
function renderCategories() {
    const catList = document.getElementById('catList');
    const catEmpty = document.getElementById('catEmpty');
    
    if (!catList) return;
    
    if (categories.length === 0) {
        catList.innerHTML = '';
        if (catEmpty) catEmpty.classList.remove('hidden');
        return;
    }
    
    if (catEmpty) catEmpty.classList.add('hidden');
    
    catList.innerHTML = categories.map(cat => `
        <table class="w-full">
            <tbody>
                <tr class="tbl-row">
                    <td class="w-10" style="padding:10px 12px">
                        <div class="cursor-move text-gray-400">⠿</div>
                    </td>
                    <td>
                        <div class="w-12 h-12 rounded-xl flex items-center justify-center text-xl flex-shrink-0" style="background:var(--orange2)">
                            ${cat.imageUrl ? `<img src="${cat.imageUrl}" class="w-full h-full object-cover rounded-xl" />` : '🍽️'}
                        </div>
                    </td>
                    <td>
                        <div class="font-bold" style="color:var(--text)">${cat.name}</div>
                        <div class="text-xs" style="color:var(--muted)">${cat.description || 'No description'}</div>
                    </td>
                    <td>
                        <span class="font-black text-sm" style="color:var(--orange)">${cat.activeMenuItemCount || 0} items</span>
                    </td>
                    <td class="hidden sm:table-cell">
                        <span class="font-black text-sm">${cat.displayOrder}</span>
                    </td>
                    <td>
                        <span class="badge ${cat.isActive ? 'bact' : 'bina'}">${cat.isActive ? '● Active' : '● Inactive'}</span>
                    </td>
                    <td>
                        <div class="flex gap-1.5">
                            <button onclick="editCategory('${cat.id}')" class="btn btn-g btn-xs">
                                <i data-lucide="pencil" style="width:11px;height:11px"></i>
                            </button>
                            <button onclick="toggleCategoryStatus('${cat.id}')" class="btn btn-g btn-xs">
                                <i data-lucide="${cat.isActive ? 'eye-off' : 'eye'}" style="width:11px;height:11px"></i>
                            </button>
                            <button onclick="confirmDeleteCategory('${cat.id}', '${cat.name}')" class="btn btn-d btn-xs">
                                <i data-lucide="trash-2" style="width:11px;height:11px"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    `).join('');
    
    lucide.createIcons();
}

// Render menu items grid
function renderMenuItems() {
    const miGrid = document.getElementById('miGrid');
    const miEmpty = document.getElementById('miEmpty');
    const miSearch = document.getElementById('miSearch');
    const miCat = document.getElementById('miCat');
    const miAvail = document.getElementById('miAvail');
    
    if (!miGrid) return;
    
    let filteredItems = [...menuItems];
    
    // Apply search filter
    if (miSearch && miSearch.value) {
        const searchTerm = miSearch.value.toLowerCase();
        filteredItems = filteredItems.filter(item => 
            item.name.toLowerCase().includes(searchTerm) ||
            item.description.toLowerCase().includes(searchTerm) ||
            item.categoryName.toLowerCase().includes(searchTerm)
        );
    }
    
    // Apply category filter
    if (miCat && miCat.value) {
        filteredItems = filteredItems.filter(item => item.categoryName === miCat.value);
    }
    
    // Apply availability filter
    if (miAvail && miAvail.value !== '') {
        const isAvailable = miAvail.value === '1';
        filteredItems = filteredItems.filter(item => item.isAvailable === isAvailable);
    }
    
    if (filteredItems.length === 0) {
        miGrid.innerHTML = '';
        if (miEmpty) miEmpty.classList.remove('hidden');
        return;
    }
    
    if (miEmpty) miEmpty.classList.add('hidden');
    
    miGrid.innerHTML = filteredItems.map(item => `
        <div class="card p-4 card-interactive">
            <div class="relative h-32 mb-3 rounded-xl overflow-hidden">
                <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=400'}" 
                     class="w-full h-full object-cover" />
                <div class="absolute top-2 right-2">
                    <span class="badge ${item.isAvailable ? 'bact' : 'bina'} text-[9px]">
                        ${item.isAvailable ? 'Available' : 'Unavailable'}
                    </span>
                </div>
                ${item.isVegetarian ? `
                    <div class="absolute top-2 left-2">
                        <div class="w-6 h-6 bg-white rounded-full flex items-center justify-center">
                            <div class="w-3 h-3 bg-green-600 rounded-full"></div>
                        </div>
                    </div>
                ` : ''}
            </div>
            <div class="font-black text-sm tracking-tight mb-1" style="color:var(--text)">${item.name}</div>
            <div class="text-xs mb-2" style="color:var(--muted)">${item.categoryName}</div>
            <div class="flex items-center justify-between mb-3">
                <div>
                    ${item.originalPrice ? `
                        <span class="text-xs line-through opacity-40">$${item.originalPrice}</span>
                    ` : ''}
                    <span class="font-black text-lg" style="color:var(--orange)">$${item.price}</span>
                </div>
                ${item.isPopular ? '<span class="text-[8px] font-black uppercase text-orange-600">Popular</span>' : ''}
            </div>
            <div class="flex gap-1.5">
                <button onclick="editMenuItem('${item.id}')" class="btn btn-g btn-xs flex-1">
                    <i data-lucide="pencil" style="width:11px;height:11px"></i>
                </button>
                <button onclick="toggleMenuItemAvailability('${item.id}')" class="btn btn-g btn-xs flex-1">
                    <i data-lucide="${item.isAvailable ? 'eye-off' : 'eye'}" style="width:11px;height:11px"></i>
                </button>
                <button onclick="confirmDeleteMenuItem('${item.id}', '${item.name}')" class="btn btn-d btn-xs flex-1">
                    <i data-lucide="trash-2" style="width:11px;height:11px"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    lucide.createIcons();
}

// Update category filter dropdown
function updateCategoryFilter() {
    const miCat = document.getElementById('miCat');
    if (!miCat) return;
    
    const currentValue = miCat.value;
    miCat.innerHTML = '<option value="">All Categories</option>' +
        categories.map(cat => `<option value="${cat.name}">${cat.name}</option>`).join('');
    miCat.value = currentValue;
}

// Update menu items count
function updateMenuItemsCount() {
    const miCount = document.getElementById('miCount');
    if (miCount) {
        const availableCount = menuItems.filter(item => item.isAvailable).length;
        miCount.textContent = `${menuItems.length} items · ${availableCount} available`;
    }
}

// Toggle category status
async function toggleCategoryStatus(categoryId) {
    try {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}/toggle-status?restaurantId=${RESTAURANT_ID}`, {
            method: 'PATCH'
        });
        
        if (!response.ok) throw new Error('Failed to toggle category status');
        
        toast('Category status updated successfully', 'success');
        loadCategories();
    } catch (error) {
        console.error('Failed to toggle category status:', error);
        toast('Failed to update category status', 'error');
    }
}

// Toggle menu item availability
async function toggleMenuItemAvailability(itemId) {
    try {
        const response = await fetch(`${API_BASE_URL}/items/${itemId}/toggle-availability?restaurantId=${RESTAURANT_ID}`, {
            method: 'PATCH'
        });
        
        if (!response.ok) throw new Error('Failed to toggle item availability');
        
        toast('Item availability updated successfully', 'success');
        loadMenuItems();
    } catch (error) {
        console.error('Failed to toggle item availability:', error);
        toast('Failed to update item availability', 'error');
    }
}

// Delete category
async function deleteCategory(categoryId) {
    try {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}?restaurantId=${RESTAURANT_ID}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete category');
        
        toast('Category deleted successfully', 'success');
        loadCategories();
    } catch (error) {
        console.error('Failed to delete category:', error);
        toast('Failed to delete category', 'error');
    }
}

// Delete menu item
async function deleteMenuItem(itemId) {
    try {
        const response = await fetch(`${API_BASE_URL}/items/${itemId}?restaurantId=${RESTAURANT_ID}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete menu item');
        
        toast('Menu item deleted successfully', 'success');
        loadMenuItems();
    } catch (error) {
        console.error('Failed to delete menu item:', error);
        toast('Failed to delete menu item', 'error');
    }
}

// Confirmation dialogs
function confirmDeleteCategory(categoryId, categoryName) {
    if (confirm(`Are you sure you want to delete category "${categoryName}"? This will also delete all items in this category.`)) {
        deleteCategory(categoryId);
    }
}

function confirmDeleteMenuItem(itemId, itemName) {
    if (confirm(`Are you sure you want to delete "${itemName}"?`)) {
        deleteMenuItem(itemId);
    }
}

// Clear menu items filters
function clearMIF() {
    const miSearch = document.getElementById('miSearch');
    const miCat = document.getElementById('miCat');
    const miAvail = document.getElementById('miAvail');
    
    if (miSearch) miSearch.value = '';
    if (miCat) miCat.value = '';
    if (miAvail) miAvail.value = '';
    
    renderMenuItems();
}

// Placeholder functions for add/edit modals (to be implemented)
function openAddCat() {
    toast('Add category modal coming soon', 'info');
}

function openAddItem() {
    toast('Add item modal coming soon', 'info');
}

function editCategory(categoryId) {
    toast('Edit category modal coming soon', 'info');
}

function editMenuItem(itemId) {
    toast('Edit item modal coming soon', 'info');
}

// Update statistics (placeholder)
function updateStatistics(data) {
    console.log('Statistics updated:', data);
}

// Toast notification function (assuming it exists globally)
function toast(message, type = 'info') {
    if (typeof window.toast === 'function') {
        window.toast(message, type);
    } else {
        console.log(`[${type}] ${message}`);
    }
}
