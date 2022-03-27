package hello.advanced.trace.hellotrace;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloTraceV1 {

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    public TraceStatus begin(String message) {
        TraceId traceId = new TraceId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
        return new TraceStatus(traceId, startTimeMs, message);
    }

    public void end(TraceStatus status) {
        complete(status, null);
    }

    public void exception(TraceStatus status, Exception exception) {
        complete(status, exception);
    }

    private void complete(TraceStatus status, Exception exception) {
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();
        if (exception == null) {
            log.info(
                "[{}] {}{} time={}ms",
                traceId.getId(), addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs
            );
        } else {
            log.info(
                "[{}] {}{} time={}ms ex={}",
                traceId.getId(), addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, exception.toString()
            );
        }
    }

    /**
     * ----- [ Prefix : --> ] -----
     * level = 0
     * level = 1, |-->
     * level = 2, |   |-->
     * ----- [ COMPLETE_PREFIX : <-- ] -----
     * level = 0
     * level = 1, |<--
     * level = 2, |   |<--
     * ----- [ EX_PREFIX : <X- ] -----
     * level = 0 ex
     * level = 1 ex, |<X-
     * level = 2 ex, |   |<X-
     * */
    private static String addSpace(String prefix, int level) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            stringBuilder.append((i == level - 1) ? "|" + prefix : "|   ");
        }
        return stringBuilder.toString();
    }
}
