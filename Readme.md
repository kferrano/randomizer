# Randomizer

A lightweight but powerful **random event mod** for Minecraft, built for **NeoForge 1.21.10**.

Randomizer periodically triggers random events for players:
- Gives random items
- Spawns random mobs
- Applies random potion effects

Designed for **SMP chaos**, **challenge runs**, and **stream content**.

---

## ✨ Features

- 🔁 **Random Events**
    - Random **items**, **mobs**, and **effects**
    - Uses the full registry – supports **modded content** out of the box
- ⏱ **Global Timer**
    - Server-controlled timer that periodically triggers events
    - Works in **singleplayer** and on **dedicated servers**
- 🧍 **Player Targeting**
    - Events target a random online player
    - Co-op friendly: everyone shares the same timer cycle
- 🖥 **HUD Timer**
    - Client-side HUD above the hotbar showing the running Randomizer timer
    - Adjustable position via config (`hudX`, `hudY`)
    - Can be fully disabled per client
- ⚙ **Configuration**
    - Common (server-side) config for gameplay & balancing
    - Client config for HUD and display options
    - Whitelists / blacklists and weights for events (items, mobs, effects)
- 🧪 **Experimental Bossbar Mode**
    - Optional bossbar showing progress toward the next event
    - Controlled server-side, disabled by default
- 🛠 **Admin Tools**
    - `/randomizer manual` – trigger extra events manually for testing or server events

---

## 📦 Requirements

- **Minecraft:** 1.21.10
- **Loader:** NeoForge 21.x
- Java 17+

Make sure the NeoForge version you use matches the Minecraft version (1.21.10) and is compatible with your other mods.

---

## 🔧 Commands

All commands require **OP / permission level 2**.

```text
/randomizer start
    Start or resume the Randomizer timer.

/randomizer pause
    Pause the timer without resetting it.

/randomizer stop
    Stop the timer and reset the internal time & HUD.

/randomizer status
    Show current state (RUNNING/PAUSED/STOPPED),
    current delay between events and elapsed time.

/randomizer manual
    Trigger a single random event on yourself.

/randomizer manual <targets>
    Trigger one random event for each targeted player.
    Examples:
    /randomizer manual KlausFerrano
    /randomizer manual @a
