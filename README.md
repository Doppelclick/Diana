# Diana Solver

<p align="left">
  <a href="https://github.com/Doppelclick/Diana/releases/latest" target="_blank">
    <img alt="version" src="https://img.shields.io/github/release/Doppelclick/Diana?color=%239f00ff&style=for-the-badge" />
  </a>
  <a href="https://github.com/Doppelclick/Diana/releases/latest" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/downloads/Doppelclick/Diana/total?color=%239f00ff&style=for-the-badge" />
  </a>
  <a href="https://discord.com/channels/@me" target="_blank">
    <img alt="discord" src="https://img.shields.io/badge/Discord-Doppelclick%235993-blue?style=for-the-badge&logo=appveyor" />
  </a>
</p>

## Diana burrow waypoints:

### Burrow Waypoints:
+ Nearby burrows and a guess for the ancestral spade
+ Inquisitor waypoints shared through party/all chat - manageable ignore list - switch between receiving/sending from/to party/all chat
+ Warp to the waypoint you are looking at using a keybind (controls menu) - travel destinations you have not unlocked yet will be blocked in the future, re-enable them in the config file

### Command
+ **/diana**
  + **help** | Help message
  + **toggle** | Toggle the mod
  + **guess** | Toggle burrow guess
  + **interpolation** | Toggle interpolation for when the position of the guess burrow changes
  + **proximity** | Toggle nearby burrow detection
  + **messages** | Toggle useless messages (default off)
  + **send** | Change who to send inquis coords to
  + **receive** | Change from who to receive inquis coords
  + **beacon** [help, block, beam, text] | Change the appearance of waypoint beacons
    + **help** | Help message
    + **block** | Toggle beacon block
    + **beam** | Toggle beacon beam
    + **text** | Toggle beacon text
  + **ignore** [list, add [player], remove [player]] | View / (add / remove players from) your inquis ignore list
    + **list** | Lists all ignored players
    + **add** [player (player1 player2 ...)] | Add one or multiple players to your ignore list (support for tab completions from players you have received inquisitor coords from)
    + **remove** [player] | Works in the same way as the above
  + **clear** | Clear burrow data
  + **reload** | Reload config values from file

- Config file: .minecraft\config\Diana.cfg

### Discord:
[![Discord](https://img.shields.io/badge/Discord-Doppelclick%235993-blue?style=for-the-badge&logo=appveyor)](https://discord.com/channels/@me)

### Credits:
Took a lot of stuff from DungeonRooms (partly from NEU), the calculation from Soopy (I tested it to be the most accurate), Hub detection from Danker's Skyblock mod, the Packet Handling from Luna, proximity values from Skytils

### Download:
<p align="left">
  <a href="https://github.com/Doppelclick/Diana/releases/latest" target="_blank">
    <img alt="download.png" src="https://img.shields.io/badge/%E2%A0%80-Download-brightgreen?style=for-the-badge&logo=appveyor" />
  </a>
</p>
