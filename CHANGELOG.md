# Changelog

## [0.8.5] - 2026-11-07

### Added

- One release jar now detects HBM Community Edition or Extended Edition and enables the matching integration. Install either fork, not both.
- Galacticraft Tier 1 rockets can use the 3x3 Extended Edition `hbm:launch_pad`, including its rocket inventory slot and a powered Galacticraft Fuel Loader.
- Rockets launched from supported HBM pads can carry up to nine Galacticraft landing pads in a dedicated inventory slot and return them through the normal lander flow. An empty slot does not create free pads.

### Improved

- HBM copper cables and Galacticraft aluminum wires can connect to and power machines from either mod. The two cable types remain separate networks.
- Rocket placement, removal, pad destruction, and launch handling now preserve rocket items and avoid duplicate or stranded rocket entities.
- HBM-pad launch exhaust vents to the sides during countdown, then adds the normal plume at liftoff.
- Galacticraft oil generation is disabled with either HBM fork in newly generated chunks; existing oil wells are unaffected.
- Exiting Galacticraft vehicles restores first-person view with either HBM fork.

### Fixed

- The Galacticraft Refinery JEI display shows a single kerosene-to-fuel recipe with the HBM fluid icon and fluid bars.
- Cable connection refresh and energy transfer issues in CE and EE.

### Scope

- Requires Minecraft 1.12.2, Galacticraft Legacy, MixinBooter 10.7 or newer, and either HBM CE or HBM EE.
- Rocket support is limited to Tier 1 and the supported 3x3 HBM pads. Tier 2, Tier 3, cargo rockets, and larger pads are outside this release.
