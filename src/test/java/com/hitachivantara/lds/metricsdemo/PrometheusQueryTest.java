package com.hitachivantara.lds.metricsdemo;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PrometheusQueryTest {

    public static final String TEST_DURATION_SECONDS = "test_duration_seconds";
    public static final String PUSHGATEWAY = "pushgateway";
    public static final String PROMETHEUS = "prometheus";

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService(PROMETHEUS, 9090, Wait.forListeningPort())
                    .withExposedService(PUSHGATEWAY, 9091, Wait.forListeningPort())
                    .withLocalCompose(true);

    private static PrometheusService prometheusService;

    @BeforeAll
    public static void setup() {
        environment.start();
        Integer port = environment.getServicePort("prometheus", 9090);
        prometheusService = new PrometheusService(String.format("http://localhost:%s/api/v1/query", port));
    }

    @Test
    public void missingMetricTest() throws URISyntaxException {
        String response = prometheusService.executeQuery("sum(jvm_memory_used_bytes)");
        System.out.println(response);
    }

    @Test
    public void pushgatewayTest() throws Exception {
        CollectorRegistry registry = new CollectorRegistry();
        Gauge duration = Gauge.build().name(TEST_DURATION_SECONDS).help("help text").register(registry);

        Gauge.Timer durationTimer = duration.startTimer();

        durationTimer.setDuration();
        Integer port = environment.getServicePort(PUSHGATEWAY, 9091);
        PushGateway pg = new PushGateway("127.0.0.1:"+port);
        pg.pushAdd(registry, "my_batch_job");

        Awaitility.await().atMost(Duration.ONE_MINUTE).until(metricIsAvailable());
    }

    private Callable<Boolean> metricIsAvailable() {
        return () -> prometheusService.executeQuery(TEST_DURATION_SECONDS).contains(TEST_DURATION_SECONDS);
    }

}
