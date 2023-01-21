package ol.core

import zelaux.arclib.settings.*

object SettingsManager {
    @JvmField
    val uiRechallenged = BooleanSettingKey("ui-rechallenged") { false }

    @JvmField
    val show = BooleanSettingKey("mod.ol.show") { false }

    @JvmField
    val check = BooleanSettingKey("mod.ol.check") { false }

    @JvmField
    val pressureUpdate = IntSettingKey("mod.ol.pressureupdate") { 0 }

    @JvmField
    val debug = BooleanSettingKey("mod.ol.debug") { false }

    init {
        SettingKey.allKeys.each { it.setDefault() }
    }
}