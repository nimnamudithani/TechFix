# TechFix — Android Studio Project (Java, SQLite-only)

## Architecture

No external backend. Everything — users, branches, technicians, device categories,
repair services, spare parts, appointments, payments — lives in a single local
SQLite data layer (`DBHelper.java` + focused DAO classes). `DBHelper` owns schema lifecycle and the DAOs own the actual queries. The database seeds itself with sample data
(2 branches, device categories, repair services, technicians, spare parts) the
first time the app runs, so there's something to browse/book immediately without
needing an admin panel.

**Trade-off worth knowing (see earlier discussion):** because everything is local
to each phone, "shared" data like technician availability isn't really shared
across devices — every install has its own seeded copy. This is fine for a
coursework demo (one phone, one demo video) but wouldn't reflect real multi-branch
usage. If you want to note this honestly in your report's Discussion section,
it's a reasonable thing to flag as a known limitation / future improvement
(e.g. "a future version would sync this through a shared backend").

## What's implemented and working

| Feature | Where | Notes |
|---|---|---|
| Register / Login | `LoginActivity`, `RegisterActivity` | Local SQLite, SHA-256 hashed passwords |
| Branch listing | `HomeActivity` | Reads from SQLite |
| Search repair services | `SearchServicesActivity` | Live search by name/description, tapping a result jumps into booking with that service pre-selected |
| Book a repair | `BookAppointmentActivity` | Device category → service → **nearest branch with BOTH an available technician AND relevant spare parts in stock** (falls through to the next-nearest branch if the closest one lacks either) → submit |
| Nearest-branch logic | `utils/LocationUtils.java` | `sortBranchesByDistance()` ranks all branches; `BookAppointmentActivity` walks that list checking technician + spare-parts availability at each |
| Track status / view appointments | `AppointmentHistoryActivity` | Shows all appointments for the logged-in user; pass `onlyCompleted=true` in the intent for a strict "history" view |
| Appointment detail | `AppointmentDetailActivity` | Shows technician/branch/price/status, camera capture for device photo, "Mark as Completed" and "Mark as Paid" actions |
| Camera integration | `AppointmentDetailActivity` | Uses `ActivityResultContracts.TakePicture()` + `FileProvider` for device/repair photos and admin sample images |
| Payment | `AppointmentDetailActivity` | "Mark as Paid" records a payment row and flips status to Paid |
| Log out | "Log Out" on Home | Clears the session and returns to Login |
| Visible branch map | "View Branches on Map" on Home (`BranchMapActivity`) | Uses OpenStreetMap (osmdroid) - no API key or Google Cloud billing account needed. Shows pins for both branches, and your own live position if you grant location permission. Makes the GPS logic that already ran invisibly in booking now visible on screen |
| **Admin/staff side** | `AdminActivity` + 5 sub-screens | Reachable via "Manage TechFix (Staff)" button on Home. Manage repair appointments (all branches), device categories, repair prices, technicians (add/toggle availability/delete), spare parts (add/edit stock/delete — routed through a real `ContentProvider`, see below), and sample repair images (camera capture + caption) |
| Sample repair images (customer-facing) | `SampleImagesActivity` | Read-only gallery shown via "See Repairs We've Done" on Home, populated by whatever staff add through the admin side |

## Deliverables coverage

| Deliverable | Where |
|---|---|
| Locations / Map GPS | `utils/LocationUtils.java` (nearest-branch distance sort), `BranchMapActivity` (osmdroid map, live location pin) |
| Web Services & Remote Data | `network/GeocodeClient.java` + `GeocodeService.java` — Retrofit calls to OpenStreetMap's Nominatim REST API for reverse geocoding |
| Complex Data Model & Adaptors | `database/DBHelper.java` (schema) + focused DAO classes for the related SQLite domains, 8 `RecyclerView` adapters in `adapters/` |
| Camera & Image Integrations | `AppointmentDetailActivity`, `AdminSampleImagesActivity` — `ActivityResultContracts.TakePicture()` + `FileProvider` |
| SQLite, Content Providers & Offline Application | `DBHelper.java` for schema lifecycle, focused DAO classes for local persistence, and `database/SparePartProvider.java` + `SparePartContract.java` — a real `ContentProvider` fronting the `spare_part` table, used end-to-end by `AdminSparePartsActivity` via `ContentResolver` instead of talking to SQLite directly |

All five deliverable categories are covered, so the brief's "one or more" requirement is comfortably exceeded.

## Roles: Customer vs Staff

The app now enforces a real split between the two roles from the brief:

| | Customer | Staff (TechFix) |
|---|---|---|
| Register / log in | ✅ | ✅ (seeded staff account, see below) |
| Browse branches, search services | ✅ | ✅ |
| Book an appointment | ✅ | — |
| Track own appointment status | ✅ (read-only) | — |
| View own repair history | ✅ | — |
| See "Manage TechFix" button | ❌ hidden | ✅ |
| Manage device categories / prices / technicians / spare parts / sample images | ❌ | ✅ |
| View all appointments across branches | ❌ | ✅ |
| Update appointment status, take repair photos, mark paid | ❌ (button hidden even on their own appointment) | ✅ |

