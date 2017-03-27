package nl.ordina.messages;

import lombok.extern.java.Log;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;
import org.opencv.core.Mat;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;


import reactor.core.publisher.Flux;
import reactor.kafka.receiver.Receiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.Sender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Log
public class ImagePublisherService {

    private Sender<Integer, byte[]> sender;
    private static final String TOPIC = "faces";

    @PostConstruct
    public void init() {
        SenderOptions<Integer, byte[]> senderOptions = SenderOptions.create(senderProps());
        sender = Sender.create(senderOptions);
    }


    public Flux<SenderResult<Integer>> send(Flux<byte[]> publisher) {
        return sender.send(
                publisher
                        .map(msg -> SenderRecord.create(new ProducerRecord<Integer, byte[]>("faces",msg), 1))
                        .doOnError(t -> t.printStackTrace())
                ,true);
    }

    private Map<String, Object> senderProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return props;
    }
}