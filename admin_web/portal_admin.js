// --- 1. REUSABLE UI COMPONENTS ---
const UI = {
    card: (title, content, action = '') => `
        <div class="card-ui">
            <div class="card-head">
                <span>${title}</span>
                ${action ? `<div class="card-action">${action}</div>` : ''}
            </div>
            <div class="card-ui-body">${content}</div>
        </div>
    `,
    badge: (text, type = 'ok') => `<span class="status-tag ${type.toLowerCase()}">${text}</span>`,
    pill: (text, type = 'open') => `<span class="status-pill ${type.toLowerCase()}">${text}</span>`,
    infoBanner: (text, icon = 'ℹ️') => `
        <div style="background:#FFF7E6; border:1px solid #FFD591; padding:16px; border-radius:4px; display:flex; gap:12px; align-items:flex-start; margin-bottom:24px;">
            <span style="font-size:1.2rem;">${icon}</span>
            <div style="font-size:0.85rem; color:#874D00;">${text}</div>
        </div>
    `,
    table: (headers, rows) => `
        <table class="table-reservations">
            <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead>
            <tbody>${rows.length ? rows.join('') : '<tr><td colspan="' + headers.length + '" style="text-align:center;">No data available</td></tr>'}</tbody>
        </table>
    `
};

// --- 2. FIREBASE CONFIG ---
const firebaseConfig = {
  apiKey: "AIzaSyAYlLHaBBfqjoyG5mwZ_0VxVmMJttQBCEY",
  authDomain: "stay-desk-pms-app.firebaseapp.com",
  databaseURL: "https://stay-desk-pms-default-rtdb.asia-southeast1.firebasedatabase.app/",
  projectId: "stay-desk-pms-app",
  storageBucket: "stay-desk-pms-app.firebasestorage.app",
  messagingSenderId: "160367544031",
  appId: "1:160367544031:web:d9177856450c3938d30b6a",
  measurementId: "G-FP1R9GQR1E"
};

// Initialize Firebase
try {
    firebase.initializeApp(firebaseConfig);
    console.log("Firebase initialized successfully");
} catch (e) {
    console.error("Firebase Initialization Error:", e);
}
const db = firebase.database(); // Use databaseURL from config automatically

let hotelId = "GangaHomes_001";
let allRooms = [];
let currentPMSDate = new Date(); // Track current start date for calendar

// --- 3. CORE NAVIGATION ---
window.toggleDropdown = (e, tabId) => {
    if (e) e.stopPropagation();
    const item = e ? e.currentTarget : null;
    if (!item) return;
    const isShowing = item.classList.contains('show-dropdown');
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('show-dropdown'));
    if (!isShowing) item.classList.add('show-dropdown');
};

document.addEventListener('click', () => {
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('show-dropdown'));
});

window.switchTab = (tabId) => {
    console.log("Switching to tab:", tabId);
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    const target = document.getElementById(`tab-${tabId}`);
    if(target) target.classList.remove('hidden');
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));

    // Set active class on nav-item
    if(window.event && window.event.currentTarget) {
        const item = window.event.currentTarget.closest('.nav-item');
        if(item) item.classList.add('active');
    }

    if(tabId === 'reservations') renderReservations();
    if(tabId === 'pms-grid') renderTimelineCalendar();
};

window.toggleMoreFilters = () => {
    const panel = document.getElementById('filters-expanded-panel');
    if(panel) panel.classList.toggle('hidden');
};

