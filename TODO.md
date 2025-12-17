# 📝 TODO – v0.6.0

This file contains all planned development tasks for version 0.6.0.
Items listed here may change during development and do not represent final features.

---

## 1. Event Control & Cooldowns

- [ ] Add global event cooldown (configurable)
- [ ] Add per-player event cooldown
- [ ] Optional additional cooldown after EXTREME tier events
- [ ] Ensure cooldowns are respected by:
  - automatic events
  - manual events (unless admin override is enabled)
- [ ] Add admin override for cooldowns (manual command only)

---

## 2. Granular Event Enable / Disable

- [ ] Add config toggles to enable/disable:
  - [ ] Item events
  - [ ] Mob events
  - [ ] Effect events
- [ ] Optional tier-based toggles:
  - [ ] Disable COMMON tier
  - [ ] Disable RARE tier
  - [ ] Disable EXTREME tier
- [ ] Ensure disabled event types are fully excluded from selection logic
- [ ] Add clear config comments and translations

---

## 3. Manual Event Command Improvements

- [ ] Extend `/randomizer manual` command:
  - [ ] Target specific player
  - [ ] Optional event type argument
  - [ ] Optional forced tier (admin-only)
- [ ] Add safety checks to prevent abuse
- [ ] Ensure manual events respect config rules by default
- [ ] Improve error messages for invalid command usage

---

## 4. Multiplayer Safety & Edge Case Handling

- [ ] Prevent duplicate events when players join/leave during event execution
- [ ] Ensure target selection is multiplayer-safe
- [ ] Handle cases where target disconnects mid-event
- [ ] Avoid overlapping events on the same player
- [ ] Verify tick-based guards work correctly in multiplayer environments

---

## 5. Event Context Awareness (Lightweight)

- [ ] Optional spawn protection:
  - [ ] No EXTREME events near world spawn
  - [ ] Configurable spawn radius
- [ ] Optional player protection:
  - [ ] No EXTREME events for new players (playtime-based)
- [ ] Optional Y-level restrictions for mob events
- [ ] Ensure all context checks are configurable and documented

---

## 6. Admin Statistics & Diagnostics

- [ ] Track event counts per:
  - [ ] Event type (item/mob/effect)
  - [ ] Rarity tier
- [ ] Add `/randomizer stats` command (admin-only)
- [ ] Add ability to reset statistics
- [ ] Ensure stats collection has minimal performance impact
- [ ] Optional debug-only extended diagnostics

---

## 7. Performance & Internal Cleanup

- [ ] Review tick → event pipeline for inefficiencies
- [ ] Optimize selection logic where needed
- [ ] Remove any remaining dead code or legacy helpers
- [ ] Improve internal method naming and structure where clarity is lacking
- [ ] Add debug logs for skipped or excluded events (debug-only)

---

## 8. Documentation & Release Prep

- [ ] Update documentation/comments for new config options
- [ ] Update `en_us.json` with all new translations
- [ ] Verify TODO completion before feature freeze
- [ ] Prepare changelog entries during development
