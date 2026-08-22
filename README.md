# TechFix — Android Studio Project (Java, SQLite-only)

## Architecture

No external backend. Everything — users, branches, technicians, device categories,
repair services, spare parts, appointments, payments — lives in a single local
SQLite data layer (`DBHelper.java` + focused DAO classes). `DBHelper` owns schema lifecycle and the DAOs own the actual queries. The database seeds itself with sample data
(2 branches, device categories, repair services, technicians, spare parts) the
first time the app runs, so there's something to browse/book immediately without
needing an admin panel.

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

All five deliverable categories are covered.

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

## Customer Service Enhancements

The current coursework build also includes several lightweight customer-service improvements:

- **Ratings & Reviews** - customers can submit one 1-5 star review after a repair is completed/paid. Reviews are stored in SQLite.
- **Quick Contact Branch** - branch cards and appointment details can open the Android dialer with the assigned branch number.
- **Repair Cost Estimate** - service screens and booking show the base repair estimate before appointment submission.
- **Repair Warranty** - appointment details display a service-based warranty period after completion.
- **FAQ / Help Centre** - offline answers to common booking, price, warranty, branch and review questions.
- **Estimated Completion Time** - each repair service displays a simple expected duration for customer planning.

These estimates are informational; final repair cost/time can vary after technician inspection and parts availability.
