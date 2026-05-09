# In-Game UI Redesign — Handoff Plan

## Why this exists

The current in-game UI looks bad. Flat 2D panels stacked on the right edge of the screen, plus oversized cards in the top corners, fight the 3D factory aesthetic of the rest of the game. We want a **diegetic** UI — info displayed *inside* the 3D world (on screens, props, holograms) — so the HUD feels like part of the factory instead of stickers on top of it.

A first attempt was made on the `3dtrygpt` branch (file `src/game/view3d/FactoryShellApp.java`). It is **not good** and the next person should treat it as a starting point at best, or revert and start fresh.

## What the game looks like now

- 3D factory shell with walls, floor, doors at the back, and a `9×11` board of cells.
- Two animated character tokens (player + opponent) walk between cells.
- Camera orbits around the board (target ~`(0, 0.85, 0)`, distance ~13.4, pitch ~48°).
- All gameplay info (player stats, opponent stats, event log, card reference, dice roll) is currently rendered as 2D `BitmapText` + colored `Quad`s on the GUI layer.

## What we want

### A. Wall-mounted 3D player screens (primary win)

Use the existing `assets/Models/boardcells/player-screen.glb` (~11MB). Mount **two instances** on the back wall behind the board — one for the player, one for the opponent — so they read like real factory monitors.

