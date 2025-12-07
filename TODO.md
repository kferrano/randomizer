# TODO – Version 0.5.0
*(Tasks planned for the next development cycle.  
Once completed, they will be moved to the roadmap “Completed” section.)*

---

## 🎯 High Priority (0.5.0 Core Goals)

### 1. Weighted Event Pools
- Add configurable weight system for:
    - Item events
    - Mob events
    - Effect events
- Config option: weight per category (common/rare/extreme events)
- Update Randomizer logic to respect weights when choosing event type

### 2. Min/Max Mob Spawn Count
- Add config settings:
    - `minMobsPerEvent`
    - `maxMobsPerEvent`
- Use dynamic/random ranges within configured limits
- Ensure compatibility with modded mobs

### 3. Improved Event Targeting Feedback
- When a player is selected as event target:
    - Show clear chat feedback
    - Highlight the affected player
    - Possibly add a short HUD notice

---

## 🔧 Medium Priority

### 4. Extended Modded Compatibility
- Auto-detect **modded potion/effect types**
- Improve scanning of modded entities to avoid edge cases
- Add automatic fallback logic for unknown entity attributes

### 5. Enhanced Config UI
- Add UI-toggle settings in the config screen for:
    - Event weights
    - Mob spawn min/max
    - Whitelist/Blacklist toggles
- Group settings by category (User-friendly layout)

---

## 🐛 Low Priority / Maintenance

### 6. Logging Improvements
- Add event logging with severity levels:
    - `INFO` (normal events)
    - `WARN` (unexpected behavior)
    - `ERROR` (failures)
- Add timestamps to log output for better debugging

### 7. Code Cleanup
- Review network handlers for unnecessary boilerplate
- Improve null-safety in event selection logic
- Consolidate duplicate randomization code blocks

---

## 📌 Notes
- All tasks in this file are for version **0.5.0** only.
- Completed tasks should be moved to `ROADMAP.md` under “Completed”.
- New ideas should go into the roadmap, **not** this file.

 (*(This file is used for planning and tracking.  
It is not intended to be a detailed changelog.)*)