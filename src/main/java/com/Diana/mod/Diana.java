package com.Diana.mod;

import com.Diana.mod.commands.DianaCommand;
import com.Diana.mod.config.config;
import com.Diana.mod.events.PacketEvent;
import com.Diana.mod.utils.WaypointUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Mod(modid = Diana.Name, version = Diana.V)
public class Diana {
    public static final String Name = "Diana";
    public static final String V = "0.1";
    public static boolean toggle = false;
    public static boolean guess = false;
    public static boolean proximity = false;
    public static boolean block = false;
    public static boolean beam = true;
    public static boolean text = true;

    static boolean echo = false;
    static float lastpitch = 0;
    static List<Float> pitch = new ArrayList<>();
    static BlockPos playerp = new BlockPos(0,0,0);
    static List<BlockPos> particles = new ArrayList<>();
    static long clicked = 0;
    static BlockPos burrow = new BlockPos(0,0,0);


    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
        //configDir = event.getModConfigurationDirectory().toString();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Diana());
        config.cfgreload();
    }

    @SubscribeEvent
    void onInteract(PlayerInteractEvent event) {
        if (!toggle) return;
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && guess) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            if (player!=null && System.currentTimeMillis()>clicked + 2000) {
                if (player.getHeldItem() != null) {
                    if (player.getHeldItem().getDisplayName().toLowerCase().contains("ancestral spade")) {
                        burrow = new BlockPos(0,0,0);
                        lastpitch = 0;
                        pitch = new ArrayList<>();
                        particles = new ArrayList<>();
                        echo = true;
                        clicked = System.currentTimeMillis();
                        playerp = new BlockPos(player.getPositionVector().xCoord, player.getPositionVector().yCoord+1, player.getPositionVector().zCoord);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    void onTick(TickEvent event) {
        if (event.phase!= TickEvent.Phase.START |! toggle) return;
        if ((System.currentTimeMillis() > clicked + 2000 || (particles.size()==13 && pitch.size()==13)) && guess && echo) {
            echo = false;
            calcBurrow();
        }
    }

    @SubscribeEvent
    void onSound(PlaySoundEvent event) {
        if (!toggle) return;
        if (event.name.equals("note.harp") && guess && echo) {
            if (lastpitch!=0) pitch.add(event.sound.getPitch()-lastpitch);
            lastpitch=event.sound.getPitch();
        }
    }

    @SubscribeEvent
    void onPacket(PacketEvent.ReceiveEvent event) {
        if (!toggle) return;
        if (event.packet instanceof S2APacketParticles && guess && echo) {
            S2APacketParticles particle = (S2APacketParticles) event.packet;
            if (particle.getParticleType() == EnumParticleTypes.FIREWORKS_SPARK && particle.getParticleSpeed()==0) {
                particles.add(new BlockPos(particle.getXCoordinate()-playerp.getX(), particle.getYCoordinate()-playerp.getY(), particle.getZCoordinate()-playerp.getZ()));
            }
        }
    }

    void calcBurrow() {
        if (pitch.size()<2 || particles.size()<13) return;
        float all = 0;
        for (float i : pitch) all+=i;
        all /= pitch.size();
        BlockPos last = particles.get(particles.size()-1);
        double dist = Math.E/all - Math.cbrt(last.getX()*last.getX() + last.getY()*last.getY() + last.getZ()*last.getZ());
        double factor = Math.sqrt(last.getX()*last.getX() + last.getY()*last.getY() + last.getZ()*last.getZ()) / dist;
        burrow = playerp.add(new Vec3i(last.getX()/factor, last.getY()/factor, last.getZ()/factor));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §r" + burrow + " " + dist));
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!toggle || (!block &! beam &! text)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player==null) return;
        if (guess &! burrow.equals(new BlockPos(0,0,0))) {
            Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
            Frustum frustum = new Frustum();
            frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
            if (frustum.isBoxInFrustum(burrow.getX(), burrow.getY(), burrow.getZ(), burrow.getX() + 1, 255, burrow.getZ() + 1))
                return;
            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
            double x = burrow.getX() - viewerX;
            double y = burrow.getY() - viewerY;
            double z = burrow.getZ() - viewerZ;
            if (block) WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), Color.BLUE, 0.4f);
            if (beam) WaypointUtils.renderBeaconBeam(x, y + 1, z, Color.BLUE.getRGB(), 0.25f, event.partialTicks);
            if (text) WaypointUtils.renderWaypointText("Guess\n"+
                   player.getDistanceSq(burrow), burrow.up(), event.partialTicks);
        } //else if (proximity)
    }

    @SubscribeEvent
    void worldUnload(WorldEvent.Unload event) {
        echo = false;
        lastpitch=0;
        pitch = new ArrayList<>();
        playerp = new BlockPos(0,0,0);
        particles = new ArrayList<>();
        clicked = 0;
        burrow = new BlockPos(0,0,0);
    }
}