**Test staff account** (seeded automatically, don't need to register it):
- Email: `staff@techfix.lk`
- Password: `staff123`

Log in with that account to see the "Manage TechFix" button. Any account created through the normal Register screen is always a customer (`is_admin = 0`) — there's no UI to self-promote to staff, which is intentional.

**Note**: this is enforced by hiding buttons and screens based on the `is_admin` flag checked at login — it's not a hardened security model (e.g. a customer could theoretically still reach an admin screen by manipulating an Android intent directly), but it's a legitimate and sufficient role separation for a coursework demo, and is worth mentioning as a "future improvement: proper access control" line in your report.

## UI redesign (bottom-nav app structure)

The app now follows a standard bottom-navigation structure (Home / Activities / Notifications / Account), matching real consumer apps rather than a flat list of buttons:

- **Home** — greeting header with role badge, a quick-action icon grid (Book a Repair/Manage TechFix depending on role, Search, Gallery, Branches), horizontal branch carousel
- **Activities** — tabbed Ongoing/Completed appointment list. Customers see their own; staff see every appointment across both branches
- **Notifications** — a real in-app notification feed. `AppointmentDao` coordinates with `NotificationDao` to insert a notification whenever: a customer submits a booking, staff updates an appointment's status, or a payment is recorded. Unread count shows as a badge on the bottom nav
- **Account** — profile card (initials avatar, name, email, role badge) plus a menu (Manage TechFix for staff, Change Password, Log Out)

New color palette: deep navy primary (`#003049`) + warm amber accent (`#F59E0B`), replacing the earlier generic indigo — chosen to read as "tech repair service" rather than a generic app template. Real adaptive app icon (phone + wrench mark, matching this palette) replaces the old placeholder vector — see `res/mipmap-anydpi-v26/ic_launcher.xml` (background/foreground/monochrome layers) with a flattened fallback in `res/mipmap/ic_launcher.xml` for API 24–25 devices. `AppointmentHistoryActivity` is now unused dead code (superseded by `ActivitiesActivity`) — harmless to leave in the project, or delete it if you want a cleaner file tree.

## What's still not built

Nothing outstanding from the earlier review — the placeholder app icon has been replaced with a real adaptive icon, the dead placeholder-backend networking code (`ApiClient`/`ApiService`) has been removed, the unused CameraX Gradle dependencies have been dropped, and a real `ContentProvider` (`SparePartProvider`) now backs the spare-parts admin screen. If you keep building on this, worth keeping in mind for your report's "future work" section:

- Role separation (customer vs staff) is enforced by hiding UI based on an `is_admin` flag, not a hardened permission model (see the Roles section above)
- No automated tests beyond the default JUnit/AndroidX Test scaffolding
- Everything is local-per-device SQLite (see the Architecture trade-off note above) — a real deployment would sync through a shared backend

## Running it

1. Open the `TechFix` folder in Android Studio (the one with `settings.gradle`), let Gradle sync.
2. Run on an emulator or real device.
3. Register a new **customer** account (or log in as staff using the seeded account above), browse branches, and try "Book a Repair" — you'll be asked for location permission the first time (needed for nearest-branch matching). If you deny it or are on an emulator without a set location, it falls back to the first branch.
4. As a customer, "My Appointments" shows a read-only view of what you've booked. Log in as staff to see the full editable version with photo/status/payment controls, reachable via "Manage TechFix" on Home.

## Suggested next steps for the team

1. Split remaining work: Search UI, Admin screens, spare-parts-aware assignment, polish.
2. Each teammate should own at least one screen per the coursework's group requirement — this maps reasonably well to: (a) Auth/Profile, (b) Booking flow, (c) Tracking/Detail/Camera/Payment, (d) Admin side + seed-data/spare-parts logic.
3. Once functionality is done, write the report alongside — screenshot each feature as you finish it rather than at the end.
4. Record the demo video walking through: register → login → browse branches → book (showing GPS nearest-branch match) → track status → take a photo → mark paid → view history.

## Final polish
- Launcher now uses a proper Android adaptive icon (`@mipmap/ic_launcher`) with separate background, foreground and monochrome layers. Android 13+ themed icons are supported.
- Key user-facing labels on Home, Login, Register and bottom navigation are stored in `strings.xml` rather than hardcoded in layouts.
- Added small JUnit model tests for `Branch` and `RepairService` as a starting point for automated testing.
- Production future work still includes a shared backend, hardened role authorization and broader unit/UI test coverage.

## Customer Service Enhancements

The current coursework build also includes several lightweight customer-service improvements:

- **Ratings & Reviews** - customers can submit one 1-5 star review after a repair is completed/paid. Reviews are stored in SQLite.
- **Quick Contact Branch** - branch cards and appointment details can open the Android dialer with the assigned branch number.
- **Repair Cost Estimate** - service screens and booking show the base repair estimate before appointment submission.
- **Repair Warranty** - appointment details display a service-based warranty period after completion.
- **FAQ / Help Centre** - offline answers to common booking, price, warranty, branch and review questions.
- **Estimated Completion Time** - each repair service displays a simple expected duration for customer planning.

These estimates are informational; final repair cost/time can vary after technician inspection and parts availability.
