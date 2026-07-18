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
  databaseURL: "https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/",
  projectId: "stay-desk-pms-app",
  storageBucket: "stay-desk-pms-app.firebasestorage.app",
  messagingSenderId: "160367544031",
  appId: "1:160367544031:web:d9177856450c3938d30b6a",
  measurementId: "G-FP1R9GQR1E"
};

// Initialize Firebase
try {
    firebase.initializeApp(firebaseConfig);
    console.log("Firebase initialized");
} catch (e) {
    console.error("Firebase Init Error:", e);
}
const db = firebase.database();

let hotelId = "GangaHomes_001";
let allRooms = [];
let currentPMSDate = new Date();

// --- 3. CORE NAVIGATION ---
window.toggleDropdown = (e, tabId) => {
    if (e) e.stopPropagation();
    const item = e ? e.currentTarget : null;
    if (!item) return;

    const isShowing = item.classList.contains('show-dropdown');
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('show-dropdown'));

    if (!isShowing) {
        item.classList.add('show-dropdown');
    }
};

document.addEventListener('click', () => {
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('show-dropdown'));
});

window.switchTab = (tabId) => {
    console.log("Switching Tab:", tabId);
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    const target = document.getElementById(`tab-${tabId}`);
    if(target) target.classList.remove('hidden');

    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    if(window.event && window.event.currentTarget) {
        window.event.currentTarget.classList.add('active');
    }

    if(tabId === 'reservations') renderReservations();
    if(tabId === 'pms-grid') renderTimelineCalendar();
};

window.toggleMoreFilters = () => {
    const panel = document.getElementById('filters-expanded-panel');
    if(panel) panel.classList.toggle('hidden');
};

window.switchSubTab = (subId) => {
    console.log("Switching Sub-Tab:", subId);

    // Ensure parent tab (Property) is shown
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    const parentTab = document.getElementById('tab-property-detail');
    if(parentTab) parentTab.classList.remove('hidden');

    // Show correct sub-pane
    document.querySelectorAll('.sub-pane').forEach(p => p.classList.add('hidden'));
    const target = document.getElementById(`sub-${subId}`);
    if(target) target.classList.remove('hidden');

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

    if(subId === 'room-details') renderRoomDetails();
    if(subId === 'amenities') renderAmenities();
};

