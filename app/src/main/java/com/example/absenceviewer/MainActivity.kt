package com.example.absenceviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import java.time.LocalDate

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
            GreetingPreview(this,appSettings)
        }
    }
}
data class Absence(val name: String, val subCategory : String, val begin : Int, val duration : Int)

data class DayAbsence(val day: String ,val absenceOfClasses : Map<String,List<Absence>>)//absenceOfClasss is a map where the class name is maped to a List of Absences

//TODO: implement data classes Lesson

class AbsencePlan{
    private val monthMapping : Map<String, String> = mapOf(
        "JANUARY" to "januar",
        "FEBRUARY" to "februar",
        "MARCH" to "maerz",
        "APRIL" to "april",
        "MAY" to "mai",
        "JUNE" to "juni",
        "JULY" to "juli",
        "AUGUST" to "august",
        "SEPTEMBER" to "september",
        "OCTOBER" to "oktober",
        "NOVEMBER" to "november",
        "DECEMBER" to "dezember"
    )
    private fun getAbsenceHtml() : String{
        println("started")
        val currentMonth = LocalDate.now().month

        val userName : String = monthMapping[currentMonth.name].toString() + "gauss"
        val password : String = "aLx6c3" + currentMonth.value
        println(currentMonth.name)
        println(userName)
        println(password)
        println("configured data for login")

        val builderBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        builderBody.addFormDataPart("user", userName)
        builderBody.addFormDataPart("pw", password)
        builderBody.addFormDataPart("login", "Einloggen")

        val requestBody = builderBody.build()

        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://www.gauss-gymnasium.de/plan/index.php")
            .post(requestBody)
            .build()
        println("created Request")
        val response = client.newCall(request).execute()
        println("got response 1")

        val responseBody = response.body?.string()

        println("got response 2")

        if (responseBody.isNullOrEmpty() ){
            throw Exception("Something went wrong while fetching the HTML")
        }

        return responseBody
    }
    private fun splitAbsenceHtml(html : String) : List<String>{
        val newHtml = html.split("Heute")[1]
        return newHtml.split("<h3>Nächster Schultag")
    }

    fun getAbsences() : MutableList<DayAbsence>{
        val absences = mutableListOf<DayAbsence>()

        val html = getAbsenceHtml()
        val absence_days = splitAbsenceHtml(html)
        absence_days.forEach {
            absences.add(transformDay(it))
        }
        println(absences)
        return absences
    }

    private fun transformDay(dayHtml : String) : DayAbsence{
        val splitedHtml = dayHtml.split("</h3>")
        val dateString = splitedHtml[0].removeSuffix(")").removePrefix(" (")
        val classAbsenceStrings = splitedHtml[1].split("<div class=\"accordion-item\">").drop(1)

        val dayAbsences = mutableMapOf<String, List<Absence>>()
        classAbsenceStrings.forEach{
            val splitedClassHtml = it.split("</h2>")
            val classNumber = splitedClassHtml[0].split("</button>")[0].split(">").last().removePrefix("\r\n                    ").removeSuffix("                                    ")
            print(classNumber + "Nummer der Klasse")

            dayAbsences[classNumber] = transformAbsence(splitedClassHtml[1])

        }
        return DayAbsence(splitedHtml[0],dayAbsences)

    }
    private fun transformAbsence(classHtml : String) : List<Absence>{
        val subAbsence = classHtml.removeSuffix("</div>").split("<div class=\"card align-top\">").drop(1)
        var classAbsence = mutableListOf<Absence>()
        subAbsence.forEach{
            classAbsence += transformSubAbsence(it)
        }
        return classAbsence
    }
    private fun transformSubAbsence(subAbsenceHtml : String) : List<Absence>{
        val splitedSubAbsenceHtml = subAbsenceHtml.split("<div class=\"card-body\">")
        val subCategory = splitedSubAbsenceHtml[0].removePrefix("\r\n                            <div class=\"card-header\">\r\n                                ").removeSuffix("                            </div>\r\n                            ")
        val lessonList = splitedSubAbsenceHtml[1].split("<li class=\"lesson-list-item \">\r\n                                            <span class=\"badge period-badge\" aria-label=\"").drop(1)

        val subClassAbsence = mutableListOf<Absence>()
        lessonList.forEach{
            val splitedLesson  = it.split("</ul>")[0].split("</li>")[0].trimEnd().split(".</span>\r\n                                            ")
            val name = splitedLesson[1]
            val time = splitedLesson[0].split(">").last().toInt()
            if(subClassAbsence.isNotEmpty() && subClassAbsence.last().name == name && subClassAbsence.last().begin + 1 == time){
                subClassAbsence[subClassAbsence.size-1] = subClassAbsence.last().copy(duration = subClassAbsence.last().duration + 1)
            }
            else{
                subClassAbsence.add(Absence(name, subCategory, time, 1))
            }

        }
        return subClassAbsence
    }
}

//TODO:
// class für den Stundenplan des Benutzers
class LessonPlan {
    //private Map<LessonDay> days;
    fun addDay(LessonDay day) {

    }

    fun addLesson() {

    }

    fun getDay() : LessonDay {

    }

}

//TODO:
class LessonDay {
    //Map<Lesson> lessons
}

@Composable
fun LoadAbsences(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter){
    var result by remember { mutableStateOf<List<DayAbsence>?>(null) }

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

fun updateMessagedGrades(grade : String, isChecked : Boolean){
    println("hello world" + isChecked.toString())
}

@Composable
fun TabChanger(lifecycleOwner: LifecycleOwner, appSettings: MessageFilter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1F)
            .background(boxColor)
    )
    {
        // Hier slider für Tabs einfügen

    }
}

@Composable
fun GreetingPreview(mainActivity: MainActivity, appSettings: MessageFilter) {

    AbsenceViewerTheme {
        Column (
        )
        {
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
            TabChanger(mainActivity,appSettings)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(backgroundColor)
            )
            {
                //TODO: content loader
                // loads content according to the selected tab
                LoadAbsences(mainActivity,appSettings)
            }
        }

    }
}