package de.bonito.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class DashboardApplication {

    private static final Log LOGGER = LogFactory.getLog(DashboardApplication.class);
    private static final String topic = "bonito/counter";
    private static int counter = 0;

    /**
     * Load the Spring Integration Application Context
     *
     * @param args - command line arguments
     */
    public static void main(final String... args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://localhost:1883"});
//        options.setUserName("guest");
//        options.setPassword("guest".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("dashboard", mqttClientFactory());

        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(topic);

        return messageHandler;
    }

    // consumer

    @Bean
    public IntegrationFlow mqttInFlow() {
        return IntegrationFlows.from(mqttInbound())
            .transform(message -> {
                counter += Integer.parseInt(message.toString());
                return message + ", received from MQTT";
            })
            .handle(logger())
            .get();
    }

    private LoggingHandler logger() {
        LoggingHandler loggingHandler = new LoggingHandler("INFO");

        loggingHandler.setLoggerName("dashboard");

        return loggingHandler;
    }

    @Bean
    public MessageProducerSupport mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("dashboard",
                mqttClientFactory(), topic);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);

        return adapter;
    }

    public static int getCounter() {
        return counter;
    }
}
