# App icon

A phone and a half-open foldable standing on a shelf. *Hylla* is Swedish for shelf.

| File | Use |
| --- | --- |
| `hylla-icon.svg` | Full-bleed 1024 master. iOS app icon; each platform applies its own mask. |
| `hylla-icon-foreground.svg` | Android adaptive-icon foreground, 108 grid, artwork inside the 66-unit safe circle. |
| `hylla-icon-monochrome.svg` | Android themed-icon layer. Screens are cut out, so it reads as a single ink. |

| Token | Hex |
| --- | --- |
| Background | `#12343B` |
| Device | `#F3EBDC` |
| Device, far panel | `#DCCFB8` |
| Screen, far panel | `#0E2A30` |
| Shelf | `#F2A541` |

Platform resources (Android vector drawables, the iOS asset catalog) are derived from these masters.
Change the SVGs first, then bring each platform copy back in sync.
