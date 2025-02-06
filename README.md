# ![img.png](/assets/img.png)

[![Discord](https://img.shields.io/discord/1011940744774303795.svg?color=7289da&logo=discord&label=Omaloon-General&style=for-the-badge)](https://discord.gg/bNMT82Hswb)
[![YouTube](https://img.shields.io/youtube/channel/subscribers/UCKYkjTAwp-ZpKBVDdknSIHw?color=ff5959&label=YouTube&logo=youtube&style=for-the-badge)](https://www.youtube.com/@omaloon)

[![Stars](https://img.shields.io/github/stars/xstabux/Omaloon?color=7289da&label=%20Please%20Star%20Omaloon%21&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public)
[![Download](https://img.shields.io/github/v/release/xStaBUx/Omaloon-mod-public?color=6aa84f&include_prereleases&label=Latest%20version&logo=github&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)
[![Total Downloads](https://img.shields.io/github/downloads/xStaBUx/Omaloon-mod-public/total?color=7289da&label&logo=docusign&logoColor=white&style=for-the-badge)](https://github.com/xStaBUx/Omaloon-mod-public/releases)

> An ambitious [Mindustry](https://github.com/Anuken/Mindustry) mod
> developed by [stabu](https://github.com/stabu-dev) and [uujuju](https://github.com/uujuju1).
> Mod aimed at expanding a campaign by adding a new star system.

## Building from Source

### Prerequisites

- JDK 17 or higher
- Android SDK (for Android builds)
- Git

### Setup

1. Clone the repository:

```bash
git clone -b master --single-branch https://github.com/xstabux/Omaloon
```

2. Set up environment variables:

- Set `ANDROID_SDK_ROOT` or `ANDROID_HOME` to your Android SDK installation path (required for Android builds)

### Building

For Desktop:

```bash
gradlew jar
```

For Android:

```bash
gradlew dex
```

The built mod will be in the `build/libs` directory.

### Running

To test the mod directly:

```bash
gradlew runClient
```

This Mindustry client uses the `run` directory to store its data.

## Contributing

### Bug Reports

- Submit bug reports in the [Issues](https://github.com/xStaBUx/Omaloon-mod-public/issues) section
- Include detailed steps to reproduce the issue
- Mention your game version and mod version

### Feature Suggestions

- Join our [Discord server](https://discord.gg/bNMT82Hswb) to suggest new content
- Provide examples and specific arguments why your suggestion should be added to Omaloon
- The Issues section is reserved for bug reports only

### Game-play Feedback

- Share your thoughts on game-play and balancing in our Discord
- Provide specific examples and reasoning for balance changes

### Pull Requests

- Improve Omaloon's code/sprites/localization and propose these changes to us by creating a [Pull Request](https://github.com/stabu-dev/Omaloon/pulls).
- Make sure your code works properly and doesn't cause any issues.
- Provide specific reasoning why your Pull Request should be accepted.

## Credits

### Developers

- stabu - Lead Dev
- uujuju - Dev

### Contributors

- randomguy - Weathers code
- zelaux - Arc library, some code
- saigononozomi - Music
- aureusstratus - A good portion of sprites
