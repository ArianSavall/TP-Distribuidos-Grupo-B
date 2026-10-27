package unla.tp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. Exchange principal compartido por toda la arquitectura
    public static final String EXCHANGE = "rentar.topic.exchange";

    // 2. Nombres de las Colas (Queues) que consume este microservicio
    public static final String Q_VERIFICACION_INTERNA = "q.policial.verificacion.solicitada";
    public static final String Q_VERIFICACION_EXTERNA = "q.policial.externos.registrados";
    public static final String Q_MANTENIMIENTO = "q.mantenimiento.reserva.confirmada";
    public static final String Q_TITULARIDAD_EXTERNA ="q.policial.externos.titularidad";

    // 3. Routing Keys de ENTRADA (Eventos que escuchamos)
    public static final String RK_VERIFICACION_SOLICITADA = "vehiculos.verificacion.solicitada";
    public static final String RK_EXTERNOS_REGISTRADOS = "vehiculos.externos.registrados";
    public static final String RK_RESERVA_CONFIRMADA = "reserva.confirmada";

    // 4. Routing Keys de SALIDA (Eventos que publicamos según la consigna)
    public static final String RK_VERIFICACION_RESULTADO = "vehiculos.verificacion.resultado";
    public static final String RK_EXTERNOS_VALIDACION_PATENTE = "vehiculos.externos.validacion.patente";
    public static final String RK_EXTERNOS_VALIDACION_TITULARIDAD = "vehiculos.externos.validacion.titularidad";
    public static final String RK_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    // --- Declaración del Exchange ---
    @Bean
    public TopicExchange rentarExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // --- Declaración de las Colas (Durables para no perder mensajes si se reinicia RabbitMQ) ---
    @Bean
    public Queue verificacionInternaQueue() {
        return QueueBuilder.durable(Q_VERIFICACION_INTERNA).build();
    }

    @Bean
    public Queue verificacionExternaQueue() {
        return QueueBuilder.durable(Q_VERIFICACION_EXTERNA).build();
    }

    @Bean
    public Queue mantenimientoQueue() {
        return QueueBuilder.durable(Q_MANTENIMIENTO).build();
    }

    @Bean Queue titularidadExternaQueue(){
        return QueueBuilder.durable(Q_TITULARIDAD_EXTERNA).build();
    }

    // --- Bindings (Unión de cada Cola al Exchange con su Routing Key exacta) ---
    @Bean
    public Binding bindVerificacionInterna(Queue verificacionInternaQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(verificacionInternaQueue)
                .to(rentarExchange)
                .with(RK_VERIFICACION_SOLICITADA);
    }

    @Bean
    public Binding bindVerificacionExterna(Queue verificacionExternaQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(verificacionExternaQueue)
                .to(rentarExchange)
                .with(RK_EXTERNOS_REGISTRADOS);
    }

    @Bean
    public Binding bindMantenimiento(Queue mantenimientoQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(mantenimientoQueue)
                .to(rentarExchange)
                .with(RK_RESERVA_CONFIRMADA);
    }

    @Bean Binding bindTitularidadExterna(Queue titularidadExternaQueue, TopicExchange rentarExchange){
        return BindingBuilder.bind(titularidadExternaQueue)
                .to(rentarExchange)
                .with(RK_EXTERNOS_REGISTRADOS);
    }

    // --- Serialización JSON para desacoplar paquetes entre microservicios ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new JacksonJsonMessageConverter(mapper.toString());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}