window.switchSubTab = (subId) => {
    console.log("Switching to sub-tab:", subId);

    // Ensure parent tab is visible first
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    const parentTab = document.getElementById('tab-property-detail');
    if(parentTab) parentTab.classList.remove('hidden');

    // Hide all sub-panes and show target
    document.querySelectorAll('.sub-pane').forEach(p => p.classList.add('hidden'));
    const target = document.getElementById(`sub-${subId}`);
    if(target) target.classList.remove('hidden');

    // Update Header Title dynamically based on selection
    const headerEl = document.getElementById('property-detail-header');
    if(headerEl) {
        const titles = {
            'general-info': "General info & property status",
            'vat-tax': "VAT/Tax/Charges",
            'photos': "Photos",
            'amenities': "Room Amenities",
            'room-details': "Room Detail",
            'your-profile': "Your Profile",
            'messaging': "Messaging Preferences"
        };
        headerEl.innerText = titles[subId] || "Property Detail";
    }

    // Auto-render specific sub-tab data
    if(subId === 'room-details') renderRoomDetails();
    if(subId === 'amenities') renderAmenities();
};

// --- 4. DATE PICKER COMPONENT (REUSABLE) ---
function setupSyncedPickers(fromSelector, untilSelector, onNightsUpdate = null) {
    let fromPicker;
    const untilPicker = flatpickr(untilSelector, {
        dateFormat: "Y-m-d",
        altInput: true,
        altFormat: "Y-m-d",
        defaultDate: new Date().getTime() + 24 * 60 * 60 * 1000,
        onChange: function(selectedDates) {
            if (selectedDates.length === 1 && fromPicker) {
                const currentRange = fromPicker.selectedDates;
                if (currentRange.length > 0) {
                    fromPicker.setDate([currentRange[0], selectedDates[0]], false);
                    if(onNightsUpdate) {
                        const diff = selectedDates[0] - currentRange[0];
                        onNightsUpdate(Math.ceil(diff / (1000 * 60 * 60 * 24)));
                    }
                }
            }
        }
    });

    fromPicker = flatpickr(fromSelector, {
        mode: "range",
        dateFormat: "Y-m-d",
        altInput: true,
        altFormat: "Y-m-d",
        defaultDate: [new Date(), new Date().getTime() + 24 * 60 * 60 * 1000],
        onReady: function(selectedDates, dateStr, instance) {
            if (selectedDates.length > 0) {
                instance.altInput.value = instance.formatDate(selectedDates[0], "Y-m-d");
            }
        },
        onValueUpdate: function(selectedDates, dateStr, instance) {
            if (selectedDates.length > 0) {
                instance.altInput.value = instance.formatDate(selectedDates[0], "Y-m-d");
            }
        },
        onChange: function(selectedDates) {
            if (selectedDates.length === 2) {
                untilPicker.setDate(selectedDates[1], false);
                if(onNightsUpdate) {
                    const diff = selectedDates[1] - selectedDates[0];
                    onNightsUpdate(Math.ceil(diff / (1000 * 60 * 60 * 24)));
                }
            }
        }
    });
}

// --- 5. INITIALIZATION & DATA SYNC ---
window.onload = () => {
    startRealtimeSync();
    renderAmenities();
    initDashboard();

    // Reusable Calendar Component implementation for both sections
    setupSyncedPickers("#res-from-date", "#res-to-date");
    setupSyncedPickers("#mb-check-in", "#mb-check-out", (nights) => {
        const nightEl = document.getElementById('mb-nights');
        if(nightEl) {
            nightEl.value = nights;
            calculateBookingTotal();
        }
    });
};

function startRealtimeSync() {
    db.ref(`hotels/${hotelId}/business_details`).on('value', snap => {
        const d = snap.val();
        if(d) {
            if(document.getElementById('header-hotel-name')) document.getElementById('header-hotel-name').innerText = d.hotelName;
            if(document.getElementById('disp-hotel-name')) document.getElementById('disp-hotel-name').innerText = d.hotelName;
            if(document.getElementById('disp-hotel-addr')) document.getElementById('disp-hotel-addr').innerText = d.address;
        }
    });

    db.ref(`hotels/${hotelId}/rooms`).on('value', snap => {
        allRooms = [];
        snap.forEach(c => allRooms.push(c.val()));
        renderRoomPhotoSections();
        renderRoomDetails();
    });
}

