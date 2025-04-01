package com.example.postgrelsql_debezium;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class SolaceConsumer {

    @Bean
    public Consumer<Message<Map<String, Object>>> cdcInListener() { 
        return message -> {
            Map<String, Object> payload = message.getPayload();

            if (payload.containsKey("operation")) {
                String operation = (String) payload.get("operation");

                if ("INSERT".equals(operation)) {
                    System.out.println(" Se insertó un registro: " + payload.get("details"));
                } else {
                    System.out.println(" Mensaje recibido desde Solace: " + payload.get("data"));
                }
            }
        };
    }
}
