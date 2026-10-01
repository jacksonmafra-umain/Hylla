# Fixtures

`devices.json` is the only artifact both apps read. Each platform parses it with its own native
model; nothing is generated or shared at the code level. All data is invented.

## Shape

```json
{
  "schemaVersion": 1,
  "people": [{ "id": "p-01", "name": "Alva Berg" }],
  "devices": [{ "id": "HYL-001", "deviceName": "Fold7 Blue", "...": "..." }]
}
```

| Field | Type | Values |
| --- | --- | --- |
| `id` | string | Shelf tag, `HYL-NNN`. Unique. What the scanner reads. |
| `deviceName` | string | |
| `deviceType` | enum | `phone` · `tablet` · `headset` · `desktop` |
| `platform` | enum | `ios` · `ipados` · `android` · `visionos` · `macos` · `carplay` |
| `modelName`, `modelNumber` | string | |
| `osVersion` | string | |
| `uiVersion` | string or null | Manufacturer skin. `null` when there is none. |
| `assignmentStatus` | enum | `inUse` · `available` · `missing` · `needsSorting` |
| `currentUser` | person id or null | Set exactly when `assignmentStatus` is `inUse`. |
| `since` | date | Calendar date, `YYYY-MM-DD`, no time, no zone. |
| `homeUse` | enum | `approved` · `officeOnly` · `notApproved` |
| `lifecycle` | enum | `inUse` · `decommission` |
| `notes` | string or null | |

Enum values are stable codes, not the Notion display labels. Mapping to and from Notion labels
belongs to the Notion adapter, and display text belongs to each app's string resources.

## Never in this file

Serial number, IMEI, IMEI 2, EID, UDID, Apple Pay account. Not even fake values. Both test suites
fail if any of these keys appear.
