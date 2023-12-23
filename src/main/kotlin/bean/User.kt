package bean

import androidx.compose.runtime.Stable
import com.ctrip.sqllin.dsl.annotation.DBRow
import kotlinx.serialization.Serializable

@DBRow
@Serializable
data class Person(
    val name: String,
    val age: Int,
)

@Stable
@DBRow(tableName = "genshinAccount")
@Serializable
data class GenshinAccount(
    val value1: String,
    val value2: String,
    override val name: String
):Account
interface Account {
    val name: String
}


@Stable
@DBRow(tableName = "TieAccount")
@Serializable
data class TieAccount(
    val value: String,
    override val name: String
):Account
