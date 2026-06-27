package com.aiwrite.ui.screens.world

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiwrite.data.local.entity.CharacterProfileEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.WorldSettingEntity
import com.aiwrite.data.repository.CharacterRepository
import com.aiwrite.data.repository.WorldRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldScreen(
    viewModel: WorldViewModel = hiltViewModel(),
    styleViewModel: StyleViewModel = hiltViewModel()
) {
    val novels by viewModel.novels.collectAsState()
    val selectedNovel by viewModel.selectedNovel.collectAsState()
    val selectedNovelId = viewModel.novels.collectAsState().value
        .find { it.id == selectedNovel?.id }?.id
    val worldSettings by viewModel.worldSettings.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val styleProfiles by styleViewModel.profiles.collectAsState()

    LaunchedEffect(selectedNovel?.id) {
        styleViewModel.setNovelId(selectedNovel?.id)
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "世界观" to Icons.Filled.Public,
        "角色" to Icons.Filled.Person,
        "写法" to Icons.Filled.Style
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("世界观") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Novel selector
            NovelSelector(
                novels = novels,
                selectedNovel = selectedNovel,
                onSelectNovel = { viewModel.selectNovel(it) }
            )

            if (selectedNovel == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "请先选择或创建一个小说项目",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Tab row
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, (title, icon) ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = { Icon(icon, contentDescription = null) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> WorldSettingsPanel(
                        settings = worldSettings,
                        onCreate = { name, category, content ->
                            viewModel.createWorldSetting(name, category, content)
                        },
                        onUpdate = { viewModel.updateWorldSetting(it) },
                        onDelete = { viewModel.deleteWorldSetting(it) }
                    )
                    1 -> CharactersPanel(
                        characters = characters,
                        worldSettings = worldSettings,
                        onCreate = { name, role -> viewModel.createCharacter(name, role) },
                        onUpdate = { viewModel.updateCharacter(it) },
                        onDelete = { viewModel.deleteCharacter(it) }
                    )
                    2 -> StylePanel(
                        profiles = styleProfiles,
                        onCreateProfile = { name, desc -> styleViewModel.createProfile(name, desc) },
                        onDeleteProfile = { styleViewModel.deleteProfile(it) },
                        onSetActive = { styleViewModel.setActive(it) },
                        onAddFeature = { profile, feature -> styleViewModel.addFeature(profile, feature) },
                        onToggleFeature = { profile, featureId, enabled ->
                            styleViewModel.toggleFeature(profile, featureId, enabled)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NovelSelector(
    novels: List<NovelEntity>,
    selectedNovel: NovelEntity?,
    onSelectNovel: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("当前小说：", style = MaterialTheme.typography.labelLarge)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedNovel?.title ?: "选择小说")
                Icon(Icons.Filled.ArrowDropDown, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                novels.forEach { novel ->
                    DropdownMenuItem(
                        text = { Text(novel.title) },
                        onClick = {
                            onSelectNovel(novel.id)
                            expanded = false
                        }
                    )
                }
                if (novels.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("暂无小说，请先去「小说」页面创建") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }
    }
    HorizontalDivider()
}

// ===== World Settings Panel =====

@Composable
private fun WorldSettingsPanel(
    settings: List<WorldSettingEntity>,
    onCreate: (String, String, String) -> Unit,
    onUpdate: (WorldSettingEntity) -> Unit,
    onDelete: (WorldSettingEntity) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WorldSettingEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (settings.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Public, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("还没有世界观设定", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val grouped = settings.groupBy { it.category }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (category, items) ->
                    val label = WorldRepository.CATEGORY_LABELS[category] ?: category
                    item {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(items, key = { it.id }) { setting ->
                        WorldSettingCard(
                            setting = setting,
                            onClick = { editingItem = setting },
                            onDelete = { onDelete(setting) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "添加设定")
        }
    }

    if (showCreateDialog) {
        WorldSettingEditDialog(
            title = "新建世界观设定",
            setting = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, category, content ->
                onCreate(name, category, content)
                showCreateDialog = false
            }
        )
    }

    editingItem?.let { item ->
        WorldSettingEditDialog(
            title = "编辑世界观设定",
            setting = item,
            onDismiss = { editingItem = null },
            onSave = { name, category, content ->
                onUpdate(item.copy(name = name, category = category, content = content))
                editingItem = null
            }
        )
    }
}

@Composable
private fun WorldSettingCard(
    setting: WorldSettingEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(setting.name, style = MaterialTheme.typography.bodyLarge)
                if (setting.content.isNotBlank()) {
                    Text(
                        setting.content.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun WorldSettingEditDialog(
    title: String,
    setting: WorldSettingEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(setting?.name ?: "") }
    var category by remember { mutableStateOf(setting?.category ?: "rule") }
    var content by remember { mutableStateOf(setting?.content ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Box {
                    OutlinedTextField(
                        value = WorldRepository.CATEGORY_LABELS[category] ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分类") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        WorldRepository.CATEGORIES.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    category = key
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("详细内容") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("描述这个世界观元素的详细信息...") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), category, content.trim()) },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ===== Characters Panel =====

@Composable
private fun CharactersPanel(
    characters: List<CharacterProfileEntity>,
    worldSettings: List<WorldSettingEntity>,
    onCreate: (String, String) -> Unit,
    onUpdate: (CharacterProfileEntity) -> Unit,
    onDelete: (CharacterProfileEntity) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingChar by remember { mutableStateOf<CharacterProfileEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (characters.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Person, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("还没有角色", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val grouped = characters.groupBy { it.role }
            val roleOrder = listOf("protagonist", "antagonist", "supporting", "minor", "")

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                roleOrder.forEach { role ->
                    grouped[role]?.let { chars ->
                        val label = CharacterRepository.ROLE_LABELS[role] ?: "其他角色"
                        item {
                            Text(
                                label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(chars, key = { it.id }) { char ->
                            CharacterCard(
                                character = char,
                                factionName = worldSettings.find { it.id == char.factionId }?.name,
                                onClick = { editingChar = char },
                                onDelete = { onDelete(char) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.PersonAdd, "添加角色")
        }
    }

    if (showCreateDialog) {
        CharacterEditDialog(
            title = "新建角色",
            character = null,
            factions = worldSettings.filter { it.category == "faction" },
            onDismiss = { showCreateDialog = false },
            onSave = { name, role, factionId, traits, background, motivation, arc, relations ->
                val id = "" // will be set by repository
                onCreate(name, role)
                showCreateDialog = false
            }
        )
    }

    editingChar?.let { char ->
        CharacterEditDialog(
            title = "编辑角色",
            character = char,
            factions = worldSettings.filter { it.category == "faction" },
            onDismiss = { editingChar = null },
            onSave = { name, role, factionId, traits, background, motivation, arc, relations ->
                onUpdate(
                    char.copy(
                        name = name, role = role, factionId = factionId,
                        traits = traits, background = background,
                        motivation = motivation, arc = arc, relations = relations
                    )
                )
                editingChar = null
            }
        )
    }
}

@Composable
private fun CharacterCard(
    character: CharacterProfileEntity,
    factionName: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(character.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    if (factionName != null) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(factionName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                if (character.background.isNotBlank()) {
                    Text(
                        character.background.take(60),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CharacterEditDialog(
    title: String,
    character: CharacterProfileEntity?,
    factions: List<WorldSettingEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(character?.name ?: "") }
    var role by remember { mutableStateOf(character?.role ?: "") }
    var factionId by remember { mutableStateOf(character?.factionId ?: "") }
    var traits by remember { mutableStateOf(character?.traits ?: "") }
    var background by remember { mutableStateOf(character?.background ?: "") }
    var motivation by remember { mutableStateOf(character?.motivation ?: "") }
    var arc by remember { mutableStateOf(character?.arc ?: "") }
    var roleExpanded by remember { mutableStateOf(false) }
    var factionExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("姓名") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Role selector
                Box {
                    OutlinedTextField(
                        value = CharacterRepository.ROLE_LABELS[role] ?: "选择角色定位",
                        onValueChange = {}, readOnly = true,
                        label = { Text("角色定位") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { roleExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        CharacterRepository.ROLES.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = key; roleExpanded = false }
                            )
                        }
                    }
                }

                // Faction selector
                if (factions.isNotEmpty()) {
                    Box {
                        val factionName = factions.find { it.id == factionId }?.name
                        OutlinedTextField(
                            value = factionName ?: "选择所属势力",
                            onValueChange = {}, readOnly = true,
                            label = { Text("所属势力") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { factionExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(expanded = factionExpanded, onDismissRequest = { factionExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("无") },
                                onClick = { factionId = ""; factionExpanded = false }
                            )
                            factions.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    onClick = { factionId = f.id; factionExpanded = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = traits, onValueChange = { traits = it },
                    label = { Text("特征标签（JSON）") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("{\"age\": 18, \"personality\": [\"勇敢\", \"善良\"]}") }
                )

                OutlinedTextField(
                    value = background, onValueChange = { background = it },
                    label = { Text("背景故事") },
                    minLines = 2, maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = motivation, onValueChange = { motivation = it },
                    label = { Text("核心动机") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = arc, onValueChange = { arc = it },
                    label = { Text("成长弧线") },
                    minLines = 2, maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), role, factionId, traits.trim(), background.trim(), motivation.trim(), arc.trim(), "") },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
