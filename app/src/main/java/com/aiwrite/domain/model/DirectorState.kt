package com.aiwrite.domain.model

enum class DirectorPhase(val label: String) {
    IDLE("待开始"),
    GENERATING_OPTIONS("生成开书方向"),
    OPTIONS_READY("方向已就绪"),
    PLANNING("宏观规划"),
    PLAN_READY("规划完成"),
    WORLD_PREP("世界观准备"),
    WORLD_READY("世界观就绪"),
    CHARACTER_PREP("角色准备"),
    CHARACTERS_READY("角色就绪"),
    VOLUME_STRATEGY("卷战略"),
    STRATEGY_READY("战略就绪"),
    CHAPTER_BREAKDOWN("拆章"),
    READY_TO_WRITE("可开写")
}

data class DirectorProgress(
    val phase: DirectorPhase = DirectorPhase.IDLE,
    val currentStep: String = "",
    val outputText: String = "",
    val isRunning: Boolean = false
)