// --- 6. DASHBOARD & BOOKING LOGIC ---
function initDashboard() {
    renderDashBookings();
}

function renderDashBookings() {
    const container = document.getElementById('dash-recent-bookings');
    if(!container) return;
    db.ref(`hotels/${hotelId}/bookings`).limitToLast(5).on('value', snap => {
        const rows = [];
        snap.forEach(child => {
            const b = child.val();
            const displayId = b.bookingNumber || b.id.slice(-8);
            rows.push(`
                <tr>
                    <td>${displayId}</td>
                    <td><b>${b.guestName}</b></td>
                    <td>Room ${b.roomNumber || '-'}</td>
                    <td>${new Date(b.checkInDate).toLocaleDateString('en-GB', { day:'2-digit', month:'short' })}</td>
                    <td>${new Date(b.checkOutDate).toLocaleDateString('en-GB', { day:'2-digit', month:'short' })}</td>
                    <td>${UI.badge(b.status || 'Confirmed', b.status || 'ok')}</td>
                </tr>
            `);
        });
        container.innerHTML = rows.reverse().join('');
    });
}

window.openManualBooking = () => {
    document.getElementById('manual-booking-modal').classList.remove('hidden');
    const typeSelect = document.getElementById('mb-room-type');
    const types = [...new Set(allRooms.map(r => r.roomType))];
    typeSelect.innerHTML = '<option value="">Select type</option>' + types.map(t => `<option value="${t}">${t}</option>`).join('');
};

window.closeManualBooking = () => document.getElementById('manual-booking-modal').classList.add('hidden');

window.filterVacantRooms = () => {
    const type = document.getElementById('mb-room-type').value;
    const roomSelect = document.getElementById('mb-room-id');
    const vacant = allRooms.filter(r => r.roomType === type);
    roomSelect.innerHTML = vacant.map(r => `<option value="${r.roomNumber || r.id}">${r.roomNumber || 'Room ' + r.id.slice(-3)}</option>`).join('');
};

window.calculateBookingTotal = () => {
    const nights = parseInt(document.getElementById('mb-nights').value) || 1;
    const rent = parseFloat(document.getElementById('mb-rent').value) || 0;
    const discountPercent = parseFloat(document.getElementById('mb-discount').value) || 0;
    const advance = parseFloat(document.getElementById('mb-advance').value) || 0;
    const fullPay = document.getElementById('mb-full-pay').checked;

    const subtotal = nights * rent;
    const discountAmount = (subtotal * discountPercent) / 100;
    const total = subtotal - discountAmount;
    document.getElementById('mb-total').value = total.toLocaleString();

    if(fullPay) {
        document.getElementById('mb-advance').value = total;
        document.getElementById('mb-remaining').value = "0";
        document.getElementById('mb-advance').disabled = true;
    } else {
        document.getElementById('mb-advance').disabled = false;
        const remaining = total - advance;
        document.getElementById('mb-remaining').value = remaining.toLocaleString();
    }
};

window.toggleFullPay = () => calculateBookingTotal();

window.submitManualBooking = () => {
    const guestName = document.getElementById('mb-guest-name').value;
    const roomType = document.getElementById('mb-room-type').value;
    if(!guestName || !roomType) { alert("Bhai, Guest Name aur Room Type toh bhar do!"); return; }

    const checkInEl = document.getElementById('mb-check-in');
    const dates = checkInEl && checkInEl._flatpickr ? checkInEl._flatpickr.selectedDates : [];
    if (dates.length < 2) { alert("Bhai, Stay Dates toh sahi se select kar lo!"); return; }

    const bookingNumber = Math.floor(1000000000 + Math.random() * 9000000000).toString();

    const newBooking = {
        id: "MB-" + Date.now(),
        bookingNumber: bookingNumber,
        guestName: guestName,
        roomNumber: roomType, // Using room type as the identifier
        checkInDate: dates[0].toISOString(),
        checkOutDate: dates[1].toISOString(),
        totalAmount: parseFloat(document.getElementById('mb-total').value.replace(/,/g, '')),
        status: "Confirmed",
        timestamp: Date.now()
    };

    db.ref(`hotels/${hotelId}/bookings`).push(newBooking).then(() => {
        closeManualBooking();
        alert("Manual Booking Successful! ID: " + bookingNumber);
    });
};

