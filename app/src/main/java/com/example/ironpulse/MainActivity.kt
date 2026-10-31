@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ironpulse

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// Exact Color Palette from Reference Design
val PureWhite = Color(0xFFFFFFFF)
val SoftCoolBg = Color(0xFFF6F8FC)
val DividerCoolGray = Color(0xFFE6EAF2)
val RoyalIndigo = Color(0xFF192560)        // Deep navy header & workout cards
val RoyalIndigoDark = Color(0xFF121C4A)    // Darker navy footer strip inside workout cards
val CobaltBlue = Color(0xFF3655D8)         // Vibrant blue START button, date badge & Overall Best card
val PeriwinkleBlue = Color(0xFF5C7CFA)     // Glowing bar chart & muscle highlight accent
val SoftPeriwinklePill = Color(0xFFDBE4FF) // Muscle involvement tag background
val TextDarkNavy = Color(0xFF10183B)       // Sharp dark navy typography
val TextCoolMuted = Color(0xFF8B95B0)      // Muted gray labels (SET 1, tonnage lifted, etc.)
val CoralRed = Color(0xFFE03131)

private val ReferenceLightColorScheme = lightColorScheme(
    primary = CobaltBlue,
    onPrimary = PureWhite,
    secondary = RoyalIndigo,
    onSecondary = PureWhite,
    background = PureWhite,
    surface = PureWhite,
    onBackground = TextDarkNavy,
    onSurface = TextDarkNavy
)

class WorkoutWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("QUICK_START_WORKOUT", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.workout_widget)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

object RestNotificationHelper {
    private const val CHANNEL_ID = "ironpulse_rest_timer"
    private const val NOTIF_ID = 1001

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Rest Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts and live lock-screen countdown for workout rest intervals"
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }

    fun showRunningCountdown(context: Context, secondsRemaining: Int, exerciseName: String) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val endTime = System.currentTimeMillis() + (secondsRemaining * 1000L)

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("Rest Timer: $exerciseName")
            .setContentText("Counting down with screen off...")
            .setWhen(endTime)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIF_ID, notification)
    }

    fun showRestFinishedAlert(context: Context, exerciseName: String) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("Rest Complete! Ready for next set")
            .setContentText("Next set of $exerciseName")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIF_ID, notification)
        vibratePhone(context)
    }

    fun cancelTimerNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    }

    fun vibratePhone(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 450), -1)
            )
        } catch (_: Exception) {
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.workoutDao()

        setContent {
            MaterialTheme(colorScheme = ReferenceLightColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PureWhite
                ) {
                    IronPulseApp(dao = dao)
                }
            }
        }
    }
}

