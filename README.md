# ![img.png](main/assets/img.png)
[![Discord](https://img.shields.io/discord/1011940744774303795.svg?color=7289da&logo=discord&label=Omaloon-Genral&style=for-the-badge)](https://discord.gg/bNMT82Hswb)
[![YouTube](https://img.shields.io/youtube/channel/subscribers/UCKYkjTAwp-ZpKBVDdknSIHw?color=ff5959&label=YouTube&logo=youtube&style=for-the-badge)](https://www.youtube.com/@omaloon)

[![Stars](https://img.shields.io/github/stars/xstabux/Omaloon?color=7289da&label=⭐️%20Please%20Star%20Omaloon%21&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public)
[![Download](https://img.shields.io/github/v/release/xStaBUx/Omaloon-mod-public?color=6aa84f&include_prereleases&label=Latest%20version&logo=github&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)[![Total Downloads](https://img.shields.io/github/downloads/xStaBUx/Omaloon-mod-public/total?color=7289da&label&logo=docusign&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)

>An ambitious [Mindustry](https://github.com/Anuken/Mindustry) modification developed by [xstabux](https://github.com/xstabux) and aimed at expanding the game's standard campaign by adding a new star system.

## Contributing

You can contribute on development by:

* Submitting bug reports in [Issues](https://github.com/xStaBUx/Omaloon-mod-public/issues) category.
* Suggesting new content (The Issues category is **not** for suggestions, better visit the Omaloon's [discord server](https://discord.gg/bNMT82Hswb))
* Providing input regarding content balancing

## Compiling
1. Clone repository.
```
git clone -b master --single-branch https://github.com/xstabux/Omaloon
```
2. Build.
```
gradlew :deploy
```
Resulting `.jar` file should be in `build/libs/`
3. Execute `:jar` and place the mod to `...\Mindustry\mods` (desktop only).
```
gradlew :jarMindustry
```
4. Execute `:jarMindustry` and run the game (desktop only).
```
gradlew :runMindustry
```