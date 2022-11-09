# PLIBInspector [![Resource](https://img.shields.io/github/v/release/ZorTik/PLIBInspector)](https://github.com/ZorTik/PLIBInspector/releases)
Inspect ProtocolLib packets on current version via Minecraft commands.<br>
This plugin's main idea is to show required types in a packet container for helping with reverse engineering.

This is Minecraft plugin, not a library!

## Installation
First, you need to install this plugin on your server where you wanna inspect packets:
1. Clone this repository to your IDE
2. Build it using ```shadowJar```
3. Upload target jar to Minecraft plugins directory

## Usage
This plugin supports two types of commands:<br>
<table>
  <thead>
    <th>Command</th>
    <th>Information</th>
  </thead>
  <tbody>
    <tr>
      <td>/plibinspect inspect <PACKET NAME></td>
      <td>Inspect provided packet type</td>
    </tr>
    <tr>
      <td>/plibinspect list</td>
      <td>List all packet types</td>
    </tr>
  </tbody>
</table>

Result of a PLAYER_INFO inspection on a 1.19.2 server:
<pre>
[18:26:14 INFO]: Report for PLAYER_INFO (Server):
[18:26:14 INFO]: --  Global (7) --
[18:26:14 INFO]:   getStructures: 2
[18:26:14 INFO]:   getModifier: 2
[18:26:14 INFO]:   getItemListModifier: 1
[18:26:14 INFO]:   getListNbtModifier: 1
[18:26:14 INFO]:   getAttributeCollectionModifier: 1
[18:26:14 INFO]:   getBlockPositionCollectionModifier: 1
[18:26:14 INFO]:   getWatchableCollectionModifier: 1
[18:26:14 INFO]: --  Specific (4) --
[18:26:14 INFO]:   getPlayerInfoDataLists: 1
[18:26:14 INFO]:   getPlayerInfoAction: 1
[18:26:14 INFO]:   getSlotStackPairLists: 1
[18:26:14 INFO]:   getIntLists: 1
</pre>

You can now see that there are 4 specific required type areas. On the left side is method name and on the right **amount of required entries**. Number on the right side is amount of fields in the current type modifier that need to be filled.<br>

You can use this information to **build a packet using** <a href="https://github.com/dmulloy2/ProtocolLib/wiki/PacketContainer">PacketContainer</a>
