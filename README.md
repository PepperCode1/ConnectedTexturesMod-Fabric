# ConnectedTexturesMod for Fabric

CTMF aims to be a direct port of the original ConnectedTexturesMod, which ran on Forge. CTMF allows the same resource pack to work on both Forge and Fabric with almost the same end result. CTMF also tries to address some of CTM's bugs and shortcomings.

In short, CTM(F) allows resource packs to create dynamic models by adding custom JSON to model or texture metadata files. CTM(F) also has an internal API that allows other mods to add their own texture types, which can then be used through texture metadata. CTM(F) is a client-side only mod.

All important information regarding this port can be found [here](https://github.com/PepperCode1/ConnectedTexturesMod-Fabric/wiki). For information on everything else, visit the original's wiki [here](https://github.com/Chisel-Team/ConnectedTexturesMod/wiki).

### Compiling from Source and Project Setup

CTMF's setup is quite basic and therefore does not need any special procedure to compile the code. Just run `./gradlew build` (Linux) or `gradlew.bat build` (Windows). If the project is imported into an IDE, running the Gradle `build` task from there works too.

Loom adds the `runClient` task, which runs the mod on the client side, but since CTMF itself doesn't add any resources, this task is not very useful. A test mod has been added to this project to test some of the mod's functionality. It can be run using the `runTestmodClient` task.

#### Links

[CTM Github](https://github.com/Chisel-Team/ConnectedTexturesMod)

[CTM CurseForge](https://www.curseforge.com/minecraft/mc-mods/ctm)
