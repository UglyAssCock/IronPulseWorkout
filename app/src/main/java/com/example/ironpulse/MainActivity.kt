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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// High-Contrast Dark Slate Color Palette
val SlateBackground = Color(0xFF0B0F19)
val SlateSurface = Color(0xFF151C2C)
val SlateCard = Color(0xFF1E293B)
val EmeraldAccent = Color(0xFF10B981)
val AmberGold = Color(0xFFF59E0B)
val PurpleBadge = Color(0xFFA855F7)
val CrimsonDanger = Color(0xFFEF4444)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val IronPulseDarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = SlateBackground,
    secondary = AmberGold,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
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
            .setContentText("Counting down even with screen off...")
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
            .setContentTitle("Rest Complete! Time for your next set")
            .setContentText("Ready for $exerciseName")
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
            MaterialTheme(colorScheme = IronPulseDarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SlateBackground
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

    // Seed initial exercises & templates once
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

    var currentTab by remember { mutableIntStateOf(0) }
    var homeBodyweightMode by remember { mutableStateOf(false) }
    var weeklyGoalDays by remember { mutableIntStateOf(prefs.getInt("weekly_goal", 4)) }

    // Active Workout Session State
    var activeSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var activeWorkoutName by remember { mutableStateOf("Push Day (Chest, Shoulders, Triceps)") }
    var selectedExerciseName by remember { mutableStateOf("Barbell Bench Press") }

    // Built-in Rest Timer State
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var restTimerRunning by remember { mutableStateOf(false) }
    var restExerciseLabel by remember { mutableStateOf("") }

    // PR Celebration Banner State
    var prBannerMessage by remember { mutableStateOf<String?>(null) }

    // Plate Calculator Dialog State
    var showPlateDialog by remember { mutableStateOf(false) }
    var plateTargetInput by remember { mutableStateOf("100") }

    // Rest Timer Coroutine
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
        containerColor = SlateBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = EmeraldAccent,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IRONPULSE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showPlateDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Plate Calculator",
                                tint = AmberGold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Home / Bodyweight Mode Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (homeBodyweightMode) EmeraldAccent.copy(alpha = 0.2f) else SlateCard)
                                .clickable {
                                    homeBodyweightMode = !homeBodyweightMode
                                    val filtered = if (homeBodyweightMode) {
                                        exercises.filter { it.isBodyweight }
                                    } else {
                                        exercises
                                    }
                                    if (filtered.isNotEmpty()) {
                                        selectedExerciseName = filtered.first().name
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = if (homeBodyweightMode) EmeraldAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (homeBodyweightMode) "HOME MODE ON" else "GYM MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (homeBodyweightMode) EmeraldAccent else TextSecondary
                            )
                        }
                    }
                }

                // Persistent Rest Timer Bar (Large One-Handed Controls)
                if (restTimerRunning || restSecondsRemaining > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EmeraldAccent, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = EmeraldAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val mins = restSecondsRemaining / 60
                                val secs = restSecondsRemaining % 60
                                Text(
                                    text = String.format(Locale.US, "REST %02d:%02d", mins, secs),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = EmeraldAccent
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        restSecondsRemaining = (restSecondsRemaining - 15).coerceAtLeast(0)
                                        if (restSecondsRemaining > 0) {
                                            RestNotificationHelper.showRunningCountdown(
                                                context, restSecondsRemaining, restExerciseLabel
                                            )
                                        }
                                    }
                                ) {
                                    Text("-15s", fontSize = 12.sp, color = TextPrimary)
                                }
                                OutlinedButton(
                                    onClick = {
                                        restSecondsRemaining += 30
                                        RestNotificationHelper.showRunningCountdown(
                                            context, restSecondsRemaining, restExerciseLabel
                                        )
                                    }
                                ) {
                                    Text("+30s", fontSize = 12.sp, color = TextPrimary)
                                }
                                Button(
                                    onClick = { stopRestTimer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                                ) {
                                    Text("SKIP", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // PR Celebration Banner
                prBannerMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberGold, RoundedCornerShape(10.dp))
                            .clickable { prBannerMessage = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = SlateSurface) {
                val navItems = listOf(
                    Triple(0, "Log Set", Icons.Default.FitnessCenter),
                    Triple(1, "Routines", Icons.Default.ListAlt),
                    Triple(2, "History", Icons.Default.CalendarMonth),
                    Triple(3, "Progress", Icons.Default.ShowChart)
                )
                navItems.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateBackground,
                            selectedTextColor = EmeraldAccent,
                            indicatorColor = EmeraldAccent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
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
                0 -> WorkoutLoggerScreen(
                    exercises = exercises,
                    templates = templates,
                    allSets = allSets,
                    homeBodyweightMode = homeBodyweightMode,
                    activeSessionId = activeSessionId,
                    activeWorkoutName = activeWorkoutName,
                    selectedExerciseName = selectedExerciseName,
                    onWorkoutNameChange = { activeWorkoutName = it },
                    onSelectExercise = { selectedExerciseName = it },
                    onNewSession = {
                        activeSessionId = UUID.randomUUID().toString()
                        Toast.makeText(context, "Started Fresh Session!", Toast.LENGTH_SHORT).show()
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
                            prBannerMessage = "NEW PR on $selectedExerciseName! Est 1RM: $new1RM kg (tap to dismiss)"
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

                1 -> RoutinesAndLibraryScreen(
                    exercises = exercises,
                    templates = templates,
                    homeBodyweightMode = homeBodyweightMode,
                    onLoadTemplateIntoLogger = { template ->
                        activeSessionId = UUID.randomUUID().toString()
                        activeWorkoutName = template.name
                        val firstEx = template.exercisesCsv.split(",")
                            .map { it.trim() }
                            .firstOrNull { it.isNotEmpty() }
                        if (firstEx != null) {
                            selectedExerciseName = firstEx
                        }
                        currentTab = 0
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

                2 -> HistoryAndStreaksScreen(
                    allSets = allSets,
                    weeklyGoalDays = weeklyGoalDays,
                    onUpdateWeeklyGoal = { newGoal ->
                        weeklyGoalDays = newGoal
                        prefs.edit().putInt("weekly_goal", newGoal).apply()
                    }
                )

                3 -> ProgressAndBodyMetricsScreen(
                    exercises = exercises,
                    allSets = allSets,
                    bodyMetrics = bodyMetrics,
                    onSaveBodyMetric = { weight, waist, chest, arms ->
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
                    }
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
}

@Composable
fun WorkoutLoggerScreen(
    exercises: List<ExerciseEntity>,
    templates: List<WorkoutTemplateEntity>,
    allSets: List<WorkoutSetEntity>,
    homeBodyweightMode: Boolean,
    activeSessionId: String,
    activeWorkoutName: String,
    selectedExerciseName: String,
    onWorkoutNameChange: (String) -> Unit,
    onSelectExercise: (String) -> Unit,
    onNewSession: () -> Unit,
    onLogSet: (Double, Int, String, Int) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit,
    onOpenPlateCalc: (Double) -> Unit
) {
    val availableExercises = remember(exercises, homeBodyweightMode) {
        if (homeBodyweightMode) exercises.filter { it.isBodyweight } else exercises
    }

    val currentExercise = exercises.find { it.name == selectedExerciseName }
    val historyForExercise = remember(allSets, selectedExerciseName) {
        allSets.filter { it.exerciseName == selectedExerciseName }
    }
    val lastSet = historyForExercise.firstOrNull()

    var weightInput by remember(selectedExerciseName, lastSet) {
        mutableStateOf(
            if (currentExercise?.isBodyweight == true) "0.0"
            else (lastSet?.weightKg ?: 60.0).toString()
        )
    }
    var repsInput by remember(selectedExerciseName, lastSet) {
        mutableStateOf((lastSet?.reps ?: 10).toString())
    }
    var restInput by remember(selectedExerciseName, currentExercise) {
        mutableStateOf((currentExercise?.defaultRestSeconds ?: 90).toString())
    }
    var selectedSetType by remember { mutableStateOf("NORMAL") }

    val sessionSets = remember(allSets, activeSessionId) {
        allSets.filter { it.sessionId == activeSessionId }.sortedBy { it.timestampMillis }
    }

    val activeTemplate = templates.find { it.name == activeWorkoutName }
    val templateExercises = remember(activeTemplate) {
        activeTemplate?.exercisesCsv?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Routine Header & Quick Switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE SESSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                            Text(
                                text = activeWorkoutName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(onClick = onNewSession) {
                            Text("NEW WORKOUT", fontSize = 12.sp, color = EmeraldAccent)
                        }
                    }

                    // Routine Exercise Pills
                    if (templateExercises.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Routine Exercises (Tap to switch):",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templateExercises.forEach { exName ->
                                FilterChip(
                                    selected = selectedExerciseName == exName,
                                    onClick = { onSelectExercise(exName) },
                                    label = { Text(exName, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldAccent,
                                        selectedLabelColor = SlateBackground
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // All Exercises Quick Picker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (homeBodyweightMode) "SELECT HOME / BODYWEIGHT EXERCISE" else "SELECT EXERCISE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableExercises.forEach { ex ->
                            FilterChip(
                                selected = ex.name == selectedExerciseName,
                                onClick = { onSelectExercise(ex.name) },
                                label = { Text("${ex.name} (${ex.category})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldAccent,
                                    selectedLabelColor = SlateBackground
                                )
                            )
                        }
                    }
                }
            }
        }

        // Last Time Autofill & Progressive Overload Card
        item {
            val suggestion = WorkoutMath.suggestNextTarget(
                lastSet = lastSet,
                isBodyweight = currentExercise?.isBodyweight == true
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedExerciseName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        if (lastSet != null) {
                            Text(
                                text = "LAST: ${lastSet.weightKg} kg x ${lastSet.reps}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // One-Handed Large Controls Set Entry Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Set Type Marking: Normal, Warm-Up, Drop Set, Superset
                    Text(
                        text = "SET TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NORMAL", "WARMUP", "DROP", "SUPERSET").forEach { type ->
                            val isSelected = selectedSetType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSetType = type },
                                label = { Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (type) {
                                        "WARMUP" -> AmberGold
                                        "DROP" -> CrimsonDanger
                                        "SUPERSET" -> PurpleBadge
                                        else -> EmeraldAccent
                                    },
                                    selectedLabelColor = SlateBackground
                                )
                            )
                        }
                    }

                    // Weight Control with Large One-Handed -2.5 / +2.5 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val current = weightInput.toDoubleOrNull() ?: 0.0
                                weightInput = (current - 2.5).coerceAtLeast(0.0).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("-2.5", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
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
                                val current = weightInput.toDoubleOrNull() ?: 0.0
                                weightInput = (current + 2.5).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("+2.5", fontWeight = FontWeight.ExtraBold, color = EmeraldAccent)
                        }
                    }

                    // Reps Control with Large One-Handed -1 / +1 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val current = repsInput.toIntOrNull() ?: 0
                                repsInput = (current - 1).coerceAtLeast(1).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("-1 REP", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
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
                                val current = repsInput.toIntOrNull() ?: 0
                                repsInput = (current + 1).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("+1 REP", fontWeight = FontWeight.ExtraBold, color = EmeraldAccent)
                        }
                    }

                    // Rest Timer Input + Plate Calc Shortcut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = restInput,
                            onValueChange = { restInput = it },
                            label = { Text("Rest Timer (sec)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                onOpenPlateCalc(weightInput.toDoubleOrNull() ?: 60.0)
                            },
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLATES", color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Giant One-Handed LOG SET Button
                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull() ?: 0.0
                            val r = repsInput.toIntOrNull() ?: 0
                            val rest = restInput.toIntOrNull() ?: 90
                            if (r > 0) {
                                onLogSet(w, r, selectedSetType, rest)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SlateBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOG SET & START REST TIMER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateBackground
                        )
                    }
                }
            }
        }

        // Logged Sets in Current Session
        item {
            Text(
                text = "CURRENT SESSION SETS (${sessionSets.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        items(sessionSets) { setItem ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${setItem.exerciseName} #${setItem.setNumber}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = setItem.setType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (setItem.setType) {
                                    "WARMUP" -> AmberGold
                                    "DROP" -> CrimsonDanger
                                    "SUPERSET" -> PurpleBadge
                                    else -> EmeraldAccent
                                }
                            )
                            if (setItem.isPR) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberGold
                                )
                            }
                        }
                        Text(
                            text = "${setItem.weightKg} kg x ${setItem.reps} reps  •  Est 1RM: ${
                                WorkoutMath.estimate1RM(setItem.weightKg, setItem.reps)
                            } kg",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { onDeleteSet(setItem) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Set",
                            tint = CrimsonDanger
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutinesAndLibraryScreen(
    exercises: List<ExerciseEntity>,
    templates: List<WorkoutTemplateEntity>,
    homeBodyweightMode: Boolean,
    onLoadTemplateIntoLogger: (WorkoutTemplateEntity) -> Unit,
    onSaveTemplate: (String, String, String, Boolean) -> Unit,
    onUpdateTemplate: (WorkoutTemplateEntity) -> Unit,
    onDeleteTemplate: (WorkoutTemplateEntity) -> Unit,
    onAddCustomExercise: (String, String, Boolean, Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")

    var showNewTemplateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<WorkoutTemplateEntity?>(null) }
    var showNewExerciseDialog by remember { mutableStateOf(false) }

    val filteredExercises = remember(exercises, selectedCategory, homeBodyweightMode) {
        exercises.filter { ex ->
            val catMatch = selectedCategory == "All" || ex.category.equals(selectedCategory, ignoreCase = true)
            val bwMatch = !homeBodyweightMode || ex.isBodyweight
            catMatch && bwMatch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT TEMPLATES",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Button(
                    onClick = { showNewTemplateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SlateBackground)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NEW ROUTINE", color = SlateBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(templates) { template ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = template.description,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Row {
                            IconButton(onClick = { editingTemplate = template }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AmberGold)
                            }
                            IconButton(onClick = { onDeleteTemplate(template) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonDanger)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Exercises: ${template.exercisesCsv}",
                        fontSize = 13.sp,
                        color = EmeraldAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onLoadTemplateIntoLogger(template) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "START THIS WORKOUT",
                            color = SlateBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXERCISE LIBRARY (${filteredExercises.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                OutlinedButton(onClick = { showNewExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CUSTOM EXERCISE", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldAccent,
                            selectedLabelColor = SlateBackground
                        )
                    )
                }
            }
        }

        items(filteredExercises) { ex ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(ex.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "${ex.category}  •  Default Rest: ${ex.defaultRestSeconds}s",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = if (ex.isBodyweight) "BODYWEIGHT" else "GYM / WEIGHTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ex.isBodyweight) AmberGold else EmeraldAccent
                    )
                }
            }
        }
    }

    if (showNewTemplateDialog || editingTemplate != null) {
        val initial = editingTemplate
        var name by remember { mutableStateOf(initial?.name ?: "") }
        var desc by remember { mutableStateOf(initial?.description ?: "") }
        var csv by remember { mutableStateOf(initial?.exercisesCsv ?: "Barbell Bench Press,Push-Ups") }
        var isBw by remember { mutableStateOf(initial?.isBodyweightOnly ?: false) }

        AlertDialog(
            onDismissRequest = {
                showNewTemplateDialog = false
                editingTemplate = null
            },
            containerColor = SlateSurface,
            title = {
                Text(
                    if (initial == null) "Create Workout Routine" else "Edit Workout Routine",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Routine Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
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
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bodyweight / Home Only", color = TextPrimary)
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
                            showNewTemplateDialog = false
                            editingTemplate = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("SAVE", color = SlateBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewTemplateDialog = false
                        editingTemplate = null
                    }
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    if (showNewExerciseDialog) {
        var exName by remember { mutableStateOf("") }
        var exCat by remember { mutableStateOf("Chest") }
        var exBw by remember { mutableStateOf(false) }
        var exRest by remember { mutableStateOf("90") }

        AlertDialog(
            onDismissRequest = { showNewExerciseDialog = false },
            containerColor = SlateSurface,
            title = { Text("Add Custom Exercise", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        label = { Text("Category (Chest, Back, Legs, Shoulders, Arms, Core, Cardio)") },
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
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bodyweight Exercise (No Equipment)", color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exName.isNotBlank()) {
                            onAddCustomExercise(
                                exName.trim(),
                                exCat.trim().ifEmpty { "Custom" },
                                exBw,
                                exRest.toIntOrNull() ?: 90
                            )
                            showNewExerciseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("ADD", color = SlateBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewExerciseDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun HistoryAndStreaksScreen(
    allSets: List<WorkoutSetEntity>,
    weeklyGoalDays: Int,
    onUpdateWeeklyGoal: (Int) -> Unit
) {
    val context = LocalContext.current
    val workoutDaysSet = remember(allSets) {
        allSets.map { it.dateEpochDay }.toSet()
    }

    // Calculate current weekly active days (last 7 days)
    val todayEpoch = WorkoutMath.currentEpochDay()
    val daysLast7 = (0..6).count { offset -> workoutDaysSet.contains(todayEpoch - offset) }

    // Calculate consecutive day or active streak
    val totalUniqueWorkoutDays = workoutDaysSet.size

    // Current Month Calendar Grid
    val cal = remember { Calendar.getInstance() }
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    var selectedDayOfMonth by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

    val selectedDaySets = remember(allSets, selectedDayOfMonth) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
        }
        val targetEpoch = tempCal.get(Calendar.YEAR) * 366L + tempCal.get(Calendar.DAY_OF_YEAR)
        allSets.filter { it.dateEpochDay == targetEpoch }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Streak & Weekly Goal Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
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
                                text = "WEEKLY WORKOUT GOAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                            Text(
                                text = "$daysLast7 / $weeklyGoalDays Days This Week",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Total Active Gym Days Logged: $totalUniqueWorkoutDays",
                                fontSize = 12.sp,
                                color = AmberGold
                            )
                        }
                        Row {
                            OutlinedButton(
                                onClick = { onUpdateWeeklyGoal((weeklyGoalDays - 1).coerceAtLeast(1)) }
                            ) {
                                Text("-", color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = { onUpdateWeeklyGoal((weeklyGoalDays + 1).coerceAtMost(7)) }
                            ) {
                                Text("+", color = EmeraldAccent)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (daysLast7.toFloat() / weeklyGoalDays.toFloat()).coerceIn(0f, 1f) },
                        color = EmeraldAccent,
                        trackColor = SlateCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }
            }
        }

        // Interactive Monthly Calendar View
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentMonthName.uppercase(Locale.US),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Button(
                            onClick = {
                                val csvData = WorkoutMath.buildCsvExport(allSets)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "IronPulse Workout Backup CSV")
                                    putExtra(Intent.EXTRA_TEXT, csvData)
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Export CSV Backup")
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SlateBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT CSV", color = SlateBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val rows = (1..daysInMonth).chunked(7)
                    rows.forEach { weekDays ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weekDays.forEach { dayNum ->
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> EmeraldAccent
                                                hasWorkout -> EmeraldAccent.copy(alpha = 0.28f)
                                                else -> SlateCard
                                            }
                                        )
                                        .clickable { selectedDayOfMonth = dayNum }
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SlateBackground else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "WORKOUT LOGS FOR DAY $selectedDayOfMonth (${selectedDaySets.size} SETS)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        if (selectedDaySets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No sets logged on this date. Tap any highlighted day above to inspect its workout sets.",
                        modifier = Modifier.padding(16.dp),
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(selectedDaySets) { s ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${s.exerciseName} (Set #${s.setNumber} • ${s.setType})",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${s.weightKg} kg x ${s.reps} reps  •  Est 1RM: ${
                                WorkoutMath.estimate1RM(s.weightKg, s.reps)
                            } kg",
                            fontSize = 13.sp,
                            color = EmeraldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressAndBodyMetricsScreen(
    exercises: List<ExerciseEntity>,
    allSets: List<WorkoutSetEntity>,
    bodyMetrics: List<BodyMetricEntity>,
    onSaveBodyMetric: (Double, Double, Double, Double) -> Unit
) {
    var selectedExercise by remember(exercises) {
        mutableStateOf(exercises.firstOrNull()?.name ?: "Barbell Bench Press")
    }
    var selectedChartMetric by remember { mutableStateOf("EST_1RM") } // WEIGHT, VOLUME, EST_1RM

    val exerciseSets = remember(allSets, selectedExercise) {
        allSets.filter { it.exerciseName == selectedExercise && it.setType != "WARMUP" }
            .sortedBy { it.timestampMillis }
    }

    val chartPoints = remember(exerciseSets, selectedChartMetric) {
        exerciseSets.map { s ->
            when (selectedChartMetric) {
                "WEIGHT" -> s.weightKg
                "VOLUME" -> s.weightKg * s.reps
                else -> WorkoutMath.estimate1RM(s.weightKg, s.reps)
            }
        }
    }

    var bwInput by remember { mutableStateOf("78.0") }
    var waistInput by remember { mutableStateOf("82.0") }
    var chestInput by remember { mutableStateOf("104.0") }
    var armsInput by remember { mutableStateOf("38.5") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXERCISE PROGRESS CHARTS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                                    selectedContainerColor = EmeraldAccent,
                                    selectedLabelColor = SlateBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "EST_1RM" to "Est. 1RM (kg)",
                            "WEIGHT" to "Top Weight (kg)",
                            "VOLUME" to "Set Volume (kg)"
                        ).forEach { (key, title) ->
                            FilterChip(
                                selected = selectedChartMetric == key,
                                onClick = { selectedChartMetric = key },
                                label = { Text(title, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGold,
                                    selectedLabelColor = SlateBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (chartPoints.isEmpty()) {
                        Text(
                            text = "Log working sets for $selectedExercise to see your progress curve.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        val minVal = chartPoints.minOrNull() ?: 0.0
                        val maxVal = chartPoints.maxOrNull() ?: 100.0
                        Text(
                            text = "Latest: ${chartPoints.last()} kg  •  Peak: $maxVal kg",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(SlateCard, RoundedCornerShape(10.dp))
                                .padding(16.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val range = (maxVal - minVal).coerceAtLeast(1.0)
                            val path = Path()
                            chartPoints.forEachIndexed { idx, value ->
                                val x = if (chartPoints.size == 1) w / 2f
                                else (idx.toFloat() / (chartPoints.size - 1)) * w
                                val y = h - (((value - minVal) / range).toFloat() * h)
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                drawCircle(
                                    color = AmberGold,
                                    radius = 5.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                            if (chartPoints.size > 1) {
                                drawPath(
                                    path = path,
                                    color = EmeraldAccent,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // Body Metrics Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BODY METRICS TRACKER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bwInput,
                            onValueChange = { bwInput = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = waistInput,
                            onValueChange = { waistInput = it },
                            label = { Text("Waist (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = chestInput,
                            onValueChange = { chestInput = it },
                            label = { Text("Chest (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = armsInput,
                            onValueChange = { armsInput = it },
                            label = { Text("Arms (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            onSaveBodyMetric(
                                bwInput.toDoubleOrNull() ?: 0.0,
                                waistInput.toDoubleOrNull() ?: 0.0,
                                chestInput.toDoubleOrNull() ?: 0.0,
                                armsInput.toDoubleOrNull() ?: 0.0
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LOG BODY MEASUREMENTS",
                            color = SlateBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        items(bodyMetrics) { metric ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(metric.timestampMillis))
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dateStr, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                    Text(
                        text = "${metric.weightKg} kg • Waist ${metric.waistCm}cm • Chest ${metric.chestCm}cm • Arms ${metric.armsCm}cm",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }
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
        containerColor = SlateSurface,
        title = {
            Text(
                text = "Barbell Plate Calculator",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it },
                    label = { Text("Target Total Weight (kg)") },
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold
                )
                if (platesPerSide.isEmpty()) {
                    Text("Empty bar or weight less than barbell.", color = TextSecondary)
                } else {
                    Text(
                        text = platesPerSide.joinToString("  +  ") { "${it}kg" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("DONE", color = SlateBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}