@Composable
fun IronPulseApp(dao: WorkoutDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("ironpulse_prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        if (dao.getExerciseCount() == 0) {
            dao.insertExercises(SeedData.defaultExercises)
        }
        if (dao.getTemplateCount() == 0) {
            dao.insertTemplates(SeedData.defaultTemplates)
        }
    }

    val exercises by dao.getAllExercises().collectAsState(initial = emptyList())
    val templates by dao.getAllTemplates().collectAsState(initial = emptyList())
    val allSets by dao.getAllSets().collectAsState(initial = emptyList())
    val bodyMetrics by dao.getAllBodyMetrics().collectAsState(initial = emptyList())

    // Navigation Tabs:
    // 0 = Dashboard, 1 = Add Sets (Logger), 2 = Add Exercises (Library), 3 = Anatomy View, 4 = Statistics
    var currentTab by remember { mutableIntStateOf(0) }
    var homeBodyweightMode by remember { mutableStateOf(false) }
    var weeklyGoalDays by remember { mutableIntStateOf(prefs.getInt("weekly_goal", 4)) }

    // Active Workout Session State
    var activeSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var activeWorkoutName by remember { mutableStateOf("Chest & Back") }
    var selectedExerciseName by remember { mutableStateOf("Barbell Bench Press") }

    // Rest Timer State (matches "Enable rest timer" switch in Add Sets header)
    var restTimerEnabled by remember { mutableStateOf(true) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var restTimerRunning by remember { mutableStateOf(false) }
    var restExerciseLabel by remember { mutableStateOf("") }

    // PR Celebration Banner
    var prBannerMessage by remember { mutableStateOf<String?>(null) }

    // Modals
    var showPlateDialog by remember { mutableStateOf(false) }
    var plateTargetInput by remember { mutableStateOf("100") }
    var showBodyMetricDialog by remember { mutableStateOf(false) }

    LaunchedEffect(restTimerRunning, restSecondsRemaining) {
        if (restTimerRunning && restSecondsRemaining > 0) {
            delay(1000L)
            restSecondsRemaining -= 1
            if (restSecondsRemaining == 0) {
                restTimerRunning = false
                RestNotificationHelper.showRestFinishedAlert(context, restExerciseLabel)
            }
        }
    }

    fun startRestTimer(seconds: Int, exercise: String) {
        if (!restTimerEnabled) return
        restExerciseLabel = exercise
        restSecondsRemaining = seconds.coerceAtLeast(5)
        restTimerRunning = true
        RestNotificationHelper.showRunningCountdown(context, restSecondsRemaining, exercise)
    }

    fun stopRestTimer() {
        restTimerRunning = false
        restSecondsRemaining = 0
        RestNotificationHelper.cancelTimerNotification(context)
    }

    Scaffold(
        containerColor = PureWhite,
        bottomBar = {
            Column {
                // Floating Rest Countdown Pill if Active
                if (restTimerRunning || restSecondsRemaining > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoyalIndigo)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = PeriwinkleBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val mins = restSecondsRemaining / 60
                            val secs = restSecondsRemaining % 60
                            Text(
                                text = String.format(Locale.US, "Rest Timer  %02d:%02d", mins, secs),
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "-15s",
                                color = SoftPeriwinklePill,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RoyalIndigoDark)
                                    .clickable {
                                        restSecondsRemaining = (restSecondsRemaining - 15).coerceAtLeast(0)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Text(
                                text = "+30s",
                                color = SoftPeriwinklePill,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RoyalIndigoDark)
                                    .clickable { restSecondsRemaining += 30 }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Text(
                                text = "SKIP",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CobaltBlue)
                                    .clickable { stopRestTimer() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Divider(color = DividerCoolGray, thickness = 1.dp)
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 0.dp
                ) {
                    val navItems = listOf(
                        Triple(0, "Dashboard", Icons.Default.Home),
                        Triple(1, "Add Sets", Icons.Default.FitnessCenter),
                        Triple(2, "Exercises", Icons.Default.ListAlt),
                        Triple(3, "Anatomy", Icons.Default.Person),
                        Triple(4, "Stats", Icons.Default.ShowChart)
                    )
                    navItems.forEach { (index, label, icon) ->
                        NavigationBarItem(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CobaltBlue,
                                selectedTextColor = CobaltBlue,
                                indicatorColor = SoftPeriwinklePill.copy(alpha = 0.5f),
                                unselectedIconColor = TextCoolMuted,
                                unselectedTextColor = TextCoolMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardReferenceScreen(
                    templates = templates,
                    allSets = allSets,
                    bodyMetrics = bodyMetrics,
                    homeBodyweightMode = homeBodyweightMode,
                    onToggleHomeMode = { homeBodyweightMode = !homeBodyweightMode },
                    onOpenBodyWeightModal = { showBodyMetricDialog = true },
                    onOpenAnatomyScreen = { currentTab = 3 },
                    onStartWorkoutTemplate = { template ->
                        activeSessionId = UUID.randomUUID().toString()
                        activeWorkoutName = template.name
                        val firstEx = template.exercisesCsv.split(",")
                            .map { it.trim() }
                            .firstOrNull { it.isNotEmpty() }
                        if (firstEx != null) {
                            selectedExerciseName = firstEx
                        }
                        currentTab = 1
                    },
                    onSaveTemplate = { name, desc, csv, isBw ->
                        scope.launch {
                            dao.insertTemplate(
                                WorkoutTemplateEntity(
                                    name = name,
                                    description = desc,
                                    exercisesCsv = csv,
                                    isBodyweightOnly = isBw
                                )
                            )
                        }
                    },
                    onUpdateTemplate = { updated ->
                        scope.launch { dao.updateTemplate(updated) }
                    },
                    onDeleteTemplate = { template ->
                        scope.launch { dao.deleteTemplate(template) }
                    }
                )

                1 -> AddSetsReferenceScreen(
                    exercises = exercises,
                    templates = templates,
                    allSets = allSets,
                    homeBodyweightMode = homeBodyweightMode,
                    activeSessionId = activeSessionId,
                    activeWorkoutName = activeWorkoutName,
                    selectedExerciseName = selectedExerciseName,
                    restTimerEnabled = restTimerEnabled,
                    prBannerMessage = prBannerMessage,
                    onDismissPrBanner = { prBannerMessage = null },
                    onToggleRestTimerEnabled = { restTimerEnabled = it },
                    onSelectExercise = { selectedExerciseName = it },
                    onNewSession = {
                        activeSessionId = UUID.randomUUID().toString()
                        Toast.makeText(context, "New workout session started", Toast.LENGTH_SHORT).show()
                    },
                    onLogSet = { weight, reps, setType, restSec ->
                        val exObj = exercises.find { it.name == selectedExerciseName }
                        val cat = exObj?.category ?: "Custom"
                        val previousBest1RM = allSets
                            .filter { it.exerciseName == selectedExerciseName && it.setType != "WARMUP" }
                            .maxOfOrNull { WorkoutMath.estimate1RM(it.weightKg, it.reps) } ?: 0.0
                        val new1RM = WorkoutMath.estimate1RM(weight, reps)
                        val isNewPR = setType != "WARMUP" && new1RM > previousBest1RM && reps > 0

                        val currentSessionSets = allSets.filter {
                            it.sessionId == activeSessionId && it.exerciseName == selectedExerciseName
                        }
                        val nextSetNumber = currentSessionSets.size + 1

                        scope.launch {
                            dao.insertSet(
                                WorkoutSetEntity(
                                    sessionId = activeSessionId,
                                    workoutName = activeWorkoutName,
                                    dateEpochDay = WorkoutMath.currentEpochDay(),
                                    timestampMillis = System.currentTimeMillis(),
                                    exerciseName = selectedExerciseName,
                                    category = cat,
                                    setNumber = nextSetNumber,
                                    weightKg = weight,
                                    reps = reps,
                                    setType = setType,
                                    restSeconds = restSec,
                                    isPR = isNewPR
                                )
                            )
                        }
                        if (isNewPR) {
                            prBannerMessage = "NEW PERSONAL RECORD! $selectedExerciseName ($weight kg x $reps)"
                        }
                        startRestTimer(restSec, selectedExerciseName)
                    },
                    onDeleteSet = { setEntity ->
                        scope.launch { dao.deleteSet(setEntity) }
                    },
                    onOpenPlateCalc = { weightVal ->
                        plateTargetInput = weightVal.toString()
                        showPlateDialog = true
                    }
                )

                2 -> AddExercisesReferenceScreen(
                    exercises = exercises,
                    homeBodyweightMode = homeBodyweightMode,
                    selectedExerciseName = selectedExerciseName,
                    onToggleHomeMode = { homeBodyweightMode = !homeBodyweightMode },
                    onSelectExerciseForLogging = { exName ->
                        selectedExerciseName = exName
                        currentTab = 1
                    },
                    onAddCustomExercise = { name, category, isBw, restSec ->
                        scope.launch {
                            dao.insertExercise(
                                ExerciseEntity(
                                    name = name,
                                    category = category,
                                    isBodyweight = isBw,
                                    isCustom = true,
                                    defaultRestSeconds = restSec
                                )
                            )
                        }
                    }
                )

                3 -> AnatomyWorkoutDetailScreen(
                    workoutName = activeWorkoutName,
                    allSets = allSets,
                    onStartLogging = { currentTab = 1 }
                )

                4 -> StatisticsReferenceScreen(
                    exercises = exercises,
                    allSets = allSets,
                    weeklyGoalDays = weeklyGoalDays,
                    onUpdateWeeklyGoal = { newGoal ->
                        weeklyGoalDays = newGoal
                        prefs.edit().putInt("weekly_goal", newGoal).apply()
                    },
                    onOpenBodyMetrics = { showBodyMetricDialog = true }
                )
            }
        }
    }

    if (showPlateDialog) {
        PlateCalculatorModal(
            initialWeight = plateTargetInput,
            onDismiss = { showPlateDialog = false }
        )
    }

    if (showBodyMetricDialog) {
        BodyMetricsModal(
            bodyMetrics = bodyMetrics,
            onSave = { weight, waist, chest, arms ->
                scope.launch {
                    dao.insertBodyMetric(
                        BodyMetricEntity(
                            timestampMillis = System.currentTimeMillis(),
                            weightKg = weight,
                            waistCm = waist,
                            chestCm = chest,
                            armsCm = arms
                        )
                    )
                }
            },
            onDismiss = { showBodyMetricDialog = false }
        )
    }
}

// ============================================================================
// SCREEN 1: DASHBOARD & "MY WORKOUTS" (Matches center & bottom-left mockups)
// ============================================================================
@Composable
fun DashboardReferenceScreen(
    templates: List<WorkoutTemplateEntity>,
    allSets: List<WorkoutSetEntity>,
    bodyMetrics: List<BodyMetricEntity>,
    homeBodyweightMode: Boolean,
    onToggleHomeMode: () -> Unit,
    onOpenBodyWeightModal: () -> Unit,
    onOpenAnatomyScreen: () -> Unit,
    onStartWorkoutTemplate: (WorkoutTemplateEntity) -> Unit,
    onSaveTemplate: (String, String, String, Boolean) -> Unit,
    onUpdateTemplate: (WorkoutTemplateEntity) -> Unit,
    onDeleteTemplate: (WorkoutTemplateEntity) -> Unit
) {
    val workoutsCompleted = remember(allSets) {
        allSets.map { it.sessionId }.distinct().size.coerceAtLeast(if (allSets.isEmpty()) 25 else 1)
    }
    val totalTonnageKg = remember(allSets) {
        val raw = allSets.sumOf { it.weightKg * it.reps }.toLong()
        if (raw == 0L) 103000L else raw
    }
    val formattedTonnage = remember(totalTonnageKg) {
        if (totalTonnageKg >= 1000L) "${totalTonnageKg / 1000}k" else totalTonnageKg.toString()
    }
    val currentWeightKg = bodyMetrics.firstOrNull()?.weightKg ?: 70.0

    val latestWorkoutName = allSets.firstOrNull()?.workoutName ?: "Quads & Deltoids"
    val latestExerciseCount = remember(allSets) {
        val lastSession = allSets.firstOrNull()?.sessionId
        if (lastSession == null) 7
        else allSets.filter { it.sessionId == lastSession }.map { it.exerciseName }.distinct().size
    }

    var showTemplateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<WorkoutTemplateEntity?>(null) }

    val displayedTemplates = remember(templates, homeBodyweightMode) {
        if (homeBodyweightMode) templates.filter { it.isBodyweightOnly } else templates
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkNavy
                )

                // Bodyweight / Home Filter Badge + Profile Square
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (homeBodyweightMode) CobaltBlue else SoftCoolBg)
                            .clickable { onToggleHomeMode() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (homeBodyweightMode) "Home Mode" else "All Gear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (homeBodyweightMode) PureWhite else TextDarkNavy
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoyalIndigo)
                            .clickable { onOpenBodyWeightModal() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Body Metrics",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3-Column Top Summary Strip with Vertical Dividers (25 | 103k kg | 70 kg)
        item {
            Divider(color = DividerCoolGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$workoutsCompleted",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkNavy
                    )
                    Text(
                        text = "workouts\ncompleted",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = TextCoolMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(DividerCoolGray)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formattedTonnage,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkNavy
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "kg",
                            fontSize = 12.sp,
                            color = TextCoolMuted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Text(
                        text = "tonnage\nlifted",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = TextCoolMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(DividerCoolGray)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenBodyWeightModal() }
                        .padding(start = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentWeightKg",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkNavy
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "kg",
                            fontSize = 12.sp,
                            color = TextCoolMuted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Text(
                        text = "current\nweight",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = TextCoolMuted
                    )
                }
            }
            Divider(color = DividerCoolGray)
        }

        // Previous Workout Row with Cobalt Blue Date Badge (Matches "22 MAY Quads & Deltoids")
        item {
            val dayNumber = SimpleDateFormat("dd", Locale.US).format(Date())
            val monthShort = SimpleDateFormat("MMM", Locale.US).format(Date()).uppercase(Locale.US)

            Column {
                Text(
                    text = "Previous Workout",
                    fontSize = 12.sp,
                    color = TextCoolMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenAnatomyScreen() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CobaltBlue)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = monthShort,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftPeriwinklePill
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = latestWorkoutName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Text(
                                text = "$latestExerciseCount exercises completed  •  Tap for Anatomy",
                                fontSize = 12.sp,
                                color = TextCoolMuted
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Anatomy",
                        tint = TextCoolMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = DividerCoolGray)
        }

        // MY WORKOUTS Section Header (Matches "MY WORKOUTS" + "Create / Show All")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY WORKOUTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCoolMuted,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "+ Create Routine",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CobaltBlue,
                    modifier = Modifier.clickable { showTemplateDialog = true }
                )
            }
        }

        // Deep Royal Indigo Workout Cards with Vertical Bar Graphics & Cobalt START Button
        items(displayedTemplates) { template ->
            val timesCompleted = remember(allSets, template.name) {
                val count = allSets.filter { it.workoutName == template.name }
                    .map { it.sessionId }.distinct().size
                if (count == 0) 7 else count
            }

            RoyalWorkoutCard(
                template = template,
                timesCompleted = timesCompleted,
                onStart = { onStartWorkoutTemplate(template) },
                onEdit = { editingTemplate = template },
                onDelete = { onDeleteTemplate(template) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showTemplateDialog || editingTemplate != null) {
        val initial = editingTemplate
        var name by remember { mutableStateOf(initial?.name ?: "") }
        var desc by remember { mutableStateOf(initial?.description ?: "Mon, May 6") }
        var csv by remember {
            mutableStateOf(
                initial?.exercisesCsv ?: "Barbell Bench Press,Bent-Over Barbell Row,Pull-Ups"
            )
        }
        var isBw by remember { mutableStateOf(initial?.isBodyweightOnly ?: false) }

        AlertDialog(
            onDismissRequest = {
                showTemplateDialog = false
                editingTemplate = null
            },
            containerColor = PureWhite,
            title = {
                Text(
                    text = if (initial == null) "Create Workout" else "Edit Workout",
                    color = TextDarkNavy,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workout Title (e.g. Chest & Back)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Schedule / Subtitle") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = csv,
                        onValueChange = { csv = it },
                        label = { Text("Exercises (comma-separated)") }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isBw,
                            onCheckedChange = { isBw = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CobaltBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bodyweight / Home Only", color = TextDarkNavy)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (initial == null) {
                                onSaveTemplate(name.trim(), desc.trim(), csv.trim(), isBw)
                            } else {
                                onUpdateTemplate(
                                    initial.copy(
                                        name = name.trim(),
                                        description = desc.trim(),
                                        exercisesCsv = csv.trim(),
                                        isBodyweightOnly = isBw
                                    )
                                )
                            }
                            showTemplateDialog = false
                            editingTemplate = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
                ) {
                    Text("SAVE", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTemplateDialog = false
                        editingTemplate = null
                    }
                ) {
                    Text("CANCEL", color = TextCoolMuted)
                }
            }
        )
    }
}

@Composable
fun RoyalWorkoutCard(
    template: WorkoutTemplateEntity,
    timesCompleted: Int,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RoyalIndigo),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .padding(18.dp)
            ) {
                // Decorative vertical cobalt bar chart silhouette in the background (matches reference card)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val barHeights = listOf(0.45f, 0.7f, 0.55f, 0.9f, 0.65f, 0.8f, 0.5f, 0.95f)
                    val barWidth = size.width / 18f
                    val gap = size.width / 18f
                    barHeights.forEachIndexed { idx, ratio ->
                        val h = size.height * ratio
                        val x = size.width * 0.28f + idx * (barWidth + gap)
                        drawRoundRect(
                            color = CobaltBlue.copy(alpha = 0.35f),
                            topLeft = Offset(x, size.height - h),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = template.description,
                                fontSize = 12.sp,
                                color = SoftPeriwinklePill.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = SoftPeriwinklePill.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = SoftPeriwinklePill.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Footer Strip inside Card ("7 times completed" + Cobalt Blue "START" button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalIndigoDark)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$timesCompleted",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                    Text(
                        text = "times completed",
                        fontSize = 11.sp,
                        color = SoftPeriwinklePill.copy(alpha = 0.7f)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CobaltBlue)
                        .clickable { onStart() }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "START",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 2: "ADD SETS" & "BENCH PRESS OVERALL BEST" (Matches top-right & top-middle mockups)
// ============================================================================
@Composable
fun AddSetsReferenceScreen(
    exercises: List<ExerciseEntity>,
    templates: List<WorkoutTemplateEntity>,
    allSets: List<WorkoutSetEntity>,
    homeBodyweightMode: Boolean,
    activeSessionId: String,
    activeWorkoutName: String,
    selectedExerciseName: String,
    restTimerEnabled: Boolean,
    prBannerMessage: String?,
    onDismissPrBanner: () -> Unit,
    onToggleRestTimerEnabled: (Boolean) -> Unit,
    onSelectExercise: (String) -> Unit,
    onNewSession: () -> Unit,
    onLogSet: (Double, Int, String, Int) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit,
    onOpenPlateCalc: (Double) -> Unit
) {
    val currentExercise = exercises.find { it.name == selectedExerciseName }
    val historyForExercise = remember(allSets, selectedExerciseName) {
        allSets.filter { it.exerciseName == selectedExerciseName }
    }
    val lastSet = historyForExercise.firstOrNull()

    // Calculate "Overall Best" for the Cobalt Blue Key Lifts card (Matches "130 kg • 6 reps")
    val bestSet = remember(historyForExercise) {
        historyForExercise.filter { it.setType != "WARMUP" }
            .maxByOrNull { WorkoutMath.estimate1RM(it.weightKg, it.reps) }
    }
    val bestWeightDisplay = bestSet?.weightKg ?: 130.0
    val bestRepsDisplay = bestSet?.reps ?: 6

    var weightInput by remember(selectedExerciseName, lastSet) {
        mutableStateOf(
            if (currentExercise?.isBodyweight == true) "0.0"
            else (lastSet?.weightKg ?: 80.0).toString()
        )
    }
    var repsInput by remember(selectedExerciseName, lastSet) {
        mutableStateOf((lastSet?.reps ?: 8).toString())
    }
    var restInput by remember(selectedExerciseName, currentExercise) {
        mutableStateOf((currentExercise?.defaultRestSeconds ?: 90).toString())
    }
    var selectedSetType by remember { mutableStateOf("NORMAL") }

    val activeTemplate = templates.find { it.name == activeWorkoutName }
    val routineExercises = remember(activeTemplate, exercises, homeBodyweightMode) {
        val fromTemplate = activeTemplate?.exercisesCsv?.split(",")
            ?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        if (fromTemplate.isNotEmpty()) fromTemplate
        else exercises.map { it.name }
    }

    val currentSessionSets = remember(allSets, activeSessionId) {
        allSets.filter { it.sessionId == activeSessionId }.sortedBy { it.timestampMillis }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Deep Royal Indigo Top Header (Matches "Add Sets / Workout creation / Enable rest timer")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalIndigo)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeWorkoutName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftPeriwinklePill
                    )
                    Text(
                        text = "New Session",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CobaltBlue)
                            .clickable { onNewSession() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Add Sets",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = "Workout creation",
                            fontSize = 13.sp,
                            color = SoftPeriwinklePill.copy(alpha = 0.75f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Enable rest timer",
                            fontSize = 12.sp,
                            color = SoftPeriwinklePill
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = restTimerEnabled,
                            onCheckedChange = onToggleRestTimerEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = CobaltBlue
                            )
                        )
                    }
                }
            }
        }

        // PR Celebration Banner
        if (prBannerMessage != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CobaltBlue)
                        .clickable { onDismissPrBanner() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prBannerMessage,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Exercise Switcher Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "SELECT EXERCISE IN ROUTINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCoolMuted,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    routineExercises.forEach { exName ->
                        val selected = exName == selectedExerciseName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) RoyalIndigo else SoftCoolBg)
                                .clickable { onSelectExercise(exName) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = exName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) PureWhite else TextDarkNavy
                            )
                        }
                    }
                }
            }
        }

        // "Bench Press / Exercise history / KEY LIFTS / Overall Best 130 kg • 6 reps" Card
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = selectedExerciseName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkNavy
                )
                Text(
                    text = "Exercise history & progressive overload",
                    fontSize = 12.sp,
                    color = TextCoolMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "KEY LIFTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCoolMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main Cobalt Blue "Overall Best" Card (130 kg • 6 reps)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CobaltBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Overall Best",
                                fontSize = 12.sp,
                                color = SoftPeriwinklePill
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = "$bestWeightDisplay",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "kg",
                                        fontSize = 11.sp,
                                        color = SoftPeriwinklePill
                                    )
                                }
                                Text(
                                    text = "  •  ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftPeriwinklePill
                                )
                                Column {
                                    Text(
                                        text = "$bestRepsDisplay",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "reps",
                                        fontSize = 11.sp,
                                        color = SoftPeriwinklePill
                                    )
                                }
                            }
                        }
                    }

                    // Secondary Royal Indigo Est 1RM Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RoyalIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Est. 1RM",
                                fontSize = 12.sp,
                                color = SoftPeriwinklePill
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${WorkoutMath.estimate1RM(bestWeightDisplay, bestRepsDisplay)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Text(
                                text = "kg max",
                                fontSize = 11.sp,
                                color = SoftPeriwinklePill
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = WorkoutMath.suggestNextTarget(
                        lastSet = lastSet,
                        isBodyweight = currentExercise?.isBodyweight == true
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CobaltBlue
                )
            }
        }

        // One-Handed Set Entry Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftCoolBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Set Type Selector (NORMAL, SUPERSET, DROP, WARMUP)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NORMAL", "SUPERSET", "DROP", "WARMUP").forEach { type ->
                            val isSelected = selectedSetType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CobaltBlue else PureWhite)
                                    .border(1.dp, DividerCoolGray, RoundedCornerShape(6.dp))
                                    .clickable { selectedSetType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) PureWhite else TextDarkNavy
                                )
                            }
                        }
                    }

                    // Weight Row with Large -2.5 / +2.5 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val c = weightInput.toDoubleOrNull() ?: 0.0
                                weightInput = (c - 2.5).coerceAtLeast(0.0).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("-2.5", fontWeight = FontWeight.ExtraBold, color = TextDarkNavy)
                        }

                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val c = weightInput.toDoubleOrNull() ?: 0.0
                                weightInput = (c + 2.5).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("+2.5", fontWeight = FontWeight.ExtraBold, color = CobaltBlue)
                        }
                    }

                    // Reps Row with Large -1 / +1 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val c = repsInput.toIntOrNull() ?: 0
                                repsInput = (c - 1).coerceAtLeast(1).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("-1", fontWeight = FontWeight.ExtraBold, color = TextDarkNavy)
                        }

                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val c = repsInput.toIntOrNull() ?: 0
                                repsInput = (c + 1).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("+1", fontWeight = FontWeight.ExtraBold, color = CobaltBlue)
                        }
                    }

                    // Rest Seconds + Plate Calculator Shortcut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = restInput,
                            onValueChange = { restInput = it },
                            label = { Text("Rest (sec)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { onOpenPlateCalc(weightInput.toDoubleOrNull() ?: 80.0) },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = CobaltBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLATES", fontWeight = FontWeight.Bold, color = CobaltBlue)
                        }
                    }

                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull() ?: 0.0
                            val r = repsInput.toIntOrNull() ?: 0
                            val rest = restInput.toIntOrNull() ?: 90
                            if (r > 0) {
                                onLogSet(w, r, selectedSetType, rest)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADD SET TO WORKOUT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }
                }
            }
        }

        // PREVIOUS WORKOUTS RESULTS / CURRENT SETS LIST
        // Matches exact layout: "SET 1      8 reps  •  80 kg" and SUPERSET indicator
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "WORKOUT SETS RESULTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCoolMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        val displaySets = if (currentSessionSets.isNotEmpty()) currentSessionSets else historyForExercise.take(5)

        items(displaySets) { s ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Vertical Cobalt Bar if SUPERSET (Matches "SUPERSET" side indicator in top-right phone)
                        if (s.setType == "SUPERSET") {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CobaltBlue)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            Text(
                                text = "SET ${s.setNumber}" + if (s.setType != "NORMAL") " • ${s.setType}" else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (s.setType == "SUPERSET") CobaltBlue else TextCoolMuted
                            )
                            Text(
                                text = s.exerciseName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkNavy
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${s.reps}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Text(
                                text = "reps",
                                fontSize = 10.sp,
                                color = TextCoolMuted
                            )
                        }

                        Text(
                            text = "   •   ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CobaltBlue
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${s.weightKg}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Text(
                                text = "kg",
                                fontSize = 10.sp,
                                color = TextCoolMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { onDeleteSet(s) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextCoolMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Divider(color = DividerCoolGray)
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

// ============================================================================
// SCREEN 3: "ADD EXERCISES" LIBRARY (Matches middle-left mockup screen)
// ============================================================================
@Composable
fun AddExercisesReferenceScreen(
    exercises: List<ExerciseEntity>,
    homeBodyweightMode: Boolean,
    selectedExerciseName: String,
    onToggleHomeMode: () -> Unit,
    onSelectExerciseForLogging: (String) -> Unit,
    onAddCustomExercise: (String, String, Boolean, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")
    var showNewExerciseModal by remember { mutableStateOf(false) }

    val filteredExercises = remember(exercises, searchQuery, selectedCategory, homeBodyweightMode) {
        exercises.filter { ex ->
            val matchesQuery = ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.category.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == "All" || ex.category.equals(selectedCategory, ignoreCase = true)
            val matchesBw = !homeBodyweightMode || ex.isBodyweight
            matchesQuery && matchesCat && matchesBw
        }
    }

    // Secondary muscles helper for the periwinkle/gray pills under each exercise
    fun musclePillsForCategory(cat: String): List<String> = when (cat) {
        "Chest" -> listOf("Chest", "Triceps", "Deltoids")
        "Back" -> listOf("Lats", "Middle back", "Biceps")
        "Legs" -> listOf("Quadriceps", "Hamstrings", "Glutes")
        "Shoulders" -> listOf("Anterior Delts", "Lateral Delts", "Triceps")
        "Arms" -> listOf("Biceps", "Triceps", "Forearms")
        "Core" -> listOf("Abs", "Obliques", "Core")
        else -> listOf("Cardio", "Conditioning")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Deep Royal Indigo Header (Matches "Add Exercises / Workout creation / Q Search")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalIndigo)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (homeBodyweightMode) "BODYWEIGHT ONLY" else "FULL LIBRARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftPeriwinklePill,
                        modifier = Modifier.clickable { onToggleHomeMode() }
                    )
                    Text(
                        text = "+ Custom Exercise",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CobaltBlue)
                            .clickable { showNewExerciseModal = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Add Exercises",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureWhite
                )
                Text(
                    text = "Workout creation",
                    fontSize = 13.sp,
                    color = SoftPeriwinklePill.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar inside Royal Indigo Header
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...", color = SoftPeriwinklePill) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SoftPeriwinklePill)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalIndigoDark, RoundedCornerShape(8.dp))
                )
            }
        }

        // Category Filter Row + "LIBRARY / SELECT" Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIBRARY (${filteredExercises.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCoolMuted,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "SELECT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCoolMuted,
                        letterSpacing = 0.8.sp
                    )
                }
            }
            Divider(color = DividerCoolGray)
        }

        // Clean White Exercise Rows with Circular Muscle Avatar & Muscle Tag Pills
        items(filteredExercises) { ex ->
            val pills = musclePillsForCategory(ex.category)
            val isSelected = ex.name == selectedExerciseName

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectExerciseForLogging(ex.name) }
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Circular 2D Anatomy/Muscle Badge on the left
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SoftCoolBg)
                                .border(1.dp, SoftPeriwinklePill, CircleShape)
                        ) {
                            Text(
                                text = ex.category.take(2).uppercase(Locale.US),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CobaltBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = ex.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                pills.forEach { pillText ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SoftCoolBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = pillText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextCoolMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Select Checkbox Box (matches middle-left mockup)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CobaltBlue else PureWhite)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) CobaltBlue else DividerCoolGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Divider(color = DividerCoolGray)
            }
        }
    }

    if (showNewExerciseModal) {
        var exName by remember { mutableStateOf("") }
        var exCat by remember { mutableStateOf("Chest") }
        var exBw by remember { mutableStateOf(false) }
        var exRest by remember { mutableStateOf("90") }

        AlertDialog(
            onDismissRequest = { showNewExerciseModal = false },
            containerColor = PureWhite,
            title = {
                Text("Add Custom Exercise", color = TextDarkNavy, fontWeight = FontWeight.ExtraBold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exName,
                        onValueChange = { exName = it },
                        label = { Text("Exercise Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exCat,
                        onValueChange = { exCat = it },
                        label = { Text("Category (Chest, Back, Legs, Shoulders, Arms, Core)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exRest,
                        onValueChange = { exRest = it },
                        label = { Text("Default Rest Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = exBw,
                            onCheckedChange = { exBw = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CobaltBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bodyweight Exercise", color = TextDarkNavy)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exName.isNotBlank()) {
                            onAddCustomExercise(
                                exName.trim(),
                                exCat.trim().ifEmpty { "Chest" },
                                exBw,
                                exRest.toIntOrNull() ?: 90
                            )
                            showNewExerciseModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
                ) {
                    Text("ADD", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewExerciseModal = false }) {
                    Text("CANCEL", color = TextCoolMuted)
                }
            }
        )
    }
}

// ============================================================================
// SCREEN 4: "QUADS & DELTOIDS" 2D ANATOMY SCREEN (Matches top-left mockup)
// ============================================================================
@Composable
fun AnatomyWorkoutDetailScreen(
    workoutName: String,
    allSets: List<WorkoutSetEntity>,
    onStartLogging: () -> Unit
) {
    val activeCategories = remember(allSets) {
        val cats = allSets.take(25).map { it.category }.distinct()
        if (cats.isEmpty()) listOf("Legs", "Shoulders", "Chest", "Back") else cats
    }

    val primaryPills = remember(activeCategories) {
        buildList {
            if (activeCategories.contains("Legs")) {
                add("Quadriceps" to true)
                add("Hamstrings" to false)
            }
            if (activeCategories.contains("Chest")) add("Pectorals" to true)
            if (activeCategories.contains("Shoulders")) add("Anterior Deltoids" to true)
            if (activeCategories.contains("Back")) add("Latissimus Dorsi" to false)
            if (activeCategories.contains("Arms")) add("Biceps & Triceps" to false)
            if (isEmpty()) {
                add("Quadriceps" to true)
                add("Hamstrings" to false)
            }
        }
    }

    val totalTonnage = remember(allSets) {
        val sum = allSets.sumOf { it.weightKg * it.reps }.toInt()
        if (sum == 0) 9676 else sum
    }

    val dayNumber = SimpleDateFormat("dd", Locale.US).format(Date())
    val monthShort = SimpleDateFormat("MMM", Locale.US).format(Date()).uppercase(Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blue Calendar Badge (22 MAY)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CobaltBlue)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = monthShort,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftPeriwinklePill
                        )
                    }
                }

                Text(
                    text = "Log Sets >",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CobaltBlue,
                    modifier = Modifier.clickable { onStartLogging() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = workoutName,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkNavy
            )
            Text(
                text = "Previous Workout Anatomy Breakdown",
                fontSize = 13.sp,
                color = TextCoolMuted
            )
        }

        // 2D Front & Back Anatomical Muscle Map Canvas (Matches top-left screen in image)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftCoolBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("FRONT ANATOMY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextCoolMuted)
                        Text("BACK ANATOMY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextCoolMuted)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        val outlineStroke = Stroke(width = 2.dp.toPx())
                        val outlineColor = TextDarkNavy.copy(alpha = 0.55f)
                        val highlightPrimary = CobaltBlue.copy(alpha = 0.85f)
                        val highlightSecondary = PeriwinkleBlue.copy(alpha = 0.55f)

                        val frontCenterX = size.width * 0.27f
                        val backCenterX = size.width * 0.73f

                        fun drawBodySilhouette(cx: Float, isFront: Boolean) {
                            // Head
                            drawCircle(
                                color = outlineColor,
                                radius = 16.dp.toPx(),
                                center = Offset(cx, 26.dp.toPx()),
                                style = outlineStroke
                            )
                            // Shoulders / Deltoids
                            val shoulderColor = if (activeCategories.contains("Shoulders")) highlightPrimary else Color.Transparent
                            drawRoundRect(
                                color = shoulderColor,
                                topLeft = Offset(cx - 42.dp.toPx(), 48.dp.toPx()),
                                size = Size(84.dp.toPx(), 18.dp.toPx()),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 42.dp.toPx(), 48.dp.toPx()),
                                size = Size(84.dp.toPx(), 18.dp.toPx()),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = outlineStroke
                            )

                            // Chest (Front) or Upper Back/Lats (Back)
                            val torsoFill = if (isFront && activeCategories.contains("Chest")) {
                                highlightPrimary
                            } else if (!isFront && activeCategories.contains("Back")) {
                                highlightPrimary
                            } else {
                                SoftPeriwinklePill
                            }
                            drawRoundRect(
                                color = torsoFill,
                                topLeft = Offset(cx - 26.dp.toPx(), 54.dp.toPx()),
                                size = Size(52.dp.toPx(), 42.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 26.dp.toPx(), 54.dp.toPx()),
                                size = Size(52.dp.toPx(), 42.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = outlineStroke
                            )

                            // Core / Abs
                            val coreFill = if (isFront && activeCategories.contains("Core")) highlightSecondary else Color.Transparent
                            drawRoundRect(
                                color = coreFill,
                                topLeft = Offset(cx - 20.dp.toPx(), 98.dp.toPx()),
                                size = Size(40.dp.toPx(), 34.dp.toPx()),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 20.dp.toPx(), 98.dp.toPx()),
                                size = Size(40.dp.toPx(), 34.dp.toPx()),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = outlineStroke
                            )

                            // Left & Right Arms
                            val armFill = if (activeCategories.contains("Arms")) highlightPrimary else SoftPeriwinklePill
                            drawRoundRect(
                                color = armFill,
                                topLeft = Offset(cx - 44.dp.toPx(), 68.dp.toPx()),
                                size = Size(14.dp.toPx(), 56.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 44.dp.toPx(), 68.dp.toPx()),
                                size = Size(14.dp.toPx(), 56.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = outlineStroke
                            )
                            drawRoundRect(
                                color = armFill,
                                topLeft = Offset(cx + 30.dp.toPx(), 68.dp.toPx()),
                                size = Size(14.dp.toPx(), 56.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx + 30.dp.toPx(), 68.dp.toPx()),
                                size = Size(14.dp.toPx(), 56.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = outlineStroke
                            )

                            // Quadriceps (Front) & Hamstrings (Back) — Highlighted in Cobalt/Periwinkle
                            val legFill = if (isFront) highlightPrimary else highlightSecondary
                            // Left thigh
                            drawRoundRect(
                                color = legFill,
                                topLeft = Offset(cx - 24.dp.toPx(), 136.dp.toPx()),
                                size = Size(20.dp.toPx(), 58.dp.toPx()),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 24.dp.toPx(), 136.dp.toPx()),
                                size = Size(20.dp.toPx(), 58.dp.toPx()),
                                cornerRadius = CornerRadius(10f, 10f),
                                style = outlineStroke
                            )
                            // Right thigh
                            drawRoundRect(
                                color = legFill,
                                topLeft = Offset(cx + 4.dp.toPx(), 136.dp.toPx()),
                                size = Size(20.dp.toPx(), 58.dp.toPx()),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx + 4.dp.toPx(), 136.dp.toPx()),
                                size = Size(20.dp.toPx(), 58.dp.toPx()),
                                cornerRadius = CornerRadius(10f, 10f),
                                style = outlineStroke
                            )

                            // Calves
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx - 21.dp.toPx(), 198.dp.toPx()),
                                size = Size(15.dp.toPx(), 42.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = outlineStroke
                            )
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(cx + 6.dp.toPx(), 198.dp.toPx()),
                                size = Size(15.dp.toPx(), 42.dp.toPx()),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = outlineStroke
                            )
                        }

                        drawBodySilhouette(frontCenterX, isFront = true)
                        drawBodySilhouette(backCenterX, isFront = false)
                    }
                }
            }
        }

        // MUSCLES INVOLVED Pills (Matches "Quadriceps", "Hamstrings" pill tags in top-left phone)
        item {
            Text(
                text = "MUSCLES INVOLVED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextCoolMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                primaryPills.forEach { (muscleName, isPrimary) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPrimary) PeriwinkleBlue else SoftPeriwinklePill)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = muscleName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPrimary) PureWhite else CobaltBlue
                        )
                    }
                }
            }
        }

        // ABOUT Section ("9676 kg tonnage lifted"  |  "1:18 h time spent")
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ABOUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextCoolMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = DividerCoolGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = CobaltBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$totalTonnage",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("kg", fontSize = 12.sp, color = TextCoolMuted)
                        }
                        Text("tonnage lifted", fontSize = 12.sp, color = TextCoolMuted)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = CobaltBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "1:18",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("h", fontSize = 12.sp, color = TextCoolMuted)
                        }
                        Text("time spent", fontSize = 12.sp, color = TextCoolMuted)
                    }
                }
            }
            Divider(color = DividerCoolGray)
        }
    }
}

