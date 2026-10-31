package com.example.ironpulse

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Chest, Back, Legs, Shoulders, Arms, Core, Cardio
    val isBodyweight: Boolean = false,
    val isCustom: Boolean = false,
    val defaultRestSeconds: Int = 90
)

@Entity(tableName = "templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val exercisesCsv: String, // Comma-separated exercise names
    val isBodyweightOnly: Boolean = false
)

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val workoutName: String,
    val dateEpochDay: Long, // Local days since 1970-01-01
    val timestampMillis: Long,
    val exerciseName: String,
    val category: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val setType: String, // NORMAL, WARMUP, DROP, SUPERSET
    val restSeconds: Int,
    val isPR: Boolean = false
)

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val weightKg: Double,
    val waistCm: Double,
    val chestCm: Double,
    val armsCm: Double
)

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM exercises ORDER BY category ASC, name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<WorkoutTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)

    @Query("SELECT * FROM workout_sets ORDER BY timestampMillis DESC, setNumber ASC")
    fun getAllSets(): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(workoutSet: WorkoutSetEntity)

    @Query("SELECT * FROM body_metrics ORDER BY timestampMillis DESC")
    fun getAllBodyMetrics(): Flow<List<BodyMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMetric(metric: BodyMetricEntity)
}

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutSetEntity::class,
        BodyMetricEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ironpulse_workout.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object WorkoutMath {
    // Epley 1RM formula: weight * (1 + reps / 30.0)
    fun estimate1RM(weightKg: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weightKg
        val raw = weightKg * (1.0 + reps / 30.0)
        return (raw * 10.0).roundToInt() / 10.0
    }

    // Progressive overload suggestion based on last working set
    fun suggestNextTarget(lastSet: WorkoutSetEntity?, isBodyweight: Boolean): String {
        if (lastSet == null) {
            return if (isBodyweight) "First time logging: Aim for 3 sets of 10-12 clean reps"
            else "First time logging: Pick a comfortable weight for 8-10 reps"
        }
        return if (isBodyweight || lastSet.weightKg == 0.0) {
            val targetReps = lastSet.reps + 2
            "Progressive Overload: Last time ${lastSet.reps} reps -> Try $targetReps reps today"
        } else if (lastSet.reps >= 10) {
            val nextWeight = lastSet.weightKg + 2.5
            "Progressive Overload: Hit ${lastSet.weightKg} kg x ${lastSet.reps} -> Try $nextWeight kg today (+2.5 kg)"
        } else {
            val nextReps = lastSet.reps + 1
            "Progressive Overload: Last ${lastSet.weightKg} kg x ${lastSet.reps} -> Try ${lastSet.weightKg} kg x $nextReps reps today"
        }
    }

    // Barbell Plate Calculator per side
    fun calculatePlatesPerSide(targetWeight: Double, barWeight: Double = 20.0): List<Double> {
        if (targetWeight <= barWeight) return emptyList()
        var remainingPerSide = (targetWeight - barWeight) / 2.0
        val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
        val result = mutableListOf<Double>()
        for (plate in availablePlates) {
            while (remainingPerSide >= plate - 0.001) {
                result.add(plate)
                remainingPerSide -= plate
            }
        }
        return result
    }

    fun currentEpochDay(): Long {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        return year * 366L + dayOfYear
    }

    fun epochDayFromMillis(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.YEAR) * 366L + cal.get(Calendar.DAY_OF_YEAR)
    }

    fun buildCsvExport(sets: List<WorkoutSetEntity>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("Date,Workout,Exercise,Category,SetNumber,SetType,WeightKg,Reps,Est1RM,RestSec,IsPR")
        sets.sortedBy { it.timestampMillis }.forEach { s ->
            val dateStr = sdf.format(Date(s.timestampMillis))
            val e1rm = estimate1RM(s.weightKg, s.reps)
            sb.appendLine(
                "\"$dateStr\",\"${s.workoutName}\",\"${s.exerciseName}\",\"${s.category}\"," +
                    "${s.setNumber},${s.setType},${s.weightKg},${s.reps},$e1rm,${s.restSeconds},${s.isPR}"
            )
        }
        return sb.toString()
    }
}

