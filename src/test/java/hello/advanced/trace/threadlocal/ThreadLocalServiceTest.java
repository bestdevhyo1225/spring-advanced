package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.ThreadLocalService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ThreadLocalServiceTest {

    private final ThreadLocalService service = new ThreadLocalService();

    @Test
    void field() {
        log.info("main start");

        Runnable userA = () -> service.logic("userA");
        Runnable userB = () -> service.logic("userB");

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start();
//        sleep(2000); // 2초를 sleep 하기 때문에 동시성 문제가 발생하지 않는다.
        sleep(100); // 100ms 만큼만 sleep 하기 때문에 거의 동시에 시작되는데, ThreadLocal 객체로 인해 동시성 문제가 발생하지 않는다.
        threadB.start();

        sleep(3000); // 메인 스레드 종료를 대기하기 위해서 3초를 준다.

        log.info("main exit");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
