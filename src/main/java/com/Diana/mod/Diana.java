package com.Diana.mod;

import com.Diana.mod.commands.DianaCommand;
import com.Diana.mod.config.config;
import com.Diana.mod.events.PacketEvent;
import com.Diana.mod.handlers.PacketHandler;
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
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
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
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(modid = Diana.Name, version = Diana.V)
public class Diana {
    public static final String Name = "Diana";
    public static final String V = "0.1.2";
    public static KeyBinding[] keyBindings = new KeyBinding[1];
    public static HashMap<BlockPos, String> warps = new HashMap<BlockPos, String>(){{
        put(new BlockPos(-3,69,-70), "hub");
        put(new BlockPos(-250,129,-45), "castle");
        put(new BlockPos(91,74,173), "da");
        put(new BlockPos(-162,61,-99), "crypt");
        put(new BlockPos(-76,75,80), "museum");
    }};
    public static String lastwarp = "unused";
    HashMap<Integer, HashMap<Integer, Integer>> hubdata = new HashMap<>();
    public static HashMap<Integer, Color> waypointColors = new HashMap<Integer, Color>(){{
       put(1, Color.GREEN);
       put(2, Color.RED);
       put(3, Color.GREEN);
       put(4, Color.YELLOW);
    }};

    public static boolean toggle = false;
    public static boolean guess = false;
    public static boolean proximity = false;
    public static boolean messages = false;
    public static boolean block = false;
    public static boolean beam = true;
    public static boolean text = true;
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    public static Minecraft mc = Minecraft.getMinecraft();
    static boolean echo = false;
    static float lastpitch = 0;
    static Vec3 playerp = new Vec3(0,0,0);
    static List<Float> pitch = new ArrayList<>();
    static List<Vec3> sounds = new ArrayList<>();
    static List<Vec3> particles = new ArrayList<>();
    static long clicked = 0;
    static Vec3 burrow = null;
    static HashMap<BlockPos, particleBurrow> particleBurrows = new HashMap<>();
    public static int lastdug = 1;
    public static BlockPos dugburrow = null;


