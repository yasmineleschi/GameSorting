package iset.dsi.gamesorting

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ScoreDatabase"
        private const val DATABASE_VERSION = 1

        // Define the table and columns
        private const val TABLE_SCORES = "scores"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_PLAYER_NAME = "player_name"
        private const val COLUMN_TIME = "time"
    }

    // Create the table
    private val CREATE_TABLE = (
            "CREATE TABLE $TABLE_SCORES ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_PLAYER_NAME TEXT, $COLUMN_TIME INTEGER);"
            )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    // Add a new score to the database
    fun saveScore(playerName: String, time: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PLAYER_NAME, playerName)
        values.put(COLUMN_TIME, time)
        db.insert(TABLE_SCORES, null, values)
        db.close()
    }

    // Get the top 10 scores from the database
    fun getTopScores(limit: Int): List<Score> {
        val scores = mutableListOf<Score>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_SCORES, arrayOf(COLUMN_ID, COLUMN_PLAYER_NAME, COLUMN_TIME),
            null, null, null, null, "$COLUMN_TIME ASC", "10"
        )


        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val playerNameIndex = cursor.getColumnIndex(COLUMN_PLAYER_NAME)
                val timeIndex = cursor.getColumnIndex(COLUMN_TIME)

                // Check if the indices are valid
                if (idIndex != -1 && playerNameIndex != -1 && timeIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val playerName = cursor.getString(playerNameIndex)
                    val time = cursor.getInt(timeIndex)

                    scores.add(Score(id, playerName, time))
                }
            } while (cursor.moveToNext())
        }

            cursor.close()
            db.close()

            return scores
        }
    }

