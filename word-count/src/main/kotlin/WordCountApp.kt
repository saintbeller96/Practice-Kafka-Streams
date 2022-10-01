import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced

class WordCountApp {
}

fun main() {
    val config = StreamsConfig(mapOf(
        StreamsConfig.APPLICATION_ID_CONFIG to "wordcount-application",
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
    ))

    val builder = StreamsBuilder()

    val wordCountInput: KStream<String, String> = builder.stream("word-count-topic")

    wordCountInput.mapValues(String::lowercase)
        .flatMapValues { text -> text.split(" ") }
        .selectKey { _, word -> word }
        .groupByKey()
        .count(Materialized.`as`("counts"))
        .toStream().to("counts", Produced.with(Serdes.String(), Serdes.Long()))

    val streams = KafkaStreams(builder.build(), config)
    streams.start()
    println(streams.toString())

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
