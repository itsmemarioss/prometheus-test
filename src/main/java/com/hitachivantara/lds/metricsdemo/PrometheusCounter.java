package com.hitachivantara.lds.metricsdemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/counter")
public class PrometheusCounter {

    private final MeterRegistry register;
    Counter counter;

    public PrometheusCounter(MeterRegistry register) {
        this.register = register;
        counter = Counter.builder("mario_counter_test").register(this.register);
    }

    @GetMapping
    public ResponseEntity<Double> increment(){
        System.out.println("incrementing...");
        counter.increment();
        return ResponseEntity.ok(counter.count());
    }

}
