package mad.god.com.zastepstwa

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_show__zastepstwa.*
import mad.god.com.zastepstwa.SQLite.MainSQLiteHelper
import org.jsoup.Jsoup
import org.jsoup.select.Selector

class ShowZastepstwa : Activity() {

    fun loadTeachers(index:Int){
        val database = MainSQLiteHelper(this).readableDatabase

        val projection = arrayOf<String>(MainSQLiteHelper.COLUMN_VALUE,MainSQLiteHelper.DATES_COLUMN_ZMIANY,MainSQLiteHelper.DATES_COLUMN_DODATKOWE)
        val selection=MainSQLiteHelper.COLUMN_ID+"=?"
        val selectionArgs=arrayOf<String>(index.toString())

        val cursor = database.query(
                MainSQLiteHelper.DATES_TABLE_NAME, // The table to query
                projection, // The columns to return
                selection, // The columns for the WHERE clause
                selectionArgs, // don't filter by row groups
                null,                                      // don't sort
                null,
                null
        )
        cursor.moveToFirst()
        val dlugosc="Zmiany w planie na dzien ".length
        dataNieobecnosci.text=cursor.getString(0).substring(dlugosc+1)
        listaNieobecnych.text=cursor.getString(1)
        dodatkoweText.text=cursor.getString(2)
        cursor.close()
        database.close()
    }

    fun prepareRow(row:TableRow,lp:TableRow.LayoutParams)
    {
        lp.gravity=Gravity.CENTER
        row.minimumHeight=30
        row.layoutParams = lp
        row.gravity = Gravity.CENTER_HORIZONTAL
        lp.bottomMargin=10

    }

    fun  prepareTeacherRow(row:TableRow,lp:TableRow.LayoutParams,qty:TextView){
        prepareRow(row,lp)
        lp.span=4
        //qty.textSize= qty.textSize+1
        //qty.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        //qty.setTypeface(null, Typeface.BOLD or Typeface.ITALIC)
        qty.setTypeface(null,  Typeface.ITALIC)
        qty.setTextColor(ResourcesCompat.getColor(getResources(), R.color.teacherCrimson, null))
    }

    fun prepareSubRow(row:TableRow,lp:TableRow.LayoutParams,qty:TextView,cellIndex:Int): Float {
        prepareRow(row,lp)
        qty.setTextColor(Color.WHITE)
        var initWeight=0.2f
        when(cellIndex){
            0->initWeight=0.22f
            1->{
                initWeight=0.18f
                if(qty.lineCount==1){
                    qty.gravity=Gravity.END
                }

            }
            2->{
                initWeight=0.4f
                qty.gravity=Gravity.CENTER
            }
            3->{
                initWeight=0.2f
                if(qty.text.matches("[zZ]ast.+".toRegex())){
                    qty.text=getString(R.string.zast)
                }
                else if(qty.text.matches(".wietlica".toRegex())){
                    qty.text=getString(R.string.swietl)
                }
            }
        }
        return initWeight
    }


    fun loadTable(index:Int){
        val database = MainSQLiteHelper(this).readableDatabase

        var projection = arrayOf<String>(MainSQLiteHelper.COLUMN_ID,MainSQLiteHelper.COLUMN_VALUE)
        var selection=MainSQLiteHelper.ID_DATY+"=?"
        var selectionArgs=arrayOf<String>(index.toString())

        val cursorNauczycieli = database.query(
                MainSQLiteHelper.NAUCZ_TABLE_NAME, // The table to query
                projection, // The columns to return
                selection, // The columns for the WHERE clause
                selectionArgs, // don't filter by row groups
                null,                                      // don't sort
                null,
                MainSQLiteHelper.COLUMN_ID
        )



        cursorNauczycieli.moveToFirst()


        while(!cursorNauczycieli.isAfterLast){
            val row = TableRow(tableLayout.context)
            val lp = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)


            val qty = TextView(row.context)
            qty.text = cursorNauczycieli.getString(1)

            prepareTeacherRow(row,lp,qty)
            runOnUiThread {
                row.addView(qty)
                tableLayout.addView(row)
            }

            projection = arrayOf<String>(MainSQLiteHelper.COLUMN_ID,MainSQLiteHelper.ZAST_COLUMN_LEKCJA,MainSQLiteHelper.ZAST_COLUMN_KLASA,
                    MainSQLiteHelper.ZAST_COLUMN_ZMIANA,MainSQLiteHelper.ZAST_COLUMN_FORMA,MainSQLiteHelper.ZAST_COLUMN_ID_NAUCZ)
            selection=MainSQLiteHelper.ID_DATY+"=? AND "+MainSQLiteHelper.ZAST_COLUMN_ID_NAUCZ+"=?"
            selectionArgs=arrayOf<String>(index.toString(),cursorNauczycieli.getInt(0).toString())
            val cursorZmian = database.query(
                    MainSQLiteHelper.ZAST_TABLE_NAME, // The table to query
                    projection, // The columns to return
                    selection, // The columns for the WHERE clause
                    selectionArgs, // don't filter by row groups
                    null,                                      // don't sort
                    null,
                    MainSQLiteHelper.COLUMN_ID
            )
            cursorZmian.moveToFirst()

            while(!cursorZmian.isAfterLast){

                val rows = TableRow(tableLayout.context)
                var qtys=arrayOf<TextView>(TextView(row.context),TextView(row.context),TextView(row.context),TextView(row.context))
                val lps = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
                var initWeight=arrayOf<Float>(0f,0f,0f,0f)
                for (i in 0..3){
                    qtys[i].text=cursorZmian.getString(i+1)
                    initWeight[i]=prepareSubRow(rows,lps,qtys[i],i)
                }

                        runOnUiThread {
                            for (i in 0..3){
                                rows.addView(qtys[i], TableRow.LayoutParams(0,
                                        TableRow.LayoutParams.WRAP_CONTENT, initWeight[i]))
                            }

                            tableLayout.addView(rows)
                        }

                cursorZmian.moveToNext()
            }

            cursorZmian.close()
            cursorNauczycieli.moveToNext()
        }

        cursorNauczycieli.close()
        database.close()

    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_show__zastepstwa)

        val kolor= Color.WHITE
        nieobecni.setTextColor(kolor)
        listaNieobecnych.setTextColor(kolor)
        dataNieobecnosci.setTextColor(kolor)
        dodatkoweText.setTextColor(kolor)

        val index = intent.getIntExtra("index",-1)

        loadTeachers(index)
        loadTable(index)

}}
