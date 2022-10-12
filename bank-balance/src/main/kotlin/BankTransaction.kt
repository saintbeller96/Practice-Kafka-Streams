import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import java.time.LocalDateTime

data class BankTransaction(
    val name: String,
    val amount: Int,
    val time: LocalDateTime = LocalDateTime.now()
)

data class Balance(
    val count: Long = 0,
    val amount: Long = 0,
    val time: LocalDateTime = LocalDateTime.MIN
) {
    infix operator fun plus(other: Balance): Balance {
        return Balance(count + other.count, amount + other.amount, if (time.isAfter(other.time)) time else other.time)
    }

    infix operator fun plus(other: BankTransaction): Balance {
        return Balance(count + 1, amount + other.amount, if (time.isAfter(other.time)) time else other.time)
    }
}

class BankTransactionSerde : Serde<BankTransaction> {

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    override fun serializer(): Serializer<BankTransaction> {
        return BankTransactionSerializer()
    }

    override fun deserializer(): Deserializer<BankTransaction> {
        return Deserializer { topic, data ->
            objectMapper.readValue<BankTransaction>(data)
        }
    }
}

class BalanceSerde : Serde<Balance> {

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    override fun deserializer(): Deserializer<Balance> {
        return Deserializer { topic, data ->
            objectMapper.readValue<Balance>(data)
        }
    }

    override fun serializer(): Serializer<Balance> {
        return Serializer { topic, data ->
            objectMapper.writeValueAsBytes(data)
        }
    }
}
