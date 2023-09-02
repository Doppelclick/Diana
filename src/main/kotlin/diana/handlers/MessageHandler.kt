package diana.handlers

import diana.Diana
import diana.Diana.Companion.mc
import diana.config.Config
import diana.core.Burrows
import diana.core.Warp
import diana.core.Waypoint
import diana.utils.Utils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3

object MessageHandler {
    val partySenderPattern = Regex("((Party > )?)((\\[\\S+]\\s)?)(?<sender>\\S+): ")
    val p1Pattern = Regex("^§\\S((\\[\\S+]\\s)?)(?<pm>\\S+) §r§ejoined the (party|dungeon group)") //§a[VIP] AA §r§ejoined the party.§r
    val p2Pattern = Regex("§\\S((\\[\\S+]\\s)?)(?<pm>\\S+) ((§r§ehas (been removed from the party | left the party)) | §r§ewas removed from your party because they disconnected | because they were offline)")
    val p3Pattern = Regex("^§eYou have joined §r§\\S((\\[\\S+]\\s)?)(?<pm>\\S+)'(s*) §r§eparty!") //§eYou have joined §r§b[MVP§r§d+§r§b] AAA's §r§eparty!§r
    val p4Pattern = Regex("^§eYou'll be partying with: (?<pm>.+)") //§eYou'll be partying with: §r§a[VIP] AA§r
    val p5Pattern = Regex("^The party was transferred to (?<pm>\\S+) because (?<pl>\\S+) left") //§eThe party was transferred to §r§7AAA §r§ebecause §r§b[MVP§r§d+§r§b] BBB §r§eleft§r
    val partyLeaderPattern = Regex("Party Leader: ((\\[\\S+]\\s)?)(?<pl>\\S+) ●") //§eParty Leader: §r§b[MVP§r§d+§r§b] AAA §r§a●§r
    val partyMembersPattern = Regex("Party Members: (?<pms>(((\\[\\S+]\\s)?)\\S+ ● )+)") //§eParty Members: §r§7AAA§r§a ● §r
    val burrowPattern = Regex("§r§eYou dug out a Griffin Burrow! §r§7\\((?<index>\\d)/4\\)") //§r§eYou dug out a Griffin Burrow! §r§7(1/4)§r§7
    val inquisPattern = Regex("\\[Diana] Inquis! \\[(?<one>(-?\\d{1,3})),(?<two>(-?\\d{1,3})),(?<three>(-?\\d{1,3}))] at ⏣")

    var receivedInquisFrom: ArrayList<String> = arrayListOf()
    var inParty = false
    var partyMembers: ArrayList<String> = arrayListOf()

