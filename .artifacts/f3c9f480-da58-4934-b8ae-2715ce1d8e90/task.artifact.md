# Task List - Project Re-organization & Refactoring

- [ ] **Phase 1: Web Portal Refactoring**
  - [ ] Create new unified directory structure (`web_app/assets`, `web_app/pages`, `web_app/styles`, `web_app/js/components`, `web_app/js/services`, `web_app/js/utils`)
  - [ ] Migrate static assets (images, icons)
  - [ ] Migrate and update CSS files
  - [ ] Split `app.js` and `admin_v3.js` into modular ES6 components (services, UI components, utils)
  - [ ] Update `admin.html` and `user.html` to use modular script tags
  - [ ] Delete legacy folders (`admin_web`, `user_web`, `STAY_DESK_FINAL_SYSTEM`)

- [ ] **Phase 2: Android App Refactoring (Kotlin)**
  - [ ] Create new package structure (`screens`, `components`, `services`, `models`, `utils`) under `com.example.staydesk`
  - [ ] Move Model classes to `models/`
  - [ ] Move Repository classes to `services/`
  - [ ] Move UI screens to `screens/`
  - [ ] Move reusable Composable widgets to `components/`
  - [ ] Move Helper classes to `utils/`
  - [ ] Update all `package` declarations in the moved files
  - [ ] Update all `import` statements across the Android app
  - [ ] Clean up empty legacy packages (`ui`, `data`)

- [ ] **Phase 3: Verification**
  - [ ] Run Gradle build to ensure no Android compilation errors
  - [ ] Verify HTML/JS file links
