// Firebase Configuration
const firebaseConfig = {
    apiKey: "AIzaSyAYlLHaBBfqjoyG5mwZ_0VxVmMJttQBCEY",
    authDomain: "stay-desk-pms-app.firebaseapp.com",
    databaseURL: "https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/",
    projectId: "stay-desk-pms-app",
    storageBucket: "stay-desk-pms-app.firebasestorage.app",
    messagingSenderId: "160367544031",
    appId: "1:160367544031:web:34372c028a72c9aed30b6a",
    measurementId: "G-5XTFR1LRSN"
};

// Initialize Firebase
try {
    firebase.initializeApp(firebaseConfig);
} catch (e) {
    console.error("Firebase Init Error:", e);
}
const db = firebase.database("https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
const auth = firebase.auth();

// Anonymous Login
auth.signInAnonymously().catch(e => console.error("Auth Error:", e));

// State
const urlParams = new URLSearchParams(window.location.search);
let hotelId = "GangaHomes_001"; // Hardcoded for 100% reliability

// Persist for refresh
localStorage.setItem('hotel_id', hotelId);

let currentHotelData = null;
let allRooms = [];
let selectedRoomForBooking = null;

// UI Elements - Define inside a helper to ensure DOM is ready
const getRoomsGrid = () => document.getElementById('rooms-grid');

// --- INITIALIZATION ---
function init() {
    console.log("Stay Desk User Web Initializing...");
    syncBusinessDetails();
    syncRooms();
    syncRecentBookings();
    setDefaultDates();
}

function syncRecentBookings() {
    const container = document.getElementById('recent-bookings-list');
    if (!container) return;

    const ref = db.ref('hotels').child(hotelId).child('bookings').orderByChild('timestamp').limitToLast(5);
    ref.on('value', snap => {
        const items = [];
        if (snap.exists()) {
            const arr = [];
            snap.forEach(c => arr.push(c.val()));
            // Sort descending by timestamp
            arr.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));

            arr.forEach(b => {
                const cin = b.checkInDate ? new Date(b.checkInDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' }) : '-';
                const cout = b.checkOutDate ? new Date(b.checkOutDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' }) : '-';
                items.push(`
                    <div class="recent-booking-item">
                        <div class="rb-left"><strong>${b.guestName}</strong><div class="muted">#${b.bookingNumber || b.id}</div></div>
                        <div class="rb-middle">${b.roomNumber || ''}</div>
                        <div class="rb-right">${cin} → ${cout}</div>
                    </div>
                `);
            });
        }

        container.innerHTML = items.join('') || '<div class="muted">No recent bookings</div>';
    });
}

function syncBusinessDetails() {
    db.ref('hotels').child(hotelId).child('business_details').on('value', snap => {
        currentHotelData = snap.val();
        if (currentHotelData) {
            // Update names if provided in DB, otherwise use "Ganga Homes" as requested
            const name = currentHotelData.hotelName || "Ganga Homes";
            document.querySelectorAll('#hotel-name, .logo-text h1, .footer-logo h3').forEach(el => el.innerText = name.toUpperCase());
            document.title = name + " - Official Website";
        }
    });
}

function syncRooms() {
    console.log("Syncing Rooms for:", hotelId);
    db.ref('hotels').child(hotelId).child('rooms').on('value', snap => {
        allRooms = [];
        if (snap.exists()) {
            snap.forEach(child => {
                const roomData = child.val();
                // CRUCIAL: If roomType is missing inside, use the database key (child.key)
                if (!roomData.roomType) roomData.roomType = child.key;
                if (!roomData.roomNumber) roomData.roomNumber = child.key;
                allRooms.push(roomData);
            });
            console.log("Rooms Loaded:", allRooms.length);
        } else {
            console.warn("No rooms found in database for this hotel ID.");
        }
        renderRooms();
    });
}

function renderRooms() {
    const roomsGridEl = getRoomsGrid();
    if (!roomsGridEl) return;
    roomsGridEl.innerHTML = '';

    if (allRooms.length === 0) {
        roomsGridEl.innerHTML = `<div style="text-align:center; padding:60px; color:#666; grid-column: 1/-1;">No rooms available at the moment.</div>`;
        return;
    }

    allRooms.forEach(room => {
        const roomImg = room.imageUrl || 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=800';
        const price = room.roomPrice || room.price || '2,500';
        const categoryName = room.roomType || room.roomNumber || "Standard Room";

        roomsGridEl.innerHTML += `
            <div class="room-card">
                <div class="room-img-wrapper">
                    <img src="${roomImg}" alt="${categoryName}">
                    ${room.isAvailable ? '' : '<div class="popular-badge" style="background:#666">Fully Booked</div>'}
                </div>
                <div class="room-info">
                    <h3>${categoryName}</h3>
                    <div class="room-stats">
                        <span>👤 ${room.maxGuests || room.maxAdults || 2} Adults</span>
                        <span>🛏️ ${room.bedType || 'Comfort Bed'}</span>
                        <span>📏 ${room.roomSize || '250 sq.ft.'}</span>
                    </div>
                    <div class="room-icons">
                        <span>📶</span> <span>🛎️</span> <span>❄️</span> <span>🚿</span>
                    </div>
                    <div class="room-footer">
                        <div class="room-price-info">
                            <span class="price-tag">₹${price}</span>
                            <span class="price-unit">/ night</span>
                        </div>
                        <button class="btn-view-details" onclick="openBookingModal('${categoryName}')">View Details</button>
                    </div>
                </div>
            </div>`;
    });
}

function setDefaultDates() {
    const today = new Date().toISOString().split('T')[0];
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    document.querySelectorAll('#hero-check-in, #check-in-date').forEach(el => el.value = today);
    document.querySelectorAll('#hero-check-out, #check-out-date').forEach(el => el.value = tomorrow);
}

// --- BOOKING MODAL ---
window.openBookingModal = (categoryId) => {
    const room = allRooms.find(r => (r.roomType === categoryId || r.roomNumber === categoryId));
    if (!room) return;
    selectedRoomForBooking = room;

    document.getElementById('modal-room-type').innerText = room.roomType || room.roomNumber;
    document.getElementById('booking-modal').classList.remove('hidden');
};

window.closeBookingModal = () => {
    document.getElementById('booking-modal').classList.add('hidden');
};

// --- CONFIRM BOOKING ---
document.getElementById('btn-confirm-booking').onclick = () => {
    const name = document.getElementById('guest-name').value.trim();
    const phone = document.getElementById('guest-phone').value.trim();
    const checkIn = document.getElementById('check-in-date').value;
    const checkOut = document.getElementById('check-out-date').value;

    if (!name || !phone || !checkIn || !checkOut) {
        alert("Please fill in all details to book.");
        return;
    }

    const bookingId = "BK" + Date.now().toString().slice(-6);
    const bookingData = {
        id: bookingId,
        bookingNumber: bookingId,
        hotelId: hotelId,
        roomNumber: selectedRoomForBooking.roomType || selectedRoomForBooking.roomNumber,
        guestName: name,
        guestPhone: phone,
        checkInDate: new Date(checkIn).getTime(),
        checkOutDate: new Date(checkOut).getTime(),
        totalAmount: parseFloat(selectedRoomForBooking.price || selectedRoomForBooking.roomPrice || 2500),
        status: "CONFIRMED",
        source: "USER_WEB",
        timestamp: Date.now()
    };

    db.ref('hotels').child(hotelId).child('bookings').child(bookingId).set(bookingData)
        .then(() => {
            closeBookingModal();
            document.getElementById('success-booking-id').innerText = "#" + bookingId;
            document.getElementById('success-modal').classList.remove('hidden');
            // Reset form
            document.getElementById('guest-name').value = '';
            document.getElementById('guest-phone').value = '';
        })
        .catch(err => {
            console.error("Booking Error:", err);
            alert("Sorry, booking failed. Please try again later.");
        });
};

// Start
init();
