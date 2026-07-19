# HBM Launch Pad Integration Roadmap

This roadmap tracks the next major project goal: allowing selected HBM Community Edition launch pads to fuel and launch Galacticraft Legacy rockets.

## Current Baseline

- The project targets Minecraft 1.12.2.
- Galacticraft Legacy is still supplied from CurseMaven.
- HBM CE is supplied from CurseMaven using the latest known 1.12.2 release dependency:
  - `curse.maven:hbm-nuclear-tech-mod-community-edition-1312314:8330665`
  - HBM CE `2.5.0.5`
- Gradle must run on Java 11 or newer because some build plugins require it.
- The mod itself still compiles for Java 8 through the Gradle toolchain setup.
- Current integration changes Galacticraft refinery input to require HBM kerosene.

## Build And Runtime Hygiene

HBM, Galacticraft, RetroFuturaGradle, and Minecraft client runs can leave large Java processes behind. Before and after heavy build or client testing:

- Check for old Java, Gradle daemon, and Minecraft client JVMs.
- Stop Gradle daemons with `.\gradlew.bat --stop` when done testing.
- Do not leave timed-out `runClient` processes running.
- If memory errors occur, check Task Manager for stale JVMs before changing Gradle memory settings.

## Phase 0: Baseline

Status: code/build baseline closed. In-game refinery and JEI behavior should be smoke-tested after any dependency update.

Goal: make sure development is happening against the intended versions and the existing kerosene integration still works.

Tasks:

- Done: confirm the latest HBM CE release dependency resolves.
- Done: confirm `.\gradlew.bat build` succeeds.
- Done: confirm `runClient` starts.
- Manual smoke test: confirm the Galacticraft refinery still accepts HBM kerosene and rejects ordinary oil.
- Manual smoke test: confirm JEI shows the custom kerosene-to-Galacticraft-fuel flow.

Success condition:

- The game launches with Galacticraft Legacy, HBM CE, JEI, and this companion mod loaded.

Closeout notes:

- HBM CE is currently pinned to release `2.5.0.5`.
- `.\gradlew.bat build` passed after the dependency update.
- `runClient` has been confirmed to reach the game.
- Gradle daemons were stopped after verification.

## Phase 1: Pad Target Selection

Status: first target selected.

Goal: decide which HBM pads are officially supported first.

First target:

- Block: `hbm:launch_pad_rusted`
- Block class: `com.hbm.blocks.bomb.LaunchPadRusted`
- Tile class: `com.hbm.tileentity.bomb.TileEntityLaunchPadRusted`
- First Galacticraft rocket target: Tier 1 rocket.

Notes:

- `TileEntityLaunchPadRusted` does not extend `TileEntityLaunchPadBase`; it has its own simpler launch flow.
- It has a redstone-triggered `launch()` path.
- It tracks HBM missile state with `missileLoaded`.
- It does not expose the normal HBM launch-pad fuel tank and power API used by `TileEntityLaunchPadBase`.
- For the first vertical slice, fueling should be proven through a Galacticraft Fuel Loader before adding HBM pad-internal fuel behavior.

Later candidates:

- `com.hbm.tileentity.bomb.TileEntityLaunchPadLarge`
- `com.hbm.tileentity.bomb.TileEntityLaunchPad`
- `com.hbm.tileentity.bomb.TileEntityLaunchTable`
- `com.hbm.tileentity.machine.TileEntitySoyuzLauncher`, if it proves useful for Galacticraft rockets.

Tasks:

- Done: inspect the current HBM CE rusted launch pad behavior.
- Done: confirm first in-game block name, `hbm:launch_pad_rusted`.
- Done: decide first Galacticraft rocket type, Tier 1 rocket.
- Later: decide which Galacticraft rocket tiers each additional HBM pad should accept.
- Later: decide whether pad size should matter for Tier 1, Tier 2, Tier 3, and cargo rockets.

Success condition:

- Done: the first supported HBM pad and first supported Galacticraft rocket type are explicitly chosen.

## Phase 2: Docking Adapter

Goal: make selected HBM pads behave like Galacticraft rocket docks.

Galacticraft expects launch pads to expose:

- `micdoodle8.mods.galacticraft.api.tile.IFuelDock`
- `micdoodle8.mods.galacticraft.api.entity.IFuelable`
- `micdoodle8.mods.galacticraft.api.entity.IDockable` for the rocket entity itself.

Tasks:

