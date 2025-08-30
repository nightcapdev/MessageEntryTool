MessageEntryTool Ver 1.2
A Swing-based GUI application for composing and saving RPG-style dialogue entries in YAML format, including character portraits and text macros.

Features:
-Compose dialogue with embedded macro support.

-Save messages in structured .yaml files.

-Automatically scans YAML files to find the latest message_x entry and enumerates from there.

-Load and select portraits from a grid-based faceset PNG.

-Insert text macros: color, wait times, font size changes, currency indicators, and more.

-Live preview of YAML output.

-Remembers last-used settings between runs via a .properties config file.

-JRE is bundled into the .exe


Notes: 
PNG images should be placed inside the faceset/ directory.

Faceset should follow a grid format of 106×106 pixel cells.

The tool auto-generates clickable grids based on the image size.

YAML file should be place inside the save/ directory.


Example workflow:
1. Place faceset PNGs into the faceset/ folder and dialogue YAMLs into the save/ folder.

2. Run the app and load your PNG and YAML. (Leave these fields blank or make them blank to remove faceset/facindex from appended text)

3. Select a portrait cell if applicable.

4. Type your message and insert macros. (Insert Name Buttons must be clicked as appropriate)
Note: Insert Macro Name refers to the "\aub" (Aubrey) macros.
Note: Insert Non-Macro Name refers to the "\n<"Aubrey">" macro.

5. Press Add Message to append to the YAML file.

6. See results in the live preview panel.
Note: Does not support editing in ver 1.2.