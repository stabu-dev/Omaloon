# ![img.png](main/assets/img.png)

> An ambitious [Mindustry](https://github.com/Anuken/Mindustry) mod
> developed by [stabu](https://github.com/stabu-dev). Aimed at expanding Mindustry's campaign by adding a new star system.

[![Discord](https://img.shields.io/discord/1011940744774303795.svg?color=7289da&logo=discord&label=Discord&style=for-the-badge)](https://discord.gg/bNMT82Hswb)
[![YouTube](https://img.shields.io/youtube/channel/subscribers/UCKYkjTAwp-ZpKBVDdknSIHw?color=ff5959&label=YouTube&logo=youtube&style=for-the-badge)](https://www.youtube.com/@omaloon)

[![Stars](https://img.shields.io/github/stars/stabu-dev/Omaloon?color=7289da&label=%20Star%20Omaloon%20&style=for-the-badge)](https://github.com/stabu-dev/Omaloon)
[![Download](https://img.shields.io/github/v/release/stabu-dev/Omaloon?color=6aa84f&include_prereleases&label=Latest%20version&logo=github&logoColor=white&style=for-the-badge)](https://github.com/stabu-dev/Omaloon/releases)
[![Total Downloads](https://img.shields.io/github/downloads/stabu-dev/Omaloon/total?color=7289da&label&logo=docusign&logoColor=white&style=for-the-badge)](https://github.com/stabu-dev/Omaloon/releases)

## Using this Mod

> [!IMPORTANT]
> **You cannot just download the `.zip` and add it to your mods folder**, as this is a Java mod.
>
> If you open an issue report revolving around this, it will be ignored, and you will be referred to this file.

Instead, you have two options:

### Releases

Head over to the [releases](https://github.com/stabu-dev/Omaloon/releases/latest) page. Download the `Omaloon.jar` from **latest release** (marked by green badge) Assets and put it in the Mindustry mods folder:

- On Windows, it should be `%APPDATA%\Mindustry\mods\`.
- On Linux, it should be `$HOME/.local/share/Mindustry/mods/`.
- On Mac, it should be `$HOME/Library/Application Support/Mindustry/mods/`.
- On Android, see the game's built-in mod import functionality below.
- On iOS you can't get the mod because mods with code are prohibited by Apple (see https://developer.apple.com/app-store/review/guidelines/#software-requirements).

You can also use the Mindustry's built-in «Import Mod» button in the Mods menu dialog by providing `Omaloon.jar` or `stabu-dev/Omaloon`, or simply download the mod from the mod browser. Then restart the game and play.

### Indev Releases

Head over to the [releases](https://github.com/stabu-dev/Omaloon/releases/latest) page. Download the `Omaloon.jar` from latest **pre-release** (marked by yellow-ish badge) Assets and put it in the Mindustry mods as described above.

Alternatively you can also use mod browser by choosing the Omaloon release containing `Indev` in its name.
(install will not work, you have to choose specific release)

### Bleeding-Edge Builds

> [!IMPORTANT]
> **Make sure you have a GitHub account**, as it requires you to have an account to download artifacts.
>
> If you open an issue report revolving around your inability to download BE, it will be ignored, and you will be referred to this file.

Head over to the [actions](https://github.com/stabu-dev/Omaloon/actions) page, click the most recent successful workflow run (marked by green checkmark), scroll down to "Artifacts" section, and download the one titled `Omaloon (in a box)`.
As the name suggests, **you must unpack (unzip) it first to extract the actual `.jar`**, then you can import it.

Be aware that the indev and bleeding-edge **are early access** builds that **usually highly unstable, unfinished or / and straight up unplayable,** plus **might require a certain Mindustry version constraint**. Use them this at your own risk.
## Contributing

> [!IMPORTANT]  
> **russian localization will never be added to Omaloon.**
>
> The russian language is and has historically been a tool of [imperialism](https://www.britannica.com/topic/Russification) and [cultural erasure](https://ich.unesco.org/en/convention). This policy of linguistic persecution is a key part of the ongoing [crimes against humanity](https://www.ohchr.org/en/instruments-mechanisms/instruments/rome-statute-international-criminal-court), which we condemn. We will not host a language that is mainly used as a tool for this system of oppression.
>
> If you open an issue report or pull request revolving around this, it will be ignored, and you will be referred to this file.

You can contribute to mod in several ways, including:

### Feature Suggestions

- Join Omaloon's [Discord server](https://discord.gg/bNMT82Hswb) to suggest and discuss new content.
- Provide examples and specific arguments why your suggestion should be added to Omaloon

### Game-play Feedback

- Share your thoughts on game-play and balancing in our Discord.
- Provide specific examples and reasoning for balance changes.

### Pull Requests

Improve Omaloon's code/sprites/localization and propose these changes to us by creating a [Pull Request](https://github.com/stabu-dev/Omaloon/pulls).<br>
Feel free to contribute, but please take these into account:
- Follow the [Mindustry contributing guidelines](https://github.com/Anuken/Mindustry/blob/master/CONTRIBUTING.md). This includes code formatting.
- Provide specific reasoning why your Pull Request should be accepted.
- Make sure your proposed requests work both on Desktop and Android and don't cause any issues.

### Issue Reports

- Head over to the [issues](https://github.com/stabu-dev/Omaloon/issues/new) page and fill up the form.

## Building from Source

Before diving into Omaloon's source code, a good understanding of Java and Git is **highly recommended**. While not impossible to work without, you'll likely encounter fewer hurdles with prior experience.

1. **Install Prerequisites:**
    * **JDK 17 or higher:** This is essential for compiling the mod.
    * **IDE (Recommended):** [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community Edition is free) is strongly suggested over basic text editors.
2. **Clone Repository:**
    * Clone this repository (or your fork) to your local machine.

> [!IMPORTANT]
> A **local copy** is *not* the ZIP archive you can download from GitHub. Use `git clone https://github.com/stabu-dev/Omaloon.git` or the cloning feature provided by your Git client (like GitHub Desktop), for version control and keeping your sanity.
>
> Downloading the ZIP bypasses Git's version control capabilities.

### Building the Mod

Builds are managed via Gradle.
Omaloon is compiled to Java 17 bytecode (the `main` module sets `sourceCompatibility`, `targetCompatibility`, and `options.release` to 17), so the built JAR requires a Java 17 runtime.

### Desktop Build (PC)

Ideal for quick testing on PC. The resulting JAR will have `Desktop` appended (`OmaloonDesktop.jar`).

1. Open your terminal in the Omaloon's root directory.
2. Ensure you have an internet connection for the first build or after a `./gradlew clean`, as Gradle might download dependencies.
3. Run:
   ```bash
   ./gradlew main:deploy
   ```
   (or `gradlew.bat main:deploy` on Windows).
   The JAR will be in `main/build/libs/`.
4. To automatically copy this JAR to your Mindustry mods folder:
   ```bash
   ./gradlew install
   ```
   You can combine these like: `./gradlew main:deploy install`.

   For a complete build, install, and launch cycle for testing (will download Mindustry client if needed):
   ```bash
   ./gradlew runClient
   ```

### Android Build (Cross-Platform)

This produces a JAR compatible with both Android and PC (`Omaloon.jar`).

* **Using GitHub Actions (Recommended):**
    * Push your changes to your GitHub repository (your fork).
    * The CI workflow (defined in `.github/workflows/ci.yml`) will automatically build both Desktop and Android JARs.
    * You can download these from the "Artifacts" section of the completed workflow run. The cross-platform JAR artifact might be named like `Omaloon (in a box).zip` (containing `Omaloon.jar`).
    * When a GitHub Release created, the cross-platform JAR (`Omaloon.jar`) is automatically uploaded.

* **Local Android Build (Optional):**
  If you need to make a build for Android locally:

    1. **Install Android SDK:**
        * Download the "**Command line tools only**" package from the [Android Studio page](https://developer.android.com/studio#command-line-tools-only) for your OS.
        * Extract the ZIP to a directory (e.g., `~/AndroidSDK` on Linux/macOS, `C:\AndroidSDK` on Windows).
        * Inside the extracted `cmdline-tools` folder, create a new folder named `latest`. Move all contents of `cmdline-tools` (like `bin`, `lib`, etc.) *into* this `latest` folder. The structure should be `AndroidSDK/cmdline-tools/latest/`.
        * Set the `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable to the full path of your `AndroidSDK` directory (e.g., `~/AndroidSDK`). Restart your terminal for changes to take effect.
        * Navigate your terminal to `AndroidSDK/cmdline-tools/latest/bin/`.
        * Run `sdkmanager --licenses` (or `sdkmanager.bat --licenses` on Windows) and accept all licenses by typing 'y' and pressing Enter for each.
        * Install the necessary SDK platforms and build tools. The versions are specified in `.github/workflows/ci.yml` (look for the `sdkmanager` command):
          ```bash
          sdkmanager "platforms;android-33" "build-tools;33.0.2"
          ```
          (or `sdkmanager.bat` on Windows).
    2. **Build the Mod:**
        * In Omaloon's root directory, run:
          ```bash
          ./gradlew main:dex
          ```
          (or `gradlew.bat main:dex` on Windows).
        * The cross-platform JAR will be located in `main/build/libs/`.

## Notable Gradle Tasks

* `main:deploy`: Builds the desktop-only JAR (`OmaloonDesktop.jar`).
* `main:dex`: Builds the Android-compatible (cross-platform) JAR (`Omaloon.jar`).
* `install`: Copies the `main:deploy` output (desktop JAR) to the local Mindustry mods folder. The target directory depends on the `mindustryPath` property in `gradle.properties`:
    * `[mindustryPath]/saves/mods/` if your `mindustryPath` points to a Steam/non-JAR installation (contains `Mindustry.exe`).
    * `[mindustryPath]/mods/` if your `mindustryPath` points to a non-Steam/other directory.
    * `Omaloon/run/mods/` if your `mindustryPath` is not set (where `Omaloon` is mod's root directory).
* `installClient`: Downloads the Mindustry client JAR (version specified by `mindustryVersion` property in `gradle.properties`) into the directory determined by `mindustryPath` (or `Omaloon/run/` if `mindustryPath` is unset).
    * This task is skipped if the target path appears to be a Steam/non-JAR Mindustry installation (contains `Mindustry.exe`).
    * Primarily used by the `runClient` task to ensure a Mindustry client is available.
* `runClient`: A comprehensive task to run local tests. It performs the following sequence:
    1. Ensures the Mindustry client is available by running `installClient` (unless it's a Steam/non-JAR setup).
    2. Builds and installs Omaloon by running `install`.
    3. Launches Mindustry with Omaloon installed:
        * If the target path (`mindustryPath`) is a Steam/non-JAR installation, it runs `Mindustry.exe`.
        * Otherwise, it runs the client JAR (e.g., `client-[mindustryVersion].jar`) using `java -jar ... -debug`.
            * **Note:** When launching the non-Steam client JAR this way, Mindustry's console output will be displayed directly in your IDE/terminal. This is not possible with the Steam version due to how Steam launches applications.

    * Sets `MINDUSTRY_DATA_DIR` to the appropriate data/saves directory and `DEVELOPMENT=true` environment variables for the game instance.
* `tools:proc`: Runs the asset processing pipeline (defined in the `tools` module), processing files from `main/assets-raw/` to `main/assets/`.
* `main:fetchComps`: Downloads and adapts Mindustry's core entity components into a temporary build directory for compilation. Fetched components are placed in the `omaloon/fetched/` package.
* `updateBundles`: Synchronizes localization (bundle) files in `main/assets/bundles/` based on `bundle.properties`. Changes are automatically committed and pushed by the CI workflow if changes are detected.
* `clean`: Deletes all `build` directories across all modules.

## Adding Dependencies

**Never** use `implementation` for Mindustry/Arc groups and their submodules. There's a reason they're `compileOnly`; they're only present in compilation and excluded from the final JARs, as on runtime they're resolved from the game instance itself. Other JAR-mod dependencies must also use
`compileOnly`. Only ever use `implementation` for external Java libraries that must be bundled with your mod.

## License

This project's source codes *(files located under `main/src/**`)* and assets *(files located under `main/assets/**` and `main/assets-raw/**`)* are licensed under [GNU GPL v3](/LICENSE), unless explicitly stated otherwise *(usually on file headers)*. The copyright notice is as follows:

> ```
> Omaloon: A Mindustry Java mod.
> Copyright (C) 2024 stabu-dev, uujuju1, zelaux, randomguy, saigononozomi
>
> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation, either version 3 of the License, or
> (at your option) any later version.
>
> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
> GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License
> along with this program.  If not, see <https://www.gnu.org/licenses/>.
> ```