- Add mixin/interface behavior for selected HBM pad tile entities.
- Track one docked Galacticraft rocket.
- Implement or bridge:
  - `getDockedEntity()`
  - `dockEntity(...)`
  - `getConnectedTiles()`
  - `isBlockAttachable(...)`
- Clear the docked rocket when it launches, dies, unloads, or the pad is broken.
- Decide rocket center position and vertical offset for each supported HBM pad.

Success condition:

- A Galacticraft rocket can be associated with an HBM pad without crashing, duplicating, or immediately detaching.

## Phase 3: Rocket Placement

Status: first Tier 1 rusted-pad placement slice implemented and smoke-tested; placement code has been refactored into reusable services/adapters.

Goal: let players place Galacticraft rockets onto supported HBM pads.

Known Galacticraft hard checks:

- `ItemTier1Rocket.placeRocketOnPad(...)` only accepts `TileEntityLandingPad`.
- `EntityAutoRocket.isDockValid(...)` only accepts `TileEntityLandingPad`.

Tasks:

- Done: intercept `hbm:launch_pad_rusted` right-clicks when the player is holding a Galacticraft Tier 1 rocket.
- Done: resolve the rusted pad core from HBM dummy multiblock blocks.
- Done: spawn a Galacticraft Tier 1 rocket centered on the rusted pad.
- Done: consume one rocket item outside creative mode.
- Done: reject duplicate placement if another Galacticraft auto rocket is already above the pad.
- Done: smoke-tested rocket appearance, interaction, inventory access, and survival item consumption.
- Done: refactor placement into `RocketPlacementService`, `GalacticraftRocketFactory`, `HbmRocketPadAdapter`, `HbmRocketPadAdapters`, and `RustedLaunchPadAdapter`.
- Later: add real dock tracking for the placed rocket.
- Expand support to Tier 2, Tier 3, and cargo rockets after Tier 1 works.

Success condition:

- Right-clicking a supported HBM pad with a Galacticraft rocket item places the rocket on that pad.

## Phase 4: Fueling

Status: first GC Fuel Loader relay slice implemented and smoke-tested.

Goal: fuel docked Galacticraft rockets without needing a normal Galacticraft landing pad.

Quick validation option:

- Done: make `TileEntityLaunchPadRusted` act as a Galacticraft `IFuelable` fuel relay.
- Done: forward fuel inserted into the rusted pad to the Galacticraft rocket above the pad.
- Done: patch Galacticraft Fuel Loader adjacency scanning so HBM dummy multiblock blocks resolve to the rusted pad core.
- Done: smoke-tested a Galacticraft Fuel Loader adjacent to the rusted pad/multiblock; it adds fuel to the rocket.

Better final option:

- Let the HBM pad consume HBM fuel or kerosene from its own tanks.
- Transfer the equivalent Galacticraft fuel amount into the docked rocket.
- Make the conversion rate configurable.

Questions to answer:

- Should HBM kerosene fuel rockets directly?
- Should HBM rocket fuel be required instead?
- Should oxidizer be required for certain pads or rocket tiers?
- Should HBM power be required while fueling?

Success condition:

- A Galacticraft rocket on an HBM pad can be fueled successfully.

## Phase 4A: Galacticraft Power Compatibility

Status: first HBM copper cable conductor, HBM-to-GC transfer, and GC-through-HBM-cable transfer slices implemented; needs in-game smoke test.

Goal: let selected HBM energy cables participate in Galacticraft power networks so launch support can use HBM-looking infrastructure.

First target:

- Block: `hbm:red_cable`
- Tile class: `com.hbm.tileentity.network.energy.TileEntityCableBaseNT`
- Galacticraft behavior target: Heavy Aluminum Wire tier, `getTierGC() == 2`.

Tasks:

- Done: make HBM red/copper cable expose Galacticraft `IConductor` behavior.
- Done: report the same GC tier as Heavy Aluminum Wire.
- Done: refresh and merge adjacent Galacticraft power networks from the HBM cable tile.
- Done: transfer HBM network power from `hbm:red_cable` into adjacent Galacticraft electrical acceptors using HBM's HE-to-RF conversion rate and Galacticraft's RF-to-GC conversion rate.
- Done: pull power from adjacent Galacticraft electrical outputs and feed it into the cable's Galacticraft power network.
- Manual smoke test: connect an HBM power source through `hbm:red_cable` to a Galacticraft machine such as a Refinery or Fuel Loader and confirm it receives power.
- Manual smoke test: connect a Galacticraft power source through `hbm:red_cable` to a Galacticraft power consumer and confirm the consumer receives power.
- Later: decide whether non-red HBM cables should also bridge to Galacticraft power.
- Later: decide whether HBM power should be converted into Galacticraft power, or whether this compatibility should only provide GC network cabling.

