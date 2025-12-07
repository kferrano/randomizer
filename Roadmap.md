# Randomizer – Development Roadmap
*(This file tracks all planned and completed work.  
ChatGPT uses this file for context and future planning.)*

---

## ✅ Completed (up to v0.4.0)
- Admin command `/randomizer manual` (self, target, selectors)
- Bossbar per-client toggle (live, no reconnect needed)
- HUD/Timer sync fixes on join/rejoin
- Fixed elapsedTicks regression
- Pillager weapon fix (finalizeSpawn + fallback)
- Default blacklist entry: `minecraft:giant`
- Removed duplicate old `onPlayerLogin` handler
- Unified all systems under a single RandomizerManager instance
- Config cleanup:
    - `disableHud` → renamed to `disableTimer`
    - Added `disableBossbar`
    - Added lang entries for admin/bossbar/timer categories

---

# 🎯 Roadmap to 1.0.0 – Core Feature Completion (High Priority)

## 1. Event System Polish
- Stabilize Tick → Event → Sync pipeline
- Ensure event timing is consistent in MP
- Improve event result messaging per target

## 2. Mod Compatibility
- Auto-detect **modded items**
- Auto-detect **modded mobs**
- Auto-detect **modded potion/effect types**
- Safety blacklist for problematic entities

## 3. Co-op Mode
- Global shared timer for all players
- Random event selects random player as target
- Sync-safe behavior during join/leave events

## 4. Config Enhancements
- Min/Max mob spawn count per event
- Weighted effect/item/mob pools
- Whitelist/Blacklist (items, mobs, effects)
- Config categories fully organized

## 5. Performance & Stability
- Tick optimization
- Clean network sync paths
- Add structured logging (info/debug/event logs)

---

# ✨ 1.1.0 – Visual & QoL Update

## Presentation Improvements
- Wheel-of-Fortune animation (optional)
- Sound effects on event trigger (optional)
- Event preview: “In 5s spawns…”
- Better HUD/Bossbar feedback

---

# ⚡ 1.2.0 – New Event Types

## New Event Families
- Weather events (rain/thunder/clear)
- World events (lightning, random teleport, explosions)
- Block/environment events (ore spawn, block swaps)
- Rare event chains / combined effects

---

# 🛠️ 1.3.0 – Admin & Server Tools

## Admin Tools Expansion
- `/randomizer force <event>`
- Event queueing system (pre-select upcoming events)
- `/randomizer status full` with detailed pool info
- Optional webhook or external integrations

---

# 🌟 Long-Term (Ideas)

## Advanced Systems
- Custom Event Editor (Ingame GUI)
- Event scripting (if event A then event B)
- World-scale anomalies (meteors, raids, structures, chaos events)

---

# 📌 Notes for ChatGPT
- Always use this roadmap as the source of truth.
- When features are completed, move them to the "Completed" section.
- When new ideas come up, add them under the appropriate milestone.
- Keep the list clean, structured and always up to date.

 (*(This file is used for planning and prioritization.  
It is not meant to be read by humans.)*)