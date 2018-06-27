package mad.god.com.zastepstwa.Sprawdzacz

import com.evernote.android.job.JobRequest
import com.evernote.android.job.Job
import java.util.concurrent.TimeUnit
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import mad.god.com.zastepstwa.MainActivity
import android.content.Intent
import android.app.PendingIntent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.widget.Toast

import mad.god.com.zastepstwa.R
import mad.god.com.zastepstwa.SQLite.MainSQLiteHelper
import org.jsoup.Jsoup
import org.jsoup.select.Selector
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class DemoSyncJob : Job() {

    val url="http://zso2.pl/nowa/index.php/zastepstwa"
    var notificationId=0

    fun checkIfNew():Boolean {

        var wynik=false
        val JsoupThread = thread(start = true) {

            try{
                    Jsoup.connect(url).get().run {
                        var currentDateIndex = 0
                        var currentTeacherIndex = -1
                        var currentSubIndex = 0

                        select("a[href]").forEachIndexed { index, element ->
                            if (element.text().matches("^Zmiany.+".toRegex())) {


                                val database = MainSQLiteHelper(context).readableDatabase
                                val projection = arrayOf<String>(MainSQLiteHelper.COLUMN_ID, MainSQLiteHelper.COLUMN_VALUE, MainSQLiteHelper.DATES_COLUMN_ZMIANY)

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
                                    wynik=true
                                }
                                else {

                                    cursor.moveToFirst()
                                    if (element.text() != cursor.getString(1)) {
                                        wynik = true
                                    }
                                    cursor.close()
                                    database.close()
                                }
                            }
                        }
                    }

            }
            catch (e: Selector.SelectorParseException) {
            }
        }
        JsoupThread.join()
        return wynik
    }

    override fun onRunJob(params: Params): Job.Result{
        val warunek=checkIfNew()
        var title="Tytul - nie pobralo"
        var text="Tekst - nie pobralo"
        if(!warunek){
            val df = SimpleDateFormat("EEE, d MMM yyyy, HH:mm")
            val date = df.format(Calendar.getInstance().getTime())
            title="Nic nowego"
            text=date
        }
        else{
            val database = MainSQLiteHelper(context).readableDatabase

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
            title=cursor.getString(1)
            text=cursor.getString(2)
            cursor.close()
            database.close()
        }



        val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(TAG, "Job Demo", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Job demo job"
            context.getSystemService(NotificationManager::class.java)!!.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, TAG)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setChannelId(TAG)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.zso2)
                .setShowWhen(true)
                .setColor(Color.GREEN)
                .setLocalOnly(true)
                .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        return Result.SUCCESS
    }

    companion object {

        val TAG = "sprawdzacz"

        fun scheduleJob() {
            JobRequest.Builder(DemoSyncJob.TAG)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(30),TimeUnit.MINUTES.toMillis(15))
//                    .startNow()

                    .build()
                    .schedule()


        }

    }
}