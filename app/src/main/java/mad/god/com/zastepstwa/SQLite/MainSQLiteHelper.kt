package mad.god.com.zastepstwa.SQLite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class MainSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        println("kurwa baza się tworzy, zjebałeś coś tutaj")
        val createDates="CREATE TABLE " + DATES_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER, " +
                COLUMN_VALUE + " TEXT,"+
                DATES_COLUMN_ZMIANY + " TEXT,"+
                DATES_COLUMN_DODATKOWE + " TEXT"+
                ")"
        val createNaucz="CREATE TABLE " + NAUCZ_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER, " +
                COLUMN_VALUE + " TEXT,"+
                ID_DATY+" INTEGER"+
                ")"
        val createZast="CREATE TABLE " + ZAST_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER, " +
                ZAST_COLUMN_LEKCJA + " TEXT,"+
                ZAST_COLUMN_KLASA + " TEXT,"+
                ZAST_COLUMN_ZMIANA + " TEXT,"+
                ZAST_COLUMN_FORMA + " TEXT,"+
                ZAST_COLUMN_ID_NAUCZ+" INTEGER,"+
                ID_DATY+" INTEGER"+
                ")"
        sqLiteDatabase.execSQL(createDates)
        sqLiteDatabase.execSQL(createNaucz)
        sqLiteDatabase.execSQL(createZast)
    }

     fun deleteDB(sqLiteDatabase: SQLiteDatabase){
         println("usuwa się")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $DATES_TABLE_NAME")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $NAUCZ_TABLE_NAME")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $ZAST_TABLE_NAME")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        println("jebany apgrejd")
        deleteDB(sqLiteDatabase)
        onCreate(sqLiteDatabase)
    }

    companion object {
        private val DATABASE_VERSION = 5
        val DATABASE_NAME = "DatesDB"

        //common
        val COLUMN_ID = "id"
        val COLUMN_VALUE = "date"
        val ID_DATY="id_daty"

        //tylko w tabelce dat
        val DATES_TABLE_NAME = "dates"
        val DATES_COLUMN_ZMIANY="zmiany"
        val DATES_COLUMN_DODATKOWE="dodatkowe"

        //tylko u nauczycieli
        val NAUCZ_TABLE_NAME="nauczyciele"


        //tylko zastepstwo
        val ZAST_TABLE_NAME="zastepstwo"
        val ZAST_COLUMN_LEKCJA="lekcja"
        val ZAST_COLUMN_KLASA="klasa"
        val ZAST_COLUMN_ZMIANA="zmiana"
        val ZAST_COLUMN_FORMA="forma"
        val ZAST_COLUMN_ID_NAUCZ="id_naucz"
    }
}