Success condition:

- `hbm:red_cable` can replace Galacticraft Heavy Aluminum Wire in a Galacticraft power line.

## Phase 4B: Full HBM Launch Pad Power And Fuel Behavior

Status: first Galacticraft electrical receiver, rocket placement, gated tank filling, and built-in fuel loader slices implemented for `hbm:launch_pad`; needs in-game smoke test.

Goal: let the full HBM launch pad receive Galacticraft power from Galacticraft aluminum wire while preserving HBM's own launch-pad fuel rules.

First target:

- Block: `hbm:launch_pad`
- Block class: `com.hbm.blocks.bomb.LaunchPad`
- Core tile class: `com.hbm.tileentity.bomb.TileEntityLaunchPad`
- Shared base tile class: `com.hbm.tileentity.bomb.TileEntityLaunchPadBase`
- Proxy tile class: `com.hbm.tileentity.TileEntityProxyCombo`

Findings:

- `hbm:launch_pad` has internal HE energy storage with `maxPower = 100000`.
- HBM launch readiness requires at least `75000` HE.
- The full pad creates HBM proxy tiles on non-core multiblock blocks, and those proxies already route HBM power/fluid/inventory access to the core.
- HBM energy connections are horizontal only.
- Direct Forge/NTM tank filling can set an empty tank to a valid mapped fluid type.
- Normal HBM launch readiness is missile-driven: the missile in slot 0 determines which two tank fluid types are required and how much fluid each tank must contain.
- `setFuel(ItemMissileStandard)` assigns tank types based on the HBM missile fuel type, for example kerosene plus peroxide or kerosene plus oxygen.
- `canLaunch()` still requires a valid launchable HBM missile, sufficient HE, sufficient fluid in both required tanks, and the pad's ready state.

Tasks:

- Done: add Galacticraft `IElectrical` behavior to the `hbm:launch_pad` core tile.
- Done: add Galacticraft `IElectrical` behavior to HBM proxy tiles when their core is `hbm:launch_pad`.
- Done: treat the full HBM launch pad as a Galacticraft power consumer, not a producer.
- Done: report Heavy Aluminum Wire tier compatibility.
- Done: convert inserted Galacticraft energy into HBM HE using the same HE/RF and RF/GC config rates used by the HBM copper cable bridge.
- Done: add Galacticraft rocket placement support for `hbm:launch_pad`.
- Done: expose the full HBM launch pad core and proxy tiles as Galacticraft `IFuelable` targets.
- Done: reject Galacticraft fuel insertion when no Galacticraft rocket is sitting on the pad.
- Done: set both HBM launch-pad tanks to Galacticraft Fuel while a Galacticraft rocket is present so the existing HBM GUI shows GC fuel storage.
- Done: store incoming Galacticraft Fuel in the pad's two HBM tanks instead of forwarding it directly to the rocket.
- Done: add a built-in pad fuel loader that moves `2 mB/t` from the HBM pad tanks into the Galacticraft rocket when the pad has stored HE.
- Done: consume `1 HE` per `1 mB` moved by the built-in loader.
- Manual smoke test: place Galacticraft aluminum wire against `hbm:launch_pad` proxy/side blocks and confirm the wire connects.
- Manual smoke test: power the pad from a Galacticraft power source and confirm the HBM pad's internal energy increases.
- Manual smoke test: right-click `hbm:launch_pad` with a Galacticraft Tier 1 rocket and confirm the rocket appears centered on the pad.
- Manual smoke test: try inserting Galacticraft Fuel into the pad with no rocket present and confirm no fuel is accepted.
- Manual smoke test: place a rocket on the pad, insert Galacticraft Fuel, and confirm both HBM tank slots identify as Fuel from GC.
- Manual smoke test: charge the pad, leave fuel in the pad tanks, and confirm the rocket's internal fuel level increases.
- Later: decide whether HBM kerosene should also be accepted and converted into Galacticraft Fuel for this pad.

Success condition:

- Galacticraft aluminum wire can connect to `hbm:launch_pad`, charge its internal HBM energy buffer, and the pad can store and load Galacticraft Fuel into a docked Galacticraft rocket without accepting fuel while idle.

