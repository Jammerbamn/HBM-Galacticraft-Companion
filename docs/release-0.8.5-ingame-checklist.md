# 0.8.5 In-Game Test Checklist

This is the release gate for the single CE/EE jar. Test the *same jar* in two separate installations, one with EE and one with CE. Use disposable worlds or backed-up saves. For failures, record the step, expected and actual result, and `run/logs/latest.log`.

Manual gameplay status (2026-11-07): user reports all EE and CE in-game checks below passed. Automated dedicated-server startup now passes for both forks. The exact manually tested jar SHA-256 and dependency versions have not been recorded, so artifact-specific release gates remain open.

Test jar SHA-256: `________________`  Galacticraft version: `________________`  MixinBooter version: `________________`

EE version/world: `________________`  CE version/world: `2.6.1.0 / ________________`

## Checks Without A World

- [x] Release jar metadata says 0.8.5; it contains the refinery, CE, EE, and refmap JSON files. Every class named by those mixin configs is packaged. The tested jar SHA-256 is not yet recorded above.
- [x] The four rocket GUI PNGs in the release jar decode successfully at 256x256; refinery textures and English translations are packaged. `mcmod.info` names version 0.8.5 and the required mods.
- [x] Static code/package check: the rocket landing-pad slot accepts only GC landing-pad items with metadata 0 and caps the stack at 9. The EE 3D preview mixin is deliberately absent. These checks do not prove the GUI or slot works in game.
- [x] Static code/package check: refinery replacement and oil-generation suppression are listed in the shared mixin config, and the camera restore handler is registered when GC and HBM are present. Their behavior still needs world testing.
- [x] `runClient -Phbm_dev_target=ee` reached Forge's 13-mod load completion. The log queued refinery and EE mixins and skipped CE mixins, with no companion mixin errors.
- [x] `runClient -Phbm_dev_target=ce` with CE 2.6.1.0 reached Forge's 13-mod load completion. The log queued refinery and CE mixins and skipped EE mixins, and the CE GC fuel type registered successfully.
- [x] Removed the redundant `MCBlock_HBMPadRocketCleanupMixin` that targeted vanilla `Block` too late. Neither fork now logs the early-target error. Pad-break behavior still needs the world checks below.
- [x] `runObfClient` with each fork loaded the packaged release jar from `run/obfuscated/mods`, reached 13-mod completion, and selected only that fork's mixins without companion injection errors.
- [x] Mixin's exported transformed classes show companion handlers on EE pad cleanup, cable, refinery, and rocket inventory classes. CE cable tick/invalidate handlers appear in both dev and obfuscated client exports. CE pad tile handlers still need a world to load.
- [x] CE-target and EE-target builds package the same 95 entry names with identical entry contents. The outer jar SHA changes across builds due to archive metadata; identify the exact final jar used for manual testing.
- [x] Start a dedicated server with each fork. After the user authorized EULA acceptance, `runObfServer` reached `Done` for EE and CE in separate worlds; both stopped and saved cleanly.
- [ ] Install the *release jar* in normal EE and CE modpacks and confirm both reach the menu with the correct mixin selection. `runObfClient` is a closer packaging check than `runClient`, but still uses Gradle's launch harness.

Gradle reports `test NO-SOURCE`; there are no automated Java tests in this project yet.

Automated client processes were closed after reaching the menu; nonzero `runClient`/`runObfClient` exit codes from forced shutdown are not startup crashes. Those automated client runs performed no world interaction; the checked gameplay results below are user-reported.

Automated startup pass (2026-11-07): `build` succeeded (`test NO-SOURCE`). `runClient` and `runObfClient` reached Forge's 13-mod completion for both EE and CE, with the correct fork mixins selected and no companion injection errors. `runObfServer` reached Forge's 12-mod completion and `Done` for EE and CE, then exited normally after `stop`. The CE server used a separate `ce-startup` world; `server.properties` was restored to `level-name=world`. The current CE-target jar at `build/libs/hbmgccompanion-0.8.5.jar` has SHA-256 `CF116C2700E1001D644B2ABEC04550A64DEB37EE10CC939BFBCAB8158A4D0588`. That is a build-artifact hash, not a confirmed hash for the earlier manual gameplay runs.

