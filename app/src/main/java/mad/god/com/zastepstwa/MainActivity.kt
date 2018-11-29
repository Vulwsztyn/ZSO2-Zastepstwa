package mad.god.com.zastepstwa

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup;
import org.jsoup.select.Selector

import java.net.SocketTimeoutException
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.widget.Toast
import android.content.ContentValues
import mad.god.com.zastepstwa.SQLite.MainSQLiteHelper
import kotlin.concurrent.thread
import android.app.job.JobParameters




class MainActivity : Activity() {
    private val jobTag = "sprawdzacz"
    val url="http://zso2.pl/nowa/index.php/zastepstwa"
    private val lock = java.lang.Object()
    var CHANNEL_ID="kanal"
    var CHANNEL_NAME=6
    var CHANNEL_DESC=8

//    private val mJobHandler = Handler(object : Handler.Callback() {
//
//        fun handleMessage(msg: Message): Boolean {
//            Toast.makeText(applicationContext,
//                    "JobService task running", Toast.LENGTH_SHORT)
//                    .show()
//            jobFinished(msg.obj as JobParameters, false)
//            return true
//        }
//    })

    @Synchronized private fun DB_addDate(i: Int,tekst: String,zmiany:String,dodatkowe:String) {
        val database = MainSQLiteHelper(this).writableDatabase
        val values = ContentValues()
        values.put(MainSQLiteHelper.COLUMN_ID, i)
        values.put(MainSQLiteHelper.COLUMN_VALUE, tekst)
        values.put(MainSQLiteHelper.DATES_COLUMN_ZMIANY, zmiany)
        values.put(MainSQLiteHelper.DATES_COLUMN_DODATKOWE, dodatkowe)
        val newRowId = database.insert(MainSQLiteHelper.DATES_TABLE_NAME, null, values)
        database.close()
    }

    @Synchronized private fun DB_addTeacher(i: Int,tekst: String,id_daty:Int) {
        val database = MainSQLiteHelper(this).writableDatabase
        val values = ContentValues()
        values.put(MainSQLiteHelper.COLUMN_ID, i)
        values.put(MainSQLiteHelper.COLUMN_VALUE, tekst)
        values.put(MainSQLiteHelper.ID_DATY, id_daty)
        val newRowId = database.insert(MainSQLiteHelper.NAUCZ_TABLE_NAME, null, values)
        database.close()
    }

    @Synchronized private fun DB_addSubstitution(values:ContentValues) {
        val database = MainSQLiteHelper(this).writableDatabase

        val newRowId = database.insert(MainSQLiteHelper.ZAST_TABLE_NAME, null, values)
        database.close()
    }

