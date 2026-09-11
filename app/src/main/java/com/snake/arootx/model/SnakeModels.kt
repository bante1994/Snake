package com.snake.arootx.model

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean =
        (dx + other.dx == 0) && (dy + other.dy == 0)
}

data class Point(val x: Int, val y: Int)

enum class DeathReason(val label: String) {
    WALL_COLLISION("Crashed into electric perimeter"),
    SELF_COLLISION("Broke integrity - self collision"),
    SURRENDER("Player terminated session")
}

enum class WallMode(val label: String, val shortName: String, val description: String) {
    WALL("WALLS", "Classic", "Electric Perimeter: Hitting border causes fatal crash"),
    WALL_LESS("WALL-LESS", "Wrap", "Borderless Matrix: Snake wraps seamlessly through edges")
}

data class Food(
    val point: Point,
    val isGolden: Boolean = false,
    val expiresAtMs: Long = 0L,
    val pointsValue: Int = 10
)

data class GameState(
    val snake: List<Point> = listOf(Point(7, 8), Point(7, 9), Point(7, 10)),
    val direction: Direction = Direction.UP,
    val nextDirection: Direction = Direction.UP,
    val food: Food = Food(Point(4, 4)),
    val gridWidth: Int = 16,
    val gridHeight: Int = 18,
    val wallMode: WallMode = WallMode.WALL,
    val score: Int = 0,
    val highScore: Int = 0,
    val applesEaten: Int = 0,
    val goldenApplesEaten: Int = 0,
    val speedLevel: Int = 1,
    val scoreMultiplier: Float = 1.0f,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val deathReason: DeathReason? = null,
    val moveCount: Int = 0,
    val startTimeMs: Long = 0L,
    val durationSeconds: Int = 0,
    val tournamentSeed: Long = 424242L,
    val antiCheatToken: String = "",
    val antiCheatVerified: Boolean = true,
    val levelUpAnnouncement: String? = null,
    val isAdShowing: Boolean = false,
    val adCountdownSeconds: Int = 5,
    val canReviveWithAd: Boolean = true,
    val revivesUsed: Int = 0,
    val invulnerableUntilMs: Long = 0L
)
