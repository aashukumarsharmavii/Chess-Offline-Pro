package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.chess.model.ChessColor
import com.example.chess.model.ChessPiece
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
    piece: ChessPiece,
    pieceTheme: PieceTheme,
    themeColors: BoardThemeColors,
    modifier: Modifier = Modifier
) {
    val pieceColor = if (piece.color == ChessColor.WHITE) themeColors.pieceWhite else themeColors.pieceBlack
    val strokeColor = if (piece.color == ChessColor.WHITE) themeColors.pieceWhiteStroke else themeColors.pieceBlackStroke

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val minDim = kotlin.math.min(w, h)

        when (pieceTheme) {
            PieceTheme.MINIMAL -> drawMinimalPiece(piece.type, pieceColor, strokeColor, minDim)
            PieceTheme.RETRO_PIXEL -> drawRetroPixelPiece(piece.type, pieceColor, minDim)
            PieceTheme.NEON -> drawNeonPiece(piece.type, pieceColor, minDim)
            PieceTheme.IMPERIAL -> drawImperialPiece(piece.type, pieceColor, strokeColor, minDim)
        }
    }
}

// 1. RETRO PIXEL PIECES (Hand-crafted 8x8 bitmap designs rendered dynamically)
private fun DrawScope.drawRetroPixelPiece(type: PieceType, color: Color, size: Float) {
    val pixelData = when (type) {
        PieceType.PAWN -> listOf(
            "....XX....",
            "....XX....",
            "...XXXX...",
            "...XXXX...",
            "....XX....",
            "...XXXX...",
            "..XXXXXX..",
            ".XXXXXXXX.",
            "XXXXXXXXXX",
            "XXXXXXXXXX"
        )
        PieceType.KNIGHT -> listOf(
            "....XXX...",
            "...XXXXX..",
            "..XX.XXXX.",
            ".XXXXXXXX.",
            ".XXX.XXXX.",
            "....XXXXX.",
            "....XXXX..",
            "...XXXXX..",
            "..XXXXXX..",
            "XXXXXXXXXX"
        )
        PieceType.BISHOP -> listOf(
            "....XX....",
            "...X..X...",
            "...XXXX...",
            "..XXXXXX..",
            "..XX.XX..",
            "...XXXX...",
            "...XXXX...",
            "..XXXXXX..",
            ".XXXXXXXX.",
            "XXXXXXXXXX"
        )
        PieceType.ROOK -> listOf(
            "..XX..XX..",
            ".XXXXXXXX.",
            ".XX.XX.XX.",
            ".XXXXXXXX.",
            "..XXXXXX..",
            "..XXXXXX..",
            "..XXXXXX..",
            ".XXXXXXXX.",
            "XXXXXXXXXX",
            "XXXXXXXXXX"
        )
        PieceType.QUEEN -> listOf(
            ".X..XX..X.",
            "XXXXXXXXXX",
            "XXXXXXXXXX",
            ".XXXXXXXX.",
            "..XXXXXX..",
            "..XXXXXX..",
            ".XXXXXXXX.",
            ".XXXXXXXX.",
            "XXXXXXXXXX",
            "XXXXXXXXXX"
        )
        PieceType.KING -> listOf(
            "....XX....",
            "..XXXXXX..",
            "....XX....",
            "...XXXX...",
            "..XXXXXX..",
            ".XXXXXXXX.",
            ".XX.XX.XX.",
            ".XXXXXXXX.",
            "XXXXXXXXXX",
            "XXXXXXXXXX"
        )
    }

    val rows = pixelData.size
    val cols = pixelData[0].length
    val pixelSizeW = size * 0.7f / cols
    val pixelSizeH = size * 0.7f / rows
    val offsetX = (size - cols * pixelSizeW) / 2
    val offsetY = (size - rows * pixelSizeH) / 2

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (pixelData[r][c] == 'X') {
                drawRect(
                    color = color,
                    topLeft = Offset(offsetX + c * pixelSizeW, offsetY + r * pixelSizeH),
                    size = Size(pixelSizeW + 0.5f, pixelSizeH + 0.5f) // overlapping subpixel to avoid grid gaps
                )
            }
        }
    }
}

