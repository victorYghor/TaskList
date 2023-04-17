import kotlinx.datetime.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.lang.reflect.ParameterizedType

data class Task(var lines: MutableList<String>, var priority: String, var date: String, var time: String )
val jsonFile = File("tasklist.json")
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
val type: ParameterizedType = Types.newParameterizedType(MutableList::class.java, Task::class.java)
val taskListAdapter: JsonAdapter<MutableList<Task>> = moshi.adapter(type)
val taskList: MutableList<Task> = mutableListOf()
val dateTimeOfTasks: MutableList<LocalDateTime> = mutableListOf()

val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date

fun start() {
    println("Input an action (add, print, edit, delete, end):")
    trigger(readln())
}

fun trigger(action: String = "") = when (action) {
    "add" -> add()
    "print" -> print(true)
    "end" -> end()
    "delete" -> delete()
    "edit" -> edit()
    else -> {
        println("The input action is invalid")
        start()
    }
}
fun prioritize(): String {
    val priorities: List<String> = listOf<String>("C", "H", "N", "L")
    println("Input the task priority (C, H, N, L):")
    val priority = readln().uppercase()
    if (!priorities.contains(priority)) {
        prioritize()
    }
    return priority
}

fun putDate(): String {
    println("Input the date (yyyy-mm-dd):")
    var dateIn = readln()
    if (dateIn.matches(Regex("\\d{4}-\\d-\\d{1,2}"))) {
        val temp = dateIn.toMutableList()
        temp.add(5, '0')
        dateIn = temp.joinToString("")
    }
    if (dateIn.matches(Regex("\\d{4}-\\d{2}-\\d"))) {
        val temp = dateIn.toMutableList()
        temp.add(8, '0')
        dateIn = temp.joinToString("")
    }
    try {

        Instant.parse("${dateIn}T00:00Z")
    } catch (e: Exception) {
        println("The input date is invalid")
        dateIn = putDate()
    }
    return dateIn
}

fun putTime(): String {
    println("Input the time (hh:mm):")
    var timeIn = readln()
    if (timeIn.matches(Regex("\\d:\\d{1,2}"))) {
        val temp = timeIn.toMutableList()
        temp.add(0,'0')
        timeIn = temp.joinToString("")
    }
    if (timeIn.matches(Regex("\\d{2}:\\d"))) {
        val temp = timeIn.toMutableList()
        temp.add(3,'0')
        timeIn = temp.joinToString("")
    }
    try {
        Instant.parse("2020-03-15T${timeIn}Z")
    } catch (e: IllegalArgumentException) {
        println("The input time is invalid")
        timeIn = putTime()
    } catch (e: Exception) {
        println("The input time is invalid")
        timeIn = putTime()
    }
    return timeIn
}

fun putLinesOfTask(): MutableList<String> {
    val linesOfTask = mutableListOf<String>()

    println("Input a new task (enter a blank line to end):")
    var currentLn = readln().trim()

    if (currentLn == "") {
        println("The task is blank")
    } else {
        while (currentLn != "") {
            linesOfTask.add(currentLn)
            currentLn = readln().trim()
        }
    }
    return linesOfTask
}
fun add() {
    val priority = prioritize()
    val date = putDate()
    val time = putTime()
    val linesOfTask = putLinesOfTask()
    val dateTime = Instant.parse("${date}T${time}Z").toLocalDateTime(TimeZone.UTC)
    dateTimeOfTasks.add(dateTime)
    taskList.add(Task(linesOfTask, priority, date, time))
    start()
}

