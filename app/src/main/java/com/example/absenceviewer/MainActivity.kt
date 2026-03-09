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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.absenceviewer.ui.theme.AbsenceViewerTheme
import com.example.absenceviewer.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var appSettings: MessageFilter

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

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        var themeMode by mutableIntStateOf(sharedPref.getInt("theme_mode", 0))

        enableEdgeToEdge()
        setContent {
            AbsenceViewerTheme(themeMode = themeMode) {
                MainView(
                    mainActivity = this,
                    appSettings = appSettings,
                    currentTheme = themeMode,
                    onThemeChange = { newTheme ->
                        themeMode = newTheme
                        sharedPref.edit().putInt("theme_mode", newTheme).apply()
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

data class Absence(val name: String, val subCategory : String, val begin : Int, val duration : Int)

data class DayAbsence(val day: String ,val absenceOfClasses : Map<String,List<Absence>>)//absenceOfClasss is a map where the class name is maped to a List of Absences

@Composable
fun LoadAbsences(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    var result by remember { mutableStateOf<List<DayAbsence>?>(null) }

    LaunchedEffect(key1 = Unit) {
        lifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val absencePlan = AbsencePlan()
                result = absencePlan.getAbsences()
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
                        text = currentClass,
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
                            LessonAbsence(absence,currentClass)
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = customColors.innerBox,
        ),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(0.45f)
            .height(60.dp)
    ) {
        Text(
            modifier = Modifier
            .padding(start = 8.dp),
            text = "Stunde " + lesson.begin.toString() + "-" + (lesson.begin + lesson.duration - 1).toString() + "\n" + lesson.name + "\n" + lesson.subCategory,
            style = TextStyle(fontWeight = FontWeight.Bold, color = customColors.onInnerBox)
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                newCheckedState ->
                isChecked = newCheckedState
                updateMessagedGrades(grade,newCheckedState)
            }
        )
    }
}

@Composable
fun AbsencePlanTab(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    LoadAbsences(lifecycleOwner, appSettings)
}

@Composable
fun SettingsTab(currentTheme: Int, onThemeChange: (Int) -> Unit){
    val customColors = LocalCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Theme Settings", color = customColors.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentTheme == 0,
                onClick = { onThemeChange(0) }
            )
            Text("Tannenzapfen - Finns special´", color = customColors.onBackground, modifier = Modifier.padding(start = 8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentTheme == 1,
                onClick = { onThemeChange(1) }
            )
            Text("Blue Theme", color = customColors.onBackground, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

fun updateMessagedGrades(grade : String, isChecked : Boolean){
    println("hello world" + isChecked.toString())
}

@Composable
fun TabChanger(selectedTabIndex: Int,
               onTabSelected: (Int) -> Unit) {
    val customColors = LocalCustomColors.current
    val tabs = listOf("Absences", "Settings")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(customColors.box) ,
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
    onThemeChange: (Int) -> Unit
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
                1 -> SettingsTab(currentTheme, onThemeChange)
            }
        }
    }
}