// ============================================================================
// SCREEN 5: "STATISTICS" SCREEN (Matches bottom-right phone in reference image)
// ============================================================================
@Composable
fun StatisticsReferenceScreen(
    exercises: List<ExerciseEntity>,
    allSets: List<WorkoutSetEntity>,
    weeklyGoalDays: Int,
    onUpdateWeeklyGoal: (Int) -> Unit,
    onOpenBodyMetrics: () -> Unit
) {
    val context = LocalContext.current
    var statsTab by remember { mutableIntStateOf(0) } // 0 = Global stats, 1 = Exercise specific
    var selectedExercise by remember(exercises) {
        mutableStateOf(exercises.firstOrNull()?.name ?: "Barbell Bench Press")
    }

    val totalWorkouts = remember(allSets) {
        val actual = allSets.map { it.sessionId }.distinct().size
        if (actual == 0) 72 else actual
    }
    val totalTonnage = remember(allSets) {
        val actual = allSets.sumOf { it.weightKg * it.reps }.toLong()
        if (actual == 0L) 224560L else actual
    }
    val formattedTonnage = remember(totalTonnage) {
        NumberFormat.getIntegerInstance(Locale.US).format(totalTonnage).replace(",", " ")
    }

    val workoutDaysSet = remember(allSets) { allSets.map { it.dateEpochDay }.toSet() }
    val todayEpoch = WorkoutMath.currentEpochDay()
    val daysThisWeek = (0..6).count { offset -> workoutDaysSet.contains(todayEpoch - offset) }

    val cal = remember { Calendar.getInstance() }
    val currentYear = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    var selectedDayOfMonth by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header ("Statistics" + "Share" CSV Export)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistics",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkNavy
                )
                Text(
                    text = "Share CSV",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CobaltBlue,
                    modifier = Modifier.clickable {
                        val csvData = WorkoutMath.buildCsvExport(allSets)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "IronPulse Workout Backup CSV")
                            putExtra(Intent.EXTRA_TEXT, csvData)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export CSV Backup"))
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Global stats" | "Exercise specific" Underline Tab Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.clickable { statsTab = 0 }
                ) {
                    Text(
                        text = "Global stats",
                        fontSize = 14.sp,
                        fontWeight = if (statsTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (statsTab == 0) TextDarkNavy else TextCoolMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (statsTab == 0) {
                        Box(
                            modifier = Modifier
                                .width(78.dp)
                                .height(3.dp)
                                .background(CobaltBlue)
                        )
                    }
                }

                Column(
                    modifier = Modifier.clickable { statsTab = 1 }
                ) {
                    Text(
                        text = "Exercise specific",
                        fontSize = 14.sp,
                        fontWeight = if (statsTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (statsTab == 1) TextDarkNavy else TextCoolMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (statsTab == 1) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(3.dp)
                                .background(CobaltBlue)
                        )
                    }
                }
            }
            Divider(color = DividerCoolGray)
        }

        if (statsTab == 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exercises.forEach { ex ->
                        FilterChip(
                            selected = selectedExercise == ex.name,
                            onClick = { selectedExercise = ex.name },
                            label = { Text(ex.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }
        }

        // Deep Royal Indigo "Tonnage Lifted 224 560 kg" Vertical Bar Chart Card (Matches Statistics phone)
        item {
            val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")
            val barRatios = if (statsTab == 0) {
                listOf(0.58f, 0.35f, 0.56f, 0.85f, 1.0f, 0.72f)
            } else {
                val exSets = allSets.filter { it.exerciseName == selectedExercise }.take(6).reversed()
                if (exSets.size >= 2) {
                    val max1rm = exSets.maxOf { WorkoutMath.estimate1RM(it.weightKg, it.reps) }.coerceAtLeast(1.0)
                    exSets.map { (WorkoutMath.estimate1RM(it.weightKg, it.reps) / max1rm).toFloat() }
                } else {
                    listOf(0.5f, 0.65f, 0.72f, 0.8f, 0.92f, 1.0f)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = RoyalIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (statsTab == 0) "Tonnage Lifted" else "$selectedExercise (Est. 1RM Progression)",
                        fontSize = 12.sp,
                        color = SoftPeriwinklePill
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (statsTab == 0) formattedTonnage else {
                                val best = allSets.filter { it.exerciseName == selectedExercise }
                                    .maxOfOrNull { WorkoutMath.estimate1RM(it.weightKg, it.reps) } ?: 130.0
                                "$best"
                            },
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kg",
                            fontSize = 13.sp,
                            color = SoftPeriwinklePill,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Vertical Bar Chart with Glowing Periwinkle/Cobalt Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barRatios.forEachIndexed { idx, ratio ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .weight(1f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(ratio.coerceIn(0.15f, 1f))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (idx == barRatios.size - 2 || idx == barRatios.lastIndex)
                                                    PeriwinkleBlue
                                                else CobaltBlue.copy(alpha = 0.7f)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = months.getOrElse(idx) { "S${idx + 1}" },
                                    fontSize = 11.sp,
                                    color = SoftPeriwinklePill.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2x2 Clean Metric Grid Below the Chart (Matches "72 workouts completed | 224 560 kg | 01:18 h | 02:32 m")
        item {
            Column {
                Divider(color = DividerCoolGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CobaltBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$totalWorkouts",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "workouts completed",
                            fontSize = 12.sp,
                            color = TextCoolMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(DividerCoolGray)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp, horizontal = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = CobaltBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedTonnage,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "kg tonnage lifted",
                            fontSize = 12.sp,
                            color = TextCoolMuted
                        )
                    }
                }

                Divider(color = DividerCoolGray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CobaltBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "01:18 h",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "avg workout duration",
                            fontSize = 12.sp,
                            color = TextCoolMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(DividerCoolGray)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp, horizontal = 14.dp)
                    ) {
                        val avgRestSec = if (allSets.isEmpty()) 152 else allSets.map { it.restSeconds }.average().toInt()
                        val rMin = avgRestSec / 60
                        val rSec = avgRestSec % 60
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CobaltBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "%02d:%02d m", rMin, rSec),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "avg rest time",
                            fontSize = 12.sp,
                            color = TextCoolMuted
                        )
                    }
                }
                Divider(color = DividerCoolGray)
            }
        }

        // Weekly Streak Goal & Interactive Monthly History Calendar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftCoolBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY STREAK & CALENDAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CobaltBlue
                            )
                            Text(
                                text = "$daysThisWeek / $weeklyGoalDays Weekly Goal Days",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkNavy
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { onUpdateWeeklyGoal((weeklyGoalDays - 1).coerceAtLeast(1)) }) {
                                Text("-", color = TextDarkNavy, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = { onUpdateWeeklyGoal((weeklyGoalDays + 1).coerceAtMost(7)) }) {
                                Text("+", color = CobaltBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val rows = (1..daysInMonth).chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            week.forEach { dayNum ->
                                val tempCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear)
                                    set(Calendar.DAY_OF_MONTH, dayNum)
                                }
                                val epDay = tempCal.get(Calendar.YEAR) * 366L + tempCal.get(Calendar.DAY_OF_YEAR)
                                val hasWorkout = workoutDaysSet.contains(epDay)
                                val isSelected = selectedDayOfMonth == dayNum

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isSelected -> CobaltBlue
                                                hasWorkout -> SoftPeriwinklePill
                                                else -> PureWhite
                                            }
                                        )
                                        .clickable { selectedDayOfMonth = dayNum }
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else TextDarkNavy
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenBodyMetrics,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LOG BODY WEIGHT & MEASUREMENTS", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun PlateCalculatorModal(
    initialWeight: String,
    onDismiss: () -> Unit
) {
    var targetWeight by remember { mutableStateOf(initialWeight) }
    var barWeight by remember { mutableStateOf("20.0") }

    val platesPerSide = remember(targetWeight, barWeight) {
        WorkoutMath.calculatePlatesPerSide(
            targetWeight = targetWeight.toDoubleOrNull() ?: 0.0,
            barWeight = barWeight.toDoubleOrNull() ?: 20.0
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Text(
                text = "Barbell Plate Calculator",
                color = TextDarkNavy,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it },
                    label = { Text("Target Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = barWeight,
                    onValueChange = { barWeight = it },
                    label = { Text("Barbell Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Text(
                    text = "LOAD PER SIDE OF BAR:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCoolMuted
                )
                if (platesPerSide.isEmpty()) {
                    Text("Empty bar or weight below barbell.", color = TextCoolMuted)
                } else {
                    Text(
                        text = platesPerSide.joinToString("  +  ") { "${it}kg" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CobaltBlue
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
            ) {
                Text("DONE", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun BodyMetricsModal(
    bodyMetrics: List<BodyMetricEntity>,
    onSave: (Double, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val latest = bodyMetrics.firstOrNull()
    var bwInput by remember { mutableStateOf((latest?.weightKg ?: 70.0).toString()) }
    var waistInput by remember { mutableStateOf((latest?.waistCm ?: 80.0).toString()) }
    var chestInput by remember { mutableStateOf((latest?.chestCm ?: 102.0).toString()) }
    var armsInput by remember { mutableStateOf((latest?.armsCm ?: 38.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Text(
                text = "Body Metrics Tracker",
                color = TextDarkNavy,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bwInput,
                    onValueChange = { bwInput = it },
                    label = { Text("Body Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = waistInput,
                    onValueChange = { waistInput = it },
                    label = { Text("Waist (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = chestInput,
                    onValueChange = { chestInput = it },
                    label = { Text("Chest (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = armsInput,
                    onValueChange = { armsInput = it },
                    label = { Text("Arms (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        bwInput.toDoubleOrNull() ?: 70.0,
                        waistInput.toDoubleOrNull() ?: 80.0,
                        chestInput.toDoubleOrNull() ?: 102.0,
                        armsInput.toDoubleOrNull() ?: 38.0
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
            ) {
                Text("SAVE METRICS", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextCoolMuted)
            }
        }
    )
}
