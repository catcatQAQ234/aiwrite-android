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

    // Phase outputs
    val generatedOptions = MutableStateFlow("")
    val selectedOptionIndex = MutableStateFlow(-1)
    val planOutput = MutableStateFlow("")
    val worldOutput = MutableStateFlow("")
    val characterOutput = MutableStateFlow("")
    val strategyOutput = MutableStateFlow("")
    val chapterBreakdown = MutableStateFlow("")

    // Error state
    val errorMessage = MutableStateFlow<String?>(null)

    fun setInspiration(text: String) {
        _inspiration.value = text
        errorMessage.value = null
    }

    // Phase 1: Generate opening options
    fun generateOptions() {
        val insp = _inspiration.value.ifBlank {
            errorMessage.value = "请先输入故事灵感"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.GENERATING_OPTIONS, "正在生成开书方向...")
            try {
                val result = generateOpeningOptions(insp)
                generatedOptions.value = result
                updateProgress(DirectorPhase.OPTIONS_READY, "请选择一个方向继续")
            } catch (e: Exception) {
                errorMessage.value = "生成失败: ${e.message}"
                updateProgress(DirectorPhase.IDLE, "")
            }
        }
    }

    // User selects an option and proceeds to planning
    fun selectOptionAndPlan(optionIndex: Int) {
        selectedOptionIndex.value = optionIndex
        val insp = _inspiration.value
        val options = generatedOptions.value

        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.PLANNING, "正在制定宏观规划...")
            try {
                val result = generatePlan(insp, options)
                planOutput.value = result
                updateProgress(DirectorPhase.PLAN_READY, "规划完成，请确认后继续")
            } catch (e: Exception) {
                errorMessage.value = "规划失败: ${e.message}"
                updateProgress(DirectorPhase.OPTIONS_READY, "")
            }
        }
    }

    // Continue from plan to world
    fun continueToWorld() {
        val insp = _inspiration.value
        val plan = planOutput.value

        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.WORLD_PREP, "正在构建世界观...")
            try {
                val result = generateWorld(insp, plan)
                worldOutput.value = result
                updateProgress(DirectorPhase.WORLD_READY, "世界观完成，请确认后继续")
            } catch (e: Exception) {
                errorMessage.value = "世界观生成失败: ${e.message}"
                updateProgress(DirectorPhase.PLAN_READY, "")
            }
        }
    }

    fun continueToCharacters() {
        val insp = _inspiration.value
        val world = worldOutput.value
        val plan = planOutput.value

        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.CHARACTER_PREP, "正在准备角色阵容...")
            try {
                val result = generateCharacters(insp, world, plan)
                characterOutput.value = result
                updateProgress(DirectorPhase.CHARACTERS_READY, "角色准备完成，请确认后继续")
            } catch (e: Exception) {
                errorMessage.value = "角色生成失败: ${e.message}"
                updateProgress(DirectorPhase.WORLD_READY, "")
            }
        }
    }

    fun continueToStrategy() {
        val plan = planOutput.value
        val chars = characterOutput.value
        val insp = _inspiration.value

        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.VOLUME_STRATEGY, "正在制定卷战略...")
            try {
                val result = generateVolumeStrategy(plan, chars, insp)
                strategyOutput.value = result
                updateProgress(DirectorPhase.STRATEGY_READY, "卷战略完成，请确认后继续")
            } catch (e: Exception) {
                errorMessage.value = "战略生成失败: ${e.message}"
                updateProgress(DirectorPhase.CHARACTERS_READY, "")
            }
        }
    }

    fun continueToChapters() {
        val strategy = strategyOutput.value
        val plan = planOutput.value
        val insp = _inspiration.value

        viewModelScope.launch(Dispatchers.IO) {
            updateProgress(DirectorPhase.CHAPTER_BREAKDOWN, "正在拆章...")
            try {
                val result = generateChapterBreakdown(strategy, plan, insp)
                chapterBreakdown.value = result
                updateProgress(DirectorPhase.READY_TO_WRITE, "开书准备完成！可以开始写作了。")
            } catch (e: Exception) {
                errorMessage.value = "拆章失败: ${e.message}"
                updateProgress(DirectorPhase.STRATEGY_READY, "")
            }
        }
    }

    // Regenerate current phase
    fun regenerate() {
        errorMessage.value = null
        when (_progress.value.phase) {
            DirectorPhase.IDLE -> generateOptions()
            DirectorPhase.OPTIONS_READY -> generateOptions()
            DirectorPhase.PLAN_READY -> selectOptionAndPlan(selectedOptionIndex.value)
            DirectorPhase.WORLD_READY -> continueToWorld()
            DirectorPhase.CHARACTERS_READY -> continueToCharacters()
            DirectorPhase.STRATEGY_READY -> continueToStrategy()
            DirectorPhase.READY_TO_WRITE -> continueToChapters()
            else -> {}
        }
    }

    fun clear() {
        _progress.value = DirectorProgress()
        _inspiration.value = ""
        generatedOptions.value = ""
        selectedOptionIndex.value = -1
        planOutput.value = ""
        worldOutput.value = ""
        characterOutput.value = ""
        strategyOutput.value = ""
        chapterBreakdown.value = ""
        errorMessage.value = null
    }

    // === Generation methods ===

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
            ChatMessage("user", "灵感：$inspiration\n\n选定方向：\n$options\n\n请制定完整宏观规划。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateWorld(inspiration: String, plan: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_WORLD),
            ChatMessage("user", "灵感：$inspiration\n\n规划：\n$plan\n\n请构建世界观设定。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateCharacters(inspiration: String, world: String, plan: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_CHARACTERS),
            ChatMessage("user", "灵感：$inspiration\n\n世界观：\n$world\n\n规划：\n$plan\n\n请生成角色阵容。")
        )
        return modelRouter.chat(TaskType.PLANNING, messages).getOrDefault("生成失败")
    }

    private suspend fun generateVolumeStrategy(plan: String, characters: String, inspiration: String): String {
        val messages = listOf(
            ChatMessage("system", SYSTEM_PROMPT_VOLUME),
            ChatMessage("user", "灵感：$inspiration\n\n规划：\n$plan\n\n角色：\n$characters\n\n请制定卷战略。")
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
        errorMessage.value = null
        _progress.value = DirectorProgress(
            phase = phase,
            currentStep = step,
            isRunning = phase.ordinal in setOf(
                DirectorPhase.GENERATING_OPTIONS.ordinal,
                DirectorPhase.PLANNING.ordinal,
                DirectorPhase.WORLD_PREP.ordinal,
                DirectorPhase.CHARACTER_PREP.ordinal,
                DirectorPhase.VOLUME_STRATEGY.ordinal,
                DirectorPhase.CHAPTER_BREAKDOWN.ordinal
            )
        )
    }

    companion object {
        val SYSTEM_PROMPT_OPTIONS = """
你是一位资深小说策划编辑。根据用户提供的灵感，生成三套开书方向方案。
每套方案包含: 书名方案(2-3个)、核心梗概(100字)、类型/风格定位、核心卖点、目标读者、预计篇幅。
请用"方案一""方案二""方案三"明确标注。
        """.trimIndent()

        val SYSTEM_PROMPT_PLAN = """
你是资深小说结构师。根据灵感和选定的开书方向，制定完整宏观规划。
包含: 故事主线(起承转合)、核心冲突、剧情节点列表、情感曲线、伏笔与回收计划、各卷大致内容范围。
        """.trimIndent()

        val SYSTEM_PROMPT_WORLD = """
你是小说世界观架构师。根据灵感和规划构建世界观。
包含: 世界规则与法则、势力分布、重要地点(地理/文化/历史)、冲突入口、世界与主线关联点。
        """.trimIndent()

        val SYSTEM_PROMPT_CHARACTERS = """
你是角色设计师。根据灵感、世界观和规划，生成完整角色阵容。
包含: 主角(身份/性格/动机/成长弧/核心冲突)、主要配角(定位/关系/独立动机)、反派/对抗力量、角色关系图谱。
        """.trimIndent()

        val SYSTEM_PROMPT_VOLUME = """
你是小说结构策划。制定卷战略与节奏规划。
包含: 全书分卷方案(核心事件/情感走向/篇幅占比)、卷间钩子/悬念设计、节奏控制、各卷关键章节节点。
        """.trimIndent()

        val SYSTEM_PROMPT_CHAPTERS = """
你是小说章节策划师。进行详细的拆章工作。
包含: 每卷章节列表(标题+一句话概要)、每章叙事视角、核心冲突/事件、章节间钩子/衔接、重点章节标注(高潮/转折/伏笔回收)。
        """.trimIndent()
    }
}
