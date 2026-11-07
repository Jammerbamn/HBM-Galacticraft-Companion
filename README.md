# HBM-Galacticraft-Companion
This mod makes Galacticraft Legacy rely more heavily on HBM Nuclear Tech fuel processing and infrastructure.

## Version 0.8.5

- Disables Galacticraft oil generation in newly generated chunks and requires HBM kerosene in the Galacticraft Refinery.
- Supports Galacticraft Tier 1 rockets on the regular and rusted 3x3 launch pads in HBM Community Edition, and on the 3x3 `hbm:launch_pad` in Extended Edition.
- Fuels rockets on the rusted CE and EE pads through a Galacticraft Fuel Loader. The regular CE pad accepts Galacticraft fuel through its tanks and fuels a docked rocket when powered.
- Lets HBM copper cable and Galacticraft aluminum wire supply machines from either mod. The two cable types do not connect directly to each other.
- Adds a landing-pad inventory slot to rockets on HBM pads. Only Galacticraft landing pads go in this slot; they travel with the rocket instead of being created for free at the destination.
- Restores first-person view after exiting Galacticraft vehicles.

One jar supports either HBM Community Edition (CE) or Extended Edition (EE) and selects the installed fork at runtime. Install one HBM fork, not both. Tier 2, Tier 3, cargo rockets, and larger HBM pads are not supported by this release.

## Requirements

- Minecraft 1.12.2 with Forge
- [Galacticraft Legacy](https://github.com/TeamGalacticraft/Galacticraft-Legacy)
- [MixinBooter](https://github.com/CleanroomMC/MixinBooter) 10.7 or newer
- Either [HBM Nuclear Tech: Community Edition](https://github.com/Warfactory-Official/Hbm-s-Nuclear-Tech-CE) or [HBM Nuclear Tech: Extended Edition](https://github.com/Alcatergit/Hbm-s-Nuclear-Tech-GIT)

JEI is optional, but useful for viewing the kerosene refinery recipe.

## Planned Changes

- Change certain crafting recipes to run through the HBM Assembler.

## Disclaimers

Existing Galacticraft oil wells remain in previously generated chunks. HBM can convert their oil to HBM oil in the fluid boiler.

For local development, `hbm_dev_target` in `gradle.properties` chooses the HBM fork for Gradle game runs (`ce` by default, or `ee`). It does not change which integrations are packaged in the release jar.

