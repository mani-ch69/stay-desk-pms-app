import { db, getHotelId, signInAnonymously } from './services/firebaseService.js';
import { renderDashBookings, renderRoomDetails, UI } from './components/adminUI.js';

let allRooms = [];
let currentPMSDate = new Date();

window.onload = async () => {
    console.log("Admin Portal Page Loaded");
    try {
        await signInAnonymously();
        startRealtimeSync();
    } catch (e) {
        alert("Database Connection Failed: " + e.message);
    }

    // Bind global functions for HTML onclicks
    bindGlobalFunctions();
};

function startRealtimeSync() {
    const hotelId = getHotelId();
    console.log("Starting Realtime Sync for:", hotelId);

    db.ref(`hotels/${hotelId}/business_details`).on('value', snap => {
        let d = snap.val();
        if(!d) {
            d = { hotelName: "Stay Desk", address: "Varanasi, India" };
            db.ref(`hotels/${hotelId}/business_details`).set(d);
        }
        if(document.getElementById('header-hotel-name')) document.getElementById('header-hotel-name').innerText = d.hotelName;
    });

    db.ref(`hotels/${hotelId}/rooms`).on('value', snap => {
        allRooms = [];
        if(snap.exists()){
            snap.forEach(c => allRooms.push(c.val()));
        }
        renderRoomDetails(allRooms, document.getElementById('room-details-container'));
        renderDashBookings(document.getElementById('dash-recent-bookings'));
    });
}

function bindGlobalFunctions() {
    window.switchTab = (tabId) => {
        document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
        const target = document.getElementById(`tab-${tabId}`);
        if(target) target.classList.remove('hidden');

        document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
        if(window.event && window.event.currentTarget) {
            window.event.currentTarget.classList.add('active');
        }
    };

    window.switchSubTab = (subId) => {
        document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
        const parentTab = document.getElementById('tab-property-detail');
        if(parentTab) parentTab.classList.remove('hidden');

        document.querySelectorAll('.sub-pane').forEach(p => p.classList.add('hidden'));
        const target = document.getElementById(`sub-${subId}`);
        if(target) target.classList.remove('hidden');
    };

    window.openAddRoomModal = () => {
        document.getElementById('add-room-modal').classList.remove('hidden');
        // HTML Injection omitted for brevity (kept in main file logic)
    };

    window.closeAddRoomModal = () => {
        document.getElementById('add-room-modal').classList.add('hidden');
    };

    window.viewLiveWebsite = () => {
        window.open('user.html', '_blank');
    };

    // ... add other global bindings ...
}
