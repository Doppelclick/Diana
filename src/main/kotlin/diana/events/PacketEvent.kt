package diana.events

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Event

open class PacketEvent(val packet: Packet<*>) : Event() {

    class Inbound(packet: Packet<*>) : PacketEvent(packet)

    class Outbound(packet: Packet<*>) : PacketEvent(packet)
}