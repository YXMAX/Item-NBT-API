package de.tr7zw.changeme.nbtapi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 * This class acts as the "Brain" of the NBTApi. It contains the main logger for
 * other classes,registers bStats and checks rather Maven shading was done
 * correctly.
 * 
 * @author tr7zw
 *
 */
@SuppressWarnings("javadoc")
public enum MinecraftVersion {
    UNKNOWN(Integer.MAX_VALUE), // Use the newest known mappings
    MC1_7_R4(174), MC1_8_R3(183), MC1_9_R1(191), MC1_9_R2(192), MC1_10_R1(1101), MC1_11_R1(1111), MC1_12_R1(1121),
    MC1_13_R1(1131), MC1_13_R2(1132), MC1_14_R1(1141), MC1_15_R1(1151), MC1_16_R1(1161), MC1_16_R2(1162),
    MC1_16_R3(1163), MC1_17_R1(1171), MC1_18_R1(1181, true), MC1_18_R2(1182, true), MC1_19_R1(1191, true),
    MC1_19_R2(1192, true), MC1_19_R3(1193, true), MC1_20_R1(1201, true), MC1_20_R2(1202, true), MC1_20_R3(1203, true),
    MC1_20_R4(1204, true), MC1_21_R1(1211, true), MC1_21_R2(1212, true), MC1_21_R3(1213, true), MC1_21_R4(1214, true), 
    MC1_21_R5(1215, true), MC1_21_R6(1216, true), MC1_21_R7(1217, true), MC26_1(260100, true), MC26_2(260200, true),
    MC26_3(260300, true);

    private static MinecraftVersion version;
    private static Boolean hasGsonSupport;
    private static Boolean isForgePresent;
    private static Boolean isNeoForgePresent;
    private static Boolean isFabricPresent;
    private static Boolean isFoliaPresent;
    private static boolean bStatsDisabled = false;
    private static boolean disablePackageWarning = false;
    private static boolean updateCheckDisabled = true;
    /**
     * Logger used by the api
     */
    private static Logger logger = Logger.getLogger("PlayerInv");

    // NBT-API Version
    protected static final String VERSION = "2.16.1";

    private final int versionId;
    private final boolean mojangMapping;

    // TODO: not nice
    private static final Map<String, MinecraftVersion> VERSION_TO_REVISION = new HashMap<String, MinecraftVersion>() {
        { 
            this.put("1.20", MC1_20_R1);
            this.put("1.20.1",  MC1_20_R1);
            this.put("1.20.2", MC1_20_R2);
            this.put("1.20.3", MC1_20_R3);
            this.put("1.20.4", MC1_20_R3);
            this.put("1.20.5", MC1_20_R4);
            this.put("1.20.6", MC1_20_R4);
            this.put("1.21", MC1_21_R1);
            this.put("1.21.1", MC1_21_R1);
            this.put("1.21.2", MC1_21_R2);
            this.put("1.21.3", MC1_21_R2);
            this.put("1.21.4", MC1_21_R3);
            this.put("1.21.5", MC1_21_R4);
            this.put("1.21.6", MC1_21_R5);
            this.put("1.21.7", MC1_21_R5);
            this.put("1.21.8", MC1_21_R5);
            this.put("1.21.9", MC1_21_R6);
            this.put("1.21.10", MC1_21_R6);
            this.put("1.21.11", MC1_21_R7);
            this.put("26.1", MC26_1);
            this.put("26.2", MC26_2);
            this.put("26.3", MC26_3);
        }
    };

    MinecraftVersion(int versionId) {
        this(versionId, false);
    }

    MinecraftVersion(int versionId, boolean mojangMapping) {
        this.versionId = versionId;
        this.mojangMapping = mojangMapping;
    }

    /**
     * @return A simple comparable Integer, representing the version.
     */
    public int getVersionId() {
        return versionId;
    }

    /**
     * @return True if method names are in Mojang format and need to be remapped
     *         internally
     */
    public boolean isMojangMapping() {
        return mojangMapping;
    }

    /**
     * This method is required to hot-wire the plugin during mappings generation for
     * newer mc versions thanks to md_5 not used mojmap.
     * 
     * @return
     */
    public String getPackageName() {
        if (this == UNKNOWN) {
            try {
                return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            } catch (Exception ex) {
                // ignore, paper without remap, will fail
            }
        }
        return this.name().replace("MC", "v");
    }

    /**
     * Returns true if the current versions is at least the given Version
     * 
     * @param version The minimum version
     * @return
     */
    public static boolean isAtLeastVersion(MinecraftVersion version) {
        return getVersion().getVersionId() >= version.getVersionId();
    }

    /**
     * Returns true if the current versions newer (not equal) than the given version
     * 
     * @param version The minimum version
     * @return
     */
    public static boolean isNewerThan(MinecraftVersion version) {
        return getVersion().getVersionId() > version.getVersionId();
    }

