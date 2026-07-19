package com.example.chess.model

import kotlin.math.abs

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
enum class ChessColor { WHITE, BLACK }

data class Position(val row: Int, val col: Int) {
    fun isValid() = row in 0..7 && col in 0..7
    override fun toString() = "${'a' + col}${8 - row}"
}

data class ChessPiece(
    val type: PieceType,
    val color: ChessColor,
    val hasMoved: Boolean = false
)

data class Move(
    val from: Position,
    val to: Position,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val promotionType: PieceType? = null
)

sealed class GameResult {
    data class Win(val winner: ChessColor, val reason: String) : GameResult()
    data class Draw(val reason: String) : GameResult()
}

class ChessEngine {
    var board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }
    var activeColor = ChessColor.WHITE
    var enPassantTarget: Position? = null
    var isCheck = false
    var isGameOver = false
    var gameResult: GameResult? = null
    
    val moveHistory = mutableListOf<Move>()
    val capturedWhite = mutableListOf<ChessPiece>()
    val capturedBlack = mutableListOf<ChessPiece>()

    init {
        resetGame()
    }

    fun resetGame() {
        board = Array(8) { Array(8) { null } }
        activeColor = ChessColor.WHITE
        enPassantTarget = null
        isCheck = false
        isGameOver = false
        gameResult = null
        moveHistory.clear()
        capturedWhite.clear()
        capturedBlack.clear()

        // Place Rooks
        board[0][0] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        board[0][7] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        board[7][0] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)
        board[7][7] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)

        // Place Knights
        board[0][1] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        board[0][6] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        board[7][1] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)
        board[7][6] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)

        // Place Bishops
        board[0][2] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        board[0][5] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        board[7][2] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)
        board[7][5] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)

        // Place Queens
        board[0][3] = ChessPiece(PieceType.QUEEN, ChessColor.BLACK)
        board[7][3] = ChessPiece(PieceType.QUEEN, ChessColor.WHITE)

        // Place Kings
        board[0][4] = ChessPiece(PieceType.KING, ChessColor.BLACK)
        board[7][4] = ChessPiece(PieceType.KING, ChessColor.WHITE)

        // Place Pawns
        for (col in 0..7) {
            board[1][col] = ChessPiece(PieceType.PAWN, ChessColor.BLACK)
            board[6][col] = ChessPiece(PieceType.PAWN, ChessColor.WHITE)
        }
    }

    // Creates a deep copy of the board for hypothetical moves
    private fun cloneBoard(): Array<Array<ChessPiece?>> {
        return Array(8) { r -> Array(8) { c -> board[r][c] } }
    }

    fun getPiece(pos: Position): ChessPiece? {
        if (!pos.isValid()) return null
        return board[pos.row][pos.col]
    }

    // Returns all legal moves for a given piece at a position
    fun getLegalMoves(pos: Position): List<Position> {
        val piece = getPiece(pos) ?: return emptyList()
        if (piece.color != activeColor) return emptyList()

        val pseudoMoves = getPseudoLegalMoves(pos, board)
        return pseudoMoves.filter { toPos ->
            // Try the move on a cloned board to see if the king would be in check
            val originalBoard = cloneBoard()
            val originalEnPassant = enPassantTarget

            executeMoveOnBoard(pos, toPos, null, simulateOnly = true)
            val kingSafe = !isInCheck(piece.color, board)

            // Restore state
            board = originalBoard
            enPassantTarget = originalEnPassant
            kingSafe
        }
    }

    // Returns all legal moves on the entire board for the active player
    fun getAllLegalMoves(): List<Pair<Position, Position>> {
        val moves = mutableListOf<Pair<Position, Position>>()
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val piece = getPiece(pos)
                if (piece != null && piece.color == activeColor) {
                    val targets = getLegalMoves(pos)
                    for (target in targets) {
                        moves.add(Pair(pos, target))
                    }
                }
            }
        }
        return moves
    }

    // Pseudo-legal moves do not check if they leave the King in check
    fun getPseudoLegalMoves(pos: Position, currentBoard: Array<Array<ChessPiece?>>): List<Position> {
        val piece = currentBoard[pos.row][pos.col] ?: return emptyList()
        val moves = mutableListOf<Position>()

        when (piece.type) {
            PieceType.PAWN -> getPawnMoves(pos, piece, currentBoard, moves)
            PieceType.KNIGHT -> getKnightMoves(pos, piece, currentBoard, moves)
            PieceType.BISHOP -> getBishopMoves(pos, piece, currentBoard, moves)
            PieceType.ROOK -> getRookMoves(pos, piece, currentBoard, moves)
            PieceType.QUEEN -> {
                getBishopMoves(pos, piece, currentBoard, moves)
                getRookMoves(pos, piece, currentBoard, moves)
            }
            PieceType.KING -> getKingMoves(pos, piece, currentBoard, moves)
        }
        return moves
    }

    private fun getPawnMoves(pos: Position, piece: ChessPiece, currentBoard: Array<Array<ChessPiece?>>, moves: MutableList<Position>) {
        val dir = if (piece.color == ChessColor.WHITE) -1 else 1
        val startRow = if (piece.color == ChessColor.WHITE) 6 else 1

        // Forward 1
        val f1 = Position(pos.row + dir, pos.col)
        if (f1.isValid() && currentBoard[f1.row][f1.col] == null) {
            moves.add(f1)
            // Forward 2
            val f2 = Position(pos.row + 2 * dir, pos.col)
            if (pos.row == startRow && currentBoard[f2.row][f2.col] == null) {
                moves.add(f2)
            }
        }

        // Diagonal Captures
        val d1 = Position(pos.row + dir, pos.col - 1)
        val d2 = Position(pos.row + dir, pos.col + 1)
        for (d in listOf(d1, d2)) {
            if (d.isValid()) {
                val target = currentBoard[d.row][d.col]
                if (target != null && target.color != piece.color) {
                    moves.add(d)
                } else if (enPassantTarget == d) {
                    moves.add(d)
                }
            }
        }
    }

    private fun getKnightMoves(pos: Position, piece: ChessPiece, currentBoard: Array<Array<ChessPiece?>>, moves: MutableList<Position>) {
        val offsets = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for (offset in offsets) {
            val target = Position(pos.row + offset.first, pos.col + offset.second)
            if (target.isValid()) {
                val targetPiece = currentBoard[target.row][target.col]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(target)
                }
            }
        }
    }

    private fun getBishopMoves(pos: Position, piece: ChessPiece, currentBoard: Array<Array<ChessPiece?>>, moves: MutableList<Position>) {
        val dirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        for (dir in dirs) {
            var curr = Position(pos.row + dir.first, pos.col + dir.second)
            while (curr.isValid()) {
                val targetPiece = currentBoard[curr.row][curr.col]
                if (targetPiece == null) {
                    moves.add(curr)
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(curr)
                    }
                    break
                }
                curr = Position(curr.row + dir.first, curr.col + dir.second)
            }
        }
    }

    private fun getRookMoves(pos: Position, piece: ChessPiece, currentBoard: Array<Array<ChessPiece?>>, moves: MutableList<Position>) {
        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        for (dir in dirs) {
            var curr = Position(pos.row + dir.first, pos.col + dir.second)
            while (curr.isValid()) {
                val targetPiece = currentBoard[curr.row][curr.col]
                if (targetPiece == null) {
                    moves.add(curr)
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(curr)
                    }
                    break
                }
                curr = Position(curr.row + dir.first, curr.col + dir.second)
            }
        }
    }

    private fun getKingMoves(pos: Position, piece: ChessPiece, currentBoard: Array<Array<ChessPiece?>>, moves: MutableList<Position>) {
        val dirs = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1),              Pair(0, 1),
            Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
        )
        for (dir in dirs) {
            val target = Position(pos.row + dir.first, pos.col + dir.second)
            if (target.isValid()) {
                val targetPiece = currentBoard[target.row][target.col]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(target)
                }
            }
        }

        // Castling (Only evaluated on the main board in normal state for simplicity)
        if (!piece.hasMoved && !isCheck && currentBoard === board) {
            val r = pos.row
            // King Side Castling
            val rookKingSide = board[r][7]
            if (rookKingSide != null && rookKingSide.type == PieceType.ROOK && !rookKingSide.hasMoved) {
                if (board[r][5] == null && board[r][6] == null) {
                    if (isSquareSafe(Position(r, 5), piece.color) && isSquareSafe(Position(r, 6), piece.color)) {
                        moves.add(Position(r, 6))
                    }
                }
            }
            // Queen Side Castling
            val rookQueenSide = board[r][0]
            if (rookQueenSide != null && rookQueenSide.type == PieceType.ROOK && !rookQueenSide.hasMoved) {
                if (board[r][1] == null && board[r][2] == null && board[r][3] == null) {
                    if (isSquareSafe(Position(r, 2), piece.color) && isSquareSafe(Position(r, 3), piece.color)) {
                        moves.add(Position(r, 2))
                    }
                }
            }
        }
    }

    fun isSquareSafe(pos: Position, defenderColor: ChessColor): Boolean {
        // A square is safe if the opponent cannot move a piece to it on the next turn
        val opponentColor = if (defenderColor == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == opponentColor) {
                    val moves = getPseudoLegalMoves(Position(r, c), board)
                    if (moves.contains(pos)) return false
                }
            }
        }
        return true
    }

    fun isInCheck(color: ChessColor, currentBoard: Array<Array<ChessPiece?>>): Boolean {
        // Find king
        var kingPos: Position? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val p = currentBoard[r][c]
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    kingPos = Position(r, c)
                    break
                }
            }
        }
        if (kingPos == null) return false

        val opponentColor = if (color == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE
        for (r in 0..7) {
            for (c in 0..7) {
                val p = currentBoard[r][c]
                if (p != null && p.color == opponentColor) {
                    val moves = getPseudoLegalMoves(Position(r, c), currentBoard)
                    if (moves.contains(kingPos)) return true
                }
            }
        }
        return false
    }

    // Applies a move on the board
    fun makeMove(from: Position, to: Position, promotionPiece: PieceType? = null): Boolean {
        if (isGameOver) return false
        val piece = getPiece(from) ?: return false
        if (piece.color != activeColor) return false

        val legal = getLegalMoves(from)
        if (!legal.contains(to)) return false

        executeMoveOnBoard(from, to, promotionPiece, simulateOnly = false)
        
        // Toggle turn
        activeColor = if (activeColor == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE

        // Determine if active is in check
        isCheck = isInCheck(activeColor, board)

        // Check for Game Over (Checkmate or Stalemate)
        val allLegal = getAllLegalMoves()
        if (allLegal.isEmpty()) {
            isGameOver = true
            if (isCheck) {
                val winner = if (activeColor == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE
                gameResult = GameResult.Win(winner, "Checkmate")
            } else {
                gameResult = GameResult.Draw("Stalemate")
            }
        } else {
            // Check for Draw (Insufficient Material, 50-move rule omitted for simplicity, but let's check for only Kings)
            if (isInsufficientMaterial()) {
                isGameOver = true
                gameResult = GameResult.Draw("Insufficient Material")
            }
        }

        return true
    }

    private fun isInsufficientMaterial(): Boolean {
        var whiteCount = 0
        var blackCount = 0
        var whiteKnightsOrBishops = 0
        var blackKnightsOrBishops = 0
        var majorPieces = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c] ?: continue
                if (p.type == PieceType.KING) continue
                if (p.color == ChessColor.WHITE) {
                    whiteCount++
                    if (p.type == PieceType.KNIGHT || p.type == PieceType.BISHOP) whiteKnightsOrBishops++
                    else majorPieces++
                } else {
                    blackCount++
                    if (p.type == PieceType.KNIGHT || p.type == PieceType.BISHOP) blackKnightsOrBishops++
                    else majorPieces++
                }
            }
        }

        if (majorPieces > 0) return false
        // King vs King
        if (whiteCount == 0 && blackCount == 0) return true
        // King & Bishop/Knight vs King
        if (whiteCount == 1 && whiteKnightsOrBishops == 1 && blackCount == 0) return true
        if (blackCount == 1 && blackKnightsOrBishops == 1 && whiteCount == 0) return true
        // King & Bishop vs King & Bishop (on same color squares, omitted for simplicity)
        return false
    }

    private fun executeMoveOnBoard(from: Position, to: Position, promotionPiece: PieceType?, simulateOnly: Boolean) {
        val piece = board[from.row][from.col] ?: return
        var captured = board[to.row][to.col]
        var isCastling = false
        var isEnPassant = false

        // Handle Castling
        if (piece.type == PieceType.KING && abs(from.col - to.col) == 2) {
            isCastling = true
            val r = from.row
            if (to.col == 6) { // King Side
                val rook = board[r][7]
                board[r][5] = rook?.copy(hasMoved = true)
                board[r][7] = null
            } else if (to.col == 2) { // Queen Side
                val rook = board[r][0]
                board[r][3] = rook?.copy(hasMoved = true)
                board[r][0] = null
            }
        }

        // Handle En Passant Capture
        if (piece.type == PieceType.PAWN && to == enPassantTarget) {
            isEnPassant = true
            val captureRow = from.row
            val captureCol = to.col
            captured = board[captureRow][captureCol]
            board[captureRow][captureCol] = null
        }

        // Standard move execution
        board[from.row][from.col] = null
        
        var finalPiece = piece.copy(hasMoved = true)
        
        // Handle Pawn Promotion
        var promoApplied: PieceType? = null
        if (piece.type == PieceType.PAWN && (to.row == 0 || to.row == 7)) {
            val promoType = promotionPiece ?: PieceType.QUEEN
            finalPiece = ChessPiece(promoType, piece.color, hasMoved = true)
            promoApplied = promoType
        }

        board[to.row][to.col] = finalPiece

        // Set next En Passant Target
        val nextEnPassant = if (piece.type == PieceType.PAWN && abs(from.row - to.row) == 2) {
            Position((from.row + to.row) / 2, from.col)
        } else {
            null
        }

        if (!simulateOnly) {
            enPassantTarget = nextEnPassant
            
            // Record captured pieces
            captured?.let {
                if (it.color == ChessColor.WHITE) capturedWhite.add(it) else capturedBlack.add(it)
            }
            
            // Record history
            moveHistory.add(Move(
                from = from,
                to = to,
                piece = piece,
                capturedPiece = captured,
                isCastling = isCastling,
                isEnPassant = isEnPassant,
                promotionType = promoApplied
            ))
        } else {
            enPassantTarget = nextEnPassant
        }
    }

    // Undoes the last move
    fun undoMove(): Boolean {
        if (moveHistory.isEmpty()) return false
        val lastMove = moveHistory.removeAt(moveHistory.size - 1)
        
        // Revert turns
        activeColor = lastMove.piece.color
        isGameOver = false
        gameResult = null

        // Revert board piece
        board[lastMove.from.row][lastMove.from.col] = lastMove.piece
        board[lastMove.to.row][lastMove.to.col] = null

        // Revert capture
        if (lastMove.capturedPiece != null) {
            if (lastMove.capturedPiece.color == ChessColor.WHITE) {
                capturedWhite.removeAt(capturedWhite.size - 1)
            } else {
                capturedBlack.removeAt(capturedBlack.size - 1)
            }

            if (lastMove.isEnPassant) {
                val captureRow = lastMove.from.row
                val captureCol = lastMove.to.col
                board[captureRow][captureCol] = lastMove.capturedPiece
            } else {
                board[lastMove.to.row][lastMove.to.col] = lastMove.capturedPiece
            }
        }

        // Revert castling
        if (lastMove.isCastling) {
            val r = lastMove.from.row
            if (lastMove.to.col == 6) { // King Side
                val rook = board[r][5]
                board[r][7] = rook?.copy(hasMoved = false)
                board[r][5] = null
            } else if (lastMove.to.col == 2) { // Queen Side
                val rook = board[r][3]
                board[r][0] = rook?.copy(hasMoved = false)
                board[r][3] = null
            }
        }

        // Recalculate check status
        isCheck = isInCheck(activeColor, board)
        
        // Recalculate previous En Passant target (approximate based on prior history or set to null)
        enPassantTarget = if (moveHistory.isNotEmpty()) {
            val prev = moveHistory.last()
            if (prev.piece.type == PieceType.PAWN && abs(prev.from.row - prev.to.row) == 2) {
                Position((prev.from.row + prev.to.row) / 2, prev.from.col)
            } else null
        } else null

        return true
    }
}