fun print(cycle:Boolean = false) {
    haveTask()
    if (!taskList.isEmpty()) {
        println("+----+------------+-------+---+---+--------------------------------------------+")
        println("| N  |    Date    | Time  | P | D |                   Task                     |")
        println("+----+------------+-------+---+---+--------------------------------------------+")
        for (task in taskList) {
            val position = taskList.indexOf(task) + 1
            val (lines, priority, _, time) = task
            var quantityOfSpace = 0
            when (position) {
                in 1..9 -> quantityOfSpace = 2
                in 10..100 -> quantityOfSpace = 1
                in 100..1000 -> quantityOfSpace = 0
            }
            val dateTimeOfTask = dateTimeOfTasks[taskList.indexOf(task)]
            val numbersOfDays = currentDate.daysUntil(dateTimeOfTask.date)
            val dueTag = if (numbersOfDays == 0) "T" else if (numbersOfDays > 0) "I" else "O"
            val colorDueTag = when (dueTag) {
                "T" -> "\u001B[103m \u001B[0m"
                "I" -> "\u001B[102m \u001B[0m"
                else -> "\u001B[101m \u001B[0m"
            }
            val colorPriority = when (priority) {
                "C" -> "\u001B[101m \u001B[0m"
                "H" -> "\u001B[103m \u001B[0m"
                "N" -> "\u001B[102m \u001B[0m"
                "L" -> "\u001B[104m \u001B[0m"
                else -> "\u001B[0m"
            }


            println("| ${position.toString() + " ".repeat(quantityOfSpace)}| ${dateTimeOfTask.date} | ${time} | $colorPriority | $colorDueTag |${lines[0].chunked(44).joinToString( "\n|    |            |       |   |   |") { it.padEnd(44) + "|" }}")
            for ( i in lines.indices) {
                if (i == 0) continue
                println("|    |            |       |   |   |${lines[i].chunked(44).joinToString("\n|    |            |       |   |   |"){ it.padEnd(44) + "|" }}")
            }
            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
        println()
        if (cycle == true) {
            start()
        }
    }

}

fun haveTask() {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        start()
    }
}
fun pickTaskNumber(): Int {
    var position: Int = -1
    try {
        val input = readln()
        if (input == "clear") {
            clear()
        } else {
            position = input.toInt()
            if (position <= 0 || position > taskList.size) {
                println("Invalid task number")
                println("Input the task number (1-${taskList.size}): Or clear for delete all tasks")
                position = pickTaskNumber()
            }
        }
    } catch (e: NumberFormatException) {
        println("Invalid task number")
        println("Input the task number (1-${taskList.size}):")
        position = pickTaskNumber()
    }
    return position
}
fun delete(){
    haveTask()
    if (!taskList.isEmpty()){
        print()
        println("Input the task number (1-${taskList.size}): Or clear for delete all tasks")
        try {
            val indexOfRemoval = pickTaskNumber() - 1
            dateTimeOfTasks.removeAt(indexOfRemoval)
            taskList.removeAt(indexOfRemoval)
            println("The task is deleted")
        } catch (e: Exception) {
            println("All tasks have been deleted")
        } finally {
            start()
        }
    }
}

fun clear() {
    dateTimeOfTasks.clear()
    taskList.clear()
}
fun edit() {
    val options: List<String> = listOf("priority", "date", "time", "task")
    haveTask()
    if (!taskList.isEmpty()){
        print()
        println("Input the task number (1-${taskList.size}):")
        val indexOfRemoval = pickTaskNumber() - 1
        fun chooseOption(): String {
            println("Input a field to edit (priority, date, time, task):")
            var option = readln()
            if (!options.contains(option)){
                println("Invalid field")
                option = chooseOption()
            }
            return option
        }
        val option = chooseOption()
        val taskEdited = taskList[indexOfRemoval]
        when (option) {
            "priority" -> {
                taskEdited.priority = prioritize()
            }
            "date" -> {
                val newDate = putDate()
                dateTimeOfTasks[indexOfRemoval] =
                    Instant.parse("${newDate}T${taskEdited.time}Z")
                        .toLocalDateTime(TimeZone.UTC)
                taskEdited.date = newDate
            }
            "time" -> {
                val newTime = putTime()
                taskEdited.time = newTime
                dateTimeOfTasks[indexOfRemoval] = Instant.parse("${dateTimeOfTasks[indexOfRemoval].date}T${newTime}Z")
                    .toLocalDateTime(TimeZone.UTC)
            }
            "task" -> {
                taskEdited.lines = putLinesOfTask()
            }
        }
        println("The task is changed")
        start()
    }
}

fun end() {
    val taskListJson = taskListAdapter.toJson(taskList)
    jsonFile.writeText(taskListJson)
    println("Tasklist exiting!")
}

fun main() {
    if (jsonFile.exists()) {
        val FiletaskListJson = jsonFile.readText()
        val FileTaskList = taskListAdapter.fromJson(FiletaskListJson)
        if (FileTaskList != null) {
            for (task in FileTaskList) {
                val (lines, priority, date, time) = task
                val dateTime = Instant.parse("${date}T${time}Z").toLocalDateTime(TimeZone.UTC)
                dateTimeOfTasks.add(dateTime)
                taskList.add(Task(lines, priority, date, time))
            }
        }
    }
    start()
}
