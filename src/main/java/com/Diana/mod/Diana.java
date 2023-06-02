package com.Diana.mod;

import com.Diana.mod.commands.DianaCommand;
import com.Diana.mod.config.config;
import com.Diana.mod.events.PacketEvent;
import com.Diana.mod.handlers.PacketHandler;
import com.Diana.mod.utils.Utils;
import com.Diana.mod.utils.WaypointUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = Diana.Name, version = Diana.V)
public class Diana {
    public static final String Name = "Diana";
    public static final String V = "0.1.9";
    public static String chatTitle = "§3[Diana]§r ";
    public static final Logger logger = LogManager.getLogger(Name);
    public static KeyBinding[] keyBindings = new KeyBinding[1];
    public static List<Warp> warps = new ArrayList<>(Arrays.asList(
            new Warp(new Vec3(-3,69,-70), "hub", true),
            new Warp(new Vec3(-250,129,-45), "castle", true),
            new Warp(new Vec3(91,74,173), "da", true),
            new Warp(new Vec3(-162,61,-99), "crypt", true),
            new Warp(new Vec3(-76,75,80), "museum", true)));
    public static String lastwarp = "undefined";
    static HashMap<Integer, HashMap<Integer, Integer>> hubdata = new HashMap<>();
    public static HashMap<Integer, Color> waypointColors = new HashMap<Integer, Color>(){{
       put(1, Color.GREEN);
       put(2, Color.RED);
       put(3, Color.WHITE);
       put(4, Color.YELLOW);
    }};
    public static HashMap<Integer, String> waypointNames = new HashMap<Integer, String>(){{
        put(1, "§aStart");
        put(2, "§cMob");
        put(3, "§fMob/Treasure");
        put(4, "§eTreasure");
    }};

    public static boolean toggle = false;
    public static boolean guess = true;
    public static boolean interpolation = true;
    public static boolean proximity = true;
    public static boolean messages = false;
    public static boolean sendInqToAll = false;
    public static boolean receiveInqFromAll = true;
    public static boolean block = true;
    public static boolean beam = true;
    public static boolean text = true;
    public static List<String> ignoredPlayers = new ArrayList<>();

    public static Minecraft mc = Minecraft.getMinecraft();
    public static boolean inHub = false;
    public static boolean doingDiana = false;
    public static boolean inParty = false;
    public static List<String> partyMembers = new ArrayList<>();
    static boolean echo = false;
    static List<Float> pitch = new ArrayList<>();
    static List<Vec3> sounds = new ArrayList<>();
    static List<Vec3> particles = new ArrayList<>();
    static List<Vec3> oldparticles = new ArrayList<>();
    static long clicked = 0;
    static boolean arrow = false;
    static Vec3 arrowStart = null;
    static Vec3 arrowDir = null;
    public static Vec3 burrow = null;
    public static Vec3 lastburrow = null;
    public static Vec3 lastlastburrow = null;
    public static HashMap<BlockPos, Waypoint> waypoints = new HashMap<>();
    public static int lastdug = 1;
    public static List<BlockPos> dugburrow = new ArrayList<>();
    public static List<BlockPos> foundBurrows = new ArrayList<>();
    public static Vec3 selected = null;
    static float scale = 1;
    static long scaleTime = 0;
    static long lastinterp = 0;
    static long interp = 0;
    static int ticks = 0;
    public static List<String> receivedInquisFrom = new ArrayList<>();


