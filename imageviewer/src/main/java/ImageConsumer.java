import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ImageConsumer {

    private static final String TOPIC = "faces";

    public Flux<byte[]> create() {
        return Flux.from(new Publisher<byte[]>() {
            @Override
            public void subscribe(Subscriber<? super byte[]> subscriber) {
                KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<Integer, byte[]>(props());
                consumer.subscribe(Arrays.asList(TOPIC));
                while (true) {
                    ConsumerRecords<Integer, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<Integer, byte[]> record : records) {
                        if(subscriber == null) {
                            System.out.println("subscriber is null");
                        }
                        if(record == null) {
                            System.out.println("record is null");
                        }
                        subscriber.onNext(record.value());
                    }
                }
            }
        });
    }

    public void listen(Consumer<byte[]> callback) {
        KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<Integer, byte[]>(props());
        consumer.subscribe(Arrays.asList(TOPIC));
        while (true) {
            ConsumerRecords<Integer, byte[]> records = consumer.poll(Long.MAX_VALUE);
            for (ConsumerRecord<Integer, byte[]> record : records) {
                Map<String, Object> data = new HashMap<>();
                data.put("partition", record.partition());
                data.put("offset", record.offset());
//                data.put("value", record.value());
                System.out.println(" image consumer: " + data);
                callback.accept(record.value());
//                System.out.println(" image consumer: " + data);

            }
        }
    }


    private Map<String, Object> props() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "imageviewer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return props;

    }
}