// --- 7. PROPERTY MANAGEMENT ---
function renderReservations() {
    const container = document.getElementById('reservations-table-body');
    if(!container) return;
    db.ref(`hotels/${hotelId}/bookings`).on('value', snap => {
        const rows = [];
        snap.forEach(child => {
            const b = child.val();
            const cin = new Date(b.checkInDate).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' });
            const cout = new Date(b.checkOutDate).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' });
            const bookedOn = b.timestamp ? new Date(b.timestamp).toLocaleDateString('en-GB', { day:'2-digit', month:'short' }) : '-';
            const displayId = b.bookingNumber || b.id.slice(-8);
            rows.push(`
                <tr>
                    <td><span class="guest-name-link">${b.guestName}</span></td>
                    <td>${cin}</td>
                    <td>${cout}</td>
                    <td>1 × ${b.roomNumber || 'Deluxe'}</td>
                    <td>${bookedOn}</td>
                    <td>${UI.badge(b.status || 'OK', b.status || 'ok')}</td>
                    <td>₹ ${(b.totalAmount || 0).toLocaleString()}</td>
                    <td>₹ 0</td>
                    <td><span class="booking-id-link">${displayId}</span></td>
                </tr>
            `);
        });
        container.innerHTML = rows.join('') || '<tr><td colspan="9" style="text-align:center;">No reservations found</td></tr>';
    });
}

function renderAmenities() {
    const amenities = ["Air conditioning", "Balcony", "View", "Flat-screen TV", "Terrace", "Electric kettle", "Toilet paper", "Towels", "Linens"];
    const container = document.getElementById('amenities-list');
    if(!container) return;
    container.innerHTML = amenities.map(name => `
        <div class="amenity-item">
            <span class="amenity-name">${name}</span>
            <div class="segmented-control">
                <div class="seg-btn" onclick="toggleSeg(this)">All</div>
                <div class="seg-btn" onclick="toggleSeg(this)">Some</div>
                <div class="seg-btn active" onclick="toggleSeg(this)">None</div>
            </div>
        </div>
    `).join('');
}

