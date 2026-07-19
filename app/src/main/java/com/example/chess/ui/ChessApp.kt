package com.example.chess.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.ai.AIDifficulty
import com.example.chess.model.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChessApp(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = ChessThemeManager.getColors(uiState.boardTheme)
    
    // Manage whether we are in the main menu or active gameplay screen
    var showMenu by remember { mutableStateOf(true) }
    var showThemesDrawer by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.appBackground
    ) {
        AnimatedContent(
            targetState = showMenu,
            transitionSpec = {
                slideInHorizontally(animationSpec = tween(350)) { -it } togetherWith
                        slideOutHorizontally(animationSpec = tween(350)) { it }
            },
            label = "ScreenTransition"
        ) { isMenu ->
            if (isMenu) {
                ChessMenuScreen(
                    uiState = uiState,
                    colors = colors,
                    onStartGame = { isAi, diff ->
                        viewModel.startNewGame(isAi, diff)
                        showMenu = false
                    },
                    onChangeBoardTheme = viewModel::changeBoardTheme,
                    onChangePieceTheme = viewModel::changePieceTheme
                )
            } else {
                ChessGameScreen(
                    uiState = uiState,
                    colors = colors,
                    onBackToMenu = { showMenu = true },
                    onSelectSquare = viewModel::selectSquare,
                    onUndo = viewModel::undoLastMove,
                    onRestart = { viewModel.startNewGame(uiState.isAiGame, uiState.aiDifficulty) },
                    onToggleAutoRotate = viewModel::toggleAutoRotate,
                    onChangeBoardTheme = viewModel::changeBoardTheme,
                    onChangePieceTheme = viewModel::changePieceTheme,
                    onPromote = viewModel::promotePawn,
                    onDismissPromotion = viewModel::dismissPromotion,
                    showThemesDrawer = showThemesDrawer,
                    onToggleThemes = { showThemesDrawer = !showThemesDrawer }
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// MENU / PLAY CONFIGURATION SCREEN
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChessMenuScreen(
    uiState: ChessUiState,
    colors: BoardThemeColors,
    onStartGame: (isAi: Boolean, difficulty: AIDifficulty) -> Unit,
    onChangeBoardTheme: (BoardTheme) -> Unit,
    onChangePieceTheme: (PieceTheme) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Title Card with modern minimalist look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.cardBackground, colors.boardBackground)
                        )
                    )
                    .border(1.dp, colors.boardBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(vertical = 36.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Custom drawn decorative Chess King silhouette logo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(colors.lightSquare, CircleShape)
                            .border(2.dp, colors.boardBorder, CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessPieceView(
                            piece = ChessPiece(PieceType.KING, ChessColor.BLACK),
                            pieceTheme = uiState.pieceTheme,
                            themeColors = colors
                        )
                    }

                    Text(
                        text = "CHESS OFFLINE",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textColor,
                            letterSpacing = 2.sp
                        )
                    )

                    Text(
                        text = "Sleek tactical pass & play or AI matches",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Mode Selection Section
            Text(
                text = "Select Game Mode",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            // Mode 1: AI Opponent Cards
            var selectedModeIsAi by remember { mutableStateOf(true) }
            var selectedAiDifficulty by remember { mutableStateOf(AIDifficulty.NEUTRAL) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI Opponent Toggle Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_ai_button")
                        .clickable { selectedModeIsAi = true },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedModeIsAi) colors.lightSquare else colors.cardBackground
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedModeIsAi) colors.textColor else colors.boardBorder.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Computer AI Opponent",
                            tint = if (selectedModeIsAi) colors.darkSquare else colors.textColor
                        )
                        Text(
                            text = "VS Computer",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedModeIsAi) colors.darkSquare else colors.textColor
                        )
                    }
                }

                // Pass and Play Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_multi_button")
                        .clickable { selectedModeIsAi = false },
                    colors = CardDefaults.cardColors(
                        containerColor = if (!selectedModeIsAi) colors.lightSquare else colors.cardBackground
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (!selectedModeIsAi) colors.textColor else colors.boardBorder.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Pass and Play Multiplayer",
                            tint = if (!selectedModeIsAi) colors.darkSquare else colors.textColor
                        )
                        Text(
                            text = "Pass & Play",
                            fontWeight = FontWeight.Bold,
                            color = if (!selectedModeIsAi) colors.darkSquare else colors.textColor
                        )
                    }
                }
            }

            // Expanded AI difficulty choices if AI mode selected
            AnimatedVisibility(
                visible = selectedModeIsAi,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "AI Difficulty Level",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AIDifficulty.values().forEach { diff ->
                            val isSelected = selectedAiDifficulty == diff
                            Button(
                                onClick = { selectedAiDifficulty = diff },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("difficulty_${diff.name.lowercase()}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) colors.textColor else colors.cardBackground,
                                    contentColor = if (isSelected) colors.appBackground else colors.textColor
                                ),
                                border = BorderStroke(1.dp, colors.boardBorder.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    text = when (diff) {
                                        AIDifficulty.EASY -> "Easy"
                                        AIDifficulty.NEUTRAL -> "Neutral"
                                        AIDifficulty.HARD -> "Hard"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Theme customization preview
            Text(
                text = "Themes & Customization",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
            )

            ThemeSelectorSection(
                uiState = uiState,
                colors = colors,
                onChangeBoardTheme = onChangeBoardTheme,
                onChangePieceTheme = onChangePieceTheme
            )

            Spacer(modifier = Modifier.weight(1f))

            // BIG START GAME BUTTON
            Button(
                onClick = { onStartGame(selectedModeIsAi, selectedAiDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_game_button")
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.lightSquare,
                    contentColor = colors.darkSquare
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start game icon"
                    )
                    Text(
                        text = "START CHESS MATCH",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// GAME SCREEN (BOARD + CONTROL PANEL)
// ----------------------------------------------------------------------------------
@Composable
fun ChessGameScreen(
    uiState: ChessUiState,
    colors: BoardThemeColors,
    onBackToMenu: () -> Unit,
    onSelectSquare: (Position) -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onToggleAutoRotate: (Boolean) -> Unit,
    onChangeBoardTheme: (BoardTheme) -> Unit,
    onChangePieceTheme: (PieceTheme) -> Unit,
    onPromote: (PieceType) -> Unit,
    onDismissPromotion: () -> Unit,
    showThemesDrawer: Boolean,
    onToggleThemes: () -> Unit
) {
    // Check if the board should rotate (flick upside down for multiplayer pass-and-play Black's turn)
    val shouldRotate = uiState.autoRotateBoard && !uiState.isAiGame && uiState.activeColor == ChessColor.BLACK
    val boardRotation by animateFloatAsState(
        targetValue = if (shouldRotate) 180f else 0f,
        animationSpec = spring(stiffness = 150f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER STATUS BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Return to Menu",
                        tint = colors.textColor
                    )
                }

                // Header status info text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isAiGame) "Solo vs Computer" else "Pass & Play Match",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = if (uiState.isAiGame) uiState.aiDifficulty.name + " AI" else "Local Game",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(
                    onClick = onToggleThemes,
                    modifier = Modifier.testTag("themes_drawer_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize theme",
                        tint = if (showThemesDrawer) colors.lightSquare else colors.textColor
                    )
                }
            }

            // 2. CAPTURED WHITE PIECES (Trophies for Black player)
            CapturedPiecesRow(
                captured = uiState.capturedWhite,
                colors = colors,
                pieceTheme = uiState.pieceTheme,
                ownerColor = ChessColor.BLACK
            )

            // 3. MAIN CHESS BOARD RENDERER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.boardBackground)
                    .border(3.dp, colors.boardBorder, RoundedCornerShape(12.dp))
                    .shadow(4.dp)
                    .rotate(boardRotation)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Standard board rows 0 (top/Black) to 7 (bottom/White)
                    for (r in 0..7) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (c in 0..7) {
                                // Calculate target positions. Row flips if board flipped, so touch maps logically
                                val cellPos = Position(r, c)
                                val isDarkSquare = (r + c) % 2 == 1
                                val squareColor = if (isDarkSquare) colors.darkSquare else colors.lightSquare

                                val isSelected = uiState.selectedSquare == cellPos
                                val isValidMove = uiState.validMoves.contains(cellPos)
                                val hasPiece = uiState.board[r][c] != null
                                val isCheckSquare = uiState.isCheck && uiState.board[r][c]?.type == PieceType.KING && uiState.board[r][c]?.color == uiState.activeColor
                                val isLastMove = uiState.lastMoveFrom == cellPos || uiState.lastMoveTo == cellPos

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                isCheckSquare -> colors.checkSquare
                                                isSelected -> colors.selectedSquare
                                                isLastMove -> colors.lastMoveHighlight
                                                else -> squareColor
                                            }
                                        )
                                        .clickable { onSelectSquare(cellPos) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Render chess piece inside square if present
                                    uiState.board[r][c]?.let { piece ->
                                        // Pieces are drawn flipped so they face the actual player holding the phone!
                                        val pieceRotation = if (uiState.autoRotateBoard && !uiState.isAiGame && piece.color == ChessColor.BLACK) 180f else 0f
                                        ChessPieceView(
                                            piece = piece,
                                            pieceTheme = uiState.pieceTheme,
                                            themeColors = colors,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxSize()
                                                .rotate(pieceRotation)
                                        )
                                    }

                                    // Valid Move indicator dot or corner overlays
                                    if (isValidMove) {
                                        if (hasPiece) {
                                            // Red circle capture outline indicator
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(0.85f)
                                                    .border(2.5.dp, colors.validMoveCapture, CircleShape)
                                            )
                                        } else {
                                            // Soft center overlay dot
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(colors.validMoveDot, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AI is thinking overlay spinner
                if (uiState.isAiThinking) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x60000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = colors.lightSquare,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "AI Calculating...",
                                color = colors.textColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            // 4. CAPTURED BLACK PIECES (Trophies for White player)
            CapturedPiecesRow(
                captured = uiState.capturedBlack,
                colors = colors,
                pieceTheme = uiState.pieceTheme,
                ownerColor = ChessColor.WHITE
            )

            // 5. STATUS BAR MESSAGES & GAME OVER STATES
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val turnText = if (uiState.activeColor == ChessColor.WHITE) "White's Turn" else "Black's Turn"
                    val promptText = when {
                        uiState.isGameOver -> {
                            when (val res = uiState.gameResult) {
                                is GameResult.Win -> "${if (res.winner == ChessColor.WHITE) "White" else "Black"} Wins by ${res.reason}!"
                                is GameResult.Draw -> "Draw by ${res.reason}!"
                                else -> "Game Over"
                            }
                        }
                        uiState.isCheck -> "CHECK! ($turnText)"
                        uiState.isAiThinking -> "AI is plotting move..."
                        uiState.isAiGame && uiState.activeColor == uiState.aiColor -> "Computer plays..."
                        else -> "$turnText"
                    }

                    // Indicator Circle for turn color
                    val indicatorColor = if (uiState.activeColor == ChessColor.WHITE) colors.pieceWhite else colors.pieceBlack
                    val indicatorBorder = if (uiState.activeColor == ChessColor.WHITE) colors.pieceWhiteStroke else colors.pieceBlackStroke
                    
                    if (!uiState.isGameOver) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(indicatorColor, CircleShape)
                                .border(1.5.dp, indicatorBorder, CircleShape)
                        )
                    }

                    Text(
                        text = promptText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.isCheck || uiState.isGameOver) colors.checkSquare else colors.textColor,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 6. ACTION CONTROLS BUTTON BAR (Undo, Restart, Rotate toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Undo Button
                Button(
                    onClick = onUndo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("undo_button"),
                    enabled = uiState.moveHistory.isNotEmpty() && !uiState.isAiThinking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cardBackground,
                        contentColor = colors.textColor,
                        disabledContainerColor = colors.cardBackground.copy(alpha = 0.5f),
                        disabledContentColor = colors.textMuted.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.boardBorder.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Move",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Undo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Auto Rotate Toggle button (Multiplayer only)
                if (!uiState.isAiGame) {
                    Button(
                        onClick = { onToggleAutoRotate(!uiState.autoRotateBoard) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("rotate_toggle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.autoRotateBoard) colors.selectedSquare else colors.cardBackground,
                            contentColor = colors.textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.boardBorder.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Perspective Rotate",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Restart Match Button
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("restart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.lightSquare,
                        contentColor = colors.darkSquare
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Board",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restart", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Expanded Theme Settings bottom Drawer sliding panel
        AnimatedVisibility(
            visible = showThemesDrawer,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, colors.boardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chessboard Theme & Styling",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textColor
                            )
                        )
                        IconButton(
                            onClick = onToggleThemes,
                            modifier = Modifier.testTag("close_themes_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close customization",
                                tint = colors.textColor
                            )
                        }
                    }

                    ThemeSelectorSection(
                        uiState = uiState,
                        colors = colors,
                        onChangeBoardTheme = onChangeBoardTheme,
                        onChangePieceTheme = onChangePieceTheme
                    )
                }
            }
        }

        // 7. PAWN PROMOTION CHIEF SELECTION MODAL
        if (uiState.showPromotionDialog) {
            PawnPromotionDialog(
                colors = colors,
                pieceTheme = uiState.pieceTheme,
                promotionColor = uiState.activeColor,
                onSelectPiece = onPromote,
                onDismiss = onDismissPromotion
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// THEME SELECTOR COMBINED ROW COMPONENT
// ----------------------------------------------------------------------------------
@Composable
fun ThemeSelectorSection(
    uiState: ChessUiState,
    colors: BoardThemeColors,
    onChangeBoardTheme: (BoardTheme) -> Unit,
    onChangePieceTheme: (PieceTheme) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Board Theme select label
        Text(
            text = "Board Style",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colors.textMuted
            )
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(BoardTheme.values()) { bt ->
                val isSelected = uiState.boardTheme == bt
                val previewColors = ChessThemeManager.getColors(bt)
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("board_theme_${bt.name.lowercase()}")
                        .clickable { onChangeBoardTheme(bt) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) colors.lightSquare else colors.cardBackground
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (isSelected) colors.textColor else colors.boardBorder.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Small preview of board squares
                        Row(modifier = Modifier.size(36.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(previewColors.lightSquare))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(previewColors.darkSquare))
                        }
                        Text(
                            text = bt.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.darkSquare else colors.textColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Piece Theme select label
        Text(
            text = "Pieces Style",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colors.textMuted
            )
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(PieceTheme.values()) { pt ->
                val isSelected = uiState.pieceTheme == pt
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("piece_theme_${pt.name.lowercase()}")
                        .clickable { onChangePieceTheme(pt) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) colors.lightSquare else colors.cardBackground
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (isSelected) colors.textColor else colors.boardBorder.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Piece preview drawing
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            ChessPieceView(
                                piece = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE),
                                pieceTheme = pt,
                                themeColors = colors
                            )
                        }
                        Text(
                            text = pt.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.darkSquare else colors.textColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// CAPTURED PIECES TROPHY ROW COMPONENT
// ----------------------------------------------------------------------------------
@Composable
fun CapturedPiecesRow(
    captured: List<ChessPiece>,
    colors: BoardThemeColors,
    pieceTheme: PieceTheme,
    ownerColor: ChessColor
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBackground.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (captured.isEmpty()) {
            Text(
                text = if (ownerColor == ChessColor.WHITE) "No Black pieces captured" else "No White pieces captured",
                fontSize = 12.sp,
                color = colors.textMuted.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group pieces to sort by value for perfect organization
                val sortedCaptured = captured.sortedBy { piece ->
                    when (piece.type) {
                        PieceType.PAWN -> 1
                        PieceType.KNIGHT -> 2
                        PieceType.BISHOP -> 3
                        PieceType.ROOK -> 4
                        PieceType.QUEEN -> 5
                        PieceType.KING -> 6
                    }
                }
                items(sortedCaptured) { piece ->
                    Box(modifier = Modifier.size(24.dp)) {
                        ChessPieceView(
                            piece = piece,
                            pieceTheme = pieceTheme,
                            themeColors = colors
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// PAWN PROMOTION CHIEF MODAL DIALOG
// ----------------------------------------------------------------------------------
@Composable
fun PawnPromotionDialog(
    colors: BoardThemeColors,
    pieceTheme: PieceTheme,
    promotionColor: ChessColor,
    onSelectPiece: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .testTag("promotion_dialog"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, colors.boardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pawn Promotion",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colors.textColor
                )
                Text(
                    text = "Select piece to promote your pawn:",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )

                val promoOptions = listOf(
                    PieceType.QUEEN to "Queen",
                    PieceType.ROOK to "Rook",
                    PieceType.KNIGHT to "Knight",
                    PieceType.BISHOP to "Bishop"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    promoOptions.forEach { opt ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.boardBackground)
                                .border(1.dp, colors.boardBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectPiece(opt.first) }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                .testTag("promote_to_${opt.second.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(36.dp)) {
                                ChessPieceView(
                                    piece = ChessPiece(opt.first, promotionColor),
                                    pieceTheme = pieceTheme,
                                    themeColors = colors
                                )
                            }
                            Text(
                                text = opt.second,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textColor
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_promotion")
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        color = colors.checkSquare
                    )
                }
            }
        }
    }
}