    @Mod.EventHandler
    void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
    }

    @Mod.EventHandler
    void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config.cfgreload();

        keyBindings[0] = new KeyBinding("Warp to selected burrow", Keyboard.KEY_NONE, "Diana");
        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mc.getResourceManager()
                .getResource(new ResourceLocation("diana", "hubdata.json")).getInputStream()))
        ) {
            JsonObject hub = new Gson().fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> xx : hub.entrySet()) {
                int x = Integer.parseInt(xx.getKey());
                HashMap<Integer, Integer> xs = new HashMap<>();
                for (Map.Entry<String, JsonElement> zz : xx.getValue().getAsJsonObject().entrySet()) {
                    int z = Integer.parseInt(zz.getKey());
                    int y = Integer.parseInt(zz.getValue().getAsString());
                    xs.put(z, y);
                }
                hubdata.put(x, xs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Finished init");
    }

    @SubscribeEvent
    void serverConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc.getCurrentServerData() == null) return;
        if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.")) {
            event.manager.channel().pipeline().addBefore("packet_handler", "diana_packet_handler", new PacketHandler());
            logger.info("Added Hypixel packet handler, searching for updates");
            updateThread();
        }
    }

    void updateThread() {
        if (mc.thePlayer == null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    updateThread();
                }
            }, 100);
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("https://api.github.com/repos/Doppelclick/Diana/releases/latest");
                        URLConnection request = url.openConnection();
                        request.connect();
                        JsonParser json = new JsonParser();
                        JsonObject latestRelease = json.parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

                        String latestTag = latestRelease.get("tag_name").getAsString();
                        DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(V);
                        DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

                        if (currentVersion.compareTo(latestVersion) < 0) {
                            logger.info("Update available");
                            String releaseURL = "https://github.com/Doppelclick/Diana/releases/latest";
                            ChatComponentText update = new ChatComponentText("§l§2  [UPDATE]  ");
                            update.setChatStyle(update.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("github - " + latestVersion))));
                            Utils.sendModMessage(new ChatComponentText(chatTitle + "§cSolver is outdated. Please update to " + latestTag + ".\n").appendSibling(update));
                        } else logger.info("No update found");
                    } catch (Exception e) {
                        logger.warn("An error has occurred connecting to github");
                        Utils.sendModMessage("§cAn error has occurred connecting to github");
                        e.printStackTrace();
                    }
                }
            }, 1000);
        }
    }

    public static class Warp {
        Vec3 pos;
        String name;
        boolean enabled;
        double distance = 9999;

        public Warp(Vec3 p, String n, boolean e) {
            this.pos = p;
            this.name = n;
            this.enabled = e;
        }
        public static void set(String name, boolean state) {
            for (Warp warp : warps) {
                if (warp.name.equals(name)) {
                    warps.get(warps.indexOf(warp)).enabled = state;
                    return;
                }
            }
        }
        public static Warp closest(Vec3 target, boolean check) {
            Warp re = new Warp(null, null, false);
            for (Warp warp : warps) {
                if ((warp.enabled |! check) && target.distanceTo(warp.pos) < re.distance) {
                    re = warp;
                    re.distance = target.distanceTo(warp.pos);
                }
            }
            return re;
        }
    }

    @SubscribeEvent
    void entity(EntityJoinWorldEvent event) {
        Entity e = event.entity;
        if (e == null |! toggle |! doingDiana) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (e.getName().contains("Minos Inquisitor") && (sendInqToAll || inParty)) {
            if (maxDistance(player.getPositionVector(), e.getPositionVector()) < 10) {
                BlockPos pos = new BlockPos(e.getPositionVector()).down();
                String warp = "nothing";
                Warp w = Warp.closest(e.getPositionVector(), false);
                if (w.pos != null) {
                    warp = w.name;
                }
                mc.thePlayer.sendChatMessage("/" + (sendInqToAll ? "a" : "p") + "c [Diana] Inquis! [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "] at ⏣ " + Utils.getLocation() + " warp: " + warp);
            }
        }
    }

    @SubscribeEvent
    void sendPacket(PacketEvent.SendEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null |! toggle |! doingDiana) return;
        if (player.getHeldItem() == null) return;
        if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
            if (event.packet instanceof C07PacketPlayerDigging) {
                C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
                if (packet.getStatus().equals(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) && mc.theWorld.getBlockState(packet.getPosition()).getBlock().equals(Blocks.grass)) {
                    dugburrow.add(packet.getPosition());
                }
            } else if (event.packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.packet;
                if (!packet.getPosition().equals(new BlockPos(-1,-1,-1)) && mc.theWorld.getBlockState(packet.getPosition()).getBlock().equals(Blocks.grass))
                    dugburrow.add(packet.getPosition());
            }
        }
    }

    @SubscribeEvent
    void interact(PlayerInteractEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (!toggle || player == null |! inHub) return;
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && guess) {
            if (System.currentTimeMillis() > clicked + 2000) {
                if (player.getHeldItem() != null) {
                    if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
                        oldparticles = particles;
                        resetData();
                        echo = true;
                        doingDiana = true;
                        clicked = System.currentTimeMillis();
                    }
                }
            }
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && doingDiana) { //this is unnecessary, but also works for right click, so I'll just leave it in
            if (player.getHeldItem() != null) {
                if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade") && mc.theWorld.getBlockState(event.pos).getBlock().equals(Blocks.grass)) {
                        dugburrow.add(event.pos);
                    }
                }
        }
    }

    @SubscribeEvent
    void tick(TickEvent event) {
        if (event.phase != TickEvent.Phase.START | !toggle || ticks > 1000) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;
        ticks++;
        if (ticks % 20 == 0 &! inHub) {
            inHub = Utils.checkHub();
            if (inHub) logger.info("Joined Skyblock hub");
        }
        if (!doingDiana) return;
        if (ticks % 20 == 0) {
            if (guess) {
                if (echo) {
                    if (System.currentTimeMillis() > clicked + 3000 || (particles.size() == 13 && pitch.size() == 13)) {
                        echo = false;
                        calcBurrow();
                    }
                }
            }
        }
        if (ticks % 200 == 0) {
            ticks = 0;
            for (Map.Entry<BlockPos, Waypoint> e : waypoints.entrySet()) {
                if (e.getValue() instanceof InquisWaypoint) {
                    if (System.currentTimeMillis() > ((InquisWaypoint) e.getValue()).time + 60000) waypoints.remove(e.getKey());
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    void sound(PlaySoundEvent event) {
        if (!toggle |! doingDiana) return;
        if (event.name.equals("note.harp") && guess && echo) {
            pitch.add(event.sound.getPitch());
            sounds.add(new Vec3(event.sound.getXPosF(), event.sound.getYPosF(), event.sound.getZPosF()));
            calcBurrow();
        }
    }

    @SubscribeEvent
    void packet(PacketEvent.ReceiveEvent event) {
        if (!toggle |! doingDiana) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (event.packet instanceof S2APacketParticles) {
            S2APacketParticles particle = (S2APacketParticles) event.packet;
            Vec3 pos = new Vec3(particle.getXCoordinate(), particle.getYCoordinate(), particle.getZCoordinate());
            BlockPos down = new BlockPos(pos).down();
            if (particle.getParticleType() == EnumParticleTypes.FIREWORKS_SPARK && particle.getParticleSpeed() == 0 && guess && echo) {
                particles.add(pos);
            } else if (particle.getParticleType() == EnumParticleTypes.REDSTONE && particle.getParticleSpeed() == 1 && particle.getParticleCount() == 0 && guess && arrow) {
                if (arrowStart == null) {
                    arrowStart = pos;
                } else if (arrowDir == null) {
                    Vec3 dir = pos.subtract(arrowStart).normalize();
                    if (dir.xCoord == 0 && dir.zCoord == 0) return;
                    arrowDir = dir;
                    arrow = false;
                }
            } else if (proximity &! foundBurrows.contains(down) &! dugburrow.contains(down)) {
                ParticleBurrowWaypoint burrow1 = new ParticleBurrowWaypoint();
                burrow1.setType(particle.getParticleType(), particle.getParticleCount(), particle.getParticleSpeed(), particle.getXOffset(), particle.getYOffset(), particle.getZOffset());
                if (burrow1.type > -1) {
                    if (!waypoints.containsKey(down)) Utils.playSound("note.pling", 0.6f, 1.2f);
                    waypoints.put(down, burrow1);
                }
            }
        }
    }

    void calcBurrow() {
        if (pitch.size() < 3 || particles.size() < 3 || sounds.size() < 3) return;

        float all = 0;
        for (int i = 1; i < pitch.size(); i++) {
            all += pitch.get(i) - pitch.get(i - 1);
        }
        all /= pitch.size() - 1;

        Vec3 first = particles.get(0);
        Vec3 last2 = particles.get(particles.size() - 2);
        Vec3 last = particles.get(particles.size() - 1);
        Vec3 lastsound = sounds.get(sounds.size() - 1);

        double lineDist = Math.sqrt(total(last2.subtract(last)));
        double distance = (Math.E / all - Math.sqrt(total(first.subtract(lastsound))));
        Vec3 changes = last.subtract(last2);
        changes = new Vec3(changes.xCoord / lineDist, changes.yCoord / lineDist, changes.zCoord / lineDist);
        double x = lastsound.xCoord + changes.xCoord * distance;
        double y = lastsound.yCoord + changes.yCoord * distance;
        double z = lastsound.zCoord + changes.zCoord * distance;
        boolean useOld = true;
        if (hubdata.containsKey((int)Math.round(x))) {
            if (hubdata.get((int)Math.round(x)).containsKey((int)Math.round(z))) {
                y = hubdata.get((int)Math.round(x)).get((int)Math.round(z));
                useOld = false;
            }
        }
        if (burrow != null && useOld) y = burrow.yCoord;
        burrow = new Vec3(x, y, z);
        if (messages &! echo) Utils.sendModMessage("[" + Math.round(burrow.xCoord) + "," + Math.round(burrow.yCoord) + "," + Math.round(burrow.zCoord) + "] " + (int)Math.round(distance));
        if (!oldparticles.isEmpty() || (arrowStart != null && arrowDir != null)) intercept();
    }

    double total(Vec3 v) {
        return total(v.xCoord, v.yCoord, v.zCoord);
    }
    double total(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    /**
     * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
     * @author Quantizr
     */
    @SubscribeEvent
    void worldRender(RenderWorldLastEvent event) {
        if (!toggle &! block &! beam &! text |! doingDiana) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        double distance = 129600;
        Vec3 guesspos = null;
        if (guess && burrow != null) {
            if (interpolation) {
                if (lastburrow == null) lastburrow = burrow;
                if (lastlastburrow == null) lastlastburrow = lastburrow;
                double interpFactor = Math.max(0, Math.min(1, Math.round((System.currentTimeMillis() - interp) * 100f) / 100f / (interp - lastinterp)));
                double x = lastlastburrow.xCoord + (lastburrow.xCoord - lastlastburrow.xCoord) * interpFactor;
                double y = lastlastburrow.yCoord + (lastburrow.yCoord - lastlastburrow.yCoord) * interpFactor;
                double z = lastlastburrow.zCoord + (lastburrow.zCoord - lastlastburrow.zCoord) * interpFactor;
                guesspos = new Vec3(x, y, z);

                if (!lastburrow.equals(burrow)) {
                    lastinterp = interp;
                    lastlastburrow = lastburrow;
                    interp = System.currentTimeMillis();
                    lastburrow = burrow;
                }
            } else guesspos = burrow;

            double dist = distanceTo(guesspos, player);
            if (dist < 129600) {
                distance = dist;
                selected = burrow;
            }
        }
        for (Map.Entry<BlockPos, Waypoint> burrow : waypoints.entrySet()) {
            double dist = distanceTo(new Vec3(burrow.getKey()), player);
            if (dist < distance) {
                distance = dist;
                selected = new Vec3(burrow.getKey());
            }
        }
        if (selected != null) {
            if (distance == 129600) {
                scale = 1;
                selected = null;
                scaleTime = 0;
            } else if (scaleTime == 0) {
                scaleTime = System.currentTimeMillis();
            } else {
                long time = System.currentTimeMillis() - scaleTime;
                if (time <= 150) {
                    scale = 1 +  (float)time / 150;
                } else if (scale != 2) {
                    scale = 2;
                }
            }
        }
        if (guess && guesspos != null) {
            float sc = 1;
            if (selected != null) if (burrow.equals(selected)) sc = scale;
            renderBeacon(event.partialTicks, "§l§bGuess " + waypointNames.get(lastdug) + " (" + lastdug + ")", sc, guesspos, waypointColors.get(lastdug));
        }
        if (!waypoints.isEmpty()) {
            for (Map.Entry<BlockPos, Waypoint> burrow : new HashSet<>(waypoints.entrySet())) {
                float sc = 1;
                if (selected != null) if (burrow.getKey().equals(new BlockPos(selected))) sc = scale;
                String display = "";
                Color color = Color.BLACK;
                if (burrow.getValue() instanceof ParticleBurrowWaypoint && proximity) {
                    ParticleBurrowWaypoint b = (ParticleBurrowWaypoint) burrow.getValue();
                    display = waypointNames.get(b.type) + " (" + (b.type == 3 ? "2/" : "") + b.type + ")";
                    color = waypointColors.get(b.type);
                } else if (burrow.getValue() instanceof InquisWaypoint) {
                    InquisWaypoint b = (InquisWaypoint) burrow.getValue();
                    if (partyMembers.contains(b.player)) display = "§9";
                    display = display + b.player + "§r's §6Inquisitor";
                    color = new Color(226, 167, 60);
                }
                renderBeacon(event.partialTicks, display, sc, new Vec3(burrow.getKey()), color);
            }
        }
    }

    public static double getYaw(Vec3 playerp, Vec3 point) { //horizontal
        double yaw = (Math.atan2(playerp.xCoord - point.xCoord, point.zCoord - playerp.zCoord) * 180/Math.PI) % 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }
    public static double getPitch(Vec3 playerp, Vec3 point) { //vertical
        return (Math.atan2(playerp.yCoord + 1 - point.yCoord, Math.hypot(playerp.xCoord - point.xCoord , playerp.zCoord - point.zCoord)) * 180/Math.PI) % 360;
    }

    void renderBeacon(float partialTicks, String info, float scale, Vec3 pos, Color color) {
        Entity viewer = mc.getRenderViewEntity();
        Frustum frustum = new Frustum();
        frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        double x = pos.xCoord - viewerX;
        double y = pos.yCoord - viewerY;
        double z = pos.zCoord - viewerZ;
        double distSq = x*x + y*y + z*z;

        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        if (distSq > 35) {
            if (beam) WaypointUtils.renderBeaconBeam(x, y + scale, z, color.getRGB(), 0.25f, partialTicks);
        } else scale = 1;
        if (block && frustum.isBoxInFrustum(pos.xCoord, pos.yCoord, pos.zCoord, pos.xCoord + 1, pos.yCoord + 1, pos.zCoord + 1))
            WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x - scale + 1, y - scale + 1, z - scale + 1, x + scale, y + scale, z + scale), color, 0.4f);
        GlStateManager.disableTexture2D();
        if (text) WaypointUtils.renderWaypointText(info, pos.addVector(0, scale,0), partialTicks, scale);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
    }

    public static class Waypoint {
    }

    public static class InquisWaypoint extends Waypoint {
        String player;
        long time;
        public InquisWaypoint(String p, long time) {
            this.player = p;
            this.time = time;
        }
    }

    public static class ParticleBurrowWaypoint extends Waypoint {
        int type = -1; //1 start, 2 treasure, 3 footsteps, 4 enchants
        //from Skytils under the GNU 3.0 license
        void setType(EnumParticleTypes particle, int count, float speed, float xOffset, float yOffset, float zOffset) {
            if (particle == EnumParticleTypes.CRIT_MAGIC && count == 4 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f) {
                this.type = 1;
            }
            else if (particle == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f) {
                this.type = 2;
            }
            else if (particle == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f) {
                this.type = 4;
            }
            else if ((particle == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f) ||
                    (particle == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f)) {
                this.type = 3;
            }
        }
    }

    @SubscribeEvent
    void key(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (!toggle || player == null |! doingDiana) return;
        if (keyBindings[0].isPressed() && selected != null) {
            Warp warp = Warp.closest(selected, true);

            if (warp.pos != null) {
                if (warp.distance * 1.1 < player.getPositionVector().distanceTo(selected)) {
                    mc.thePlayer.sendChatMessage("/warp " + warp.name);
                    if (messages) Utils.sendModMessage("Warped to " + warp.name);
                    lastwarp = warp.name;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    void chat(ClientChatReceivedEvent event) {
        if (!toggle || event.type != 0) return;
        String message = event.message.getFormattedText();
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        Matcher p1 = Pattern.compile("^§\\S((\\[\\S+\\]\\s)?)(?<pm>\\S+) §r§ejoined the (party|dungeon group)").matcher(message); //§a[VIP] AA §r§ejoined the party.§r
        Matcher p2 = Pattern.compile("§\\S((\\[\\S+\\]\\s)?)(?<pm>\\S+) ((§r§ehas (been removed from the party | left the party)) | §r§ewas removed from your party because they disconnected | because they were offline)").matcher(message);
        Matcher p3 = Pattern.compile("^§eYou have joined §r§\\S((\\[\\S+\\]\\s)?)(?<pm>\\S+)'(s*) §r§eparty!").matcher(message); //§eYou have joined §r§b[MVP§r§d+§r§b] AAA's §r§eparty!§r
        Matcher p4 = Pattern.compile("^§eYou'll be partying with: (?<pm>.+)").matcher(message); //§eYou'll be partying with: §r§a[VIP] AA§r
        Matcher p5 = Pattern.compile("^The party was transferred to (?<pm>\\S+) because (?<pl>\\S+) left").matcher(message); //§eThe party was transferred to §r§7AAA §r§ebecause §r§b[MVP§r§d+§r§b] BBB §r§eleft§r
        Matcher pl = Pattern.compile("Party Leader: ((\\[\\S+\\]\\s)?)(?<pl>\\S+) ●").matcher(unformatted); //§eParty Leader: §r§b[MVP§r§d+§r§b] AAA §r§a●§r
        Matcher pms = Pattern.compile("Party Members: (?<pms>(((\\[\\S+\\]\\s)?)\\S+ ● )+)").matcher(unformatted); //§eParty Members: §r§7AAA§r§a ● §r
        Matcher burrow = Pattern.compile("§r§eYou dug out a Griffin Burrow! §r§7\\((?<number>\\d)/4\\)").matcher(message); //§r§eYou dug out a Griffin Burrow! §r§7(1/4)§r§7
        Matcher inquis = Pattern.compile("\\[Diana\\] Inquis! \\[(?<one>(-?\\d{1,3})),(?<two>(-?\\d{1,3})),(?<three>(-?\\d{1,3}))\\] at ⏣ ").matcher(message);
        String sender = getSender(unformatted);
        if (message.contains("§r§9Party §8>") && sender != null) {
            if (!partyMembers.contains(sender) &! sender.equals(mc.thePlayer.getName())) partyMembers.add(sender);
            inParty = true;
        } else if (message.contains("You are not currently in a party.") || message.contains("You have been kicked from the party by") || message.contains("You left the party.") ||
                message.contains("The party was disbanded because all invites expired and the party was empty") || message.contains("§r§ehas disbanded the party!")) {
            partyMembers.clear();
            inParty = false;
        } else if (p1.find()) {
            partyMembers.add(p1.group("pm"));
            inParty = true;
        } else if (p2.find()) {
            if (partyMembers.remove(p2.group("pm"))) {
                if (partyMembers.isEmpty()) inParty = false;
            }
        } else if (p3.find()) {
            partyMembers.clear();
            partyMembers.add(p3.group("pm"));
            inParty = true;
        } else if (p4.find()) {
            for (String member : p4.group("pm").replaceAll("§\\S", "").split(", ")) {
                String m = member.replaceFirst("(\\[\\S+\\]\\s)", "");
                if (!m.equals(mc.thePlayer.getName()) &! partyMembers.contains(m)) {
                    partyMembers.add(m);
                }
            }
            inParty = true;
        } else if (p5.find()) {
            String pm = StringUtils.stripControlCodes(p5.group("pm")).replaceAll("(\\[\\S+\\]\\s)", "");
            if (!partyMembers.contains(pm) &! pm.equals(mc.thePlayer.getName())) partyMembers.add(pm);
            String ptl = StringUtils.stripControlCodes(p5.group("pl")).replaceAll("(\\[\\S+\\]\\s)", "");
            partyMembers.remove(ptl);
            inParty = true;
        } else if (pl.find() && message.startsWith("§eParty Leader: ")) {
            String ptl = pl.group("pl");
            if (!ptl.equals(mc.thePlayer.getName()) &! partyMembers.contains(ptl)) partyMembers.add(ptl);
            inParty = true;
        } else if (pms.find() && message.startsWith("§eParty Members: ")) {
            for (String m : pms.group("pms").split(" ● ")) {
                String member = m.replaceFirst("(\\[\\S+\\]\\s)", "");
                if (!member.equals(mc.thePlayer.getName()) &! partyMembers.contains(member)) {
                    partyMembers.add(member);
                }
            }
            inParty = true;
        } else if (message.contains("§r§cYou haven't unlocked this fast travel destination!§r") &! lastwarp.equals("undefined")) {
            if (lastwarp.equals("hub")) {
                lastwarp = "undefined";
                return;
            }
            for (Warp warp : warps) {
                if (warp.name.equals(lastwarp)) {
                    if (warp.enabled) {
                        config.writeBooleanConfig("warps", lastwarp, false);
                        Warp.set(lastwarp, false);
                        logger.info("Set fast travel location " + lastwarp + " to unavailable");
                        lastwarp = "undefined";
                    }
                    return;
                }
            }
        } else if (message.contains("§r§eYou dug out a Griffin Burrow! §r§7(") || message.contains("§r§eYou finished the Griffin burrow chain! §r§7(4/4)§r")) { // message.contains("§r§eFollow the arrows to find the §r§6treasure§r§e!") -> unnecessary since the burrow regex is triggered too at the same time, not using burrow.find here because it only works once (?)
            resetRender();
            arrowStart = null;
            arrowDir = null;
            particles = new ArrayList<>();
            oldparticles = new ArrayList<>();
            if (!dugburrow.isEmpty()) {
                for (BlockPos b : dugburrow) {
                    if (Diana.burrow != null) {
                        if (maxDistance(new Vec3(b), Diana.burrow) < 5) {
                            Diana.burrow = null;
                        }
                    }
                    if (waypoints.remove(b) != null) {
                        foundBurrows.add(b);
                    }
                }
                dugburrow = new ArrayList<>();
            }
            try {
                lastdug = burrow.find() ? (Integer.parseInt(burrow.group("number")) % 4 + 1) : 1;
            } catch (NumberFormatException ne) {
                ne.printStackTrace();
            }
            arrow = lastdug != 1;
        } else if (message.contains("§7You were killed by ")) { //§r§c ☠ §r§7You were killed by §r§2Exalted Gaia Construct§r§7§r§7.§r
            oldparticles = new ArrayList<>();
            if (!dugburrow.isEmpty()) {
                for (BlockPos b : dugburrow) {
                    if (Diana.burrow != null) {
                        if (maxDistance(new Vec3(b), Diana.burrow) < 5) {
                            Diana.burrow = null;
                        }
                    }
                    if (waypoints.remove(b) != null) {
                        foundBurrows.add(b);
                    }
                }
                dugburrow = new ArrayList<>();
            }
        } else if (inquis.find() && (sender != null)) {
             if ((receiveInqFromAll || (message.contains("§r§9Party §8>") || partyMembers.contains(sender))) &! sender.equals(mc.thePlayer.getName()) &! ignoredPlayers.contains(sender)) {
                 try {
                     BlockPos pos = new BlockPos(Integer.parseInt(inquis.group("one")), Integer.parseInt(inquis.group("two")), Integer.parseInt(inquis.group("three")));
                     waypoints.put(pos, new InquisWaypoint(sender, System.currentTimeMillis()));
                     Utils.showClientTitle("", "§c" + sender + " 's Inquis near " + Warp.closest(new Vec3(pos), true).name);
                     Utils.ping();
                     ChatComponentText ignore = new ChatComponentText("§c [Ignore this player] ");
                     ignore.setChatStyle(ignore.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/diana ignore add " + sender)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("ignore add " + sender))));
                     Utils.sendModMessage(new ChatComponentText(chatTitle + "§cInquis Waypoint received§r from " + sender + " ").appendSibling(ignore));
                     receivedInquisFrom.add(sender);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
            }
        }
    }

    public static String getSender(String unformatted) {
        Matcher matcher = Pattern.compile("((Party > )?)((\\[\\S+\\]\\s)?)(?<sender>\\S+):").matcher(unformatted);
        if (matcher.find()) {
            return matcher.group("sender");
        } return null;
    }

    public static double distanceTo(Vec3 burrow, EntityPlayerSP player) {
        Vec3 playerp = player.getPositionVector().addVector(0,player.getEyeHeight(),0);
        float yaw = player.rotationYaw % 360;
        if (yaw < 0) yaw += 360;
        float pitch = player.rotationPitch;
        double lowery = getYaw(playerp, burrow);
        double highery = getYaw(playerp, burrow.addVector(1,1,1));
        double lowp = getPitch(playerp, burrow.addVector(0.5,1,0.5));
        double topp = getPitch(playerp, new Vec3(burrow.xCoord + 0.5, 255, burrow.zCoord + 0.5));
        double distance = 129600;
        if (lowery - 3 < yaw && yaw < highery + 3 && pitch < lowp + 4 && pitch > topp) distance = (highery - yaw) * (lowp - pitch);
        return distance;
    }

    public static double maxDistance(Vec3 pos, Vec3 target) {
        double x = Math.abs(target.xCoord - pos.xCoord);
        double y = Math.abs(target.yCoord - pos.yCoord);
        double z = Math.abs(target.zCoord - pos.zCoord);
        return Math.max(x, Math.max(y, z));
    }

    public static void intercept() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || particles.size() < 2) return;
        Vec3 playerp = player.getPositionVector();

        Vec3 p1 = particles.get(0);
        Vec3 p2 = particles.get(1).subtract(p1);
        Vec3 a1;
        Vec3 a2;
        if (oldparticles.size() > 3) {
            a1 = oldparticles.get(0);
            a2 = oldparticles.get(1).subtract(a1);
        } else if (arrowStart != null && arrowDir != null) {
            a1 = arrowStart;
            a2 = arrowDir;
        } else return;

        /*
        y1 = mx1 + b
        m = d(irection)y1 / dx1
        y1 = m1 * x1 + b1
        repeat for set 2
        x = (b2 - b1) / (m1 - m2)
        y = m1 * x + b1  or into set 2
        */
        if (a2.xCoord == 0 || p2.xCoord == 0) return; //cba to code something proper

        double pslope = p2.zCoord / p2.xCoord;
        double py = p1.zCoord - pslope * p1.xCoord;
        double aslope = a2.zCoord / a2.xCoord;
        double ay = a1.zCoord - aslope * a1.xCoord;
        if (pslope - aslope == 0) pslope = 0.00001D;
        double x = (ay - py) / (pslope - aslope);
        double z = pslope * x + aslope;

        Vec3 intercept = new Vec3(x, 0, z);
        if (hubdata.containsKey((int) Math.round(intercept.xCoord))) {
            if (hubdata.get((int) Math.round(intercept.xCoord)).containsKey((int) Math.round(intercept.zCoord))) {
                intercept.addVector(0, hubdata.get((int) Math.round(intercept.xCoord)).get((int) Math.round(intercept.zCoord)), 0);
            }
        }
        if (burrow == null) {
            if (intercept.yCoord == 0) intercept.addVector(0,60,0);
            burrow = intercept;
        } else {
            if (intercept.yCoord == 0) intercept.addVector(0, burrow.yCoord, 0);

            Vec3 relguess = burrow.subtract(playerp);
            Vec3 relintercept = intercept.subtract(playerp);
            if ((Math.signum(relguess.xCoord) == Math.signum(relintercept.xCoord) && Math.signum(relguess.zCoord) == Math.signum(relintercept.zCoord) && burrow.distanceTo(intercept) < 20) || playerp.distanceTo(intercept) < 15) {
                burrow = intercept;
            }
        }
    }

    static void resetData() {
        echo = false;
        pitch = new ArrayList<>();
        sounds = new ArrayList<>();
        particles = new ArrayList<>();
        clicked = 0;
        selected = null;
    }
    static void resetRender() {
        scale = 1;
        scaleTime = 0;
        lastburrow = null;
        lastlastburrow = null;
        lastinterp = 0;
        interp = 0;
    }

    @SubscribeEvent
    void worldUnload(WorldEvent.Unload event) {
        inHub = false;
        doingDiana = false;
        arrow = false;
        arrowStart = null;
        arrowDir = null;
        oldparticles = new ArrayList<>();
        lastdug = 1;
        burrow = null;
        waypoints = new HashMap<>();
        foundBurrows = new ArrayList<>();
        dugburrow = new ArrayList<>();
        ticks = 0;
        resetData();
        resetRender();
    }
}