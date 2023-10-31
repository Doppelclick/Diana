package diana.forge

import diana.Diana.Companion.modID
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@MCVersion(ForgeVersion.mcVersion)
class DianaLoadingPlugin: IFMLLoadingPlugin {
    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.$modID.json")
        MixinEnvironment.getCurrentEnvironment().obfuscationContext = "searge"
    }

    override fun getASMTransformerClass(): Array<String> = emptyArray()

    override fun getModContainerClass(): String? = null

    override fun getSetupClass(): String? = null

    override fun injectData(data: MutableMap<String, Any>?) {}

    override fun getAccessTransformerClass(): String? = null
}