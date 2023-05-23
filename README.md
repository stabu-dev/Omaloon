[![Discord](https://img.shields.io/discord/1011940744774303795.svg?color=7289da&logo=discord&label=Omaloon-Genral&style=for-the-badge)](https://discord.gg/bNMT82Hswb)
[![YouTube](https://img.shields.io/youtube/channel/subscribers/UCKYkjTAwp-ZpKBVDdknSIHw?color=ff5959&label=YouTube&logo=youtube&style=for-the-badge)](https://www.youtube.com/@omaloon)
[![Stars](https://img.shields.io/github/stars/xStaBUx/Omaloon-mod-public?color=7289da&label=⭐️%20Please%20Star%20Omaloon%21&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public)
[![Download](https://img.shields.io/github/v/release/xStaBUx/Omaloon-mod-public?color=6aa84f&include_prereleases&label=Latest%20version&logo=github&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)[![Total Downloads](https://img.shields.io/github/downloads/xStaBUx/Omaloon-mod-public/total?color=7289da&label&logo=docusign&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)
[![](https://img.shields.io/badge/trello-7B68EE?style=for-the-badge&logo=trello&logoColor=white)](https://trello.com/b/KhLg7TaE/omaloon)

> The [Mindustry](https://github.com/Anuken/Mindustry) mod aims to expand the campaign by adding a new star system.
> In active development. For now, not suitable for games outside the sandbox.

## Contributing

You can contribute on development by:

* Submitting bug reports in [Issues](https://github.com/xStaBUx/Omaloon-mod-public/issues) category.
* Suggesting new content (The Issues category is **not** for suggestions, better visit the [discord server](https://discord.gg/bNMT82Hswb))
* Providing input regarding content balancing

## Compiling

1. Clone project 
```
https://github.com/xStaBUx/Omaloon.git
```

2. Pack sprites. (Only necessary if new sprites are added)
```
gradlew tools:pack
```

3. Build project
```
gradlew build
```
`Resulting .jar file should be in build/libs/`

4. Build project with automatic placement of mod into "mods" folder
```
gradlew mjar
```
5. Update the mod in the "mods" folder and run mindustry
```
gradlew mrun
```

**YOU MUST USE JAVA 16 OR ABOVE TO COMPILE OMALOON**