## Phase 5: Launch Trigger

Goal: launch docked Galacticraft rockets from HBM pad behavior.

Tasks:

- Detect HBM pad launch actions such as redstone, GUI buttons, or existing launch commands.
- If a Galacticraft rocket is docked, call the rocket's launch path, likely `igniteWithResult()`.
- Prevent the HBM pad from trying to launch an HBM missile when a Galacticraft rocket is docked.
- Gate launch on chosen requirements:
  - rocket has fuel
  - pad has enough power
  - pad structure is ready
  - large pad is erected
  - fuel or oxidizer requirements are satisfied

Success condition:

- Triggering launch on the HBM pad starts the normal Galacticraft launch countdown.

## Phase 5A: Lander Pad Return

Status: first rocket landing-pad inventory slice implemented; needs in-game GUI and destination smoke test.

Goal: when a Galacticraft rocket launches from a supported HBM pad, let the player bring Galacticraft launch pads in a dedicated rocket slot so the destination lander can use those pads without creating free resources.

Notes:

- Stock Galacticraft records returned launch pads on `GCPlayerStats` during `EntityAutoRocket.onLaunch()`.
- HBM pads should remain in the origin world.
- HBM launches should not automatically create Galacticraft pads, because that creates a resource loop.
- Players who want Galacticraft pads at the destination should put those pads in the new rocket GUI landing-pad slot.
- The first implementation supports `hbm:launch_pad_rusted` through the shared HBM pad adapter lookup.

Tasks:

- Done: ship companion rocket GUI textures with a dedicated landing-pad slot.
- Done: swap Galacticraft rocket inventory GUI textures to the companion versions.
- Done: add a restricted rocket GUI slot that only accepts normal Galacticraft launch pads.
- Done: persist the dedicated landing-pad stack on Galacticraft auto rockets.
- Done: hook Galacticraft `EntityAutoRocket.onLaunch()` after vanilla launch-pad collection.
- Done: detect whether the launching rocket is sitting on a supported HBM pad.
- Done: when launching from a supported HBM pad, move the dedicated slot stack into the rider's Galacticraft launch-pad return stack.
- Done: clear the rocket's dedicated pad slot after handing the stack to Galacticraft.
- Manual smoke test: open the rocket GUI and confirm the new slot appears, shows a tooltip, and only accepts normal Galacticraft launch pads.
- Manual smoke test: launch from `hbm:launch_pad_rusted` with pads in the new slot, travel to another dimension, and confirm the lander contains or places those pads.
- Manual smoke test: launch from `hbm:launch_pad_rusted` with the slot empty and confirm no free Galacticraft pads appear at the destination.
- Later: decide whether each HBM pad type should return Galacticraft pads, no pads, or a custom item.

Success condition:

- A player launching a Galacticraft rocket from a supported HBM pad can bring Galacticraft launch pads in the rocket GUI and receive those pads through the normal destination lander behavior.

## Phase 6: UX And Config

Goal: make the integration understandable and adjustable.

Tasks:

- Add config options:
  - enable or disable HBM pad rocket support
  - supported pad types
  - fuel conversion rate
  - require HBM power
  - require HBM oxidizer
  - allow or disallow specific Galacticraft rocket tiers
- Add chat or GUI status feedback for failed placement, fueling, and launch attempts.
- Add localization strings.
- Add JEI or tooltip notes for supported pads.

Success condition:

- Players can tell why a rocket can or cannot be placed, fueled, or launched.

## Phase 7: Compatibility Pass

Goal: make the feature robust across rocket types and world conditions.

Tasks:

- Support Tier 2 rockets.
- Support Tier 3 rockets.
- Support cargo rockets.
- Test multiplayer behavior.
- Test chunk unload and reload behavior.
- Test pad destruction while a rocket is docked.
- Test rocket destruction while docked.
- Test save/load persistence.

Success condition:

- The feature works across supported rocket and pad combinations without duplication, loss, or stale references.

## Recommended Vertical Slice

The first playable implementation should be intentionally narrow:

1. Support `hbm:launch_pad_rusted` / `TileEntityLaunchPadRusted`.
2. Support Galacticraft Tier 1 rockets.
3. Add the docking adapter.
4. Patch Tier 1 rocket placement.
5. Prove fueling through a Galacticraft Fuel Loader.
6. Trigger a normal Galacticraft launch countdown from the HBM pad.

Once that works, expand the feature instead of guessing at every pad and rocket combination up front.