// 2. MINIMAL PIECES (Geometric, ultra-modern designer style)
private fun DrawScope.drawMinimalPiece(type: PieceType, color: Color, strokeColor: Color, size: Float) {
    val center = Offset(size / 2f, size / 2f)
    val rBase = size * 0.15f
    val baseWidth = size * 0.55f
    val baseHeight = size * 0.08f

    // Draw solid geometric shapes
    when (type) {
        PieceType.PAWN -> {
            // Circle and base line
            drawCircle(
                color = color,
                radius = size * 0.18f,
                center = center.copy(y = size * 0.42f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PieceType.KNIGHT -> {
            // Triangle head and neck
            val path = Path().apply {
                moveTo(size * 0.25f, size * 0.75f)
                lineTo(size * 0.25f, size * 0.35f)
                lineTo(size * 0.45f, size * 0.25f)
                lineTo(size * 0.75f, size * 0.45f)
                lineTo(size * 0.50f, size * 0.55f)
                lineTo(size * 0.70f, size * 0.75f)
                close()
            }
            drawPath(path = path, color = color)
            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PieceType.BISHOP -> {
            // Ellipse cut
            drawOval(
                color = color,
                topLeft = Offset(size * 0.36f, size * 0.25f),
                size = Size(size * 0.28f, size * 0.48f)
            )
            // Slash slit
            val slitPath = Path().apply {
                moveTo(size * 0.32f, size * 0.35f)
                lineTo(size * 0.68f, size * 0.48f)
            }
            drawPath(path = slitPath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))
            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PieceType.ROOK -> {
            // Solid column with castle top
            drawRect(
                color = color,
                topLeft = Offset(size * 0.32f, size * 0.32f),
                size = Size(size * 0.36f, size * 0.43f)
            )
            // Castle crenellations
            drawRect(color = strokeColor, topLeft = Offset(size * 0.41f, size * 0.32f), size = Size(size * 0.06f, size * 0.12f))
            drawRect(color = strokeColor, topLeft = Offset(size * 0.53f, size * 0.32f), size = Size(size * 0.06f, size * 0.12f))
            
            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PieceType.QUEEN -> {
            // Crown shape
            val path = Path().apply {
                moveTo(size * 0.22f, size * 0.75f)
                lineTo(size * 0.18f, size * 0.30f)
                lineTo(size * 0.38f, size * 0.48f)
                lineTo(size * 0.50f, size * 0.25f)
                lineTo(size * 0.62f, size * 0.48f)
                lineTo(size * 0.82f, size * 0.30f)
                lineTo(size * 0.78f, size * 0.75f)
                close()
            }
            drawPath(path = path, color = color)
            
            // Crown dots
            drawCircle(color = color, radius = size * 0.04f, center = Offset(size * 0.18f, size * 0.28f))
            drawCircle(color = color, radius = size * 0.04f, center = Offset(size * 0.50f, size * 0.23f))
            drawCircle(color = color, radius = size * 0.04f, center = Offset(size * 0.82f, size * 0.28f))

            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PieceType.KING -> {
            // Sturdy miter with distinct cross top
            drawOval(
                color = color,
                topLeft = Offset(size * 0.33f, size * 0.32f),
                size = Size(size * 0.34f, size * 0.43f)
            )
            // Cross top
            val crossW = size * 0.06f
            val crossH = size * 0.18f
            drawRect(color = color, topLeft = Offset((size - crossW) / 2, size * 0.16f), size = Size(crossW, crossH))
            drawRect(color = color, topLeft = Offset((size - crossH) / 2, size * 0.22f), size = Size(crossH, crossW))

            drawRoundRect(
                color = color,
                topLeft = Offset((size - baseWidth) / 2, size * 0.75f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

// 3. NEON GLOW PIECES (Hollow illuminated neon lines)
private fun DrawScope.drawNeonPiece(type: PieceType, color: Color, size: Float) {
    val strokeWidth = 2.5f.dp.toPx()
    val glowWidth = 6f.dp.toPx()
    val glowColor = color.copy(alpha = 0.3f)

    // Helper to draw both outline stroke and soft background neon glow
    fun drawNeonPath(path: Path) {
        drawPath(path = path, color = glowColor, style = Stroke(width = glowWidth))
        drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
    }

    val baseWidth = size * 0.58f
    val baseRow = size * 0.75f

    when (type) {
        PieceType.PAWN -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.62f, size * 0.62f)
                quadraticTo(size * 0.65f, size * 0.50f, size * 0.58f, size * 0.46f)
                
                // Head sphere
                addOval(Size(size * 0.26f, size * 0.26f).let { s -> 
                    androidx.compose.ui.geometry.Rect(Offset((size - s.width) / 2, size * 0.20f), s) 
                })
                
                moveTo(size * 0.42f, size * 0.46f)
                quadraticTo(size * 0.35f, size * 0.50f, size * 0.38f, size * 0.62f)
                lineTo((size - baseWidth) / 2, baseRow)
                close()
            }
            drawNeonPath(path)
        }
        PieceType.KNIGHT -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.68f, size * 0.64f)
                quadraticTo(size * 0.72f, size * 0.45f, size * 0.60f, size * 0.32f)
                lineTo(size * 0.54f, size * 0.22f) // Ears
                lineTo(size * 0.48f, size * 0.28f)
                lineTo(size * 0.30f, size * 0.34f) // Snout
                quadraticTo(size * 0.25f, size * 0.42f, size * 0.38f, size * 0.46f)
                quadraticTo(size * 0.34f, size * 0.58f, size * 0.32f, size * 0.64f)
                close()
            }
            drawNeonPath(path)
        }
        PieceType.BISHOP -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.64f, size * 0.66f)
                quadraticTo(size * 0.70f, size * 0.44f, size * 0.50f, size * 0.26f) // Oval head top
                quadraticTo(size * 0.30f, size * 0.44f, size * 0.36f, size * 0.66f)
                close()
            }
            drawNeonPath(path)
            
            // Draw cross slash line inside bishop head
            val slash = Path().apply {
                moveTo(size * 0.42f, size * 0.38f)
                lineTo(size * 0.58f, size * 0.48f)
            }
            drawNeonPath(slash)
            
            // Bishop top small bead
            val bead = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(Offset(size * 0.47f, size * 0.20f), Size(size * 0.06f, size * 0.06f)))
            }
            drawNeonPath(bead)
        }
        PieceType.ROOK -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.66f, size * 0.66f)
                lineTo(size * 0.66f, size * 0.40f)
                lineTo(size * 0.74f, size * 0.40f) // Outer crown right
                lineTo(size * 0.74f, size * 0.26f)
                lineTo(size * 0.62f, size * 0.26f)
                lineTo(size * 0.62f, size * 0.34f)
                lineTo(size * 0.54f, size * 0.34f) // Center slit
                lineTo(size * 0.54f, size * 0.26f)
                lineTo(size * 0.46f, size * 0.26f)
                lineTo(size * 0.46f, size * 0.34f)
                lineTo(size * 0.38f, size * 0.34f)
                lineTo(size * 0.38f, size * 0.26f) // Left crown outer
                lineTo(size * 0.26f, size * 0.26f)
                lineTo(size * 0.26f, size * 0.40f)
                lineTo(size * 0.34f, size * 0.40f)
                lineTo(size * 0.34f, size * 0.66f)
                close()
            }
            drawNeonPath(path)
        }
        PieceType.QUEEN -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.66f, size * 0.66f)
                lineTo(size * 0.80f, size * 0.32f) // Far right crown spike
                lineTo(size * 0.62f, size * 0.50f)
                lineTo(size * 0.50f, size * 0.22f) // Center crown spike
                lineTo(size * 0.38f, size * 0.50f)
                lineTo(size * 0.20f, size * 0.32f) // Far left crown spike
                lineTo(size * 0.34f, size * 0.66f)
                close()
            }
            drawNeonPath(path)
            
            // Mini neon crown dots
            listOf(Offset(size * 0.20f, size * 0.30f), Offset(size * 0.50f, size * 0.20f), Offset(size * 0.80f, size * 0.30f)).forEach { pt ->
                val dot = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(pt - Offset(size * 0.02f, size * 0.02f), Size(size * 0.04f, size * 0.04f)))
                }
                drawNeonPath(dot)
            }
        }
        PieceType.KING -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                lineTo(size * 0.66f, size * 0.66f)
                quadraticTo(size * 0.72f, size * 0.44f, size * 0.62f, size * 0.35f)
                lineTo(size * 0.38f, size * 0.35f)
                quadraticTo(size * 0.28f, size * 0.44f, size * 0.34f, size * 0.66f)
                close()
            }
            drawNeonPath(path)
            
            // Prominent Cross on Top
            val cross = Path().apply {
                moveTo(size * 0.50f, size * 0.16f)
                lineTo(size * 0.50f, size * 0.32f)
                moveTo(size * 0.42f, size * 0.22f)
                lineTo(size * 0.58f, size * 0.22f)
            }
            drawNeonPath(cross)
        }
    }
}

