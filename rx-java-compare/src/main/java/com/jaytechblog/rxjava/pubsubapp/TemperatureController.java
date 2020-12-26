package com.jaytechblog.rxjava.pubsubapp;

import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class TemperatureController {
    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @GetMapping("/temperature-stream")
    public SseEmitter events(HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter();
        clients.add(emitter);

        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onCompletion(() -> clients.remove(emitter));
        return emitter;
    }

    /**
     * 스프링에서 제공하는 발행-구독 구조
     * 스프링 프레임워크에서 이 메커니즘은 처음에 응용 프로그램 수명 주기 이벤트를 처리하기 위해 도입된 것. 고부하 및 고성능 시나리오를 위한 것이 아님.
     * 다른 단점은 스프링 프레임워크의 내부 메커니즘을 사용한다는 것. 프레임워크의 사소한 변경으로 인해 응용프로그램의 안정성을 위협할 수 있다.
     * 게다가 ApplicationContext 를 로드하지 않고 비지니스 로직은 유닛 테스트하기도 어렵다.
     *
     * 많은 메서드에 @EventListener 가 붙어있고 전체 플로우를 설명하는 코드도 없다.
     *
     * @param temperature 온도
     */
    @Async
    @EventListener
    public void handleMessage(Temperature temperature) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        clients.forEach(emitter -> {
            try {
                emitter.send(temperature, MediaType.APPLICATION_JSON);
            } catch (Exception ignore) {
                deadEmitters.add(emitter);
            }
        });
        clients.removeAll(deadEmitters);
    }
}
