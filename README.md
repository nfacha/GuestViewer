# GuestViewer

A Minecraft plugin that restricts non-whitelisted players to spectator mode with movement limitations.

![Build Status](https://github.com/nfacha/GuestViewer/actions/workflows/build.yml/badge.svg)

## Features

- Permission-based whitelist system - players without the `guestviewer.bypass` permission are automatically set to spectator mode
- Players with the `guestviewer.bypass` permission are automatically set to survival mode
- Spectators can only move within 100 blocks of the player they're spectating
- Permission-based free roaming near world spawn
- Configurable distance limitations and messages
- Simple and lightweight

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` directory
3. Restart your server or run `/reload`
4. Configure the plugin as needed (see Configuration section)

## Usage

### Permissions

- `guestviewer.bypass` - Players with this permission play normally (default: op)
- `guestviewer.admin` - Required to use admin commands like reload (default: op)
- `guestviewer.freeroam` - Allows spectators to roam freely within the configured distance of world spawn (default: false)

### Commands

- `/guestviewer` or `/gview` - Displays plugin info
- `/guestviewer reload` or `/gview reload` - Reloads the plugin configuration

## Configuration

The plugin's configuration is stored in `plugins/GuestViewer/config.yml`:

```yaml
# Maximum distance (in blocks) a spectator can move from their target
max-distance: 100

# Message shown to players when they join and are set to spectator mode
join-message: "&cYou are not whitelisted! You have been set to spectator mode."

# Message shown when a spectator tries to move beyond the max distance
distance-warning: "&cYou cannot move more than 100 blocks from the player you are spectating!"

# Allow spectators to freely roam within 100 blocks of world spawn if not spectating a player
# This setting must be true AND the player needs the guestviewer.freeroam permission
allow-free-roam: true
```

## How it Works

1. When a player joins, the plugin checks if they have the `guestviewer.bypass` permission
2. If they have the permission, they're set to survival mode
3. If they don't have the permission, they're set to spectator mode
4. Spectators are restricted to a 100-block radius (configurable) around the player they're spectating
5. Spectators with the `guestviewer.freeroam` permission can move within the configured radius of world spawn
6. If they try to move beyond allowed boundaries, they're teleported back

## Support

If you encounter any issues or have suggestions, please open an issue on the GitHub repository.

## License

This plugin is released under the MIT License.

## Development

### Building from Source

The project uses Maven for build automation. To build from source:

```bash
mvn clean package
```

The built jar file will be in the `target` directory.

### Continuous Integration

This project uses GitHub Actions to automatically build the plugin. The workflow:
- Builds the plugin on every push and pull request to main/master branches
- Uses JDK 21 for compilation
- Creates artifacts that can be downloaded from the Actions tab 