// 4. IMPERIAL PIECES (Stately classic vector silhouettes with clean contours)
private fun DrawScope.drawImperialPiece(type: PieceType, color: Color, strokeColor: Color, size: Float) {
    val drawStyle = Fill
    val strokeWidth = 1.5f.dp.toPx()

    // Helper to draw filled contour with a clean outer stroke
    fun drawContouredShape(path: Path) {
        drawPath(path = path, color = color, style = drawStyle)
        drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
    }

    val baseWidth = size * 0.60f
    val baseRow = size * 0.75f

    when (type) {
        PieceType.PAWN -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.75f, size * 0.71f, size * 0.62f, size * 0.68f)
                quadraticTo(size * 0.60f, size * 0.48f, size * 0.56f, size * 0.45f)
                
                // Head ball
                addOval(Size(size * 0.28f, size * 0.28f).let { s -> 
                    androidx.compose.ui.geometry.Rect(Offset((size - s.width) / 2, size * 0.18f), s) 
                })
                
                moveTo(size * 0.44f, size * 0.45f)
                quadraticTo(size * 0.40f, size * 0.48f, size * 0.38f, size * 0.68f)
                quadraticTo(size * 0.25f, size * 0.71f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)
            
            // Decorative line on Pawn collar
            drawLine(strokeColor, Offset(size * 0.39f, size * 0.48f), Offset(size * 0.61f, size * 0.48f), strokeWidth)
        }
        PieceType.KNIGHT -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.72f, size * 0.72f, size * 0.66f, size * 0.62f)
                quadraticTo(size * 0.74f, size * 0.42f, size * 0.62f, size * 0.28f)
                quadraticTo(size * 0.58f, size * 0.20f, size * 0.54f, size * 0.22f) // Ear top
                lineTo(size * 0.48f, size * 0.28f)
                quadraticTo(size * 0.38f, size * 0.30f, size * 0.25f, size * 0.35f) // Mane to snout
                quadraticTo(size * 0.20f, size * 0.45f, size * 0.32f, size * 0.48f) // Mouth
                quadraticTo(size * 0.38f, size * 0.54f, size * 0.34f, size * 0.62f)
                quadraticTo(size * 0.28f, size * 0.72f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)
            
            // Eye dot
            drawCircle(color = strokeColor, radius = size * 0.025f, center = Offset(size * 0.42f, size * 0.36f))
        }
        PieceType.BISHOP -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.75f, size * 0.72f, size * 0.62f, size * 0.66f)
                quadraticTo(size * 0.68f, size * 0.42f, size * 0.50f, size * 0.26f) // Bishop miter dome
                quadraticTo(size * 0.32f, size * 0.42f, size * 0.38f, size * 0.66f)
                quadraticTo(size * 0.25f, size * 0.72f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)

            // Draw classic cross slit of a Bishop
            drawLine(strokeColor, Offset(size * 0.50f, size * 0.36f), Offset(size * 0.50f, size * 0.54f), strokeWidth)
            drawLine(strokeColor, Offset(size * 0.44f, size * 0.42f), Offset(size * 0.56f, size * 0.42f), strokeWidth)

            // Small bead on top of Bishop crown
            drawCircle(color = strokeColor, radius = size * 0.03f, center = Offset(size * 0.50f, size * 0.24f))
            drawCircle(color = color, radius = size * 0.025f, center = Offset(size * 0.50f, size * 0.24f))
        }
        PieceType.ROOK -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.73f, size * 0.71f, size * 0.64f, size * 0.66f)
                lineTo(size * 0.64f, size * 0.38f)
                lineTo(size * 0.70f, size * 0.38f)
                lineTo(size * 0.70f, size * 0.24f) // Castle Right Spire
                lineTo(size * 0.58f, size * 0.24f)
                lineTo(size * 0.58f, size * 0.30f)
                lineTo(size * 0.52f, size * 0.30f) // Crest notch
                lineTo(size * 0.52f, size * 0.24f)
                lineTo(size * 0.48f, size * 0.24f)
                lineTo(size * 0.48f, size * 0.30f)
                lineTo(size * 0.42f, size * 0.30f)
                lineTo(size * 0.42f, size * 0.24f) // Left Spire
                lineTo(size * 0.30f, size * 0.24f)
                lineTo(size * 0.30f, size * 0.38f)
                lineTo(size * 0.36f, size * 0.38f)
                lineTo(size * 0.36f, size * 0.66f)
                quadraticTo(size * 0.27f, size * 0.71f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)
            
            // Horizontal brick line
            drawLine(strokeColor, Offset(size * 0.36f, size * 0.46f), Offset(size * 0.64f, size * 0.46f), strokeWidth)
        }
        PieceType.QUEEN -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.72f, size * 0.71f, size * 0.64f, size * 0.66f)
                lineTo(size * 0.74f, size * 0.32f) // Spikes start
                lineTo(size * 0.60f, size * 0.48f)
                lineTo(size * 0.50f, size * 0.22f) // Center crown spike
                lineTo(size * 0.40f, size * 0.48f)
                lineTo(size * 0.26f, size * 0.32f) // Left spike
                lineTo(size * 0.36f, size * 0.66f)
                quadraticTo(size * 0.28f, size * 0.71f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)
            
            // Queen collar bands
            drawLine(strokeColor, Offset(size * 0.37f, size * 0.58f), Offset(size * 0.63f, size * 0.58f), strokeWidth)

            // Little balls on the three main tips of the Queen's crown
            listOf(Offset(size * 0.26f, size * 0.32f), Offset(size * 0.50f, size * 0.22f), Offset(size * 0.74f, size * 0.32f)).forEach { pt ->
                drawCircle(color = strokeColor, radius = size * 0.025f, center = pt)
                drawCircle(color = color, radius = size * 0.015f, center = pt)
            }
        }
        PieceType.KING -> {
            val path = Path().apply {
                moveTo((size - baseWidth) / 2, baseRow)
                lineTo((size + baseWidth) / 2, baseRow)
                quadraticTo(size * 0.72f, size * 0.72f, size * 0.64f, size * 0.66f)
                quadraticTo(size * 0.72f, size * 0.42f, size * 0.60f, size * 0.34f) // King crown body
                lineTo(size * 0.40f, size * 0.34f)
                quadraticTo(size * 0.28f, size * 0.42f, size * 0.36f, size * 0.66f)
                quadraticTo(size * 0.28f, size * 0.72f, (size - baseWidth) / 2, baseRow)
                close()
            }
            drawContouredShape(path)

            // Prominent Cross on top of crown
            val cross = Path().apply {
                moveTo(size * 0.50f, size * 0.15f)
                lineTo(size * 0.50f, size * 0.32f)
                moveTo(size * 0.41f, size * 0.21f)
                lineTo(size * 0.59f, size * 0.21f)
            }
            drawContouredShape(cross)

            // Crown band accent
            drawLine(strokeColor, Offset(size * 0.40f, size * 0.44f), Offset(size * 0.60f, size * 0.44f), strokeWidth)
        }
    }
}
