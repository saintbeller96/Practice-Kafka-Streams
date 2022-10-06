import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Produced
import java.util.*

class FavoriteColorTest : StringSpec({

    val config = mapOf(
        StreamsConfig.APPLICATION_ID_CONFIG to "favorite-color-application",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
        StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG to "0"//각 단계별 과정을 보기 위해 캐시를 설정하지 않는다(개발 단계에서만 사용할 것)
    )

    val props = Properties().apply {
        putAll(config)
    }

    lateinit var driver: TopologyTestDriver
    lateinit var inputTopics: TestInputTopic<String, String>
    lateinit var outputTopics: TestOutputTopic<String, Long>

    afterEach {
        driver.close()
    }

    "favorite color test" {
        val inputData = listOf("js,blue", "kim,green", "alice,red", "js,red", "kim,black")

        val builder = StreamsBuilder()

        //문자열을 처리해 중간 토픽에 기록
        builder.stream<String, String>(inputTopic)
            .filter { _, value -> regx.matches(value) }
            .mapValues { _, value -> value.split(",") }
            .selectKey { _, value -> value.first() }
            .mapValues { value -> value.last() }
            .filter { _, color -> color in validColors }
            .to("favor-color-by-name")

        //중간 토픽에 있는 log를 KTable로 가져오면서 log compaction 된다. 따라서 최신 데이터로 갱신됨.
        builder.table<String, String>("favor-color-by-name")
            .groupBy { name, color -> KeyValue<String, String>(color, color) }
            .count(Named.`as`("counts"))
            .toStream()
            .to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()))

        driver = TopologyTestDriver(builder.build(), props)
        inputTopics = driver.createInputTopic(inputTopic, NothingSerde(), StringSerializer())
        outputTopics = driver.createOutputTopic(outputTopic, StringDeserializer(), LongDeserializer())

        inputTopics.pipeValueList(inputData)

        val expected = mapOf(
            "blue" to 0L,
            "green" to 1L,
            "red" to 2L,
        )

        outputTopics.toMap() shouldContainExactly expected
        outputTopics.isEmpty shouldBe true
    }
}) {
    companion object {
        const val inputTopic = "favor-input-topic"
        const val outputTopic = "favor-output-topic"
        val validColors = setOf("red", "green", "blue")
        val regx = Regex(".+,.+")
    }
}

fun TestOutputTopic<String, Long>.toMap(): Map<String, Long> {
    return readKeyValuesToMap().toMap()
}

internal class NothingSerde<T>() : Serializer<T>, Deserializer<T>, Serde<T> {

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

    override fun close() {}

    override fun serialize(topic: String?, data: T): ByteArray? {
        if (data != null) {
            throw IllegalArgumentException()
        }
        return null
    }

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        if (data != null) {
            throw IllegalArgumentException()
        }
        return null
    }

    override fun serializer(): Serializer<T> = this
    override fun deserializer(): Deserializer<T> = this
}
