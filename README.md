# ConnectedTexturesMod for Fabric

ConnectedTexturesMod for Fabric tries to maintain feature parity with the original [ConnectedTexturesMod](https://www.curseforge.com/minecraft/mc-mods/ctm) so that CTM/Chisel format resource packs can work on both platforms without any changes. CTMF also adds some new features and fixes some bugs from the original. It is a client-side only mod that requires the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and utilizes the Fabric Rendering API for efficiency and compatibility.

CTM/CTMF allows resource packs to create dynamic models by adding custom JSON to model or texture metadata files. **CTM/CTMF by itself does not add connected textures. A separate resource pack is needed.** CTM/CTMF also has a public facing API that allows other mods to add their own texture types, which can then be used through texture metadata.

All important information regarding this port can be found [here](https://github.com/PepperCode1/ConnectedTexturesMod-Fabric/wiki). For information on everything else, visit the original's wiki [here](https://github.com/Chisel-Team/ConnectedTexturesMod/wiki).

### Compiling from Source and Project Setup

CTMF's setup is quite basic and therefore does not need any special procedure to compile the code. Just run `./gradlew build` (Linux) or `gradlew build` (Windows) in the project's directory. If Gradle is installed globally, use `gradle` instead of `gradlew`. If the project is imported into an IDE, running the `build` Gradle task from there works too.

Loom adds the `runClient` Gradle task, which runs the mod on the client side, but since CTMF itself doesn't add any resources, this task is not very useful. A test mod has been added to this project to test some of the mod's functionality. It can be run using the `runTestmodClient` Gradle task. Alternatively, debugging using Loom in an IDE allows the testmod to launch as well.

A checkstyle has been added to make sure the code style is consistent. It is based on the checkstyle from [Fabric API](https://github.com/FabricMC/fabric). To validate the code's style, run the `check` Gradle task.

#### Links

[CTMF Wiki](https://github.com/PepperCode1/ConnectedTexturesMod-Fabric/wiki)

[CTMF CurseForge](https://www.curseforge.com/minecraft/mc-mods/ctm-fabric)

[CTM Wiki](https://github.com/Chisel-Team/ConnectedTexturesMod/wiki)

[CTM Github](https://github.com/Chisel-Team/ConnectedTexturesMod)

[CTM CurseForge](https://www.curseforge.com/minecraft/mc-mods/ctm)

[Fabric API Github](https://github.com/FabricMC/fabric)

[Fabric API CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
