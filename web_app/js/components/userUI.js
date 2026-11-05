export function renderUserRooms(allRooms, container) {
    if (!container) return;
    container.innerHTML = '';

    if (allRooms.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding:60px; color:#666; grid-column: 1/-1;">No rooms available at the moment.</div>`;
        return;
    }

    allRooms.forEach(room => {
        const roomImg = room.imageUrl || 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=800';
        const price = room.roomPrice || room.price || '2,500';
        const categoryName = room.roomType || room.roomNumber || "Standard Room";

        container.innerHTML += `
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
                        <button class="btn-view-details" onclick="window.openBookingModal('${categoryName}')">View Details</button>
                    </div>
                </div>
            </div>`;
    });
}