object SeedData {
    val defaultExercises = listOf(
        // Chest
        ExerciseEntity(name = "Barbell Bench Press", category = "Chest", isBodyweight = false, defaultRestSeconds = 120),
        ExerciseEntity(name = "Incline Dumbbell Press", category = "Chest", isBodyweight = false, defaultRestSeconds = 90),
        ExerciseEntity(name = "Cable Chest Fly", category = "Chest", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Push-Ups", category = "Chest", isBodyweight = true, defaultRestSeconds = 60),
        ExerciseEntity(name = "Decline Push-Ups", category = "Chest", isBodyweight = true, defaultRestSeconds = 60),
        // Back
        ExerciseEntity(name = "Barbell Deadlift", category = "Back", isBodyweight = false, defaultRestSeconds = 150),
        ExerciseEntity(name = "Lat Pulldown", category = "Back", isBodyweight = false, defaultRestSeconds = 90),
        ExerciseEntity(name = "Bent-Over Barbell Row", category = "Back", isBodyweight = false, defaultRestSeconds = 90),
        ExerciseEntity(name = "Pull-Ups", category = "Back", isBodyweight = true, defaultRestSeconds = 90),
        ExerciseEntity(name = "Inverted Bodyweight Row", category = "Back", isBodyweight = true, defaultRestSeconds = 60),
        // Legs
        ExerciseEntity(name = "Barbell Back Squat", category = "Legs", isBodyweight = false, defaultRestSeconds = 120),
        ExerciseEntity(name = "Romanian Deadlift", category = "Legs", isBodyweight = false, defaultRestSeconds = 120),
        ExerciseEntity(name = "Leg Press", category = "Legs", isBodyweight = false, defaultRestSeconds = 90),
        ExerciseEntity(name = "Bulgarian Split Squat (Bodyweight)", category = "Legs", isBodyweight = true, defaultRestSeconds = 75),
        ExerciseEntity(name = "Bodyweight Jump Squats", category = "Legs", isBodyweight = true, defaultRestSeconds = 60),
        // Shoulders
        ExerciseEntity(name = "Overhead Barbell Press", category = "Shoulders", isBodyweight = false, defaultRestSeconds = 120),
        ExerciseEntity(name = "Dumbbell Lateral Raise", category = "Shoulders", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Face Pulls", category = "Shoulders", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Pike Push-Ups", category = "Shoulders", isBodyweight = true, defaultRestSeconds = 75),
        // Arms
        ExerciseEntity(name = "Barbell EZ Curl", category = "Arms", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Tricep Rope Pushdown", category = "Arms", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Hammer Curls", category = "Arms", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Diamond Push-Ups", category = "Arms", isBodyweight = true, defaultRestSeconds = 60),
        ExerciseEntity(name = "Bench Tricep Dips", category = "Arms", isBodyweight = true, defaultRestSeconds = 60),
        // Core
        ExerciseEntity(name = "Hanging Leg Raises", category = "Core", isBodyweight = true, defaultRestSeconds = 60),
        ExerciseEntity(name = "Ab Wheel Rollout", category = "Core", isBodyweight = true, defaultRestSeconds = 60),
        ExerciseEntity(name = "Plank Hold (Seconds as Reps)", category = "Core", isBodyweight = true, defaultRestSeconds = 45),
        ExerciseEntity(name = "Cable Woodchoppers", category = "Core", isBodyweight = false, defaultRestSeconds = 60),
        // Cardio
        ExerciseEntity(name = "Burpees", category = "Cardio", isBodyweight = true, defaultRestSeconds = 45),
        ExerciseEntity(name = "Mountain Climbers", category = "Cardio", isBodyweight = true, defaultRestSeconds = 45),
        ExerciseEntity(name = "Treadmill Sprint Intervals", category = "Cardio", isBodyweight = false, defaultRestSeconds = 60),
        ExerciseEntity(name = "Rowing Machine Erg", category = "Cardio", isBodyweight = false, defaultRestSeconds = 60)
    )

    val defaultTemplates = listOf(
        WorkoutTemplateEntity(
            name = "Push Day (Chest, Shoulders, Triceps)",
            description = "Heavy compound pressing + hypertrophy isolation",
            exercisesCsv = "Barbell Bench Press,Incline Dumbbell Press,Overhead Barbell Press,Dumbbell Lateral Raise,Tricep Rope Pushdown",
            isBodyweightOnly = false
        ),
        WorkoutTemplateEntity(
            name = "Pull Day (Back, Rear Delts, Biceps)",
            description = "Vertical and horizontal pulling + biceps",
            exercisesCsv = "Barbell Deadlift,Pull-Ups,Bent-Over Barbell Row,Face Pulls,Barbell EZ Curl",
            isBodyweightOnly = false
        ),
        WorkoutTemplateEntity(
            name = "Leg Day (Quads, Hamstrings, Core)",
            description = "Lower body strength and core stability",
            exercisesCsv = "Barbell Back Squat,Romanian Deadlift,Leg Press,Hanging Leg Raises",
            isBodyweightOnly = false
        ),
        WorkoutTemplateEntity(
            name = "Full Body Strength",
            description = "Complete compound full-body routine",
            exercisesCsv = "Barbell Back Squat,Barbell Bench Press,Bent-Over Barbell Row,Overhead Barbell Press,Hanging Leg Raises",
            isBodyweightOnly = false
        ),
        WorkoutTemplateEntity(
            name = "Home Bodyweight (No Equipment)",
            description = "Zero equipment calisthenics & home conditioning",
            exercisesCsv = "Push-Ups,Pull-Ups,Bulgarian Split Squat (Bodyweight),Pike Push-Ups,Diamond Push-Ups,Hanging Leg Raises",
            isBodyweightOnly = true
        )
    )
}