// --- 4. DATE PICKER ---
function setupSyncedPickers(fromSelector, untilSelector, onNightsUpdate = null) {
    if (typeof flatpickr === 'undefined') return;

    let fromPicker;
    const untilPicker = flatpickr(untilSelector, {
        dateFormat: "Y-m-d",
        altInput: true,
        altFormat: "Y-m-d",
        defaultDate: new Date().getTime() + 86400000,
        onChange: function(selectedDates) {
            if (selectedDates.length === 1 && fromPicker) {
                const currentRange = fromPicker.selectedDates;
                if (currentRange.length > 0) {
                    fromPicker.setDate([currentRange[0], selectedDates[0]], false);
                    if(onNightsUpdate) {
                        const diff = selectedDates[0] - currentRange[0];
                        onNightsUpdate(Math.ceil(diff / 86400000));
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
        defaultDate: [new Date(), new Date().getTime() + 86400000],
        onReady: function(selectedDates, dateStr, instance) {
            if (selectedDates.length > 0) instance.altInput.value = instance.formatDate(selectedDates[0], "Y-m-d");
        },
        onChange: function(selectedDates) {
            if (selectedDates.length === 2) {
                untilPicker.setDate(selectedDates[1], false);
                if(onNightsUpdate) {
                    const diff = selectedDates[1] - selectedDates[0];
                    onNightsUpdate(Math.ceil(diff / 86400000));
                }
            }
        }
    });
}

// --- 5. INITIALIZATION ---
window.onload = () => {
    startRealtimeSync();
    renderAmenities();
    initDashboard();
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
        }
    });

    db.ref(`hotels/${hotelId}/rooms`).on('value', snap => {
        allRooms = [];
        snap.forEach(c => allRooms.push(c.val()));
        renderRoomDetails();
    });
}

function initDashboard() { renderDashBookings(); }

function renderDashBookings() {
    const container = document.getElementById('dash-recent-bookings');
    if(!container) return;
    db.ref(`hotels/${hotelId}/bookings`).limitToLast(5).on('value', snap => {
        const rows = [];
        snap.forEach(child => {
            const b = child.val();
            rows.push(`
                <tr>
                    <td>${b.bookingNumber || b.id.slice(-8)}</td>
                    <td><b>${b.guestName}</b></td>
                    <td>${b.roomNumber || '-'}</td>
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

window.calculateBookingTotal = () => {
    const nights = parseInt(document.getElementById('mb-nights').value) || 1;
    const rent = parseFloat(document.getElementById('mb-rent').value) || 0;
    const discount = parseFloat(document.getElementById('mb-discount').value) || 0;
    const advance = parseFloat(document.getElementById('mb-advance').value) || 0;
    const fullPay = document.getElementById('mb-full-pay').checked;

    const total = (nights * rent) * (1 - discount/100);
    document.getElementById('mb-total').value = total.toLocaleString();

    if(fullPay) {
        document.getElementById('mb-advance').value = total;
        document.getElementById('mb-remaining').value = "0";
    } else {
        document.getElementById('mb-remaining').value = (total - advance).toLocaleString();
    }
};

window.toggleFullPay = () => calculateBookingTotal();

window.submitManualBooking = () => {
    const guestName = document.getElementById('mb-guest-name').value;
    const roomType = document.getElementById('mb-room-type').value;
    const checkInEl = document.getElementById('mb-check-in');
    const dates = checkInEl && checkInEl._flatpickr ? checkInEl._flatpickr.selectedDates : [];

    if(!guestName || !roomType || dates.length < 2) { alert("Please fill all details!"); return; }

    const bookingId = "MB-" + Date.now();
    const newBooking = {
        id: bookingId,
        bookingNumber: Math.floor(1000000000 + Math.random() * 9000000000).toString(),
        guestName: guestName,
        roomNumber: roomType,
        checkInDate: dates[0].getTime(),
        checkOutDate: dates[1].getTime(),
        totalAmount: parseFloat(document.getElementById('mb-total').value.replace(/,/g, '')),
        status: "CONFIRMED",
        timestamp: Date.now()
    };

    db.ref(`hotels/${hotelId}/bookings/${bookingId}`).set(newBooking).then(() => {
        closeManualBooking();
        alert("Booking Saved!");
    });
};

function renderReservations() {
    const container = document.getElementById('reservations-table-body');
    if(!container) return;
    db.ref(`hotels/${hotelId}/bookings`).on('value', snap => {
        const rows = [];
        snap.forEach(child => {
            const b = child.val();
            rows.push(`
                <tr>
                    <td>${b.guestName}</td>
                    <td>${new Date(b.checkInDate).toLocaleDateString('en-GB')}</td>
                    <td>${new Date(b.checkOutDate).toLocaleDateString('en-GB')}</td>
                    <td>1 × ${b.roomNumber}</td>
                    <td>${b.timestamp ? new Date(b.timestamp).toLocaleDateString('en-GB') : '-'}</td>
                    <td>${UI.badge(b.status || 'OK', 'ok')}</td>
                    <td>₹ ${(b.totalAmount || 0).toLocaleString()}</td>
                    <td>₹ 0</td>
                    <td>${b.bookingNumber || b.id.slice(-8)}</td>
                </tr>
            `);
        });
        container.innerHTML = rows.join('') || '<tr><td colspan="9">No reservations found</td></tr>';
    });
}

function renderAmenities() {
    const amenities = ["Air conditioning", "Wi-Fi", "TV", "Hot Water", "Kettle", "Towels"];
    const container = document.getElementById('amenities-list');
    if(!container) return;
    container.innerHTML = amenities.map(name => `
        <div class="amenity-item">
            <span class="amenity-name">${name}</span>
            <div class="segmented-control">
                <div class="seg-btn" onclick="this.parentElement.querySelectorAll('.seg-btn').forEach(b=>b.classList.remove('active')); this.classList.add('active')">All</div>
                <div class="seg-btn active" onclick="this.parentElement.querySelectorAll('.seg-btn').forEach(b=>b.classList.remove('active')); this.classList.add('active')">None</div>
            </div>
        </div>
    `).join('');
}

window.closeAddRoomModal = () => {
    const modal = document.getElementById('add-room-modal');
    if (modal) modal.classList.add('hidden');
};

window.openAddRoomModal = () => {
    document.getElementById('add-room-modal').classList.remove('hidden');
    document.getElementById('add-room-form-container').innerHTML = `
        <div class="form-section-card">
            <h3>Please select</h3>
            <div class="form-group">
                <label>Room type</label>
                <select id="field-room-type" class="form-select">
                    <option value="">Please select</option>
                    <option>Single Room</option>
                    <option>Double Room</option>
                    <option>Twin Room</option>
                    <option>Triple Room</option>
                    <option>Quadruple Room</option>
                    <option>Family Room</option>
                    <option>Suite</option>
                </select>
            </div>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>Number of rooms (of this type)</label>
                    <input type="number" id="field-room-count" class="form-input-text" value="1">
                </div>
                <div class="form-group">
                    <label>Smoking policy</label>
                    <select id="field-smoking" class="form-select">
                        <option>Non-smoking</option>
                        <option>Smoking</option>
                        <option>Both smoking and non-smoking available</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="form-section-card">
            <h3>Room location</h3>
            <div class="form-group">
                <label>Floor Level</label>
                <select id="field-floor" class="form-select">
                    <option value="">No selection</option>
                    <option>Ground floor</option>
                    <option>1st floor</option>
                    <option>2nd floor</option>
                    <option>3rd floor</option>
                    <option>Upper floor</option>
                </select>
            </div>
        </div>

        <div class="form-section-card">
            <h3>Bed options</h3>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>What kind of beds are available?</label>
                    <select id="field-bed-type" class="form-select">
                        <option>Twin bed(s) / 90-130 cm wide</option>
                        <option>Full bed(s) / 131-150 cm wide</option>
                        <option>Queen bed(s) / 151-180 cm wide</option>
                        <option>King bed(s) / 181-210 cm wide</option>
                        <option>Bunk bed</option>
                        <option>Sofa bed</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Number of beds</label>
                    <select id="field-bed-count" class="form-select">
                        <option>1</option>
                        <option>2</option>
                        <option>3</option>
                        <option>4</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="form-section-card">
            <h3>Occupancy</h3>
            <div class="form-group">
                <label>Maximum guests</label>
                <input type="number" id="field-max-guests" class="form-input-text" value="2">
            </div>
            <div class="form-row-flex">
                <div class="form-group">
                    <label>Maximum adults</label>
                    <input type="number" id="field-max-adults" class="form-input-text" value="2">
                </div>
                <div class="form-group">
                    <label>Maximum children</label>
                    <input type="number" id="field-max-children" class="form-input-text" value="0">
                </div>
            </div>
        </div>

        <div class="form-section-card">
            <h3>Bathroom options</h3>
            <div class="form-group">
                <label>Number of bathrooms</label>
                <input type="number" id="field-bath-count" class="form-input-text" value="1">
            </div>
            <div class="form-group">
                <label>Is the bathroom private?</label>
                <div style="display:flex; gap:20px; margin-top:10px;">
                    <label><input type="radio" name="bath-private" value="yes" checked> Yes</label>
                    <label><input type="radio" name="bath-private" value="no"> No</label>
                </div>
            </div>
        </div>
    `;
};

window.saveNewRoom = () => {
    const type = document.getElementById('field-room-type').value;
    if(!type) return alert("Bhai, Room type toh select kar lo!");

    const units = parseInt(document.getElementById('field-room-count').value) || 1;
    const smoking = document.getElementById('field-smoking').value;
    const floor = document.getElementById('field-floor').value;
    const bedType = document.getElementById('field-bed-type').value;
    const bedCount = parseInt(document.getElementById('field-bed-count').value) || 1;
    const maxG = parseInt(document.getElementById('field-max-guests').value) || 1;
    const maxA = parseInt(document.getElementById('field-max-adults').value) || 1;
    const maxC = parseInt(document.getElementById('field-max-children').value) || 0;
    const bathCount = parseInt(document.getElementById('field-bath-count').value) || 1;
    const isBathPrivate = document.querySelector('input[name="bath-private"]:checked').value === "yes";

    const newRoom = {
        roomNumber: type,
        roomType: type,
        totalUnits: units,
        smokingPolicy: smoking,
        floorLevel: floor,
        bedType: bedType,
        numberOfBeds: bedCount,
        maxGuests: maxG,
        maxAdults: maxA,
        maxChildren: maxC,
        numBathrooms: bathCount,
        isBathroomPrivate: isBathPrivate,
        hotelId: hotelId,
        isAvailable: true,
        timestamp: Date.now()
    };

    db.ref(`hotels/${hotelId}/rooms/${type}`).set(newRoom).then(() => {
        closeAddRoomModal();
        alert("Room Category Saved successfully!");
    }).catch(e => alert("Error saving room: " + e.message));
};

function renderRoomDetails() {
    const container = document.getElementById('room-details-container');
    if(!container) return;

    container.innerHTML = allRooms.map(room => {
        const roomImg = room.imageUrl || 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=800';
        // Generate a pseudo-ID if not present
        const displayId = room.id || Math.floor(100000000 + Math.random() * 900000000).toString();

        return `
            <div class="room-card-v2">
                <div class="room-card-header">
                    <img src="${roomImg}" alt="${room.roomType}">
                    <div class="room-title-overlay">
                        <h4>${room.roomType}</h4>
                        <p>(${displayId})</p>
                    </div>
                </div>
                <div class="room-card-content">
                    <div class="stat-row">Maximum guests: <b>${room.maxGuests} guests</b></div>
                    <div class="stat-row">Maximum adults: <b>${room.maxAdults} adults</b></div>
                    <div class="stat-row">Maximum children: <b>${room.maxChildren} children</b></div>
                    <div class="stat-row">Number of this type: <b>${room.totalUnits}</b></div>

                    <div class="room-card-actions-v2">
                        <button class="btn-edit-lite" onclick="editRoom('${room.roomType}')">Edit</button>
                        <button class="btn-upload-blue" onclick="alert('Upload photos feature coming soon')">Upload photos</button>
                        <button class="btn-edit-lite" style="color:red; border-color:red;" onclick="deleteRoom('${room.roomType}')">Delete</button>
                    </div>
                </div>
            </div>
        `;
    }).join('') + `
        <div class="create-room-card" onclick="openAddRoomModal()">
            <h3>Create a new room</h3>
            <div class="plus-circle">+</div>
        </div>
    `;
}

window.editRoom = (roomType) => {
    const room = allRooms.find(r => r.roomType === roomType);
    if (!room) return;

    window.openAddRoomModal();

    // Wait a tiny bit for the HTML to be injected into the modal
    setTimeout(() => {
        if(document.getElementById('field-room-type')) document.getElementById('field-room-type').value = room.roomType;
        if(document.getElementById('field-room-count')) document.getElementById('field-room-count').value = room.totalUnits;
        if(document.getElementById('field-smoking')) document.getElementById('field-smoking').value = room.smokingPolicy;
        if(document.getElementById('field-floor')) document.getElementById('field-floor').value = room.floorLevel;
        if(document.getElementById('field-bed-type')) document.getElementById('field-bed-type').value = room.bedType;
        if(document.getElementById('field-bed-count')) document.getElementById('field-bed-count').value = room.numberOfBeds;
        if(document.getElementById('field-max-guests')) document.getElementById('field-max-guests').value = room.maxGuests;
        if(document.getElementById('field-max-adults')) document.getElementById('field-max-adults').value = room.maxAdults;
        if(document.getElementById('field-max-children')) document.getElementById('field-max-children').value = room.maxChildren;
        if(document.getElementById('field-bath-count')) document.getElementById('field-bath-count').value = room.numBathrooms;

        const radioYes = document.querySelector('input[name="bath-private"][value="yes"]');
        const radioNo = document.querySelector('input[name="bath-private"][value="no"]');
        if (room.isBathroomPrivate) { if(radioYes) radioYes.checked = true; }
        else { if(radioNo) radioNo.checked = true; }

        // Update the header
        const header = document.querySelector('#add-room-modal h2');
        if(header) header.innerText = "Edit Room Details";
    }, 50);
};

window.deleteRoom = (roomType) => {
    if (confirm(`Are you sure you want to delete the category '${roomType}'?`)) {
        db.ref(`hotels/${hotelId}/rooms/${roomType}`).remove()
            .then(() => alert("Category deleted successfully"))
            .catch(e => alert("Error: " + e.message));
    }
};

function renderTimelineCalendar() {
    const content = document.getElementById('pms-timeline-content');
    const daysHeader = document.getElementById('pms-days-header');
    if(!content || !daysHeader) return;

    const dates = [];
    for(let i=0; i<31; i++) {
        let d = new Date(currentPMSDate); d.setDate(d.getDate() + i);
        dates.push(d);
    }

    daysHeader.innerHTML = dates.map(d => `<div class="day-cell"><span>${d.getDate()}</span></div>`).join('');

    db.ref(`hotels/${hotelId}/bookings`).get().then(snap => {
        const bookings = []; snap.forEach(c => bookings.push(c.val()));
        content.innerHTML = allRooms.map(room => {
            const rBookings = bookings.filter(b => b.roomNumber === room.roomType);
            return `
                <div class="timeline-row">
                    <div class="timeline-room-sidebar"><b>${room.roomType}</b></div>
                    <div class="timeline-grid-bg">${dates.map(() => '<div class="grid-line"></div>').join('')}</div>
                    ${rBookings.map(b => {
                        const start = new Date(b.checkInDate);
                        const diff = Math.floor((start - dates[0]) / 86400000);
                        if(diff < 0 || diff > 30) return '';
                        return `<div class="booking-bar bar-blue" style="left:${240 + diff*40}px; width:40px;">${b.guestName}</div>`;
                    }).join('')}
                </div>
            `;
        }).join('');
    });
}

window.pmsShift = (days) => { currentPMSDate.setDate(currentPMSDate.getDate() + days); renderTimelineCalendar(); };
window.pmsToday = () => { currentPMSDate = new Date(); renderTimelineCalendar(); };
