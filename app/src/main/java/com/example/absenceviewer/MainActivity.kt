package com.example.absenceviewer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.absenceviewer.ui.theme.AbsenceViewerTheme
import com.example.absenceviewer.ui.theme.CustomColors
import com.example.absenceviewer.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var appSettings: MessageFilter
    private lateinit var settings: Settings

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.sendNotification("Test", "App wurde gestartet")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.initialize(this)
        appSettings = MessageFilter(this)
        settings = Settings(this)

        var themeMode by mutableIntStateOf(settings.themeMode)
        var selectedClass by mutableStateOf(settings.selectedClass)

        enableEdgeToEdge()
        setContent {
            AbsenceViewerTheme(themeMode = themeMode) {
                MainView(
                    mainActivity = this,
                    appSettings = appSettings,
                    currentTheme = themeMode,
                    onThemeChange = { newTheme ->
                        themeMode = newTheme
                        settings.themeMode = newTheme
                    },
                    selectedClass,
                    onClassChange = { newClass ->
                        selectedClass = newClass
                        settings.selectedClass = newClass
                    }
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                NotificationHelper.sendNotification("Test", "App wurde gestartet")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            NotificationHelper.sendNotification("Test", "App wurde gestartet")
        }
    }
}

@Composable
fun LoadAbsences(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    var result by remember { mutableStateOf<List<DayAbsence>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        lifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val absencePlan = AbsencePlan()
                result = absencePlan.getAbsences(context = context)
            }
        }
    }
    val scrollState = rememberScrollState()

    Column (
        modifier = Modifier
            .verticalScroll(scrollState)
    ){
        for (dayAbsence in result ?: emptyList()) {
            AbsenceCards(dayAbsence)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AbsenceCards(dayAbsence: DayAbsence){
    val customColors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(2.dp, customColors.border)
            .background(customColors.box)
            .fillMaxWidth()
    ) {
        Column (
            modifier = Modifier.padding(8.dp)
        ){
            Text(
                text = dayAbsence.day,
                style = TextStyle(fontWeight = FontWeight.ExtraBold, color = customColors.onBox),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)
            )
            for (currentClass in dayAbsence.absenceOfClasses.keys) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor =  customColors.card,
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Klasse " + currentClass.label,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = customColors.onCard),
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        for (absence in dayAbsence.absenceOfClasses[currentClass] ?: emptyList()) {
                            LessonAbsence(absence,currentClass.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonAbsence(lesson : Absence, grade: String){
    val customColors = LocalCustomColors.current
    var isChecked: Boolean by remember { mutableStateOf(false) }
    val time = if (lesson.duration > 1) "Stunde " + lesson.begin.toString() + "-" + (lesson.begin + lesson.duration - 1).toString() else "Stunde " + lesson.begin.toString()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = customColors.innerBox,
        ),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(0.45f)
    ) {
        Text(
            modifier = Modifier
            .padding(start = 8.dp),
            text = time + "\n" + lesson.name + "\n" + lesson.subCategory,
            style = TextStyle(fontWeight = FontWeight.Bold, color = customColors.onInnerBox)
        )
    }
}

@Composable
fun AbsencePlanTab(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    LoadAbsences(lifecycleOwner, appSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    currentTheme: Int,
    onThemeChange: (Int) -> Unit,
    currentClass: ClassName,
    onClassChange: (ClassName) -> Unit // Changed to use the Enum for type safety
) {
    val customColors = LocalCustomColors.current

    // Remember lists to avoid recreation on every recomposition
    val themes = remember { listOf("Tannenzapfen", "Blue Theme", "Red Theme", "Green Theme", "Dark") }
    val classEntries = remember { ClassName.entries }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Consistent spacing between sections
    ) {
        // Theme Selection
        SettingsDropdown(
            label = "Theme Settings",
            selectedOptionText = themes.getOrElse(currentTheme) { "Select Theme" },
            options = themes,
            onOptionSelected = { index, _ -> onThemeChange(index) },
            customColors = customColors
        )

        // Class Selection
        SettingsDropdown(
            label = "Klasse",
            selectedOptionText = currentClass.label,
            options = classEntries.map { it.label },
            onOptionSelected = { index, _ ->
                onClassChange(classEntries[index])
            },
            customColors = customColors
        )
    }
}

/**
 * A reusable Dropdown component for settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    selectedOptionText: String,
    options: List<String>,
    onOptionSelected: (index: Int, text: String) -> Unit,
    customColors: CustomColors // Replace with your actual CustomColors type
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = customColors.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedOptionText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = customColors.border,
                    unfocusedBorderColor = customColors.border.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            onOptionSelected(index, text)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(name: String, themeId: Int, currentTheme: Int, onThemeChange: (Int) -> Unit, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = currentTheme == themeId,
            onClick = { onThemeChange(themeId) }
        )
        Text(name, color = textColor, modifier = Modifier.padding(start = 8.dp))
    }
}

fun updateMessagedGrades(grade : String, isChecked : Boolean){
    println("hello world" + isChecked.toString())
}

@Composable
fun TabChanger(selectedTabIndex: Int,
               onTabSelected: (Int) -> Unit) {
    val customColors = LocalCustomColors.current
    val tabs = listOf("Absences")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(customColors.box),
        containerColor = customColors.box,
        contentColor = customColors.onBox
    )
    {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
    }
}

@Composable
fun StundenplanAdd(onSettingsClick: () -> Unit){
    val customColors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(customColors.banner)
            .padding(top = 10.dp)
    )
    {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .aspectRatio(1F)
                .scale(2F)
                .padding(end = 10.dp)
                .align(Alignment.CenterEnd),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "Vertretungsplan",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopStart)
                .padding(top = 15.dp)
                .padding(horizontal = 15.dp),
            fontSize = 40.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = Color.White,
            fontWeight = FontWeight(1000)
        )
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
fun MainView(
    mainActivity: MainActivity,
    appSettings: MessageFilter,
    currentTheme: Int,
    onThemeChange: (Int) -> Unit,
    selectedClass : ClassName,
    onClassChange: (ClassName) -> Unit

) {
    val customColors = LocalCustomColors.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column()
    {
        StundenplanAdd(onSettingsClick = { selectedTabIndex = 1 })

        TabChanger(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { newIndex -> selectedTabIndex = newIndex }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(customColors.background)
        )
        {
            when (selectedTabIndex) {
                0 -> AbsencePlanTab(mainActivity, appSettings)
                1 -> SettingsTab(currentTheme, onThemeChange,selectedClass,onClassChange)
            }
        }
    }
}