## Test Setup

- [x] Confirm `hbmgccompanion-0.8.5.jar` contains both CE and EE mixin configs and classes.
- [ ] Record the SHA-256 of the jar used for both fork tests; do not rebuild or swap jars between runs.
- [x] Start EE with Minecraft 1.12.2, Galacticraft Legacy, HBM Extended Edition, MixinBooter, JEI, and the test jar. Do not load CE simultaneously.
- [x] Confirm the Mods screen reports 0.8.5 with the release jar. Check `latest.log`: EE mixins queue, CE mixins skip, and there are no companion mixin errors or missing classes.

## Shared Compatibility - EE

- [x] GC Refinery accepts HBM kerosene from a container and produces GC fuel. Ordinary oil does not substitute for kerosene.
- [x] GC Refinery accepts HBM kerosene through a pipe and produces GC fuel.
- [x] JEI shows one correct kerosene-to-GC-fuel refinery recipe, including the kerosene fluid icon and both fluid bars.
- [x] In newly generated chunks, check that Galacticraft oil wells do not appear. Existing oil wells in old chunks are outside this check.
- [x] Enter and exit a GC rocket and a lander; the camera returns to first person on exit.
- [x] Save, leave the world, and rejoin. The refinery recipe, JEI display, and camera behavior still work without new log errors.

## EE Launch Pad

Use the 3x3 `hbm:launch_pad` and a GC Tier 1 rocket. Test survival mode unless a step says otherwise.

- [x] Right-click a pad part with a rocket item. One item is consumed, one centered rocket appears, and the pad rocket slot contains that item.
- [x] Open the pad GUI. The rocket slot accepts a GC Tier 1 rocket, including shift-click insertion, and the rocket appears on the pad.
- [x] Remove the item from the pad rocket slot. The spawned rocket disappears and the returned stack retains its type, fuel, and other original NBT.
- [x] Reinsert the rocket and open its inventory. The HBM pad version of the rocket GUI shows the GC landing-pad slot and its hover tooltip; that slot rejects unrelated items.
- [x] The EE pad GUI does not show a CE-style 3D rocket preview window.
- [x] Open a Tier 1 rocket on a normal GC pad. Its normal GUI appears without the extra landing-pad slot.
- [x] The landing-pad slot accepts at most 9 GC landing pads and rejects non-pad blocks and other items.
- [x] Attach a powered GC Fuel Loader to a middle/edge part of the HBM pad. With GC fuel in the loader, the rocket's fuel increases; without power or fuel, it does not.
- [x] Save and rejoin with a rocket on the HBM pad. It remains centered, its pad slot and NBT remain intact, and fueling resumes without creating another rocket.
- [x] Break a middle/edge pad part with a rocket present. The rocket entity disappears, the rocket item drops once, and no rocket respawns.
- [x] Repeat by breaking the pad core. The rocket entity disappears and the rocket item drops once.
- [x] With the pad intact, destroy the rocket entity. The rocket drops once, the pad slot clears, and no replacement rocket appears. Check whether custom item NBT survives in the dropped rocket.

## EE Power

- [x] An HBM copper cable physically connects to a GC producer and powers a GC consumer through the HBM cable.
- [x] Check HBM copper cable next to a GC machine in both placement orders. The cable visibly connects and transfers power after either order.
- [x] Place HBM copper cable next to GC aluminum wire in both orders. Neither cable extends into the other, and the two cable networks do not exchange power through that boundary. Same-mod cable runs still connect.
- [x] A GC aluminum cable physically connects to an HBM EE producer and powers an HBM EE consumer through the GC cable.
- [x] A GC producer powers an HBM EE consumer, and an HBM EE producer powers a GC consumer. Check that power decreases at the source and increases at the consumer.
- [x] Use both cable types separately between an EE machine producer and an EE machine consumer, and between a GC producer and GC consumer. Confirm neither cable type is restricted to devices from its own mod.
- [x] Test a machine face that rejects power connections. Neither cable attaches or transfers through that face; an allowed face still works.
- [x] Disconnect each cable in turn. Transfer stops without a crash or persistent phantom connection.

