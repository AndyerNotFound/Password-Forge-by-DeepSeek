package com.passwordtool

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ========================== 工具 ==========================

fun hexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16)
    return Color(0xFF000000L or value)
}

fun copyText(text: String, cm: ClipboardManager, toast: (String) -> Unit) {
    cm.setText(AnnotatedString(text))
    toast("已复制到剪贴板")
}

// ========================== 主题 ==========================

private val THEME_COLORS = listOf(
    Color(0xFF6750A4), Color(0xFF005AC1), Color(0xFF006E2A),
    Color(0xFFB5004C), Color(0xFF964900)
)

// ========================== 根组件 ==========================

@Composable
fun App() {
    var tab by remember { mutableIntStateOf(0) }
    var primary by remember { mutableStateOf(Color(0xFF6750A4)) }
    var analyzeInput by remember { mutableStateOf("") }
    var analyzeTrigger by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun toast(msg: String) {
        scope.launch { snackbar.showSnackbar(msg) }
    }

    fun requestAnalyze(pw: String) {
        analyzeInput = pw
        analyzeTrigger++
        tab = 3
    }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.25f),
        onPrimaryContainer = primary.copy(alpha = 0.9f),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        surface = Color(0xFFFFFBFE),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurface = Color(0xFF1C1B1F),
        onSurfaceVariant = Color(0xFF49454F),
        outlineVariant = Color(0xFFCAC4D0),
        error = Color(0xFFB3261E),
        errorContainer = Color(0xFFF9DEDC)
    )) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Lock, null) }, label = { Text("随机密码") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.Refresh, null) }, label = { Text("短语密码") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Build, null) }, label = { Text("哈希计算") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.Info, null) }, label = { Text("强度分析") })
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                Header(primary, onTheme = { primary = it })
                when (tab) {
                    0 -> RandomPasswordScreen(onAnalyze = { requestAnalyze(it) }, toast = ::toast)
                    1 -> PassphraseScreen(onAnalyze = { requestAnalyze(it) }, toast = ::toast)
                    2 -> HashScreen(toast = ::toast)
                    3 -> AnalyzeScreen(
                        input = analyzeInput, trigger = analyzeTrigger, toast = ::toast
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(primary: Color, onTheme: (Color) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("🔐 密码工坊", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Material 3 · Kotlin v4.0", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            THEME_COLORS.forEach { c ->
                Box(
                    Modifier.size(26.dp).background(c, CircleShape).clickable { onTheme(c) }
                        .then(if (c == primary) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                )
            }
        }
    }
}

// ========================== 通用小部件 ==========================

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StrengthBadge(label: String, color: Color) {
    Text(
        label,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.background(color.copy(alpha = 0.13f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
private fun EntropyRow(entropy: Double, color: Color) {
    Column {
        LinearProgressIndicator(
            progress = { (entropy / 128f).toFloat().coerceIn(0f, 1f) },
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text("熵值 ${"%.1f".format(entropy)} bits", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ========================== 随机密码 ==========================

@Composable
private fun RandomPasswordScreen(onAnalyze: (String) -> Unit, toast: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var length by remember { mutableStateOf(16f) }
    var lower by remember { mutableStateOf(true) }
    var upper by remember { mutableStateOf(true) }
    var digits by remember { mutableStateOf(true) }
    var special by remember { mutableStateOf(true) }
    var avoid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var entropy by remember { mutableStateOf(0.0) }
    var label by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.Gray) }
    var showResult by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("随机密码生成", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("密码长度", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${length.toInt()}", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = length, onValueChange = { length = it },
                    valueRange = 8f..128f, steps = 119)
                CheckboxRow("小写字母 (a-z)", lower) { lower = it }
                CheckboxRow("大写字母 (A-Z)", upper) { upper = it }
                CheckboxRow("数字 (0-9)", digits) { digits = it }
                CheckboxRow("特殊符号 (!@#...)", special) { special = it }
                CheckboxRow("排除易混字符 (0O1lI)", avoid) { avoid = it }
                Button(onClick = {
                    password = generateRandomPassword(length.toInt(), upper, lower, digits, special, avoid)
                    entropy = calculateEntropy(password)
                    val (l, c) = strengthLabel(entropy)
                    label = l
                    color = hexColor(c)
                    showResult = true
                }, modifier = Modifier.fillMaxWidth()) { Text("生成随机密码") }
            }
        }
        if (showResult) {
            Card {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(password, fontFamily = FontFamily.Monospace, fontSize = 16.sp,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { copyText(password, clipboard, toast) }) {
                            Icon(Icons.Default.Share, "复制")
                        }
                    }
                    EntropyRow(entropy, color)
                    StrengthBadge(label, color)
                    OutlinedButton(onClick = { onAnalyze(password) }, modifier = Modifier.fillMaxWidth()) {
                        Text("验证强度")
                    }
                }
            }
        }
    }
}

// ========================== 短语密码 ==========================

@Composable
private fun PassphraseScreen(onAnalyze: (String) -> Unit, toast: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var count by remember { mutableStateOf(6f) }
    var sep by remember { mutableStateOf("-") }
    var titleCase by remember { mutableStateOf(false) }
    var includeNumber by remember { mutableStateOf(false) }
    var phrases by remember { mutableStateOf(listOf<String>()) }
    var entropy by remember { mutableStateOf(0.0) }
    var label by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.Gray) }
    val seps = listOf("-" to "连字符 -", "_" to "下划线 _", " " to "空格", "." to "句点 .", "" to "无分隔符")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("短语密码生成", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("单词数量", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${count.toInt()}", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = count, onValueChange = { count = it },
                    valueRange = 4f..48f, steps = 43)
                Text("分隔符", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    seps.forEach { (value, name) ->
                        FilterChip(selected = sep == value, onClick = { sep = value }, label = { Text(name) })
                    }
                }
                CheckboxRow("首字母大写 (Title Case)", titleCase) { titleCase = it }
                CheckboxRow("随机插入数字", includeNumber) { includeNumber = it }
                Button(onClick = {
                    phrases = List(5) {
                        generatePassphrase(count.toInt(), sep, titleCase, includeNumber)
                    }
                    entropy = calculateEntropy(phrases.first())
                    val (l, c) = strengthLabel(entropy)
                    label = l
                    color = hexColor(c)
                }, modifier = Modifier.fillMaxWidth()) { Text("生成短语密码") }
            }
        }
        if (phrases.isNotEmpty()) {
            Card {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("候选结果（共 5 条）· 熵值 ${"%.1f".format(entropy)} bits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    phrases.forEach { phrase ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(phrase, fontFamily = FontFamily.Monospace, fontSize = 14.sp,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { onAnalyze(phrase) }) {
                                Icon(Icons.Default.Search, "验证强度", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { copyText(phrase, clipboard, toast) }) {
                                Icon(Icons.Default.Share, "复制")
                            }
                        }
                        HorizontalDivider()
                    }
                    StrengthBadge(label, color)
                }
            }
        }
    }
}

