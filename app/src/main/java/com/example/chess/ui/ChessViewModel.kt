package com.example.chess.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.ai.AIDifficulty
import com.example.chess.ai.ChessAI
import com.example.chess.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChessUiState(
    val board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } },
    val activeColor: ChessColor = ChessColor.WHITE,
    val selectedSquare: Position? = null,
    val validMoves: List<Position> = emptyList(),
    val isCheck: Boolean = false,
    val isGameOver: Boolean = false,
    val gameResult: GameResult? = null,
    val capturedWhite: List<ChessPiece> = emptyList(),
    val capturedBlack: List<ChessPiece> = emptyList(),
    val isAiGame: Boolean = true,
    val aiDifficulty: AIDifficulty = AIDifficulty.NEUTRAL,
    val aiColor: ChessColor = ChessColor.BLACK,
    val isAiThinking: Boolean = false,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD,
    val pieceTheme: PieceTheme = PieceTheme.IMPERIAL,
    val autoRotateBoard: Boolean = true,
    val moveHistory: List<Move> = emptyList(),
    val showPromotionDialog: Boolean = false,
    val promotionFrom: Position? = null,
    val promotionTo: Position? = null,
    val lastMoveFrom: Position? = null,
    val lastMoveTo: Position? = null
)

class ChessViewModel : ViewModel() {
    private val engine = ChessEngine()
    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndReset()
    }

    private fun loadSettingsAndReset() {
        engine.resetGame()
        syncStateWithEngine()
    }

    fun startNewGame(isAiGame: Boolean, difficulty: AIDifficulty) {
        engine.resetGame()
        _uiState.update {
            it.copy(
                isAiGame = isAiGame,
                aiDifficulty = difficulty,
                selectedSquare = null,
                validMoves = emptyList(),
                lastMoveFrom = null,
                lastMoveTo = null,
                showPromotionDialog = false,
                promotionFrom = null,
                promotionTo = null
            )
        }
        syncStateWithEngine()
        triggerAiIfNecessary()
    }

    fun selectSquare(pos: Position) {
        val state = _uiState.value
        if (state.isGameOver || state.isAiThinking) return

        // If clicked on a valid move destination, perform the move
        if (state.validMoves.contains(pos)) {
            val from = state.selectedSquare ?: return
            handleMove(from, pos)
            return
        }

        val piece = engine.getPiece(pos)
        if (piece != null && piece.color == engine.activeColor) {
            // Cannot select pieces if it's AI's turn
            if (state.isAiGame && piece.color == state.aiColor) return

            val legalMoves = engine.getLegalMoves(pos)
            _uiState.update {
                it.copy(
                    selectedSquare = pos,
                    validMoves = legalMoves
                )
            }
        } else {
            // Clicked on empty space or enemy piece without moving (deselect)
            _uiState.update {
                it.copy(
                    selectedSquare = null,
                    validMoves = emptyList()
                )
            }
        }
    }

    private fun handleMove(from: Position, to: Position, promotionPiece: PieceType? = null) {
        val piece = engine.getPiece(from) ?: return

        // Check if pawn promotion is triggered
        if (piece.type == PieceType.PAWN && (to.row == 0 || to.row == 7) && promotionPiece == null) {
            _uiState.update {
                it.copy(
                    showPromotionDialog = true,
                    promotionFrom = from,
                    promotionTo = to
                )
            }
            return
        }

        // Apply move
        val success = engine.makeMove(from, to, promotionPiece)
        if (success) {
            _uiState.update {
                it.copy(
                    selectedSquare = null,
                    validMoves = emptyList(),
                    lastMoveFrom = from,
                    lastMoveTo = to,
                    showPromotionDialog = false,
                    promotionFrom = null,
                    promotionTo = null
                )
            }
            syncStateWithEngine()
            triggerAiIfNecessary()
        }
    }

    fun promotePawn(type: PieceType) {
        val state = _uiState.value
        val from = state.promotionFrom ?: return
        val to = state.promotionTo ?: return
        handleMove(from, to, type)
    }

    fun dismissPromotion() {
        _uiState.update {
            it.copy(
                showPromotionDialog = false,
                promotionFrom = null,
                promotionTo = null,
                selectedSquare = null,
                validMoves = emptyList()
            )
        }
    }

    fun undoLastMove() {
        val state = _uiState.value
        if (state.isAiThinking) return

        if (state.isAiGame) {
            // Undo AI move + Player move (2 moves)
            if (engine.moveHistory.size >= 2) {
                engine.undoMove()
                engine.undoMove()
            }
        } else {
            // Undo 1 move in pass and play
            engine.undoMove()
        }

        // Update last move highlight from remaining history
        val lastMove = engine.moveHistory.lastOrNull()
        _uiState.update {
            it.copy(
                selectedSquare = null,
                validMoves = emptyList(),
                lastMoveFrom = lastMove?.from,
                lastMoveTo = lastMove?.to
            )
        }
        syncStateWithEngine()
    }

    fun changeBoardTheme(theme: BoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun changePieceTheme(theme: PieceTheme) {
        _uiState.update { it.copy(pieceTheme = theme) }
    }

    fun toggleAutoRotate(enabled: Boolean) {
        _uiState.update { it.copy(autoRotateBoard = enabled) }
    }

    private fun syncStateWithEngine() {
        _uiState.update {
            it.copy(
                board = Array(8) { r -> Array(8) { c -> engine.board[r][c] } },
                activeColor = engine.activeColor,
                isCheck = engine.isCheck,
                isGameOver = engine.isGameOver,
                gameResult = engine.gameResult,
                capturedWhite = ArrayList(engine.capturedWhite),
                capturedBlack = ArrayList(engine.capturedBlack),
                moveHistory = ArrayList(engine.moveHistory)
            )
        }
    }

    private fun triggerAiIfNecessary() {
        val state = _uiState.value
        if (state.isGameOver) return

        if (state.isAiGame && state.activeColor == state.aiColor) {
            _uiState.update { it.copy(isAiThinking = true) }
            
            viewModelScope.launch {
                // Compute the AI move in IO thread to avoid lagging UI
                val move = withContext(Dispatchers.Default) {
                    val ai = ChessAI(state.aiDifficulty)
                    // Add artificial thinking delay (300-600ms) for natural feel
                    val startTime = System.currentTimeMillis()
                    val selected = ai.selectMove(engine)
                    val duration = System.currentTimeMillis() - startTime
                    if (duration < 500) {
                        delay(500 - duration)
                    }
                    selected
                }

                _uiState.update { it.copy(isAiThinking = false) }

                if (move != null) {
                    handleAiMove(move.first, move.second)
                }
            }
        }
    }

    private fun handleAiMove(from: Position, to: Position) {
        // AI pawn promotion logic defaults to QUEEN
        val success = engine.makeMove(from, to, PieceType.QUEEN)
        if (success) {
            _uiState.update {
                it.copy(
                    lastMoveFrom = from,
                    lastMoveTo = to
                )
            }
            syncStateWithEngine()
        }
    }
}
