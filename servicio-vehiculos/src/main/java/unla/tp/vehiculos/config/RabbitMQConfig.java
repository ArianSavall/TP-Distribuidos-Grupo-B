package unla.tp.vehiculos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "rentar.topic.exchange";

    // --- Routing Keys que PUBLICA servicio-vehiculos ---
    public static final String RK_VERIFICACION_SOLICITADA = "vehiculos.verificacion.solicitada";
    public static final String RK_EXTERNOS_REGISTRADOS = "vehiculos.externos.registrados";
    public static final String RK_EXTERNOS_VALIDACION_COMPLETADA = "vehiculos.externos.validacion.completada";

    // --- Routing Keys que ESCUCHA servicio-vehiculos ---
    public static final String RK_VERIFICACION_RESULTADO = "vehiculos.verificacion.resultado";
    public static final String RK_EXTERNOS_VALIDACION_PATENTE = "vehiculos.externos.validacion.patente";
    public static final String RK_EXTERNOS_VALIDACION_TITULARIDAD = "vehiculos.externos.validacion.titularidad";

    // --- Nombres de las Colas (Queues) propias de servicio-vehiculos ---
    public static final String Q_VERIFICACION_RESULTADO = "q.vehiculos.verificacion.resultado";
    public static final String Q_EXTERNOS_PATENTE = "q.vehiculos.externos.validacion.patente";
    public static final String Q_EXTERNOS_TITULARIDAD = "q.vehiculos.externos.validacion.titularidad";
    public static final String Q_EXTERNOS_COMPLETADA = "q.vehiculos.externos.validacion.completada";

    @Bean
    public TopicExchange rentarExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // --- Declaración de Colas ---
    @Bean
    public Queue verificacionResultadoQueue() {
        return QueueBuilder.durable(Q_VERIFICACION_RESULTADO).build();
    }

    @Bean
    public Queue externosPatenteQueue() {
        return QueueBuilder.durable(Q_EXTERNOS_PATENTE).build();
    }

    @Bean
    public Queue externosTitularidadQueue() {
        return QueueBuilder.durable(Q_EXTERNOS_TITULARIDAD).build();
    }

    @Bean
    public Queue externosCompletadaQueue() {
        return QueueBuilder.durable(Q_EXTERNOS_COMPLETADA).build();
    }

    // --- Bindings ---
    @Bean
    public Binding bindVerificacionResultado(Queue verificacionResultadoQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(verificacionResultadoQueue).to(rentarExchange).with(RK_VERIFICACION_RESULTADO);
    }

    @Bean
    public Binding bindExternosPatente(Queue externosPatenteQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(externosPatenteQueue).to(rentarExchange).with(RK_EXTERNOS_VALIDACION_PATENTE);
    }

    @Bean
    public Binding bindExternosTitularidad(Queue externosTitularidadQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(externosTitularidadQueue).to(rentarExchange).with(RK_EXTERNOS_VALIDACION_TITULARIDAD);
    }

    @Bean
    public Binding bindExternosCompletada(Queue externosCompletadaQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(externosCompletadaQueue).to(rentarExchange).with(RK_EXTERNOS_VALIDACION_COMPLETADA);
    }

    // --- Conversor JSON con inferencia de tipos para evitar conflictos de paquetes ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(String.valueOf(mapper));
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}