## EE Launch And Landing

- [x] Launch from the EE pad with no GC landing pads in the rocket slot. The rocket survives liftoff, the HBM pad remains, and no free GC pads appear in the lander.
- [x] During countdown, exhaust vents sideways from the HBM pad. During liftoff, the center plume appears and the side exhaust lingers briefly.
- [x] Launch again with 9 GC landing pads in the rocket slot. The lander receives the 9 pads once, the source slot is emptied, and returning does not create extra pads.
- [x] Reach the destination and confirm normal GC landing/lander behavior and no crash or missing items.

## CE Run With The Same Jar

- [ ] In a separate installation/world, load the *same SHA-256 release jar* with HBM CE instead of EE. Confirm 0.8.5 in Mods and that the log queues CE mixins, skips EE mixins, and has no companion mixin errors.
- [x] Repeat the refinery container/pipe, JEI, new-chunk oil generation, and rocket/lander camera checks with CE.
- [x] On `hbm:launch_pad_rusted`, place a GC rocket by right-click and by its HBM inventory slot. Confirm one rocket appears, one item is consumed in survival, removing the slot item removes the rocket, and its NBT survives.
- [x] On `hbm:launch_pad_rusted`, attach a powered GC Fuel Loader. Confirm rocket fuel increases; with no power or fuel, it does not.
- [x] On `hbm:launch_pad`, place a GC rocket by right-click and by its HBM inventory slot. Confirm the rocket appears centered and can be removed without duplication.
- [x] On `hbm:launch_pad`, confirm its tanks show GC fuel only when a rocket is present, accept GC fuel from a GC fluid pipe, render the fuel texture/name, and fuel the rocket when powered.
- [x] On both CE pads, confirm the HBM rocket GUI shows the landing-pad slot and tooltip, while a normal GC pad keeps the stock GUI. Check the CE pad 3D rocket preview is centered, normally colored, and animates.
- [x] On both CE pads, break a pad part/core with a rocket present. Confirm the entity disappears and its item drops once. Save/rejoin with a rocket on each pad and confirm no duplicate or lost rocket.
- [x] Confirm HBM copper cable connects to GC machines and GC aluminum cable connects to CE machines. Test GC-to-HBM and HBM-to-GC power flow, then disconnect and confirm transfer stops.
- [x] Place a GC power machine next to an existing HBM copper cable, then reverse the placement order. The HBM cable visibly connects and power transfers both ways.
- [x] Place a GC aluminum wire next to HBM copper cable in both orders. Neither cable extends into the other and no power crosses the boundary; same-mod cable runs still connect.
- [x] Use GC aluminum wire alone from a CE MK2 producer to a CE MK2 consumer and from a GC producer to a CE MK2 consumer. Use HBM copper alone from a GC producer to a GC consumer and from an HBM producer to a GC consumer. Check source/consumer energy values, not only cable shapes.
- [x] Test a CE machine face that rejects power connections. Neither cable attaches or transfers through that face; an allowed face still works.
- [x] Launch from each CE pad without GC pads in the rocket slot. Confirm the rocket survives liftoff, the HBM pad remains, side exhaust and liftoff plume appear, and the lander gains no free GC pads.
- [x] Launch from a CE pad with 9 GC landing pads in the rocket slot. Confirm the lander receives exactly 9 once and returning does not duplicate them.
- [x] Confirm a normal GC pad still launches, disappears, and returns its landing pads by stock GC behavior.

## Release Decision

- [ ] All EE and CE checks pass on the same jar, or failures are fixed and retested on both forks. Do not ship on a known duplication, crash, or fuel/power regression.
- [ ] Final jar, version, and release notes match the tested artifact.

## Failure Notes

| Step | Fork / world | Expected | Actual | Log or screenshot |
| --- | --- | --- | --- | --- |
| | | | | |
