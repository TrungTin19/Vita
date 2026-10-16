# Vita functional completion

Goal: complete the seven gaps identified against the existing project specification.
Scope: Android Java/XML app only; preserve SQLite data and existing design tokens.
Execution: sequential modules, TDD for important logic, ponytail-review after each module. No dependencies added.

1. BMI: share BMICalculator and latest recorded weight between Home and Profile; test fallback and classification boundaries.
2. Sleep: calculate actual start date, sum multiple sessions per wake date; test same-day, overnight and multiple sessions.
3. History: inclusive date range picker and DAO query; test boundaries, ordering and user isolation on Android SQLite.
4. Statistics: weight and heart-rate charts with 7/30 day ranges, dates and units; test extraction of nullable metrics and chronological positioning.
5. Weather: cache by city, reject stale UI callbacks, preserve offline timestamp/city; test cache selection and daily validity.
6. Hydration: daily effective goal = base + 300 ml for same-day selected-city temperature >= 33 C; show reason, never overwrite profile base. Historical chart labels today's goal explicitly. Test threshold, stale data and repeated calculation.
7. Notifications: request permission for water as well as medicines, show denied state with settings action; prevent reminders/actions affecting logged-out or other users. Test session ownership and device permission flows.

Verification: targeted RED/GREEN tests per module, full unit suite, debug APK, Android integration/UI checks when device is available. Existing instrumentation tests erase app data, so run only on an isolated test application ID.

Decisions: existing specification authorizes these bounded fixes; continue in the supplied checkout and leave changes uncommitted for review. AQI remains advisory because no hydration formula is specified. Sum sleep sessions by wake date, consistent with storage convention.

## Evidence and review ledger

- Baseline: checkout clean except user-owned .audit-cache/ and opencode.json. Existing output reports 64 historical unit passes; requires fresh verification.
- Environment: Gradle under JDK 25/26 fails in Java NIO Unix-domain loopback before project configuration. Testing installed JDK 17 as an environment-only workaround.
- BMI: RED at Asian boundary 22.9965; GREEN 7 tests. Shared latest weight and raw classification; removed duplicate BmiUtils and its duplicate tests. Ponytail review: both screens call BMICalculator; profile base-weight label remains registration weight intentionally.
- Sleep: RED missing start-date behavior; GREEN 6 date/time tests. Same-day and year-boundary cases covered. Sleep totals share chart data helper; removed adapter's duplicated date arithmetic. Review: sum by wake date, preserve individual sessions.
- History: reuse existing inclusive/user-scoped getRecordsRange query; Material date-range picker uses UTC date conversion and descending display. Device regression test covers SQL boundaries and account separation. Review: no extra DAO method or dependency needed.
- Charts: RED missing nullable metric/day-position extraction; GREEN 2 chart-data tests. Added weight/heart-rate cards and preserved actual calendar x positions. Single sample visible. Linear BP interpolation avoids suggesting unmeasured curve extrema.
- Weather: RED missing city validation/cache timestamp contract; GREEN 5 parser/cache tests. Per-city cache supports matching legacy cache; AQI failure does not discard forecast. Review: app context, request generation, view-destroy guard prevent outdated UI callbacks.
- Hydration: RED missing formula; GREEN 2 tests. 33 C boundary, other city, previous day, null weather and repeated calculation covered. All consumers share HydrationGoal. Historical chart goal explicitly labeled today's reference.
- Notifications: RED missing positive-owner matching; GREEN test. Water permission requested on Reminder entry, denied state remains visible with settings action. Notification actions check owner; logout cancels reminders/notifications, login restores current user's medicine alarms.
- Fresh full JUnit run: 70 tests, zero failures via scripts/test-logic.ps1. Fresh production Java compilation: all 48 files compiled with installed Android/dependency jars and AAPT-generated R.java.
- Native resources: AAPT2 found pre-existing invalid PNG extension (JPEG data) and non-positional water format arguments. Renamed asset .jpg without changing pixels; fixed positional placeholders. AAPT2 resource compilation and linking against cached debug APK succeed. This is verification, not a new distributable Gradle APK.
- Limitations: Gradle still cannot establish Java NIO loopback (JDK 17/25 tested). ADB works outside sandbox but no device is attached; Android UI/integration tests remain unrun.
- Independent final review found and fixed two issues: preserve water alarm cadence across app resumes with FLAG_NO_CREATE (cancel removes the token); use app-details settings below API 26. Reviewer rechecked both changes and reported no remaining concrete blockers.
- Final verification: 70/70 JUnit tests pass; all 48 production/resource Java files compile; all four Android test source files compile. Device regression tests use unique fixture accounts and clean up only those accounts, preserving the prior session. Gradle and device execution are still unverified as stated above.

## Verification retry 2026-10-16

The earlier Gradle/device limitations above are resolved for this run.

- Root cause: Java NIO attempts AF_UNIX loopback connections which fail with `Invalid argument: connect` on this Windows environment. JDK source confirms PipeImpl falls back to TCP when Unix socket bind fails. A process-local `jdk.net.unixdomain.tmpdir` pointing to an absent directory triggers that supported fallback; Selector.open and Gradle then succeed. No JDK/system configuration was changed.
- Reusable command from repository root: `./scripts/gradle.ps1`. It builds the debug APK and runs unit tests, restores environment variables, and uses a fresh daemon. The earlier direct JUnit fallback script was removed because native Gradle now works.
- Native Gradle: `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest`, and test APK installation all succeed. Fresh XML reports: 70 tests, 0 failures, 0 errors.
- Android 13 Pixel_6 emulator: FunctionalRegressionDeviceTest (3), SleepMedicineUiTest (2), VitaSystemIntegrationTest (1) all pass: `OK (6 tests)`.
- MedicineAlarmDeviceTest passes with the emulator screen switched off: `OK (1 test)`, 55.462 seconds. Android dumpsys records the delivered wakeup and next day's exact alarm. Fixture now explicitly signs in its account and asserts notification permission rather than relying on leftover session state.
- Device tests ran against temporary application ID `vn.edu.tdmu.vita.verification`; their database resets do not affect `vn.edu.tdmu.vita`.
- Final normal debug APK was rebuilt with original application ID `vn.edu.tdmu.vita`; APK Signature Scheme v2 verification succeeds. Output: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.
- Not covered by this retry: physical hardware, Android 7 settings fallback, reboot recovery, and forced Doze delivery.
