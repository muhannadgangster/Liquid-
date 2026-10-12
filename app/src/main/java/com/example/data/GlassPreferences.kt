package com.example.data

/**
 * Material presets for Liquid Glass appearance.
 */
enum class GlassMaterialPreset(
    val id: String,
    val displayName: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val refractionColorHex: Long
) {
    CYBER_IRIDESCENT(
        id = "cyber_iridescent",
        displayName = "Cyber Iridescent",
        description = "Fluid rainbow prism refraction with vibrant neon highlights",
        primaryColorHex = 0x334FD1C5, // teal-cyan
        secondaryColorHex = 0x337F9CF5, // indigo
        accentColorHex = 0xFF38BDF8, // sky blue
        refractionColorHex = 0x88EC4899 // pink sheen
    ),
    SAPPHIRE_CRYSTAL(
        id = "sapphire_crystal",
        displayName = "Sapphire Crystal",
        description = "Deep translucent oceanic blues with crisp diamond sheen",
        primaryColorHex = 0x2A1E3A8A, // deep blue
        secondaryColorHex = 0x2A3B82F6, // bright blue
        accentColorHex = 0xFF60A5FA, // light blue
        refractionColorHex = 0x6693C5FD // crystal edge
    ),
    MERCURY_METAL(
        id = "mercury_metal",
        displayName = "Mercury Liquid Metal",
        description = "Futuristic liquid chrome with sharp specular reflection",
        primaryColorHex = 0x3064748B, // slate
        secondaryColorHex = 0x3094A3B8, // silver
        accentColorHex = 0xFFE2E8F0, // platinum
        refractionColorHex = 0x77FFFFFF // specular highlight
    ),
    EMERALD_AURORA(
        id = "emerald_aurora",
        displayName = "Emerald Aurora",
        description = "Glowing northern lights with bioluminescent mint luster",
        primaryColorHex = 0x2A065F46, // forest green
        secondaryColorHex = 0x2A10B981, // emerald
        accentColorHex = 0xFF34D399, // mint
        refractionColorHex = 0x666EE7B7 // neon green
    ),
    AMETHYST_QUARTZ(
        id = "amethyst_quartz",
        displayName = "Amethyst Quartz",
        description = "Rich violet crystal with soft magenta refractions",
        primaryColorHex = 0x2A581C87, // deep purple
        secondaryColorHex = 0x2A9333EA, // purple
        accentColorHex = 0xFFC084FC, // violet
        refractionColorHex = 0x66F472B6 // rose edge
    );

    companion object {
        fun fromId(id: String): GlassMaterialPreset =
            entries.find { it.id == id } ?: CYBER_IRIDESCENT
    }
}

/**
 * Preferences model holding all configuration for the Liquid Glass engine.
 */
data class GlassPreferences(
    val masterEnabled: Boolean = true,
    val overlayEnabled: Boolean = false,
    val transparency: Float = 0.35f, // 0.1 (solid) to 0.85 (very transparent)
    val blurIntensity: Float = 0.70f, // 0.0 to 1.0
    val reflectionIntensity: Float = 0.80f, // 0.0 to 1.0
    val glowIntensity: Float = 0.65f, // 0.0 to 1.0
    val animationSpeed: Float = 1.0f, // 0.5x to 2.0x
    val glassThickness: Float = 3.0f, // 1.0 to 6.0 border/bevel depth
    val cornerRadius: Float = 24.0f, // 8.0 to 36.0 dp
    val themeMode: String = "SYSTEM", // "DARK", "LIGHT", "SYSTEM"
    val preset: GlassMaterialPreset = GlassMaterialPreset.CYBER_IRIDESCENT,
    val batterySaverMode: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val bubbleSizeDp: Int = 56, // 44 to 72 dp
    val edgeSnapping: Boolean = true,
    val perAppProfilesEnabled: Boolean = true,
    val showQuickToolsInOverlay: Boolean = true,
    val showQuickAppsInOverlay: Boolean = true
)

/**
 * Per-app custom visual profile.
 */
data class AppVisualProfile(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val customPreset: GlassMaterialPreset? = null,
    val customTransparency: Float? = null
)