window.toggleSeg = (el) => {
    const parent = el.parentElement;
    parent.querySelectorAll('.seg-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
};

window.openAddRoomModal = () => {
    document.getElementById('add-room-modal').classList.remove('hidden');
    renderAddRoomForm();
};

window.closeAddRoomModal = () => document.getElementById('add-room-modal').classList.add('hidden');

function renderAddRoomForm() {
    const container = document.getElementById('add-room-form-container');
    if(!container) return;
    container.innerHTML = `
        <div class="form-section-card">
            <h3>Basic Info</h3>
            <div class="form-group">
                <label>Room type (Category Name)</label>
                <input type="text" class="form-input-text" id="field-room-type" placeholder="e.g. Deluxe Double Room">
            </div>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>Number of units</label>
                    <input type="number" class="form-input-text" value="1" id="field-room-count">
                </div>
                <div class="form-group">
                    <label>Smoking policy</label>
                    <select class="form-select" id="field-smoking">
                        <option>Non-smoking</option>
                        <option>Smoking</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="form-section-card">
            <h3>Room Specifications</h3>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>Floor Level</label>
                    <select class="form-select" id="field-floor">
                        <option>Ground floor</option>
                        <option>1st floor</option>
                        <option>2nd floor</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Bed Type</label>
                    <input type="text" class="form-input-text" id="field-bed-type" value="Double Bed">
                </div>
            </div>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>Max Guests</label>
                    <input type="number" class="form-input-text" id="field-max-guests" value="2">
                </div>
                <div class="form-group">
                    <label>Room Size (sqft)</label>
                    <input type="text" class="form-input-text" id="field-room-size" value="250 sqft">
                </div>
            </div>
        </div>
    `;
}

window.saveNewRoom = () => {
    const roomType = document.getElementById('field-room-type').value;
    if(!roomType) { alert('Bhai, Room type toh bhar do!'); return; }

    const roomCount = parseInt(document.getElementById('field-room-count').value) || 1;
    const smoking = document.getElementById('field-smoking').value;
    const floor = document.getElementById('field-floor').value;
    const bed = document.getElementById('field-bed-type').value;
    const maxG = parseInt(document.getElementById('field-max-guests').value) || 2;
    const size = document.getElementById('field-room-size').value;

    const newRoom = {
        roomNumber: roomType, // Using Category Name as ID
        totalUnits: roomCount,
        hotelId: hotelId,
        qrToken: "QR-" + Math.random().toString(36).substr(2, 9).toUpperCase(),
        roomType: roomType,
        smokingPolicy: smoking,
        floorLevel: floor,
        bedType: bed,
        numberOfBeds: 1,
        maxGuests: maxG,
        maxAdults: maxG,
        maxChildren: 0,
        numBathrooms: 1,
        isBathroomPrivate: true,
        roomSize: size,
        hasAc: true,
        isBathroomInside: true,
        hasGeyser: true,
        hasKettle: true,
        imageUrl: "",
        isAvailable: true
    };

    db.ref(`hotels/${hotelId}/rooms/${roomType}`).set(newRoom).then(() => {
        closeAddRoomModal();
        alert("Room Category added successfully!");
    }).catch(err => {
        console.error("Save Error:", err);
        alert("Error saving room: " + err.message);
    });
};

function renderRoomDetails() {
    const container = document.getElementById('room-details-container');
    if(!container) return;

    container.innerHTML = allRooms.map(room => `
        <div class="room-card">
            <div class="room-card-img-wrap">
                <img src="${room.imageUrl || 'https://i.ibb.co/6P0f9pL/ganga-homes-logo.jpg'}">
                <div class="room-card-info-bar">
                    <h4>${room.roomType}</h4>
                    <p>(${room.roomNumber})</p>
                </div>
            </div>
            <div class="room-card-body">
                <div class="room-stat-line">Units Available: <b>${room.totalUnits || 1}</b></div>
                <div class="room-stat-line">Max Guests: <b>${room.maxGuests}</b></div>
                <div class="room-stat-line">Bed Type: <b>${room.bedType}</b></div>
                <div class="room-stat-line">Status: <b>${room.isAvailable ? 'Bookable' : 'Closed'}</b></div>
            </div>
            <div class="room-card-actions">
                <button class="btn btn-outline btn-sm" onclick="alert('Edit logic synced with Android')">Edit</button>
                <button class="btn btn-outline btn-sm" onclick="db.ref('hotels/${hotelId}/rooms/${room.roomNumber}').remove()">Delete</button>
            </div>
        </div>
    `).join('') + `
        <div class="create-room-card" onclick="openAddRoomModal()">
            <h3>Create a new room</h3>
            <div class="plus-circle">+</div>
        </div>
    `;
}

function renderRoomPhotoSections() {
    const container = document.getElementById('room-photos-container');
    if(!container) return;
    const distinctTypes = [...new Set(allRooms.map(r => r.roomType))];
    container.innerHTML = distinctTypes.map(type => UI.card(`${type} Gallery`, '<div class="gallery-grid"><div class="photo-add-btn">+</div></div>', '<button class="btn btn-outline btn-sm">+ Add Photos</button>')).join('');
}

// --- 8. TIMELINE CALENDAR V2 LOGIC ---

window.pmsShift = (days) => {
    currentPMSDate.setDate(currentPMSDate.getDate() + days);
    renderTimelineCalendar();
};

window.pmsToday = () => {
    currentPMSDate = new Date();
    renderTimelineCalendar();
};

function renderTimelineCalendar() {
    const monthsContainer = document.getElementById('pms-months-header');
    const daysContainer = document.getElementById('pms-days-header');
    const contentContainer = document.getElementById('pms-timeline-content');
    const titleEl = document.getElementById('calendar-title');

    if(!monthsContainer || !daysContainer || !contentContainer) return;

    // 1. Generate 31 days
    const dates = [];
    const tempDate = new Date(currentPMSDate);
    for(let i=0; i<31; i++) {
        dates.push(new Date(tempDate));
        tempDate.setDate(tempDate.getDate() + 1);
    }

    // Update Title Range
    const endRangeDate = new Date(dates[30]);
    titleEl.innerText = `Calendar ${dates[0].toLocaleDateString('en-GB', {day:'2-digit', month:'2-digit', year:'numeric'})} - ${endRangeDate.toLocaleDateString('en-GB', {day:'2-digit', month:'2-digit', year:'numeric'})}`;

    // 2. Render Months Header
    const monthsMap = {};
    dates.forEach(d => {
        const m = d.toLocaleDateString('en-GB', { month: 'long' });
        monthsMap[m] = (monthsMap[m] || 0) + 1;
    });
    monthsContainer.innerHTML = Object.entries(monthsMap).map(([name, count]) => `
        <div class="month-label" style="width: ${count * 40}px;">${name}</div>
    `).join('');

    // 3. Render Days Header
    daysContainer.innerHTML = dates.map(d => `
        <div class="day-cell ${[0,6].includes(d.getDay()) ? 'weekend' : ''}">
            <label>${d.toLocaleDateString('en-GB', { weekday: 'short' }).substring(0,2)}</label>
            <span>${d.getDate()}</span>
        </div>
    `).join('');

    // 4. Render Rows (Rooms)
    db.ref(`hotels/${hotelId}/bookings`).get().then(snap => {
        const liveBookings = [];
        snap.forEach(c => liveBookings.push(c.val()));

        contentContainer.innerHTML = allRooms.map((room, idx) => {
            // Filter bookings for this room category
            const roomBookings = liveBookings.filter(b => b.roomNumber === room.roomType);

            return `
                <div class="timeline-row">
                    <div class="timeline-room-sidebar">
                        <div class="room-name">${room.roomType}</div>
                        <div class="room-desc">${room.totalUnits} Units Available</div>
                    </div>

                    <!-- Grid Background -->
                    <div class="timeline-grid-bg">
                        ${dates.map(d => `<div class="grid-line ${[0,6].includes(d.getDay()) ? 'weekend' : ''}"></div>`).join('')}
                    </div>

                    <!-- Booking Bars (Live Data) -->
                    ${roomBookings.map(b => {
                        const bStart = new Date(b.checkInDate);
                        const bEnd = new Date(b.checkOutDate);

                        // Calculate offset from current calendar start
                        const dayDiff = Math.floor((bStart - dates[0]) / (1000 * 60 * 60 * 24));
                        const duration = Math.ceil((bEnd - bStart) / (1000 * 60 * 60 * 24));

                        // Only show if within visible range
                        if (dayDiff + duration < 0 || dayDiff > 31) return "";

                        const leftPos = 240 + (dayDiff * 40);
                        const widthSize = (duration * 40) - 4;

                        return `
                            <div class="booking-bar bar-blue" style="left: ${leftPos}px; width: ${widthSize}px;">
                                <div class="bar-content">
                                    <span class="icon-user">👤</span>
                                    <span>${b.guestName}</span>
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `;
        }).join('');
    });
}
