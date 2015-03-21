# Xfire screenshot and videos backup tool

This tool allows you to backup the screenshots and videos you saved on xfire.com in case you want a local backup of your data.
Launch the tool by starting a command prompt in the directory where the jar file is located (W7 and later OS, right click on the folder->Open Command Prompt Here) and typing "java -jar xfssdl.jar -user username -path downloadpath".

To make sure the software detected the correct amount of games (which may fail if the connection is broken), use your browser's JS console to count the amount of elements of class "media-item-more". In Chrome, you can go to your screenshot page, press F12, switch to the console tab, then type "document.getElementsByCount("media-item-more").length".

Ideas for improvement:
- Multithreading: thought about it, but it might put too much load on the Xfire server and take everything down.
- Saving screenshots with actual names
- Generating small HTML pages with description and comments: might increase load on the Xfire server and exponentially increase the backup time
