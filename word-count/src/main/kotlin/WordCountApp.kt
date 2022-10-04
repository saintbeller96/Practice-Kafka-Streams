import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced

fun main() {
    val config = StreamsConfig(
        mapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to "wordcount-application",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
        )
    )

    val builder = StreamsBuilder()

    val wordCountInput: KStream<String, String> = builder.stream("word-count-input")

    wordCountInput.mapValues(String::lowercase)
        //record = (null, "hello world hello kafka")
        .flatMapValues { text -> text.split(" ") }
        //(null, "hello"), (null, "world"), (null, "hello"), (null, "kafka")
        .selectKey { _, word -> word }
        //("hello", "hello"), ("world", "world"), ("hello", "hello"), ("kafka", "kafka")
        .groupByKey()
        //("hello", [("hello", "hello"), ("hello", "hello")]), ("world", [("world", "world")]), ("kafka", [("kafka", "kafka")]
        .count(Materialized.`as`("counts"))
        //("hello": 2), ("world":1), ("kafka":1)
        .toStream()
        .to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()))

    val streams = KafkaStreams(builder.build(), config)
    streams.start()
    println(streams.toString())

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