    @Mod.EventHandler
    void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
    }

    @Mod.EventHandler
    void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config.cfgreload();

        keyBindings[0] = new KeyBinding("Warp to closest spot to the burrow wp", Keyboard.KEY_NONE, "Diana");
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
    }

    @SubscribeEvent
    void serverConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc.getCurrentServerData() == null) return;
        if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.")) {
            event.manager.channel().pipeline().addBefore("packet_handler", "diana_packet_handler", new PacketHandler());
            new Thread(() -> {
                try {
                    while (mc.thePlayer == null) {
                        Thread.sleep(100);
                    }
                    Thread.sleep(1000);

                    URL url = new URL("https://api.github.com/repos/Doppelclick/Diana/releases/latest");
                    URLConnection request = url.openConnection();
                    request.connect();
                    JsonParser json = new JsonParser();
                    JsonObject latestRelease = json.parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

                    String latestTag = latestRelease.get("tag_name").getAsString();
                    DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(V);
                    DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

                    if (currentVersion.compareTo(latestVersion) < 0) {
                        String releaseURL = "https://github.com/Doppelclick/Diana/releases/latest";
                        ChatComponentText update = new ChatComponentText("§l§2  [UPDATE]  ");
                        update.setChatStyle(update.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)));
                        mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana]§c Solver is outdated. Please update to " + latestTag + ".\n").appendSibling(update));
                    }
                } catch (Exception e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §cAn error has occurred connecting to github"));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SubscribeEvent
    void sendPacket(PacketEvent.SendEvent event) {
        if (event.packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
            if (packet.getStatus().equals(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                dugburrow = packet.getPosition();
            }
        }
    }

    @SubscribeEvent
    void interact(PlayerInteractEvent event) {
        if (!toggle) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && guess) {
            if (System.currentTimeMillis() > clicked + 2000) {
                if (player.getHeldItem() != null) {
                    if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
                        resetData();
                        playerp = player.getPositionVector();
                        echo = true;
                        clicked = System.currentTimeMillis();
                    }
                }
            }
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (player.getHeldItem() != null) {
                if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade") && mc.theWorld.getBlockState(event.pos).getBlock().equals(Blocks.grass)) {
                        dugburrow = event.pos;
                    }
                }
        }
        //else if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
        //    dugburrow = event.pos;
        //}
    }

    @SubscribeEvent
    void tick(TickEvent event) {
        if (event.phase != TickEvent.Phase.START |! toggle) return;
        if (guess && echo) {
            if (System.currentTimeMillis() > clicked + 3000 || (particles.size() == 13 && pitch.size() == 13)) {
                echo = false;
                calcBurrow();
            }
        }
    }

    //@SubscribeEvent
    //void playerUpdate(LivingEvent.LivingUpdateEvent event) {
    //    if (!toggle) return;
    //    if (event.entity.equals(mc.thePlayer) && burrow != null && guess) {
    //        if (event.entity.getDistanceSq(new BlockPos(burrow)) < 15) resetData();
    //    }
    //}

    @SubscribeEvent
    void sound(PlaySoundEvent event) {
        if (!toggle) return;
        if (event.name.equals("note.harp") && guess && echo) {
            pitch.add(event.sound.getPitch());
            sounds.add(new Vec3(event.sound.getXPosF(), event.sound.getYPosF(), event.sound.getZPosF()));
            calcBurrow();
        }
    }

    @SubscribeEvent
    void packet(PacketEvent.ReceiveEvent event) {
        if (!toggle) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (event.packet instanceof S2APacketParticles) {
            S2APacketParticles particle = (S2APacketParticles) event.packet;
            if (particle.getParticleType() == EnumParticleTypes.FIREWORKS_SPARK && particle.getParticleSpeed() == 0 && guess && echo) {
                particles.add(new Vec3(particle.getXCoordinate(), particle.getYCoordinate(), particle.getZCoordinate()));
            } else if (proximity && player.getHeldItem() != null) {
                if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
                    particleBurrow burrow1 = new particleBurrow();
                    burrow1.setType(particle.getParticleType(), particle.getParticleCount(), particle.getParticleSpeed(), particle.getXOffset(), particle.getYOffset(), particle.getZOffset());
                    particleBurrows.put(new BlockPos(particle.getXCoordinate(), particle.getYCoordinate(), particle.getZCoordinate()), burrow1);
                }
            }
        }
    }

    void calcBurrow() {
        if (pitch.size()<3 || particles.size()<3 || sounds.size()<3) return;

        float all = 0;
        for (int i = 1; i < pitch.size(); i++) {
            all+=(pitch.get(i)-pitch.get(i - 1));
        }
        all /= pitch.size() - 1;

        Vec3 first = particles.get(0);
        Vec3 last2 = particles.get(particles.size()-2);
        Vec3 last = particles.get(particles.size()-1);
        Vec3 lastsound = sounds.get(sounds.size()-1);

        double lineDist = Math.sqrt(total(last2.subtract(last)));
        double distance = (Math.E / all - Math.sqrt(total(first.subtract(lastsound))));
        Vec3 changes = last.subtract(last2);
        changes = new Vec3(changes.xCoord/lineDist, changes.yCoord/lineDist, changes.zCoord/lineDist);
        double x = lastsound.xCoord + changes.xCoord * distance;
        double y = lastsound.yCoord + changes.yCoord * distance;
        double z = lastsound.zCoord + changes.zCoord * distance;
        if (hubdata.containsKey((int)Math.round(x))) {
            if (hubdata.get((int)Math.round(x)).containsKey((int)Math.round(z))) {
                y = hubdata.get((int)Math.round(x)).get((int)Math.round(z));
            }
        }
        burrow = new Vec3(x, y, z);
        if (messages &! echo) mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §r[" + Math.round(burrow.xCoord) + "," + Math.round(burrow.yCoord) + "," + Math.round(burrow.zCoord) + "] " + (int)Math.round(distance)));
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
        if (!toggle) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player==null) return;

        if (!block &! beam &! text) return;
        //Vec3 eyes = new Vec3(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
        //float aspectRatio = (float) mc.displayWidth / (float) mc.displayHeight;
        //float playerYaw = player.rotationYaw;
        //float playerPitch = player.rotationPitch;
        //double fovV = mc.gameSettings.fovSetting * mc.thePlayer.getFovModifier();
        //double fovH = Math.atan(aspectRatio * Math.tan(fovV * DEG_TO_RAD / 2)) * 2 * RAD_TO_DEG;
        //float verticalSpacing = (float) (fovV * 0.8);
        //float horizontalSpacing = (float) (fovH * 0.9);
        //if (mc.gameSettings.thirdPersonView == 2) {
        //    playerYaw = playerYaw + 180.0F;
        //    playerPitch = -playerPitch;
        //}

        //float yawScaled = horizontalSpacing * ((playerPitch * playerPitch / 8100) + 1);

        //Vec3 direction = getVectorFromRotation(yawScaled + playerYaw, verticalSpacing + playerPitch);

        //Vec3 playerview = eyes.addVector(direction.xCoord * 64, direction.yCoord * 64, direction.zCoord * 64);

        if (guess && burrow != null) {
            renderBeacon(event.partialTicks, "§bGuess (" + lastdug + "/4)", 1, burrow, waypointColors.get(lastdug));
        }
        //if (proximity &! particleBurrows.isEmpty()) {
        //    for (Map.Entry<BlockPos, particleBurrow> burrow : particleBurrows.entrySet()) {
        //        renderBeacon(event.partialTicks, "(" + burrow.getValue().type + "/4)", 1, new Vec3(burrow.getKey()), waypointColors.get(burrow.getValue().type));
        //    }
        //}
    }

    public static Vec3 getVectorFromRotation(float yaw, float pitch) {
        float f = MathHelper.cos(-yaw * (float) DEG_TO_RAD - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * (float) DEG_TO_RAD - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * (float) DEG_TO_RAD);
        float f3 = MathHelper.sin(-pitch * (float) DEG_TO_RAD);
        return new Vec3( f1 * f2, f3, f * f2);
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
        if (block && frustum.isBoxInFrustum(pos.xCoord, pos.yCoord, pos.zCoord, pos.xCoord + 1, pos.yCoord + 1, pos.zCoord + 1))
            WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, 0.4f);
        GlStateManager.disableTexture2D();
        if (beam && distSq > 25) WaypointUtils.renderBeaconBeam(x, y + 1, z, color.getRGB(), 0.25f, partialTicks);
        if (text) WaypointUtils.renderWaypointText(info, pos.addVector(0,1,0), partialTicks, scale);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
    }

    static class particleBurrow {
        int type = 0; //1 mob, 2 treasure, 3 footsteps, 4 enchants
        void setType(EnumParticleTypes particle, int count, float speed, float xOffset, float yOffset, float zOffset) {
                if (particle == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f) this.type=1;
                else if (particle == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f) this.type=2;
                else if (particle == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f) this.type=3;
                else if(particle == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f) this.type=4;
        }
    }

    @SubscribeEvent
    void key(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (!toggle || player==null) return;
        if (keyBindings[0].isPressed() && burrow != null) {
            String name = "unused";
            BlockPos pos = new BlockPos(player.getPositionVector());
            for (Map.Entry<BlockPos, String> warp : warps.entrySet()) {
                if (new BlockPos(burrow).distanceSq(pos) > new BlockPos(burrow).distanceSq(warp.getKey()) &! warp.getValue().equals("unused")) {
                    name = warp.getValue();
                    pos = warp.getKey();
                }
            }
            if (!name.equals("unused")) {
                mc.thePlayer.sendChatMessage("/warp " + name);
                lastwarp = name;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    void chat(ClientChatReceivedEvent event) {
        if (!toggle) return;
        String message = event.message.getFormattedText();
        if (message.contains("&r&cYou haven't unlocked this fast travel destination!&r") &! lastwarp.equals("unused")) {
            if (lastwarp.equals("hub")) {
                lastwarp = "unused";
                return;
            }
            for (Map.Entry<BlockPos, String> warp : warps.entrySet()) {
                if (warp.getValue().equals(lastwarp)) {
                    config.writeBooleanConfig("warps", lastwarp, false);
                    lastwarp = "unused";
                    warps.put(warp.getKey(), lastwarp);
                    return;
                }
            }
        } if (message.contains("§r§eYou dug out a Griffin Burrow! §r§7(")) {
            if (dugburrow != null) {
                boolean removed = false;
                if (particleBurrows.containsKey(dugburrow)) {
                    particleBurrows.remove(dugburrow);
                    removed = true;
                }
                if (burrow.squareDistanceTo(new Vec3(dugburrow)) < 10) {
                    removed = true;
                    burrow = null;
                }
                if (removed) dugburrow = null;
            }
            lastdug = Integer.parseInt(message.substring(message.indexOf("(") + 1, message.indexOf("(") + 2)) % 4 + 1;
        }
    }

    static void resetData() {
        lastdug = 1;
        echo = false;
        burrow = null;
        lastpitch = 0;
        playerp = new Vec3(0,0,0);
        pitch = new ArrayList<>();
        sounds = new ArrayList<>();
        particles = new ArrayList<>();
        clicked = 0;
        particleBurrows = new HashMap<>();
        dugburrow = null;
    }

    @SubscribeEvent
    void worldUnload(WorldEvent.Unload event) {
        resetData();
    }
}