// ========================== 哈希 ==========================

@Composable
private fun HashScreen(toast: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var input by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val algorithms = listOf("MD5", "SHA-1", "SHA-256", "SHA-512", "SHA3-256")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("哈希值计算", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    label = { Text("输入要哈希的文本") },
                    placeholder = { Text("支持任意字符，按 UTF-8 编码计算") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    if (input.isEmpty()) { toast("请输入内容"); return@Button }
                    results = algorithms.map { it to hashHex(it, input) }
                }, modifier = Modifier.fillMaxWidth()) { Text("计算哈希值") }
            }
        }
        if (results.isNotEmpty()) {
            Card {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("算法", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    results.forEach { (algo, hash) ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(algo, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(80.dp))
                            Text(hash, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { copyText(hash, clipboard, toast) }) {
                                Icon(Icons.Default.Share, "复制", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================== 强度分析 ==========================

@Composable
private fun AnalyzeScreen(input: String, trigger: Int, toast: (String) -> Unit) {
    var text by remember { mutableStateOf(input) }
    var result by remember { mutableStateOf<Analysis?>(null) }

    LaunchedEffect(input, trigger) {
        if (trigger > 0) {
            text = input
            if (input.isNotEmpty()) result = analyze(input)
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("密码强度分析", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    label = { Text("待分析密码") },
                    placeholder = { Text("输入密码进行强度与破解时间分析...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    if (text.isEmpty()) { toast("请输入密码"); return@Button }
                    result = analyze(text)
                }, modifier = Modifier.fillMaxWidth()) { Text("分析强度") }
            }
        }
        result?.let { r ->
            Card {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (r.commonPassword) {
                        Text("⚠️ 此密码在常见弱密码黑名单中，可被瞬间破解！请立即更换。",
                            color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                                .padding(12.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("长度", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${r.length} 位", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("熵值", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.1f".format(r.entropy)} bits", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("评级", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StrengthBadge(r.strength.first, hexColor(r.strength.second))
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (r.entropy / 128f).toFloat().coerceIn(0f, 1f) },
                        color = hexColor(r.strength.second),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("预计破解所需时间", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                    r.crackRows.forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(row.scene, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f))
                            Text(row.time, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
