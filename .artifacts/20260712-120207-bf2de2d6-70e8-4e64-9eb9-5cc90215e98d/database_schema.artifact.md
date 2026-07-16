# Stay Desk - Unified Database Schema (Firebase)

This document defines the real-time database structure that connects the Admin App, Admin Web, and User Web.

## Schema Structure

```json
{
  "hotels": {
    "HOTEL_ID": {
      "business_details": {
        "hotelName": "Ganga Homes",
        "address": "Varanasi",
        "contact": "9876543210",
        "email": "info@gangahomes.com",
        "currency": "INR",
        "timezone": "Asia/Kolkata"
      },
      "rooms": {
        "ROOM_ID": {
          "roomNumber": "101",
          "roomType": "Deluxe",
          "price": 2500,
          "isAvailable": true,
          "capacity": 2,
          "amenities": ["AC", "WiFi", "TV"],
          "ota_links": {
            "booking_com": "ical_url_here",
            "airbnb": "ical_url_here"
          }
        }
      },
      "bookings": {
        "BOOKING_ID": {
          "guestName": "John Doe",
          "guestPhone": "9999988888",
          "checkIn": 1719600000000,
          "checkOut": 1719772800000,
          "roomId": "ROOM_ID",
          "status": "CONFIRMED",
          "source": "USER_WEB",
          "totalAmount": 5000,
          "createdAt": 1719500000000
        }
      },
      "settings": {
        "auto_sync_ota": true,
        "sync_interval_minutes": 15
      }
    }
  },
  "admins": {
    "ADMIN_UID": {
      "hotelId": "HOTEL_ID",
      "role": "SUPER_ADMIN",
      "email": "admin@gangahomes.com"
    }
  }
}
```

## Key Integration Points
1. **User Web**: Reads `rooms` to show availability, writes to `bookings` when a guest reserves.
2. **Admin App/Web**: Reads `bookings` in real-time to show notifications, manages `rooms` and `settings`.
3. **Cloud Functions**: Triggered on `bookings` write to sync with OTA or send emails.