     fun displayData(){

         datesTable.removeAllViews()

        val database = MainSQLiteHelper(this).readableDatabase

        val projection = arrayOf<String>(MainSQLiteHelper.COLUMN_ID,MainSQLiteHelper.COLUMN_VALUE,MainSQLiteHelper.DATES_COLUMN_ZMIANY)

        val cursor = database.query(
                MainSQLiteHelper.DATES_TABLE_NAME, // The table to query
                projection, // The columns to return
                null, // The columns for the WHERE clause
                null, // don't filter by row groups
                null,                                      // don't sort
                null,
                MainSQLiteHelper.COLUMN_ID
        )// The values for the WHERE clause
        // don't group the rows

         cursor.moveToFirst()

        while (!cursor.isAfterLast) {


            //println(cursor.getInt(0).toString()+" "+cursor.getString(1)+" "+cursor.getString(2))

            val row = TableRow(datesTable.context)
            val lp = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)

            lp.gravity = Gravity.CENTER
            row.minimumHeight = 100
            row.layoutParams = lp
            row.gravity = Gravity.CENTER_HORIZONTAL

            val qty = TextView(row.context)
            qty.text = cursor.getString(1)
            qty.setTextColor(Color.WHITE)

            val index=cursor.getInt(0)

            runOnUiThread {

                row.setOnClickListener {
                    val intent = Intent(this@MainActivity, ShowZastepstwa::class.java).apply {
                        putExtra("index", index)
                    }
                    startActivity(intent)
                }
                row.addView(qty)
                datesTable.addView(row)
            }

            cursor.moveToNext()
        }
        cursor.close()
        database.close()
    }

    fun checkIfNew():Boolean {

        val database = MainSQLiteHelper(this).readableDatabase

        val projection = arrayOf<String>(MainSQLiteHelper.COLUMN_ID,MainSQLiteHelper.COLUMN_VALUE,MainSQLiteHelper.DATES_COLUMN_ZMIANY)

        val cursor = database.query(
                MainSQLiteHelper.DATES_TABLE_NAME, // The table to query
                projection, // The columns to return
                null, // The columns for the WHERE clause
                null, // don't filter by row groups
                null,                                      // don't sort
                null,
                MainSQLiteHelper.COLUMN_ID
        )
        if(cursor.isAfterLast){
            cursor.close()
            database.close()
            return true
        }
        cursor.moveToFirst()
        val aktualnyNajwyzej=cursor.getString(1)
        cursor.close()
        database.close()


        var wynik=false
        val JsoupThread = thread(start = true) {
            try {
                var statusCode = 0;
                try {
                    val response = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                            .timeout(10000)
                            .execute()

                    statusCode = response.statusCode()
                } catch (e: SocketTimeoutException) {
                }
                if (statusCode != 200) {
                    runOnUiThread {
                        Toast.makeText(this, "Problem z połączeniem", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Jsoup.connect(url).get().run {
                        var sprawdzone=false
                        select("a[href]").forEachIndexed { index, element ->
                            if(!sprawdzone){
                                if (element.text().matches("^Zmiany.+".toRegex())) {
                                    if(element.text()!=aktualnyNajwyzej){
                                        wynik=true
                                    }
                                    sprawdzone=true
                                }
                            }
                        }
                    }
                }
            }
            catch (e: Selector.SelectorParseException) {
                runOnUiThread {
                    Toast.makeText(this, "Chyba coś z połączeniem, ale głowy nie daję", Toast.LENGTH_SHORT).show()
                }
            }
        }
        JsoupThread.join()
        return wynik
    }


    fun downloadData(czyWypisywac: Boolean){
        if(czyWypisywac)Toast.makeText(this, "Sprawdzam", Toast.LENGTH_SHORT).show()
            if(!checkIfNew()&&false) {
                if(czyWypisywac) Toast.makeText(this, "Nic nowego", Toast.LENGTH_SHORT).show()
                return
            }
            val database = MainSQLiteHelper(this).writableDatabase
            MainSQLiteHelper(this).onUpgrade(database,0,0)
            database.close()

            //val url="https://vulwsztyn.github.io/Test1/"

        val JsoupThread = thread(start = true) {

            try {
                    Jsoup.connect(url).get().run {
                        var currentDateIndex = 0
                        var currentTeacherIndex = -1
                        var currentSubIndex = 0

                        select("a[href]").forEachIndexed { index, element ->

                            if (element.text().matches("^Zmiany.+".toRegex())) {
                                println(element.text())
                                Jsoup.connect(element.attr("abs:href")).get().run {
                                    var listaNieobecnych=""
                                    select("table").get(0).run {
                                        var columnNumber = 0
                                        val values = ContentValues()
                                        select("tr").forEachIndexed { indexWiersza, tableRow ->
                                            val numberOfCells = tableRow.select("td").size
                                            tableRow.select("td").forEachIndexed { cellIndex, cell ->

                                                if (numberOfCells == 1) {
                                                    currentTeacherIndex++
                                                    DB_addTeacher(currentTeacherIndex, cell.text(), currentDateIndex)
                                                    listaNieobecnych+=cell.text()+" "
                                                } else {
                                                    when (columnNumber) {
                                                        0 -> {
                                                            values.put(MainSQLiteHelper.COLUMN_ID, currentSubIndex)
                                                            values.put(MainSQLiteHelper.ZAST_COLUMN_LEKCJA, cell.text())
                                                        }
                                                        1 -> values.put(MainSQLiteHelper.ZAST_COLUMN_KLASA, cell.text())
                                                        2 -> values.put(MainSQLiteHelper.ZAST_COLUMN_ZMIANA, cell.text())
                                                        3 -> {
                                                            values.put(MainSQLiteHelper.ZAST_COLUMN_FORMA, cell.text())
                                                            values.put(MainSQLiteHelper.ZAST_COLUMN_ID_NAUCZ, currentTeacherIndex)
                                                            values.put(MainSQLiteHelper.ID_DATY, currentDateIndex)

                                                            DB_addSubstitution(values)
                                                            currentSubIndex++
                                                            columnNumber = -1
                                                        }
                                                    }
                                                    columnNumber++
                                                }
                                            }
                                        }
                                    }
                                    var dodatkowe=""
                                    dodatkowe+='\n'
                                    select("article>div~p").forEachIndexed { index, element ->
                                        dodatkowe+=element.text()+'\n'+'\n'
                                    }

                                    DB_addDate(currentDateIndex, element.text(), listaNieobecnych,dodatkowe)
                                }
                                currentDateIndex++
                            }
                        }

                    }

            } catch (e: Selector.SelectorParseException) {
                runOnUiThread {
                    Toast.makeText(this, "Chyba coś z połączeniem, ale głowy nie daję", Toast.LENGTH_SHORT).show()
                }
            }

        }
        JsoupThread.join()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(CHANNEL_NAME)
            val description = getString(CHANNEL_DESC)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        button.isClickable=!true
        downloadData(false)
        displayData()
//        button.isClickable=true
//        button.setOnClickListener{
//            button.isClickable=!true
//            downloadData(true)
//            displayData()
//            button.isClickable=true
//        }

        //createNotificationChannel()
        //JobManager.create(this).addJobCreator(DemoJobCreator())

        //DemoSyncJob.scheduleJob()



    }

}
