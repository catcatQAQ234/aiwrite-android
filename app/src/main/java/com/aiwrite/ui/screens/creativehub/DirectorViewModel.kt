package com.aiwrite.ui.screens.creativehub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.remote.ModelRouter
import com.aiwrite.data.remote.TaskType
import com.aiwrite.domain.model.ChatMessage
import com.aiwrite.domain.model.DirectorPhase
import com.aiwrite.domain.model.DirectorProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectorViewModel @Inject constructor(
    private val modelRouter: ModelRouter
) : ViewModel() {

    private val _progress = MutableStateFlow(DirectorProgress())
    val progress: StateFlow<DirectorProgress> = _progress

    private val _inspiration = MutableStateFlow("")
    val inspiration: StateFlow<String> = _inspiration

    private val _generatedOptions = MutableStateFlow("")
    val generatedOptions: StateFlow<String> = _generatedOptions

    private val _worldOutput = MutableStateFlow("")
    val worldOutput: StateFlow<String> = _worldOutput

    private val _characterOutput = MutableStateFlow("")
    val characterOutput: StateFlow<String> = _characterOutput

    private val _planOutput = MutableStateFlow("")
    val planOutput: StateFlow<String> = _planOutput

    // Store accumulated outputs between phases
    private val accumulatedOutput = mutableMapOf<DirectorPhase, String>()

    fun setInspiration(text: String) {
        _inspiration.value = text
    }

    fun startDirector() {
        val inspiration = _inspiration.value
        if (inspiration.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Phase 1: Generate opening options
                updateProgress(DirectorPhase.GENERATING_OPTIONS, "正在生成开书方向...")
                val options = generateOpeningOptions(inspiration)
                _generatedOptions.value = options
                accumulatedOutput[DirectorPhase.OPTIONS_READY] = options
                updateProgress(DirectorPhase.OPTIONS_READY, "开书方向已生成")

                // Phase 2: Macro planning
                updateProgress(DirectorPhase.PLANNING, "正在制定宏观规划...")
                val plan = generatePlan(inspiration, options)
                _planOutput.value = plan
                accumulatedOutput[DirectorPhase.PLAN_READY] = plan
                updateProgress(DirectorPhase.PLAN_READY, "宏观规划完成")

                // Phase 3: World building
                updateProgress(DirectorPhase.WORLD_PREP, "正在构建世界观...")
                val world = generateWorld(inspiration, plan)
                _worldOutput.value = world
                accumulatedOutput[DirectorPhase.WORLD_READY] = world
                updateProgress(DirectorPhase.WORLD_READY, "世界观构建完成")

                // Phase 4: Character preparation
                updateProgress(DirectorPhase.CHARACTER_PREP, "正在准备角色阵容...")
                val characters = generateCharacters(inspiration, world, plan)
                _characterOutput.value = characters
                accumulatedOutput[DirectorPhase.CHARACTERS_READY] = characters
                updateProgress(DirectorPhase.CHARACTERS_READY, "角色准备完成")

                // Phase 5: Volume strategy
                updateProgress(DirectorPhase.VOLUME_STRATEGY, "正在制定卷战略...")
                val strategy = generateVolumeStrategy(plan, characters, inspiration)
                accumulatedOutput[DirectorPhase.STRATEGY_READY] = strategy
                updateProgress(DirectorPhase.STRATEGY_READY, "卷战略完成")

                // Phase 6: Chapter breakdown
                updateProgress(DirectorPhase.CHAPTER_BREAKDOWN, "正在拆章...")
                val chapters = generateChapterBreakdown(strategy, plan, inspiration)
                accumulatedOutput[DirectorPhase.READY_TO_WRITE] = chapters
                updateProgress(DirectorPhase.READY_TO_WRITE, "开书准备完成！可以开始写作了。")

            } catch (e: Exception) {
                updateProgress(
                    DirectorPhase.IDLE,
                    "错误: ${e.message}"
                )
            }
        }
    }

    private suspend fun generateOpeningOptions(inspiration: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_OPTIONS),
            ChatMessage("user", "请根据以下灵感，生成三套开书方向方案：\n\n$inspiration")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generatePlan(inspiration: String, options: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_PLAN),
            ChatMessage("user", "灵感：$inspiration\n\n选定的开书方向：\n$options\n\n请制定完整的宏观规划。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateWorld(inspiration: String, plan: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_WORLD),
            ChatMessage("user", "灵感：$inspiration\n\n宏观规划：\n$plan\n\n请构建本书的世界观设定。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateCharacters(inspiration: String, world: String, plan: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_CHARACTERS),
            ChatMessage("user", "灵感：$inspiration\n\n世界观：\n$world\n\n故事规划：\n$plan\n\n请生成角色阵容。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateVolumeStrategy(plan: String, characters: String, inspiration: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_VOLUME),
            ChatMessage("user", "灵感：$inspiration\n\n规划：\n$plan\n\n角色：\n$characters\n\n请制定卷战略与节奏拆章。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateChapterBreakdown(strategy: String, plan: String, inspiration: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_CHAPTERS),
            ChatMessage("user", "灵感：$inspiration\n\n规划：\n$plan\n\n卷战略：\n$strategy\n\n请进行详细拆章。")
        )
        return modelRouter.chat(TaskType.WRITING, messages).getOrDefault("生成失败")
    }

    private fun updateProgress(phase: DirectorPhase, step: String) {
        _progress.value = DirectorProgress(
            phase = phase,
            currentStep = step,
            isRunning = phase != DirectorPhase.IDLE && phase != DirectorPhase.READY_TO_WRITE
        )
    }

    companion object {
        private val SYSTEM_PROMPT_OPTIONS = """
你是一位资深的小说策划编辑。根据用户提供的灵感，你需要生成三套完整的开书方向方案。

每套方案应包含：
1. 书名方案（2-3个备选）
2. 核心梗概（100字以内）
3. 类型/风格定位
4. 核心卖点/看点
5. 目标读者画像
6. 预计篇幅（字数/卷数）

请在输出中明确标注"方案一"、"方案二"、"方案三"。
        """.trimIndent()

        private val SYSTEM_PROMPT_PLAN = """
你是一位资深的小说结构师。请根据灵感和选定的开书方向，制定完整的宏观规划。

输出应包含：
1. 故事主线（起承转合）
2. 核心冲突（人物冲突/世界观冲突/内心冲突）
3. 剧情节点（关键情节点列表）
4. 情感曲线（读者情感体验设计）
5. 伏笔与回收计划
6. 各卷大致内容范围
        """.trimIndent()

        private val SYSTEM_PROMPT_WORLD = """
你是一位小说世界观架构师。请根据灵感和规划构建世界观。

输出应包含：
1. 世界规则与法则（物理/魔法/科技体系）
2. 势力分布（权力结构、组织、阵营）
3. 重要地点（地理/文化/历史）
4. 冲突入口（世界层面可用的冲突来源）
5. 世界与故事主线的关联点
        """.trimIndent()

        private val SYSTEM_PROMPT_CHARACTERS = """
你是一位角色设计师。请根据灵感、世界观和规划，生成完整的角色阵容。

输出应包含：
1. 主角（身份、性格、动机、成长弧线、核心冲突）
2. 主要配角（每人含：定位、与主角关系、独立动机）
3. 反派/对抗力量（动机、手段、与主角的镜像关系）
4. 角色关系图谱
5. 各角色在世界中的位置与边界
        """.trimIndent()

        private val SYSTEM_PROMPT_VOLUME = """
你是一位小说结构策划。请制定卷战略与节奏规划。

输出应包含：
1. 全书分卷方案（每卷：核心事件、情感走向、篇幅占比）
2. 卷与卷之间的钩子/悬念设计
3. 节奏控制（紧张/舒缓交替设计）
4. 各卷关键章节节点
        """.trimIndent()

        private val SYSTEM_PROMPT_CHAPTERS = """
你是一位小说章节策划师。请进行详细的拆章工作。

输出应包含：
1. 每卷下的章节列表（章节标题 + 一句话概要）
2. 每章的叙事视角
3. 每章的核心冲突/事件
4. 章节间的钩子/衔接
5. 重点章节标注（情感高潮/剧情转折/伏笔回收）
        """.trimIndent()
    }
}
