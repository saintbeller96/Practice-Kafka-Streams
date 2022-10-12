import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore

const val OUTPUT_TOPIC = "bank-balances"

fun main() {
    val config = StreamsConfig(
        mapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to "bank-balance-stream",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG to "0",
            StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG) to "earliest",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to BankTransactionSerde::class.java
        )
    )

    val builder = StreamsBuilder()
    builder.stream<String?, BankTransaction>(INPUT_TOPIC)
        .groupBy { key, value -> value.name }
        .aggregate(
            { Balance() },
            { key, value, aggregate -> aggregate + value },
            Materialized.`as`<String?, Balance?, KeyValueStore<Bytes, ByteArray>?>("bank-balance-aggr-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(BalanceSerde())
        )
        .toStream()
        .peek { key, value -> println("$key $value") }
        .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), BalanceSerde()))

    val streams = KafkaStreams(builder.build(), config)
    streams.cleanUp()
    streams.start()

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