    fun onChatmessage(event: S02PacketChat) {
        if (event.type.toInt() != 0) return
        val message = event.chatComponent.formattedText
        val unformatted = StringUtils.stripControlCodes(event.chatComponent.unformattedText)
        val sender = getSender(unformatted)

        if (message.contains("§r§9Party §8>") && sender != null) {
            if (!partyMembers.contains(sender) && (sender != mc.thePlayer?.name)) partyMembers.add(sender)
            inParty = true

        } else if (message.contains("You are not currently in a party.") || message.contains("You have been kicked from the party by") || message.contains("You left the party.") ||
            message.contains("The party was disbanded because all invites expired and the party was empty") || message.contains("§r§ehas disbanded the party!")) {
            partyMembers.clear()
            inParty = false
        }

        p1Pattern.find(message)?.let {
            it.groups["pm"]?.value?.run {
                partyMembers.add(this)
            }
            inParty = true
        } ?: p2Pattern.find(message)?.let {
            it.groups["pm"]?.value?.run {
                partyMembers.remove(this)
            }
        } ?: p3Pattern.find(message)?.let {
            partyMembers.clear()
            it.groups["pm"]?.value?.run {
                partyMembers.add(this)
            }
            inParty = true
        } ?: p4Pattern.find(message)?.let {
            it.groups["pm"]?.value?.replace("§\\S".toRegex(), "")?.split(", ")?.forEach {
                it.replace("(\\[\\S+]\\s)".toRegex(), "").run {
                    if (this != mc.thePlayer.name && !partyMembers.contains(this)) {
                        partyMembers.add(this)
                    }
                }
            }
            inParty = true
        } ?: p5Pattern.find(message)?.let {
            it.groups["pm"]?.value?.run {
                StringUtils.stripControlCodes(this.replace("(\\[\\S+]\\s)".toRegex(), "")).run {
                    if (!partyMembers.contains(this) && this != mc.thePlayer.name) {
                        partyMembers.add(this)
                    }
                }
            }
            it.groups["pl"]?.value?.run {
                StringUtils.stripControlCodes(this.replace("(\\[\\S+]\\s)".toRegex(), "")).run {
                    partyMembers.remove(this)
                }
            }
            inParty = true
        } ?: partyLeaderPattern.find(unformatted).takeIf { message.startsWith("§eParty Leader: ") }?.let {
            it.groups["pl"]?.value?.run {
                if (this != mc.thePlayer.name && !partyMembers.contains(this)) partyMembers.add(this)
            }
            inParty = true
        } ?: partyMembersPattern.find(unformatted).takeIf { message.startsWith("§eParty Members: ") }?.let {
            it.groups["pms"]?.value?.split(" ● ")?.forEach {
                it.replace("(\\[\\S+]\\s)".toRegex(), "").run {
                    if (this != mc.thePlayer.name && !partyMembers.contains(this)) {
                        partyMembers.add(this)
                    }
                }
            }
            inParty = true
        } ?: inquisPattern.find(unformatted).takeIf { sender != null && Config.receiveInq != 0 }?.let {
            if ((Config.receiveInq == 2 || (message.contains("§r§9Party §8>") || partyMembers.contains(sender))) && sender != mc.thePlayer.name && !Config.getIgnoreList().contains(sender?.lowercase())) {
                val pos = BlockPos(
                    it.groups["one"]?.value?.toIntOrNull() ?: return,
                    it.groups["two"]?.value?.toIntOrNull() ?: return,
                    it.groups["three"]?.value?.toIntOrNull() ?: return
                )
                Burrows.waypoints.add(Waypoint.InquisWaypoint(pos, sender!!, System.currentTimeMillis()))
                Utils.showClientTitle(
                    "",
                    "§c" + sender + " 's Inquis near " + Warp.closest(Vec3(pos), true)?.name
                )
                Utils.ping()
                val ignore = ChatComponentText("§c [Ignore this player] ")
                ignore.setChatStyle(
                    ignore.getChatStyle().setChatClickEvent(
                        ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/diana ignore add $sender"
                        )
                    ).setChatHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("ignore add $sender")
                        )
                    )
                )
                val component = ChatComponentText(Diana.chatTitle + "§cInquis Waypoint received§r from " + sender + " ")
                component.appendSibling(ignore)
                Utils.modMessage(component)
                receivedInquisFrom.add(sender)
            }
        } ?: let { burrowPattern.find(message)?.groups?.get("index")?.value?.toIntOrNull() ?: if (message.contains("§r§eYou finished the Griffin burrow chain! §r§7(4/4)§r")) 4 else null }?.let { index ->
                Render.resetRender()
                Burrows.arrowStart = null
                Burrows.arrowDir = null
                Burrows.particles = arrayListOf()
                Burrows.oldParticles.clear()
                Burrows.dugBurrows.forEach { pos ->
                    if (Burrows.burrow != null) {
                        if (Utils.maxDistance(Vec3(pos), Burrows.burrow!!) < 5) {
                            Burrows.burrow = null
                        }
                    }
                    if (Burrows.waypoints.removeIf { it.pos == pos }) {
                        Burrows.foundBurrows.add(pos)
                    }
                }
                Burrows.dugBurrows.clear()
                Burrows.lastDug = index % 4 + 1
                Burrows.arrow = index != 4
        } ?: run {
            if (message.contains("§r§cYou haven't unlocked this fast travel destination!§r") && (Warp.lastwarp != "undefined")) {
                if (Warp.lastwarp != "hub") {
                    Diana.warps.find { it.name == Warp.lastwarp }?.apply {
                        if (this.enabled()) {
                            Config.setWarp(Warp.lastwarp, false)
                        }
                    }
                }
                Warp.lastwarp = "undefined"
            } else if (message.contains("§7You were killed by ")) { //§r§c ☠ §r§7You were killed by §r§2Exalted Gaia Construct§r§7§r§7.
                Burrows.arrowStart = null
                Burrows.arrowDir = null
                Burrows.oldParticles.clear()
                Burrows.dugBurrows.forEach { pos ->
                    if (Burrows.burrow != null) {
                        if (Utils.maxDistance(Vec3(pos), Burrows.burrow!!) < 5) {
                            Burrows.burrow = null
                        }
                    }
                    if (Burrows.waypoints.removeIf { it.pos == pos }) {
                        Burrows.foundBurrows.add(pos)
                    }
                }
                Burrows.dugBurrows.clear()
            }
        }
    }

    private fun getSender(unformatted: String): String? =
        partySenderPattern.find(unformatted)?.let { it.groups["sender"]?.value }
}