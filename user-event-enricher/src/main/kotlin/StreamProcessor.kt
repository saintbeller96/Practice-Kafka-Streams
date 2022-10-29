import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.kstream.KStream

fun main() {
    val config = StreamsConfig(
        mapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to "user-event-enricher",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG) to "earliest",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
        )
    )

    val builder = StreamsBuilder()

    val users: GlobalKTable<String, String> = builder.globalTable(USER_TOPIC)

    val purchases: KStream<String, String> = builder.stream(PURCHASE_TOPIC)

    purchases.join(
        users,
        { key, value -> key },//KeyValueMapper
        { purchase, user -> "Purchase = $purchase, User = $user" } //ValueJoiner
    ).to("user-purchase-inner-join")

    purchases.leftJoin(
        users,
        { key, value -> key },
        { purchase, user: String? -> "Purchase = $purchase, User = $user" }
    ).to("user-purchase-left-join")

    val streams = KafkaStreams(builder.build(), config)
    streams.cleanUp()
    streams.start()

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
