# CaveAbyss negative-height support (6.3.1)

ForgeWorld exposes the optional CaveAbyss dimension minimum (-64 in the extended
Overworld, 0 elsewhere). Core selection clamps, data validation, fill/liquid
masks, cylinders, navigation/targeting and full-height selection use this minimum.
Regeneration history now covers the complete supported height. Detached clipboard
regions preserve negative coordinates instead of clamping them to zero.

Validation: 38 JUnit tests pass, including new tests for negative cuboid/polygon
selection, set/replace, undo/redo, clipboard copy/paste, cylinder and fill. Boundary
checks reject Y=-65 and Y=256; vanilla worlds retain Y=0. Tests use an in-memory
WorldEdit world; an actual Minecraft command session still needs runtime checking.
Snapshot restore from external Anvil backups is not covered by this change.

Build in this environment used installed Gradle 8.9 / Java 21, the existing Oracle
Java 8 compilation toolchain and build/local-build.init.gradle to select cached
Kotlin build-tool dependencies. Existing compiled Minecraft classes were reused
because regeneration of their extracted resource directory was blocked by locked
files. WorldEdit source compilation, reobfuscation and all tests completed.
