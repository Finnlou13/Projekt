package com.example.absenceviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.absenceviewer.ui.theme.AbsenceViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val bannerColor =  Color(62, 103, 121)
val backgroundColor = Color(90, 58, 49)
val borderColor = Color(56, 59, 83)
val boxColor = Color(49, 134, 29)
val cardColor = Color(196, 203, 202)
val innerBoxColor = Color(242, 245, 234)


class MainActivity : ComponentActivity() {

    private lateinit var appSettings : MessageFilter

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        appSettings = MessageFilter(this)

        enableEdgeToEdge()
        setContent {
            MainView(this,appSettings)
        }
    }
}
data class Absence(val name: String, val subCategory : String, val begin : Int, val duration : Int)

data class DayAbsence(val day: String ,val absenceOfClasses : Map<String,List<Absence>>)//absenceOfClasss is a map where the class name is maped to a List of Absences

//TODO: implement data classes Lesson

@Composable
fun LoadAbsences(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    var result by remember { mutableStateOf<List<DayAbsence>?>(null) }

    //TODO: edit layout for more pleasing design

    LaunchedEffect(key1 = Unit) {
        lifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val absencePlan = AbsencePlan()
                // Perform network operations here
                result = absencePlan.getAbsences()
            }
        }
    }
    val scrollState = rememberScrollState()

    //Text(text = "Result : ${result ?: "Loading..."}")
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
    //TODO: edit layout for more pleasing design
    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(2.dp, borderColor) // Add a border (optional)
            .background(boxColor)
            .fillMaxWidth()


    ) {
        Column (
            modifier = Modifier.padding(8.dp)
        ){ // Arrange header and Card vertically
            Text(
                text = dayAbsence.day,
                style = TextStyle(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)


            )
            for (currentClass in dayAbsence.absenceOfClasses.keys) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor =  cardColor,

                    ),
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Text(
                        text = currentClass,
                        style = TextStyle(fontWeight = FontWeight.Bold),
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
    //TODO: edit layout for more pleasing design
    var isChecked: Boolean by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = innerBoxColor,
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
            style = TextStyle(fontWeight = FontWeight.Bold)

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

// no priority, maybe discard
//@Composable
//fun ScheduleTab(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
//    Text("Schedule Content", color = Color.White)
//}

//TODO: change from Tab to Button on top right corner
@Composable
fun SettingsTab(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    Text("Settings Content", color = Color.White)
}

fun updateMessagedGrades(grade : String, isChecked : Boolean){
    println("hello world" + isChecked.toString())
}

@Composable
fun TabChanger(selectedTabIndex: Int,
               onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Absences", /*"Schedule",*/ "Settings")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(boxColor) ,
        containerColor = boxColor,
        contentColor = Color.White
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
fun StundenplanAdd(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(bannerColor)
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
    }
}


@Composable
fun MainView(mainActivity: MainActivity, appSettings: MessageFilter) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    //TODO: change Layout to have a bit of extra padding on top of the screen to make sure the camera of the phone doesn't block the view

    AbsenceViewerTheme {
        Column()
        {
            // Header
            StundenplanAdd()
            //TODO: add a Settings button with an icon to get to the settings tab

            TabChanger(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { newIndex -> selectedTabIndex = newIndex }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(backgroundColor)
            )
            {

                when (selectedTabIndex) {
                    0 -> AbsencePlanTab(mainActivity, appSettings)
                    //1 -> ScheduleTab(mainActivity, appSettings)
                    1 -> SettingsTab(mainActivity, appSettings)
                }
            }
        }

    }
}