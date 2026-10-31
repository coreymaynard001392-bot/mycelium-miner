# Mycelium Miner (prototype)

A client-side Fabric mod project targeting **Minecraft Java 1.21.11**.

## Current prototype features
- In-game menu (Right Shift by default).
- Manual X/Y/Z entry for route points, jump points, and mining targets.
- Route recording while you walk (F7 toggles recording).
- Route playback toggle (F6).
- Basic waypoint steering, jump-radius triggering, and mining-target aiming.

## Important limitations
This is an **early prototype source project**, not a tested release JAR. I cannot compile or launch Minecraft in this environment, so the first build may need small API adjustments.
- It is intended for single-player testing only.
- Route and coordinate lists are currently session-only; they are not saved after closing the game.
- The movement controller is deliberately basic. It follows horizontal waypoints and does not solve obstacles, cliffs, water, or complex terrain.
- Mining targets must be reachable and within normal interaction range. The game still applies normal tool, block, and line-of-sight rules.
- Test in a copy of your world first. Stop with F6 or close the menu and disable the mod if movement behaves unexpectedly.

## Build
Requirements:
- JDK 21
- Gradle 9.x installed

From this folder, run:

```bat
gradle build
```

The built mod JAR will be in `build/libs/`. Put that JAR in your Minecraft `mods` folder alongside Fabric API for Minecraft 1.21.11.

## Default controls
- Right Shift: open menu
- F6: start/pause route playback
- F7: toggle route recording

You can change these in Options → Controls → Key Binds → Mycelium Miner.

## Build without installing Java/Gradle on your own PC (GitHub Actions)

This project includes `.github/workflows/build.yml`. If you upload the extracted project to a GitHub repository, GitHub's hosted runner installs Java 21 and Gradle and attempts the build for you. You do not need Java or Gradle installed locally.

1. Create a new repository on GitHub.
2. Upload the **contents** of this project folder (including the hidden `.github` folder).
3. Open the repository's **Actions** tab and select **Build Mycelium Miner**.
4. Choose **Run workflow** if it has not started automatically.
5. When it finishes successfully, open that workflow run and download the `mycelium-miner-1.21.11` artifact.
6. Extract the downloaded artifact ZIP. Put the `.jar` inside your Dawn profile's `mods` folder, alongside Fabric API.

If the workflow fails, the source needs fixing before there can be a usable mod JAR. The build log will show the compiler error; this project has not yet been successfully compiled or tested in-game.
