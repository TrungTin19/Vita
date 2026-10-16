# Vita review improvements

Current scope: medication schedules, measurement context and water reminders for the school project.
Architecture: Java/XML, SQLite DAOs and one independent AlarmManager alarm per medicine time. No new dependencies.
Workflow: TDD for important logic, ui-ux-pro-max for native controls, systematic debugging for failures, Ponytail review and cleanup per module.

## Medication

- Store sorted, deduplicated HH:mm times in the medicine time column.
- Keep an independent alarm and date/time identity for every scheduled dose.
- Record TAKEN/SKIPPED separately by medicine, date and scheduled time.
- Show individual dose rows, editable time chips and dose history.
- Preserve pending alarms when the app resumes; advance only the delivered time.
- Cancel original time alarms before changing a schedule, deleting a medicine or logging out.
- Preserve old medicine history through incremental SQLite migrations.

## Measurements

- Save optional measurement time and glucose context: unspecified, fasting, before meal or after meal.
- Preserve unknown time for older records.
- Order records by date/time and show context in lists, detail screens and chart markers.
- Include a glucose chart and separate intraday points by measurement time.

## Water

- Save each profile's reminder window, quick-add amount and weather preference.
- Suppress notifications outside the window and when the daily goal is reached.
- Schedule the next check inside the allowed window, including windows spanning midnight.
- Weather adjustment defaults off; enabling it adds the existing 300 ml suggestion without changing the personal base goal.
- Handle notification permission changes and exact-alarm restrictions.

## Verification

- Logic regressions were observed failing before implementation, then passing.
- Emulator checks cover SQLite upgrades, independent doses, delayed notification actions, recreated editors and water settings.
- Current checks: 77 unit tests pass; normal APK and Android-test APK compile; lint has no errors.
- The normal package is vn.edu.tdmu.vita. Device fixtures use a separate verification application ID.
- Journal schema and existing user records remain intact.
- Physical Android phones and OEM-specific alarm behavior have not been tested.
