import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

const val USER_TOPIC = "users"
const val PURCHASE_TOPIC = "purchases"

fun main() {
    val producer = KafkaProducer<String, String>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.CLIENT_ID_CONFIG to "user-event-producer",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer().javaClass,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer().javaClass,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to "3",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true"
        )
    )

    println("1. user 생성 후 purchase 생성")
    producer.send(USER_TOPIC, "1", "first=kim,last=js")
    producer.send(PURCHASE_TOPIC, "1", "banana")
    Thread.sleep(10000)

    println("2. 존재하지 않는 user의 purchase 생성")
    producer.send(PURCHASE_TOPIC, "2", "apple")
    Thread.sleep(10000)

    println("3. user 갱신 후 purchase 생성")
    producer.send(USER_TOPIC, "1", "first=kim,last=rizzle")
    producer.send(PURCHASE_TOPIC, "1", "mango")
    Thread.sleep(10000)

    println("4. 현재는 존재하지 않는 3번 user의 purchase가 생성됨, 이후 user 생성")
    producer.send(PURCHASE_TOPIC, "3", "peach")
    producer.send(USER_TOPIC, "3", "first=lee,last=don")
    producer.send(PURCHASE_TOPIC, "3", "apple")
    producer.send(USER_TOPIC, "3", null)
    Thread.sleep(10000)

    println("5. user를 생성했지만 purchase가 들어오기 전 user 삭제")
    producer.send(USER_TOPIC, "4", "first=park,last=alice")
    producer.send(USER_TOPIC, "4", null)
    producer.send(PURCHASE_TOPIC, "4", "apple")
    Thread.sleep(10000)

    producer.close()
}

fun KafkaProducer<String, String>.send(topic:String, key: String?, value: String?) {
    send(ProducerRecord(topic, key, value)).get().also { println(it.topic()) }
}
