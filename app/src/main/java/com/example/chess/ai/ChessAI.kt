package com.example.chess.ai

import com.example.chess.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class AIDifficulty { EASY, NEUTRAL, HARD }

class ChessAI(private val difficulty: AIDifficulty) {

    // Piece-Square Tables (PST) for evaluations.
    // Values represent bonuses/penalties from White's perspective (row 0 is top, row 7 is bottom).
    // Note: We flip the rows for Black's perspective.
    
    private val pawnPST = arrayOf(
        intArrayOf(  0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf( 50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf( 10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(  5,  5, 10, 25, 25, 10,  5,  5),
        intArrayOf(  0,  0,  0, 20, 20,  0,  0,  0),
        intArrayOf(  5, -5,-10,  0,  0,-10, -5,  5),
        intArrayOf(  5, 10, 10,-20,-20, 10, 10,  5),
        intArrayOf(  0,  0,  0,  0,  0,  0,  0,  0)
    )

    private val knightPST = arrayOf(
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50),
        intArrayOf(-40,-20,  0,  0,  0,  0,-20,-40),
        intArrayOf(-30,  0, 10, 15, 15, 10,  0,-30),
        intArrayOf(-30,  5, 15, 20, 20, 15,  5,-30),
        intArrayOf(-30,  0, 15, 20, 20, 15,  0,-30),
        intArrayOf(-30,  5, 10, 15, 15, 10,  5,-30),
        intArrayOf(-40,-20,  0,  5,  5,  0,-20,-40),
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50)
    )

    private val bishopPST = arrayOf(
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5, 10, 10,  5,  0,-10),
        intArrayOf(-10,  5,  5, 10, 10,  5,  5,-10),
        intArrayOf(-10,  0, 10, 10, 10, 10,  0,-10),
        intArrayOf(-10, 10, 10, 10, 10, 10, 10,-10),
        intArrayOf(-10,  5,  0,  0,  0,  0,  5,-10),
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20)
    )

    private val rookPST = arrayOf(
        intArrayOf(  0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(  5, 10, 10, 10, 10, 10, 10,  5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf( -5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(  0,  0,  0,  5,  5,  5,  0,  0)
    )

    private val queenPST = arrayOf(
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5,  5,  5,  5,  0,-10),
        intArrayOf( -5,  0,  5,  5,  5,  5,  0, -5),
        intArrayOf(  0,  0,  5,  5,  5,  5,  0,  0),
        intArrayOf(-10,  5,  5,  5,  5,  5,  5,-10),
        intArrayOf(-10,  0,  5,  0,  0,  5,  0,-10),
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20)
    )

    private val kingMiddleGamePST = arrayOf(
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-20,-30,-30,-40,-40,-30,-30,-20),
        intArrayOf(-10,-20,-20,-20,-20,-20,-20,-10),
        intArrayOf( 20, 20,  0,  0,  0,  0, 20, 20),
        intArrayOf( 20, 30, 10,  0,  0, 10, 30, 20)
    )

    // Selects and returns the best move: Pair(fromPosition, toPosition)
    fun selectMove(engine: ChessEngine): Pair<Position, Position>? {
        val legalMoves = engine.getAllLegalMoves()
        if (legalMoves.isEmpty()) return null

        when (difficulty) {
            AIDifficulty.EASY -> {
                // 50% chance of a completely random move
                if (Random.nextFloat() < 0.5f) {
                    return legalMoves.random()
                }
                // Otherwise evaluate immediate best capture or basic move (depth 1)
                return selectImmediateMove(engine, legalMoves)
            }
            AIDifficulty.NEUTRAL -> {
                // Minimax search at depth 2 (perfect for casual play, ultra-fast)
                return selectMinimaxMove(engine, depth = 2, useAlphaBeta = false)
            }
            AIDifficulty.HARD -> {
                // Minimax search at depth 3 with Alpha-Beta pruning!
                return selectMinimaxMove(engine, depth = 3, useAlphaBeta = true)
            }
        }
    }

    private fun selectImmediateMove(engine: ChessEngine, legalMoves: List<Pair<Position, Position>>): Pair<Position, Position> {
        val color = engine.activeColor
        var bestScore = -1000000
        val bestMoves = mutableListOf<Pair<Position, Position>>()

        for (move in legalMoves) {
            val from = move.first
            val to = move.second
            val originalBoard = cloneBoard(engine.board)
            val originalEP = engine.enPassantTarget

            // Execute on simulator
            val testEngine = ChessEngine()
            testEngine.board = originalBoard
            testEngine.enPassantTarget = originalEP
            testEngine.activeColor = color
            testEngine.makeMove(from, to)

            val score = evaluateBoard(testEngine.board, color)
            if (score > bestScore) {
                bestScore = score
                bestMoves.clear()
                bestMoves.add(move)
            } else if (score == bestScore) {
                bestMoves.add(move)
            }
        }

        return if (bestMoves.isNotEmpty()) bestMoves.random() else legalMoves.random()
    }

    private fun selectMinimaxMove(engine: ChessEngine, depth: Int, useAlphaBeta: Boolean): Pair<Position, Position>? {
        val legalMoves = engine.getAllLegalMoves()
        if (legalMoves.isEmpty()) return null

        val color = engine.activeColor
        var bestScore = -10000000
        val bestMoves = mutableListOf<Pair<Position, Position>>()

        // Sort moves to optimize alpha-beta pruning (e.g. captures first)
        val sortedMoves = sortMoves(engine, legalMoves)

        for (move in sortedMoves) {
            val from = move.first
            val to = move.second
            
            val testEngine = ChessEngine()
            testEngine.board = cloneBoard(engine.board)
            testEngine.enPassantTarget = engine.enPassantTarget
            testEngine.activeColor = color
            testEngine.makeMove(from, to)

            val score = if (useAlphaBeta) {
                -alphaBeta(testEngine, depth - 1, -10000000, 10000000, false)
            } else {
                -minimax(testEngine, depth - 1, false)
            }

            if (score > bestScore) {
                bestScore = score
                bestMoves.clear()
                bestMoves.add(move)
            } else if (score == bestScore) {
                bestMoves.add(move)
            }
        }

        return if (bestMoves.isNotEmpty()) bestMoves.random() else legalMoves.random()
    }

    private fun minimax(engine: ChessEngine, depth: Int, isMaximizing: Boolean): Int {
        if (depth == 0 || engine.isGameOver) {
            return evaluateBoard(engine.board, engine.activeColor)
        }

        val legalMoves = engine.getAllLegalMoves()
        if (legalMoves.isEmpty()) {
            if (engine.isCheck) {
                return -250000 // Checkmate
            }
            return 0 // Stalemate
        }

        if (isMaximizing) {
            var maxEval = -10000000
            for (move in legalMoves) {
                val testEngine = ChessEngine()
                testEngine.board = cloneBoard(engine.board)
                testEngine.enPassantTarget = engine.enPassantTarget
                testEngine.activeColor = engine.activeColor
                testEngine.makeMove(move.first, move.second)

                val evaluation = minimax(testEngine, depth - 1, false)
                maxEval = max(maxEval, evaluation)
            }
            return maxEval
        } else {
            var minEval = 10000000
            for (move in legalMoves) {
                val testEngine = ChessEngine()
                testEngine.board = cloneBoard(engine.board)
                testEngine.enPassantTarget = engine.enPassantTarget
                testEngine.activeColor = engine.activeColor
                testEngine.makeMove(move.first, move.second)

                val evaluation = minimax(testEngine, depth - 1, true)
                minEval = min(minEval, evaluation)
            }
            return minEval
        }
    }

    private fun alphaBeta(engine: ChessEngine, depth: Int, alphaInput: Int, betaInput: Int, isMaximizing: Boolean): Int {
        var alpha = alphaInput
        var beta = betaInput
        
        if (depth == 0 || engine.isGameOver) {
            return evaluateBoard(engine.board, engine.activeColor)
        }

        val legalMoves = engine.getAllLegalMoves()
        if (legalMoves.isEmpty()) {
            if (engine.isCheck) {
                return -250000 + (3 - depth) // Prefer faster mate
            }
            return 0 // Stalemate
        }

        val sortedMoves = sortMoves(engine, legalMoves)

        if (isMaximizing) {
            var maxEval = -10000000
            for (move in sortedMoves) {
                val testEngine = ChessEngine()
                testEngine.board = cloneBoard(engine.board)
                testEngine.enPassantTarget = engine.enPassantTarget
                testEngine.activeColor = engine.activeColor
                testEngine.makeMove(move.first, move.second)

                val evaluation = alphaBeta(testEngine, depth - 1, alpha, beta, false)
                maxEval = max(maxEval, evaluation)
                alpha = max(alpha, evaluation)
                if (beta <= alpha) break // Pruning
            }
            return maxEval
        } else {
            var minEval = 10000000
            for (move in sortedMoves) {
                val testEngine = ChessEngine()
                testEngine.board = cloneBoard(engine.board)
                testEngine.enPassantTarget = engine.enPassantTarget
                testEngine.activeColor = engine.activeColor
                testEngine.makeMove(move.first, move.second)

                val evaluation = alphaBeta(testEngine, depth - 1, alpha, beta, true)
                minEval = min(minEval, evaluation)
                beta = min(beta, evaluation)
                if (beta <= alpha) break // Pruning
            }
            return minEval
        }
    }

    // Basic move sorting to improve alpha-beta cuts
    private fun sortMoves(engine: ChessEngine, moves: List<Pair<Position, Position>>): List<Pair<Position, Position>> {
        return moves.sortedByDescending { move ->
            val movingPiece = engine.getPiece(move.first)
            val targetPiece = engine.getPiece(move.second)
            var score = 0
            if (targetPiece != null) {
                // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
                score += 10 * getPieceValue(targetPiece.type) - getPieceValue(movingPiece?.type ?: PieceType.PAWN)
            }
            // Encourage pawn promotion moves
            if (movingPiece?.type == PieceType.PAWN && (move.second.row == 0 || move.second.row == 7)) {
                score += 900
            }
            score
        }
    }

    private fun evaluateBoard(board: Array<Array<ChessPiece?>>, forColor: ChessColor): Int {
        var whiteScore = 0
        var blackScore = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c] ?: continue
                val material = getPieceValue(piece.type)
                val positionBonus = getPiecePositionBonus(piece.type, piece.color, r, c)
                
                val totalValue = material + positionBonus

                if (piece.color == ChessColor.WHITE) {
                    whiteScore += totalValue
                } else {
                    blackScore += totalValue
                }
            }
        }

        return if (forColor == ChessColor.WHITE) whiteScore - blackScore else blackScore - whiteScore
    }

    private fun getPieceValue(type: PieceType): Int {
        return when (type) {
            PieceType.PAWN -> 100
            PieceType.KNIGHT -> 320
            PieceType.BISHOP -> 330
            PieceType.ROOK -> 500
            PieceType.QUEEN -> 900
            PieceType.KING -> 20000
        }
    }

    private fun getPiecePositionBonus(type: PieceType, color: ChessColor, r: Int, c: Int): Int {
        // Adjust indices for Black pieces (flip the board vertically)
        val row = if (color == ChessColor.WHITE) r else 7 - r
        val col = c

        return when (type) {
            PieceType.PAWN -> pawnPST[row][col]
            PieceType.KNIGHT -> knightPST[row][col]
            PieceType.BISHOP -> bishopPST[row][col]
            PieceType.ROOK -> rookPST[row][col]
            PieceType.QUEEN -> queenPST[row][col]
            PieceType.KING -> kingMiddleGamePST[row][col]
        }
    }

    private fun cloneBoard(board: Array<Array<ChessPiece?>>): Array<Array<ChessPiece?>> {
        return Array(8) { r -> Array(8) { c -> board[r][c] } }
    }
}
