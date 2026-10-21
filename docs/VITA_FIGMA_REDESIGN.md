# Vita — Figma redesign

Created 2026-10-19 from `VITA_PRODUCT_SPEC_FOR_FIGMA.md`.

- Design file: https://www.figma.com/design/zdfTzqy6XlRRRr7pJWoPkO?node-id=4-274
- File key: `zdfTzqy6XlRRRr7pJWoPkO`
- Pages: `00 · Foundations` (`0:1`), `01 · Screens` (`2:278`), `02 · Hướng dẫn & bàn giao` (`2:279`).
- Main screen IDs: S01 `4:175`, S02 `4:198`, S03 `4:229`, S04 `4:274`, S05 `4:336`, S06 `4:396`, S07 `4:444`, S08 `4:491`, S09 `4:565`, S10 `4:616`, S11 `4:755`, S12 `4:789`, S13 `4:838`, S14 `4:875`.
- 50 editable mobile frames: 14 primary contexts, edit modes, illustrative error/empty/weather/permission/reminder states, dialogs and BMI WHO comparison.
- Last structural inspection: 876 text layers, 193 instances, 231 prototype connections, no missing navigation destinations or scrolling viewports detected.
- Visual inspection completed for Home and Reminders, followed by spacing and palette refinements. Additional screenshot review was blocked by the Figma Starter MCP tool call limit; remaining frames have structural inspection only.
- Prototype simulates navigation with fixed example data. It does not save form input, run Android permissions/alarms or implement arbitrary picker selections.
- Visual direction: forest green, off-white surfaces, blue hydration accent, purple sleep accent; Roboto, 412 × 892 frames, reusable buttons/fields/badges and Material 3 switches. Local semantic variables reference primitives.
- App source was not changed. Refer to the handoff page for reviewing and implementation notes.

## Resume

Inspect the existing file and exact IDs before making changes. Continue in this file; do not create a replacement. Figma's MCP call limit must be available again before further tool-based edits or screenshots. `VITA_PRODUCT_SPEC_FOR_FIGMA.md` is a historical snapshot from before Weather/AQI and Sleep were removed. Use the current Android source and newer project decisions as the functionality reference.
