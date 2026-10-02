# Shelf tags

One QR code per device in `fixtures/devices.json`. Each code holds the device's link,
`hylla://device/HYL-NNN`, so scanning it in Hylla opens that device, and scanning it with the
system camera offers to open Hylla.

- `HYL-NNN.png`: one tag, with the shelf tag and the device name under the code.
- `all-tags-sheet.png`: all tags on one sheet, for printing.

Generated with Core Image's `CIQRCodeGenerator` (error correction M). Regenerate after the
fixture changes.
