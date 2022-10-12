import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

const val INPUT_TOPIC = "bank-transactions"

fun main() {
    val producer = KafkaProducer<String, BankTransaction>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.CLIENT_ID_CONFIG to "bank-balance-app",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to BankTransactionSerializer::class.java,
            ProducerConfig.RETRIES_CONFIG to "3",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
        )
    )

    runCatching {
        while (true) {
            producer.record("kim")
            producer.record("js")
            producer.record("rizzle")
            Thread.sleep(100)
        }
    }.onFailure {
        println(it)
        return@onFailure
    }

    producer.close()
}

fun KafkaProducer<String, BankTransaction>.record(name: String) {
    send(ProducerRecord(INPUT_TOPIC, BankTransaction(name, (100..1000).random() * 100))).get().also {
        println(it.topic())
    }
}

class BankTransactionSerializer : Serializer<BankTransaction> {

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    override fun serialize(topic: String?, data: BankTransaction?): ByteArray {
        return objectMapper.writeValueAsBytes(data)
    }
}