Each screen shows:
- A **portrait/avatar** (we only have `assets/Textures/ui/yeti-portrait.png`; falling back to a colored circle with the monster's first initial is acceptable until proper portraits are made)
- **Label**: `PLAYER` / `OPPONENT` chip + current `Role` (`SCARER` / `LAUGHER`)
- **Monster name** (large)
- **Energy bar** with `n / 1000` count
- **Position**: `CELL n / 99`
- **Status chip** (frozen / shield / confusion / momentum / focus) when active

### B. Diegetic card-effects display

The 5 power-up cards (`ENERGY STEAL`, `SHIELD`, `SWAPPER`, `START OVER`, `CONFUSION`) are reference info — they don't change. Treat them as a **third wall display** between the two player screens (or somewhere unobtrusive on the back wall). They can be a single low-poly sign / kiosk with the 5 cards as small thumbnails.

### C. Slim 2D bottom strip for the dynamic stuff

Event log + dice-roll CTA stay 2D, because they update every turn and need to be glanceable. They go in a **single horizontal strip across the bottom of the screen**, semi-transparent so they don't crowd the board.

```
============================================ back wall + colored doors =====
   ┏━━ player screen ━━┓                      ┏━━ player screen ━━┓
   ┃ avatar  PLAYER    ┃                      ┃ avatar  OPPONENT  ┃
   ┃         RANDALL   ┃    (card ref kiosk)  ┃         FUNGUS    ┃
   ┃ energy ███▒▒  9/99┃                      ┃ energy █▒▒▒  37/99┃
   ┗━━━━━━━━━━━━━━━━━━━┛                      ┗━━━━━━━━━━━━━━━━━━━┛
   ░░░░░░░░░░░░░░░░░░  3D BOARD  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
─────────────────────── 2D bottom strip ──────────────────────────────
  TURN  |  • You moved 7→9   • Scarer door +115   |  [SPACE]  ROLL DICE
```

## Strongly recommended approach: render-to-texture

The first attempt parented flat `Quad`s + `BitmapText` to the screen prop in world space, with a small forward offset (`SCREEN_OVERLAY_OFFSET = 0.08f`). It looks bad because:

- The overlay rectangles don't actually align with the screen's screen-face mesh (we don't know the .glb's exact internal layout).
- Z-fighting and depth-sorting issues with `Bucket.Transparent`.
- BitmapText scaled to world units is blurry and aliased at this distance.

**Better approach: render the UI to a `FrameBuffer` once, then use that FrameBuffer's texture as the diffuse map of the screen-mesh's material** inside the .glb. This is jME's standard render-to-texture pattern (`com.jme3.texture.FrameBuffer`, `RenderManager.renderViewPort`).

Steps:
1. In Blender, open `player-screen.glb` and identify the sub-mesh that represents the actual screen surface (the flat panel on the front). Note its material name.
2. At runtime, locate that sub-mesh by name (`spatial.depthFirstTraversal(...)` with name match) after loading the model.
3. Build an off-screen `ViewPort` that renders a 2D scene (the same `BitmapText` + `Quad`s, but in pixels with `Bucket.Gui`) into a `FrameBuffer` with a `Texture2D` color attachment.
4. Set that texture as `ColorMap` (or whatever the screen material's diffuse param is named) on the screen sub-mesh's `Material`.
5. Re-render the off-screen scene whenever monster state changes (`refreshInfoPanels()` is the existing hook, called from `startBoardGame`, `handleUsePowerup`, and the move handler).

This gives crisp, perspective-correct, properly-lit screen content that bends with the actual geometry of the .glb, not floating quads pretending to be on top of it.

If render-to-texture is too much, the fallback (what was attempted) is acceptable but the screen-prop's own front face must be **invisible** (transparent or removed) so it doesn't occlude the overlay; otherwise the overlay must be sized + positioned to exactly match the prop's screen recess, which requires opening the model in Blender first.

## Where to start in the code

All UI code lives in **one file**:
`src/game/view3d/FactoryShellApp.java` (~1600 lines, single big class).

Key existing functions:
- `loadFactoryShell()` (~line 642) — builds floor + walls.
- `attachBackWall()` (~line 666) — back wall is at `z = -ROOM_HALF = -16`, walls are `WALL_HEIGHT = 7.2` tall.
- `startBoardGame(Role)` (~line 284) — entry point after role selection. Calls `attachWallScreens()`, `refreshInfoPanels()`, `ensureGameplayHud()`.
- `refreshInfoPanels()` (~line 906) — single hook called whenever monster state changes. Currently calls `refreshWallScreens()` + `ensureGameplayHud()`.
- `refreshGameplayHud()` (~line 352) — rebuilds the 2D right-side rail (event log, card effects, dice roll).
- `attachWallScreens()` / `createWallScreen()` / `populateWallScreen()` (~line 910–1050) — **the failed first attempt at the wall screens**. Either replace these with the render-to-texture approach above or fix them with proper alignment to the .glb's screen-face mesh.

Constants worth knowing (top of file):
- `ROOM_HALF = 16` — half the room size; back wall at `-16`, side walls at `±16`.
- `WALL_HEIGHT = 7.2`, `BOARD_CELL_SIZE = 1.5`.
- Camera orbits with `ORBIT_START_DIST = 13.4`, pitch `48°`. Camera target is `(0, 0.85, 0)`.
- `Constants.WINNING_ENERGY = 1000` (max energy), `Constants.WINNING_POSITION = 99` (final cell).

## Concrete task list

1. **Open `player-screen.glb` in Blender.** Note: orientation, dimensions, name of the sub-mesh + material that represents the screen face. Without this you're guessing.
2. **Position two screens on the back wall** at sensible coordinates. Suggested starting values: `y = 4.0`, `z = -8.0`, `x = ±6.5`, with a small inward yaw of about `±15°` so they angle toward the orbit camera. Tune by eye until they read at the orbit angles the camera uses.
3. **Render dynamic UI to a FrameBuffer-backed texture** and apply it to the screen sub-mesh's material. Each player gets its own texture/FrameBuffer; refresh on `refreshInfoPanels()`.
4. **Delete the right-side 2D rail** in `refreshGameplayHud()` and replace with a slim **bottom strip**: most-recent event title on the left, dice CTA (`PRESS [SPACE] TO ROLL`) on the right, last roll text in the middle. Height ~`80px`, full screen width minus margins, semi-transparent backdrop.
5. **Add a card-effects kiosk** between the two player screens on the back wall. A single textured plane with the 5 cards as small icons + names is enough; no dynamic content (these never change).
6. **Remove dead code** from the failed first attempt: `attachWallScreens`, `createWallScreen`, `populateWallScreen`, `worldQuad`, `worldText`, the `screenRoot` / `playerScreenContent` / `opponentScreenContent` fields, and all the right-side rail helpers (`attachHudPanelFrame`, `attachEventRows`, `attachCardRows`, `attachCardRow`, `attachRollPanel`).
7. **Test at the actual camera angle.** The orbit camera moves — make sure the screens stay readable across the full pitch/yaw range, not just the starting frame.

## Things the first attempt got wrong (don't repeat)

- Treated the screen prop as a backdrop and stacked floating `Quad`s on top instead of drawing onto the prop's actual screen surface.
- Hardcoded an `0.08f` z-offset for the overlay without verifying the prop's front-face direction in Blender.
- Used world-space `BitmapText` at small font sizes — looks blurry at the camera's typical distance.
- Kept the right-side 2D rail in addition to the new screens, doubling up the info and crowding the screen.
- Used the monster's first letter in a colored circle as an "avatar" — fine as a stub, but reads as low-effort. Prefer plain initials *only* until real portraits exist for each monster (`celia`, `fungus`, `randall`, `sullivan`, `yeti`, plus the ones that share a yeti-portrait fallback).

## How to run

```
gradle run
```

Press `S` (Scarer) or `L` (Laugher) at the role-selection menu to enter the board.

## Branch state

Branch: `3dtrygpt`. The first attempt's changes are still in `src/game/view3d/FactoryShellApp.java` (uncommitted). If the next dev wants a clean slate, run:

```
git checkout -- src/game/view3d/FactoryShellApp.java
```

…to restore the previous "right-side rail + 3D floating panels" version, then start the rebuild from there.
