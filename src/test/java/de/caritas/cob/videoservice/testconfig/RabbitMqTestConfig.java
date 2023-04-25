package de.caritas.cob.videoservice.testconfig;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMqTestConfig {

  public static final String STATISTICS_EXCHANGE_NAME = "statistics.topic";
  private static final String QUEUE_PREFIX = "statistics.";
  public static final String QUEUE_NAME_START_VIDEO_CALL =
      QUEUE_PREFIX + EventType.START_VIDEO_CALL;

  @Bean
  ConnectionFactory connectionFactory() {
    return new CachingConnectionFactory(new MockConnectionFactory());
  }

  /**
   * RabbitMQ configuration method.
   *
   * @return an {@link Declarables} instance
   */
  @Bean
  public Declarables topicBindings() {
    Queue assignSessionStatisticEventQueue = new Queue(QUEUE_NAME_START_VIDEO_CALL, true);

    TopicExchange topicExchange = new TopicExchange(STATISTICS_EXCHANGE_NAME, true, false);

    return new Declarables(
        assignSessionStatisticEventQueue,
        topicExchange,
        BindingBuilder.bind(assignSessionStatisticEventQueue)
            .to(topicExchange)
            .with(EventType.START_VIDEO_CALL));
  }
}
