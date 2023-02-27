package com.Diana.mod;

import com.Diana.mod.commands.DianaCommand;
import com.Diana.mod.config.config;
import com.Diana.mod.events.PacketEvent;
import com.Diana.mod.handlers.PacketHandler;
import com.Diana.mod.utils.WaypointUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = Diana.Name, version = Diana.V)
public class Diana {
    public static final String Name = "Diana";
    public static final String V = "0.1";
    public static boolean toggle = false;
    public static boolean guess = false;
    public static boolean proximity = false;
    public static boolean messages = false;
    public static boolean block = false;
    public static boolean beam = true;
    public static boolean text = true;

    public static Minecraft mc = Minecraft.getMinecraft();
    static boolean echo = false;
    static float lastpitch = 0;
    static List<Float> pitch = new ArrayList<>();
    static BlockPos playerp = new BlockPos(0,0,0);
    static List<BlockPos> sounds = new ArrayList<>();
    static List<BlockPos> particles = new ArrayList<>();
    static long clicked = 0;
    static BlockPos burrow = new BlockPos(0,0,0);


    @Mod.EventHandler
    void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
        //configDir = event.getModConfigurationDirectory().toString();
    }

    @Mod.EventHandler
    void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config.cfgreload();
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
                        mc.thePlayer.addChatMessage(new ChatComponentText("$cDiana Solver is outdated. Please update to " + latestTag + ".\n").appendSibling(update));
                    }
                } catch (Exception e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §cAn error has occurred connecting to github"));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SubscribeEvent
    void interact(PlayerInteractEvent event) {
        if (!toggle) return;
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && guess) {
            EntityPlayerSP player = mc.thePlayer;
            if (player!=null && System.currentTimeMillis()>clicked + 2000) {
                if (player.getHeldItem() != null) {
                    if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
                        resetData();
                        echo = true;
                        clicked = System.currentTimeMillis();
                        playerp = new BlockPos(player.getPositionVector().xCoord, player.getPositionVector().yCoord+1, player.getPositionVector().zCoord);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    void tick(TickEvent event) {
        if (event.phase != TickEvent.Phase.START |! toggle) return;
        if (guess && echo) {
            if (System.currentTimeMillis() > clicked + 2000 || (particles.size() == 13 && pitch.size() == 13)) {
                echo = false;
                calcBurrow();
            }
        }
    }

    @SubscribeEvent
    void playerUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!toggle) return;
        if (event.entity.equals(mc.thePlayer) && guess) {
            if (event.entity.getDistanceSq(burrow) < 15) resetData();
        }
    }

    @SubscribeEvent
    void sound(PlaySoundEvent event) {
        if (!toggle) return;
        if (event.name.equals("note.harp") && guess && echo) {
            if (lastpitch!=0) pitch.add(event.sound.getPitch()-lastpitch);
            lastpitch=event.sound.getPitch();
            sounds.add(new BlockPos(event.sound.getXPosF() - playerp.getX(), event.sound.getYPosF() - playerp.getY(), event.sound.getZPosF() - playerp.getZ()));
        }
    }

    @SubscribeEvent
    void packet(PacketEvent.ReceiveEvent event) {
        if (!toggle) return;
        if (guess && echo) {
            if (event.packet instanceof S2APacketParticles) {
                S2APacketParticles particle = (S2APacketParticles) event.packet;
                if (particle.getParticleType() == EnumParticleTypes.FIREWORKS_SPARK && particle.getParticleSpeed() == 0) {
                    particles.add(new BlockPos(particle.getXCoordinate() - playerp.getX(), particle.getYCoordinate() - playerp.getY(), particle.getZCoordinate() - playerp.getZ()));
                }
            }
        }
    }

    void calcBurrow() {
        if (pitch.size()<2 || particles.size()<2) return;
        float all = 0;
        for (float i : pitch) all+=i;
        all /= pitch.size();
        Vec3 last = new Vec3(0,0,0);
        if (particles.size()>sounds.size()) last = new Vec3(particles.get(particles.size()-1));
        else if (particles.size()<sounds.size()) last = new Vec3(sounds.get(sounds.size()-1));
        else {
            BlockPos a = particles.get(particles.size()-1);
            BlockPos b = sounds.get(sounds.size()-1);
            last = new Vec3(((float) a.getX()+b.getX())/2, ((float) a.getX()+b.getX())/2, ((float) a.getX()+b.getX())/2);
        }
        double x = last.xCoord;
        double y = last.yCoord;
        double z = last.zCoord;
        double d = x*x + y*y + z*z;
        double dist = Math.E/all - Math.cbrt(d);
        double factor = Math.sqrt(d) / dist;
        burrow = playerp.add(x/factor, y/factor, z/factor);
        if (messages) mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §r[" + burrow.getX() + "," + burrow.getY() + "," + burrow.getZ() + "] " + (int)Math.round(dist)));
    }

    /**
     * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
     * @author Quantizr
     */
    @SubscribeEvent
    void worldRender(RenderWorldLastEvent event) {
        if (!toggle || (!block &! beam &! text)) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player==null) return;
        if (guess &! burrow.equals(new BlockPos(0,0,0))) {
            Entity viewer = mc.getRenderViewEntity();
            Frustum frustum = new Frustum();
            frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
            double x = burrow.getX() - viewerX;
            double y = burrow.getY() - viewerY;
            double z = burrow.getZ() - viewerZ;
            double distSq = x*x + y*y + z*z;

            GlStateManager.disableDepth();
            GlStateManager.disableCull();
            if (block && frustum.isBoxInFrustum(burrow.getX(), burrow.getY(), burrow.getZ(), burrow.getX() + 1, burrow.getY() + 1, burrow.getZ() + 1))
                WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), Color.BLUE, 0.4f);
            GlStateManager.disableTexture2D();
            if (beam && distSq > 5*5) WaypointUtils.renderBeaconBeam(x, y + 1, z, Color.BLUE.getRGB(), 0.25f, event.partialTicks);
            if (text) WaypointUtils.renderWaypointText("§bGuess", burrow.up(), event.partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
        } //if (proximity)
    }

    static void resetData() {
        echo = false;
        burrow = new BlockPos(0,0,0);
        lastpitch = 0;
        pitch = new ArrayList<>();
        particles = new ArrayList<>();
        clicked = 0;
        playerp = new BlockPos(0,0,0);
    }

    @SubscribeEvent
    void worldUnload(WorldEvent.Unload event) {
        resetData();
    }
}