package com.example.sqlserver_debezium;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

@Component
public class CDCProcessor extends RouteBuilder {
    private final String offsetStorageFileName = "C:\\Users\\crist\\OneDrive - UVG\\Escritorio\\Solace\\Projects\\offset-file.dat";
    private final String host = "192.168.1.18"; // IP de tu SQL Server
    private final String port = "1433"; // Puerto SQL Server
    private final String username = "sa"; // Usuario SQL Server
    private final String password = "12345678"; // Contraseña
    private final String database = "TalmaDB"; // Nombre de la base de datos

    private final Consumer<Message<Map<String, Object>>> cdcOutListener;

    public CDCProcessor(Consumer<Message<Map<String, Object>>> cdcOutListener) {
        this.cdcOutListener = cdcOutListener;
    }

    @Override
    public void configure() {
        String debeziumConfig = String.format(
                "debezium-sqlserver:dbz-sqlserver?"+ 
                        "offsetStorageFileName=%s"+
                        "&databaseHostname=%s"+
                        "&databasePort=%s"+
                        "&databaseUser=%s"+
                        "&databasePassword=%s"+
                        "&databaseDbname=%s"+
                        "&pluginName=sqlserver"+
                        "&slotName=debezium_slot_%d"+
                        "&topicPrefix=cdc", 
                offsetStorageFileName, host, port, username, password, database, System.currentTimeMillis());

        from(debeziumConfig)
                .log("Evento Capturado: ${body}")
                .log("Event received from Debezium : ${body}")
                .log("    with this identifier ${headers.CamelDebeziumIdentifier}")
                .log("    with these source metadata ${headers.CamelDebeziumSourceMetadata}")
                .log("    the event occurred upon this operation '${headers.CamelDebeziumSourceOperation}'")
                .log("    on this database '${headers.CamelDebeziumSourceMetadata[db]}' and this table '${headers.CamelDebeziumSourceMetadata[table]}'")
                .log("    with the key ${headers.CamelDebeziumKey}")
                .log("    the previous value is ${headers.CamelDebeziumBefore}")
                .process(exchange -> {
                    String event = exchange.getIn().getBody(String.class);
                    Map<String, Object> messageData = new HashMap<>();

                    if (event == null || event.trim().isEmpty()) {
                        messageData.put("operation", "DELETE");
                        messageData.put("details", "Registro eliminado");
                    } else {
                        messageData.put("operation", "INSERT/UPDATE");
                        messageData.put("data", event);
                    }

                    Message<Map<String, Object>> message = MessageBuilder.withPayload(messageData).build();
                    cdcOutListener.accept(message);
                });
    }
}
