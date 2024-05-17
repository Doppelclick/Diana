package diana.config.categories

import diana.config.Category

object CategoryDebug : Category("Debug") {
    var forceHub by boolean("Force Hub", false).doNotInclude()
}