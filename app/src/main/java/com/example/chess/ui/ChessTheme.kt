package com.example.chess.ui

import androidx.compose.ui.graphics.Color

enum class BoardTheme {
    CLASSIC_WOOD,
    FOREST_MOSS,
    CYBER_NEON,
    SUNSET_GOLD,
    ARCTIC_WINTER;

    val displayName: String
        get() = when (this) {
            CLASSIC_WOOD -> "Classic Wood"
            FOREST_MOSS -> "Forest Moss"
            CYBER_NEON -> "Cyber Neon"
            SUNSET_GOLD -> "Sunset Gold"
            ARCTIC_WINTER -> "Arctic Winter"
        }
}

enum class PieceTheme {
    IMPERIAL,    // High-fidelity stylized vector silhouettes
    MINIMAL,     // Sleek geometric shapes
    NEON,        // Glowing futuristic hollow designs
    RETRO_PIXEL; // Authentic 8-bit retro pixel icons

    val displayName: String
        get() = when (this) {
            IMPERIAL -> "Imperial Classic"
            MINIMAL -> "Minimalist"
            NEON -> "Neon Glow"
            RETRO_PIXEL -> "8-Bit Retro"
        }
}

data class BoardThemeColors(
    val lightSquare: Color,
    val darkSquare: Color,
    val selectedSquare: Color,
    val validMoveDot: Color,
    val validMoveCapture: Color,
    val checkSquare: Color,
    val lastMoveHighlight: Color,
    val boardBorder: Color,
    val boardBackground: Color,
    val appBackground: Color,
    val cardBackground: Color,
    val textColor: Color,
    val textMuted: Color,
    val pieceWhite: Color,
    val pieceWhiteStroke: Color,
    val pieceBlack: Color,
    val pieceBlackStroke: Color
)

object ChessThemeManager {
    fun getColors(theme: BoardTheme): BoardThemeColors {
        return when (theme) {
            BoardTheme.CLASSIC_WOOD -> BoardThemeColors(
                lightSquare = Color(0xFFF1E4C3), // Soft birch cream
                darkSquare = Color(0xFF594545),  // Dark mahogany wood
                selectedSquare = Color(0x80C29B70), // Translucent caramel
                validMoveDot = Color(0x99815B5B), // Muted dark brown dot
                validMoveCapture = Color(0x66E76F51), // Reddish soft indicator
                checkSquare = Color(0x80D90429), // Bright red highlight
                lastMoveHighlight = Color(0x60F4A261), // Warm amber hue
                boardBorder = Color(0xFF3B2E2E),
                boardBackground = Color(0xFF2C2222),
                appBackground = Color(0xFF1C1414), // Dark espresso
                cardBackground = Color(0xFF281D1D),
                textColor = Color(0xFFF1E4C3),
                textMuted = Color(0xFF9F8383),
                pieceWhite = Color(0xFFFFFFFF),
                pieceWhiteStroke = Color(0xFF2C2222),
                pieceBlack = Color(0xFF1E1E1E),
                pieceBlackStroke = Color(0xFF9F8383)
            )
            BoardTheme.FOREST_MOSS -> BoardThemeColors(
                lightSquare = Color(0xFFE2E7DE), // Sand/Birch
                darkSquare = Color(0xFF4A6B53),  // Warm moss green
                selectedSquare = Color(0x80A3B19B), // Pale sage highlight
                validMoveDot = Color(0x992D4A36), // Deep green indicator
                validMoveCapture = Color(0x66D88A8A), // Soft rose capture
                checkSquare = Color(0x80E63946),
                lastMoveHighlight = Color(0x60E9C46A),
                boardBorder = Color(0xFF2D3E32),
                boardBackground = Color(0xFF1E2A21),
                appBackground = Color(0xFF111713), // Forest depth
                cardBackground = Color(0xFF19221C),
                textColor = Color(0xFFE2E7DE),
                textMuted = Color(0xFF8AA393),
                pieceWhite = Color(0xFFF4F6F0),
                pieceWhiteStroke = Color(0xFF1E2A21),
                pieceBlack = Color(0xFF242E28),
                pieceBlackStroke = Color(0xFFD4DAD0)
            )
            BoardTheme.CYBER_NEON -> BoardThemeColors(
                lightSquare = Color(0xFF1E293B), // Slate Grey
                darkSquare = Color(0xFF0F172A),  // Dark Ink
                selectedSquare = Color(0x6038BDF8), // Cyber Cyan glow
                validMoveDot = Color(0xFF06B6D4), // Cyan active dots
                validMoveCapture = Color(0xFFF43F5E), // Intense hot pink capture
                checkSquare = Color(0xCCEF4444), // Cyber Red check
                lastMoveHighlight = Color(0x40A855F7), // Cyber Purple trail
                boardBorder = Color(0xFF334155),
                boardBackground = Color(0xFF0B0F19),
                appBackground = Color(0xFF030712), // Deep abyss black
                cardBackground = Color(0xFF111827),
                textColor = Color(0xFFF3F4F6),
                textMuted = Color(0xFF9CA3AF),
                pieceWhite = Color(0xFF38BDF8), // Electric Cyan
                pieceWhiteStroke = Color(0xFF0284C7),
                pieceBlack = Color(0xFFEC4899), // Neon Pink
                pieceBlackStroke = Color(0xFFBE185D)
            )
            BoardTheme.SUNSET_GOLD -> BoardThemeColors(
                lightSquare = Color(0xFFFDE1D3), // Sunlit cream
                darkSquare = Color(0xFF9E2A2B),  // Rust/Cinnabar
                selectedSquare = Color(0x80F4A261), // Warm amber highlight
                validMoveDot = Color(0xFFE76F51),
                validMoveCapture = Color(0xFF2A9D8F), // Turquoise capture contrast
                checkSquare = Color(0xCCE63946),
                lastMoveHighlight = Color(0x70E9C46A), // Bright golden trail
                boardBorder = Color(0xFF6E1B1C),
                boardBackground = Color(0xFF541212),
                appBackground = Color(0xFF270606), // Wine/crimson shadow
                cardBackground = Color(0xFF380C0C),
                textColor = Color(0xFFFDE1D3),
                textMuted = Color(0xFFCD968D),
                pieceWhite = Color(0xFFFFF7ED),
                pieceWhiteStroke = Color(0xFF7C2D12),
                pieceBlack = Color(0xFF200A05),
                pieceBlackStroke = Color(0xFFF4A261)
            )
            BoardTheme.ARCTIC_WINTER -> BoardThemeColors(
                lightSquare = Color(0xFFE0F2FE), // Glacier ice blue
                darkSquare = Color(0xFF0F4C81),  // Polar deep blue
                selectedSquare = Color(0x807DD3FC), // Sky blue selection
                validMoveDot = Color(0xFF38BDF8),
                validMoveCapture = Color(0xFFFB7185), // Soft coral capture
                checkSquare = Color(0xCCEF4444),
                lastMoveHighlight = Color(0x50F59E0B),
                boardBorder = Color(0xFF0C4A6E),
                boardBackground = Color(0xFF07263E),
                appBackground = Color(0xFF03101C), // Deep frozen ocean
                cardBackground = Color(0xFF081C2E),
                textColor = Color(0xFFE0F2FE),
                textMuted = Color(0xFF6793B7),
                pieceWhite = Color(0xFFF0F9FF),
                pieceWhiteStroke = Color(0xFF0F4C81),
                pieceBlack = Color(0xFF0F172A),
                pieceBlackStroke = Color(0xFF93C5FD)
            )
        }
    }
}
