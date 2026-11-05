import { db, getHotelId } from '../services/firebaseService.js';

export const UI = {
    badge: (text, type = 'ok') => `<span class="status-tag ${type.toLowerCase()}">${text}</span>`,
    table: (headers, rows) => `
        <table class="table-reservations">
            <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead>
            <tbody>${rows.length ? rows.join('') : '<tr><td colspan="' + headers.length + '" style="text-align:center;">No data available</td></tr>'}</tbody>
        </table>
    `
};

export function renderDashBookings(container) {
    if(!container) return;
    db.ref(`hotels/${getHotelId()}/bookings`).limitToLast(5).on('value', snap => {
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

export function renderRoomDetails(allRooms, container) {
    if(!container) return;
    container.innerHTML = allRooms.map(room => {
        const roomImg = room.imageUrl || 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=800';
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
                        <button class="btn-edit-lite" onclick="window.editRoom('${room.roomType}')">Edit</button>
                        <button class="btn-edit-lite" style="color:red; border-color:red;" onclick="window.deleteRoom('${room.roomType}')">Delete</button>
                    </div>
                </div>
            </div>
        `;
    }).join('') + `
        <div class="create-room-card" onclick="window.openAddRoomModal()">
            <h3>Create a new room</h3>
            <div class="plus-circle">+</div>
        </div>
    `;
}
