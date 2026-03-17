package com.example.absenceviewer

import android.content.Context
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import java.time.LocalDate

data class Absence(val name: String, val subCategory : String, val begin : Int, val duration : Int)

data class DayAbsence(val day: String ,val absenceOfClasses : Map<ClassName,List<Absence>>)//  is a map where the class name is maped to a List of Absences


class AbsencePlan{

    private val classNameMapping : Map<String, ClassName> = mapOf(
        "Allgemein" to ClassName.General,
        "Klasse 5L" to ClassName.G5,
        "Klasse 6L" to ClassName.G6,
        "Klasse 7a" to ClassName.G7a,
        "Klasse 7b" to ClassName.G7b,
        "Klasse 7c" to ClassName.G7c,
        "Klasse 7d" to ClassName.G7d,
        "Klasse 8a" to ClassName.G8a,
        "Klasse 8b" to ClassName.G8b,
        "Klasse 8c" to ClassName.G8c,
        "Klasse 8d" to ClassName.G8d,
        "Klasse 9a" to ClassName.G9a,
        "Klasse 9b" to ClassName.G9b,
        "Klasse 9c" to ClassName.G9c,
        "Klasse 9d" to ClassName.G9d,
        "Klasse 10" to ClassName.G10,
        "Klasse 11" to ClassName.G11,
        "Klasse 12" to ClassName.G12
    )
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

    fun getAbsences(context : Context) : MutableList<DayAbsence>{
        val absences = mutableListOf<DayAbsence>()

        val html = getAbsenceHtml()
        val absence_days = splitAbsenceHtml(html)
        if(absence_days.size != 2 && absence_days.size != 0) {
            throw Exception("Something went wrong while parsing the HTML")
        }
        absence_days.forEach {
            absences.add(transformDay(it, context))
        }
        println(absences)

        //TODO: filter Absences for class or teacher the user has
        return absences
    }

    private fun transformDay(dayHtml : String,context: Context) : DayAbsence{
        val splitedHtml = dayHtml.split("</h3>")
        val dateString = splitedHtml[0].removeSuffix(")").removePrefix(" (")
        val classAbsenceStrings = splitedHtml[1].split("<div class=\"accordion-item\">").drop(1)

        val settings = Settings(context)
        val selectedClass = settings.selectedClass
        val dayAbsences = mutableMapOf<ClassName, List<Absence>>()
        classAbsenceStrings.forEach{
            val splitedClassHtml = it.split("</h2>")
            val classLabelFromHtml = splitedClassHtml[0].split("</button>")[0].split(">").last().trim()

            val currentClassName = classNameMapping[classLabelFromHtml] ?: ClassName.All

            print(currentClassName.label + "Nummer der Klasse\tselectedClass "+ selectedClass.label +"\n")

            if(currentClassName == selectedClass) {
                dayAbsences[currentClassName] = transformAbsence(splitedClassHtml[1],true)
            }
            else{
                dayAbsences[currentClassName] = transformAbsence(splitedClassHtml[1])
            }

        }
        return DayAbsence(splitedHtml[0],dayAbsences)

    }
    private fun transformAbsence(classHtml : String, send : Boolean = false) : List<Absence>{
        val subAbsence = classHtml.removeSuffix("</div>").split("<div class=\"card align-top\">").drop(1)
        var classAbsence = mutableListOf<Absence>()
        subAbsence.forEach{
            classAbsence += transformSubAbsence(it,send)
        }
        return classAbsence
    }
    private fun transformSubAbsence(subAbsenceHtml : String,send : Boolean = true) : List<Absence>{
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
