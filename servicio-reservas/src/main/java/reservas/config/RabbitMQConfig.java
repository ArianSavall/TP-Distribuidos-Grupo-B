package reservas.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "rentar.topic.exchange";

    // Routing key que PUBLICA servicio-reservas
    public static final String RK_RESERVA_CONFIRMADA = "reserva.confirmada";

    // Routing key que ESCUCHA servicio-reservas
    public static final String RK_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    // Cola propia de servicio-reservas
    public static final String Q_MANTENIMIENTO_RESULTADO = "q.reservas.mantenimiento.resultado";

    @Bean
    public TopicExchange rentarExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue mantenimientoResultadoQueue() {
        return QueueBuilder.durable(Q_MANTENIMIENTO_RESULTADO).build();
    }

    @Bean
    public Binding bindMantenimientoResultado(Queue mantenimientoResultadoQueue, TopicExchange rentarExchange) {
        return BindingBuilder.bind(mantenimientoResultadoQueue)
                .to(rentarExchange)
                .with(RK_MANTENIMIENTO_RESULTADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}