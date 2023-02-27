package com.Diana.mod.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
 * @author Quantizr
 */
@Cancelable
public class PacketEvent extends Event {
    /**
     * Taken from Skytils under the GNU Affero General Public License v3.0
     * https://github.com/Skytils/SkytilsMod/blob/0.x/LICENSE
     * @author My-Name-Is-Jeff (lily)
     */

     public Direction direction;
     public net.minecraft.network.Packet<?> packet;

     public PacketEvent(net.minecraft.network.Packet<?> packet) {
         this.packet = packet;
     }

     public static class ReceiveEvent extends PacketEvent {
         public ReceiveEvent(net.minecraft.network.Packet<?> packet) {
             super(packet);
             this.direction = Direction.INBOUND;
         }
     }

     public static class SendEvent extends PacketEvent {
         public SendEvent(net.minecraft.network.Packet<?> packet) {
             super(packet);
             this.direction = Direction.OUTBOUND;
         }
     }

     enum Direction {
         INBOUND,
         OUTBOUND
     }
}
