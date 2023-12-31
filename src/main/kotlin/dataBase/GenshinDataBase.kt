package dataBase

import com.ctrip.sqllin.driver.*
import com.ctrip.sqllin.dsl.Database

class GenshinDataBase {
    companion object {
        val database by lazy {
            Database(
                DatabaseConfiguration(
                    name = "genshinAssistant.db",
                    path = System.getProperty("compose.application.resources.dir").toDatabasePath(),
                    version = 1,
                    isReadOnly = false,
                    inMemory = false,
                    journalMode = JournalMode.WAL,
                    synchronousMode = SynchronousMode.NORMAL,
                    busyTimeout = 5000,
                    create = {
                        it.execSQL("create table person (id integer primary key autoincrement, name text, age integer)")
                        it.execSQL(
                            "CREATE TABLE genshinAccount (\n" +
                                    "    name TEXT PRIMARY KEY,\n" +
                                    "    value1 TEXT NOT NULL,\n" +
                                    "    value2 TEXT NOT NULL\n" +
                                    ");"
                        )
                        it.execSQL(
                            "CREATE TABLE TieAccount (\n" +
                                    "    name TEXT PRIMARY KEY,\n" +
                                    "    value TEXT NOT NULL\n" +
                                    ");"
                        )
                        it.execSQL(
                            "CREATE TABLE TieGamePath (\n" +
                                    "id integer primary key autoincrement,\n" +
                                    "    value TEXT NOT NULL\n" +
                                    ");"
                        )
                        it.execSQL(
                            "CREATE TABLE GenshinGamePath (\n" +
                                    "id integer primary key autoincrement,\n" +
                                    "    value TEXT NOT NULL\n" +
                                    ");"
                        )
                    },
                )
            )
        }
    }

}