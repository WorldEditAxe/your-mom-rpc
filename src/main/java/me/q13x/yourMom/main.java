package me.q13x.yourMom;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class main {
    static Core rpcCore;
    static CustomRPC[] customRPCs;
    static Activity currentActivity;

    static boolean showInfo = true;

    static int delayMinMs = 300000;
    static int delayMaxMs = 600000;

    public static void main(String[] args) throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.out.println("This program is only compatible with Windows!");
            System.exit(1);
        }

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--no-info")) {
                showInfo = false;
            }
        }

        new main().start(args);
    }

    public void start(String[] args) throws Exception {
        new Thread(() -> {
            new CLIWindow();
        }).start();

        Properties properties = new Properties();
        InputStream is = main.class.getClassLoader().getResourceAsStream("programMetadata.properties");

        if (is != null) {
            properties.load(is);
            System.out.printf("Loading RPC program (version: %s)...\n", properties.getProperty("version"));
        } else {
            System.out.println("Loading RPC program...");
        }

        System.out.println("Starting your mom...");

        System.out.println("Loading custom RPCs...");

        File customRPCsFile = new File("./custom_rpcs.txt");
        if (customRPCsFile.exists()) {
            System.out.println("Found custom RPCs file at " + customRPCsFile.getAbsolutePath() + ".");
            initResourceRPCs(customRPCsFile.getAbsolutePath());
        } else {
            initResourceRPCs(null);
        }

        try {
            // Core.init(new File(ClassLoader.getSystemResource("discord_game_sdk.dll").toExternalForm()));
            final File discordDll = downloadDiscordLibrary();
            Core.init(discordDll);
            discordDll.getParentFile().deleteOnExit();
        } catch (Exception err) {
            StringBuilder sb = new StringBuilder();

            sb.append(err + "");

            for (StackTraceElement ste : err.getStackTrace()) {
                sb.append("\n   " + ste.toString());
            }

            System.out.println("Error starting native library: " + sb);
            System.exit(1);
        }

        try (CreateParams params = new CreateParams()) {
            // init
            params.setClientID(941530515503214594L);
            params.setFlags(CreateParams.getDefaultFlags());

            try (Core core = new Core(params)) {
                rpcCore = core;
                startChooserThread();

                while (currentActivity == null) {
                    Thread.sleep(10);
                }
                
                System.out.println("Done! Close the window to exit.");

                while (true) {
                    Thread.sleep(10);
                    core.runCallbacks();
                }
            }
        }
    }

    public static Activity getDefaultPresence(String areaName, String thumbnail, String thumbnailText) {
        final Activity a = new Activity();

        a.setDetails("Playing with your mom");
        a.setState("In " + (areaName != null ? areaName : "unknown area"));

        a.timestamps().setStart(Instant.now());

        a.assets().setLargeImage(thumbnail != null ? thumbnail : "unknown_area_image");
        a.assets().setLargeText(thumbnailText != null ? thumbnailText : "Um... hi! There's no thumbnail text for this area.");

        return a;
    }

    public void initResourceRPCs(String filePath) throws Exception {
        final InputStream is = filePath != null ? new FileInputStream(filePath) : getClass().getResourceAsStream("/custom_rpcs.txt");
        List<String> lines = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.toList());
        lines = lines.stream().filter(line -> !(line.startsWith("#") || line.isEmpty())).collect(Collectors.toList());

        if (lines.size() == 0) {
            System.out.println("No custom RPCs found in custom_rpcs.txt. Please add some.");
            System.out.println("RPC line syntax: <area name>|<thumbnail image asset id>|<thumbnail image highlight text>");
            System.out.println("Example: \"My Area|my_area_image|My Area\"");
            System.exit(1);
        }

        customRPCs = new CustomRPC[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            try {
                customRPCs[i] = new CustomRPC(lines.get(i));
            } catch (Exception ignored) {
                i--;
                System.out.println("Something went wrong while reading a custom RPC! [line: " + (i + 1) + "]");
            }
        }
    }

    public static Activity getRandomCustomRPC() {
        double rand = Math.random() * customRPCs.length;
        final CustomRPC randomCustomRPC = customRPCs[new Random().nextInt(customRPCs.length)];
        final Activity builtActivity = getDefaultPresence(randomCustomRPC.areaName, randomCustomRPC.thumbnailImage, randomCustomRPC.thumbnailText);
        return builtActivity;
    }

    public static void startChooserThread() {
        new Thread(() -> {
            try {
                while (true) {
                    // random int between delayMinMs and delayMaxMs
                    final int delay = (int) (Math.random() * (delayMaxMs - delayMinMs)) + delayMinMs;

                    currentActivity = getRandomCustomRPC();
                    rpcCore.activityManager().updateActivity(currentActivity);

                    Thread.sleep(delay);
                }
            } catch (Exception err) {

                StringWriter sw = new StringWriter();
                err.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();

                System.out.println("Error in chooser thread: " + stackTrace);
            }
        }).start();
    }

    public static File downloadDiscordLibrary() throws IOException
    {
        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        String name = "discord_game_sdk";
        String suffix;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if(osName.contains("windows"))
        {
            suffix = ".dll";
        }
        else if(osName.contains("linux"))
        {
            suffix = ".so";
        }
        else if(osName.contains("mac os"))
        {
            suffix = ".dylib";
        }
        else
        {
            throw new RuntimeException("cannot determine OS type: "+osName);
        }

		/*
		Some systems report "amd64" (e.g. Windows and Linux), some "x86_64" (e.g. Mac OS).
		At this point we need the "x86_64" version, as this one is used in the ZIP.
		 */
        if(arch.equals("amd64"))
            arch = "x86_64";

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/"+arch+"/"+name+suffix;

        // Open the URL as a ZipInputStrea
        URLConnection con = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip").openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        con.connect();

        ZipInputStream zin = new ZipInputStream(con.getInputStream());

        // Search for the right file inside the ZIP
        ZipEntry entry;
        while((entry = zin.getNextEntry())!=null)
        {
            if(entry.getName().equals(zipPath))
            {
                // Create a new temporary directory
                // We need to do this, because we may not change the filename on Windows
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-"+name+System.nanoTime());
                if(!tempDir.mkdir())
                    throw new IOException("Cannot create temporary directory");
                tempDir.deleteOnExit();

                // Create a temporary file inside our directory (with a "normal" name)
                File temp = new File(tempDir, name+suffix);
                temp.deleteOnExit();

                // Copy the file in the ZIP to our temporary file
                Files.copy(zin, temp.toPath());

                // We are done, so close the input stream
                zin.close();

                // Return our temporary file
                return temp;
            }
            // next entry
            zin.closeEntry();
        }
        zin.close();
        // We couldn't find the library inside the ZIP
        return null;
    }
}
