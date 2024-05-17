package diana.config.json

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * The following code was taken and modified from CCBlueX - LiquidBounce under the GNU General Public License 3.0
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Exclude

class ExcludeStrategy : ExclusionStrategy {
    override fun shouldSkipClass(clazz: Class<*>?) = false
    override fun shouldSkipField(field: FieldAttributes) = field.getAnnotation(Exclude::class.java) != null
}