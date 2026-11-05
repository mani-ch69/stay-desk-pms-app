# Implementation Plan - Project Re-organization & Refactoring

Aapne poore project (Android App + Web Portals) ko ek clean, modular, aur production-ready standard folder structure mein refactor karne ko kaha hai. Ye ek **Major Architectural Change** hai. Isme files ko move karna, package names badalna, aur imports update karna shamil hai.

## User Review Required

> [!CAUTION]
> Ye ek bahut bada badlav hai jo poore codebase ko affect karega. Web (HTML/JS) aur Android (Kotlin) dono ko standard structure (`components`, `screens`, `services`, `utils`) mein transfer kiya jayega.
> Mujhe shuru karne se pehle aapki **Approval** chahiye kyunki isme 50+ files modify hongi.

## Open Questions

- Web portals abhi **Vanilla JS** (bina React/Node) par hain. Unhe `components` aur `services` mein todne ke liye **ES6 Modules (`<script type="module">`)** ka use karna padega. Kya ye approach theek hai?
- Aapke paas web ke 3 folders hain (`admin_web`, `user_web`, aur `STAY_DESK_FINAL_SYSTEM`). Kya main purane `admin_web` aur `user_web` ko delete kar doon taaki sirf ek clean `web_app/` folder bache?

## Proposed Changes

### 1. Web Portal Refactoring (`web_app/` unified folder)
Hum `STAY_DESK_FINAL_SYSTEM` ko base mankar ek naya unified structure banayenge aur purana kachra hata denge.
- **`web_app/assets/`**: `favicon.svg`, images.
- **`web_app/styles/`**: `portal_style.css`, `style.css`
- **`web_app/pages/`**: `admin.html`, `user.html`
- **`web_app/js/components/`**: UI render karne ke functions (jaise `renderRoomCard()`, `renderReservations()`).
- **`web_app/js/services/`**: Firebase setup, DB calls (jaise `syncRooms()`, `saveNewRoom()`).
- **`web_app/js/utils/`**: Helper functions (jaise `calculateBookingTotal()`).

### 2. Android App Refactoring (`admin_app`)
Android ke andar Kotlin files standard Android structure (`ui`, `data`) se badalkar aapke bataye gaye global standard par shift ki jayengi:
- **`com.example.staydesk.screens`**: Saari UI screens (`AdminMenuScreen`, `LoginScreen`, etc.) yahan aayengi (Pehle `ui/waiter`, `ui/auth` aadi me thi).
- **`com.example.staydesk.components`**: Reusable Compose widgets (`BookingComponents`, `CommonDateComponents`).
- **`com.example.staydesk.services`**: Saare Repositories (`RoomRepository`, `AuthRepository`) jo Firebase se baat karte hain (Pehle `data/` me the).
- **`com.example.staydesk.models`**: Data classes (`Room`, `Booking`).
- **`com.example.staydesk.utils`**: Helper classes (`SecurityManager`, `ReceiptHelper`).

### 3. De-duplication & Cleanup
- [DELETE] Purane/Unused folders (`admin_web`, `user_web`) jinme outdated code hai.
- [MODIFY] Saare Android Kotlin files ke `package` declarations aur `import` statements update kiye jayenge taaki build fail na ho.

## Verification Plan

### Automated/Build Tests
- Android app ka `gradlew assembleDebug` run karke check karenge ki imports theek hain ya nahi.

### Manual Verification
- Web app ke naye `pages/admin.html` ko run karke check karenge ki JS modules theek se load ho rahe hain ya nahi.