    /**
     * Getter for this servers MinecraftVersion. Also init's bStats and checks the
     * shading.
     * 
     * @return The enum for the MinecraftVersion this server is running
     */
    public static MinecraftVersion getVersion() {
        if (version != null) {
            return version;
        }
        try {
            final String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            logger.info("Found Minecraft: " + ver + "! Trying to find NMS support");
            version = MinecraftVersion.valueOf(ver.replace("v", "MC"));
        } catch (Exception ex) {
            logger.info("Found Minecraft: " + Bukkit.getServer().getBukkitVersion().split("-")[0]
                    + "! Trying to find NMS support");
            version = VERSION_TO_REVISION.get(Bukkit.getServer().getBukkitVersion().split("-")[0]);
            if (version == null) {
                // check for modern versions with the new versioning scheme, also the new paper version format
                String versionString = Bukkit.getServer().getBukkitVersion().split("-")[0].split(".build")[0];
                for (Entry<String, MinecraftVersion> entry : VERSION_TO_REVISION.entrySet()) {
                    if (versionString.startsWith(entry.getKey()) && version == null) {
                        version = entry.getValue();
                        // pick the highest revision that matches the version string, in case 26.1.3 is somehow different to 26.1
                    } else if (versionString.startsWith(entry.getKey()) && entry.getValue().getVersionId() > version.getVersionId()) {
                        version = entry.getValue();
                    }
                }
            }
            if (version == null) {
                version = UNKNOWN;
            }
        }
        if (version != UNKNOWN) {
            logger.info("NMS support '" + version.name() + "' loaded!");
        } else {
            logger.warning("This Server-Version(" + Bukkit.getServer().getBukkitVersion()
                    + ") is not supported by this NBT-API Version(" + VERSION + ") in PlayerInv"
                    + ". The NBT-API will try to work as good as it can! Some functions may not work!");
        }
        return version;
    }

    public static String getNBTAPIVersion() {
        return VERSION;
    }

    /**
     * @return True, if Gson is usable
     */
    public static boolean hasGsonSupport() {
        if (hasGsonSupport != null) {
            return hasGsonSupport;
        }
        try {
            Class.forName("com.google.gson.Gson");
            hasGsonSupport = true;
        } catch (Exception ex) {
            logger.info("[NBTAPI] Gson not found! This will not allow the usage of some methods!");
            hasGsonSupport = false;
        }
        return hasGsonSupport;
    }

    /**
     * @return True, if Fabric is present
     */
    public static boolean isFabricPresent() {
        if (isFabricPresent != null) {
            return isFabricPresent;
        }
        try {
            logger.info("[NBTAPI] Found Fabric: " + Class.forName("net.fabricmc.api.ModInitializer"));
            isFabricPresent = true;
        } catch (Exception ex) {
            isFabricPresent = false;
        }
        return isFabricPresent;
    }
    
    /**
     * @return True, if Forge is present
     */
    public static boolean isForgePresent() {
        if (isForgePresent != null) {
            return isForgePresent;
        }
        try {
            logger.info("[NBTAPI] Found Forge: "
                    + (getVersion() == MinecraftVersion.MC1_7_R4 ? Class.forName("cpw.mods.fml.common.Loader")
                            : Class.forName("net.minecraftforge.fml.common.Loader")));
            isForgePresent = true;
        } catch (Exception ex) {
            isForgePresent = false;
        }
        return isForgePresent;
    }
    
    /**
     * @return True, if NeoForge is present
     */
    public static boolean isNeoForgePresent() {
        if (isNeoForgePresent != null) {
            return isNeoForgePresent;
        }
        try {
            logger.info("[NBTAPI] Found NeoForge: " + Class.forName("net.neoforged.neoforge.common.NeoForge"));
            isNeoForgePresent = true;
        } catch (Exception ex) {
            isNeoForgePresent = false;
        }
        return isNeoForgePresent;
    }

    /**
     * @return True, if Folia is present
     */
    public static boolean isFoliaPresent() {
        if (isFoliaPresent != null) {
            return isFoliaPresent;
        }
        try {
            logger.info("[NBTAPI] Found Folia: " + Class.forName("io.papermc.paper.threadedregions.RegionizedServer"));
            isFoliaPresent = true;
        } catch (Exception ex) {
            isFoliaPresent = false;
        }
        return isFoliaPresent;
    }

    /**
     * Calling this function before the NBT-Api is used will disable bStats stats
     * collection. Please consider not to do that, since it won't affect your plugin
     * and helps the NBT-Api developer to see api's demand.
     */
    public static void disableBStats() {
        bStatsDisabled = true;
    }

    /**
     * Disables the update check. Uses Spiget to get the current version and prints
     * a warning when outdated.
     */
    public static void disableUpdateCheck() {
        updateCheckDisabled = true;
    }

    /**
     * Enables the update check. Uses Spiget to get the current version and prints a
     * warning when outdated.
     */
    public static void enableUpdateCheck() {
        updateCheckDisabled = false;
    }

    /**
     * Forcefully disables the log message for plugins not shading the API to
     * another location. This may be helpful for networks or development
     * environments, but please don't use it for plugins that are uploaded to
     * Spigotmc.
     */
    public static void disablePackageWarning() {
        disablePackageWarning = true;
    }

    /**
     * @return Logger used by the NBT-API
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Replaces the NBT-API logger with a custom implementation.
     * 
     * @param logger The new logger(can not be null!)
     */
    public static void replaceLogger(Logger logger) {
        if (logger == null)
            throw new NullPointerException("Logger can not be null!");
        MinecraftVersion.logger = logger;
    }

}
