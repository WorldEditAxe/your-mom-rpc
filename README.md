# Your Mom Discord RPC
A Discord RPC client for your mom, written in Java for fun.  
This application was only proven to work on Java 1.8 running on Windows x64. **I cannot guarantee 100% functionality on any other platform!**  

## Feature(s)
* Automatic area changing
## Installaton
### Install Java (if you haven't)
This is a required prequisite. Without Java, you will not be able to run this program.  
1. Go to Java's download page at https://www.java.com/download/ie_manual.jsp.
2. Download and open the executable. Follow the instructions from Java's installation wizard.
### Setup 
1. Download the binary of the latest release (generally the .jar file you see).
2. Double-click the file, and it will run. A small window will open with something along the lines of `Done! Close the window to exit.`
# Customization
## Adding custom areas
This section is written under the assumption that you have basic Java knowledge and some basic Discord developer knowledge.
In the future, custom area addition will not require modification of source code.  
As of right now, you cannot add your own Client IDs. However, this will be allowed in a later release.  
1. Install IntelliJ and import this repository into the IDE.
2. Replace the client ID on line `90` to your own application's client ID (don't take away the L!). Go look up how to create a Discord application and how to get it's client ID if you don't know how.
3. In the `resources` folder, there is a `custom_rpcs.txt` file. Replace it with your own areas (remember the syntax!).
   - The asset ID is a pointer towards an rich presence asset (Rich Presence > Art Assets).
4. Start the application with the "Run" configuration to check if it is working. If it is, go ahead and rebuild the artifact. (Build > Build Artifacts... > yourMomRPC.jar > Rebuild)
5. There will be a folder named `out`. Go to `<project root>/out/artifacts/yourMomRPC_jar/yourMomRPC.jar` and drag it to any folder on your PC.
6. Double-click the .jar file to start it!
# FAQs
1. help how do i run this  
\- Refer to the section "Installation".
2. stop trying to hack me u hacker  
\- It is up to you on whether to download it or not. Feel free to view the source code and build it on your own.
3. license?????  
\- This project is licensed under the MIT license. In layman's terms, you can do anything with it apart from holding me responsible for any defects of the program.
4. i want pull request  
\- Feel free to open a pull request!
