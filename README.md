# Spring 핵심 원리 - 고급편

## Thread Local

싱글톤 클래스에 필드를 선언해서 사용하면, 동시성 문제가 반드시 발생하는데, 필드를 읽는 경우에는 동시성 문제가 발생하지 않지만, 필드를 `동시에 수정하는 경우` 에 동시성 문제가 발생한다. 이
때, `ThreadLocal` 객체를 사용하면, 동시성 문제를 해결할 수 있다. ThreadLocal 특징을 다음과 같이 요약할 수 있다.

- 해당 스레드만 접근할 수 있는 특별한 저장소이다.
- 각 스레드의 별도 내부 저장소이다.
- 같은 인스턴스의 스레드 로컬 필드에 접근해도 문제가 없다.

### 주의할 점

ThreadLocal을 사용하고 나면, 반드시 `remove()` 메서드를 통해 저장된 값을 제거해야한다. WAS(Tomcat) 처럼 Thread Pool을 사용하는 경우 심각한 문제가 발생한다.

- `remove()` 를 하지 않으면, 이전 사용자의 정보가 남아있는 치명적인 문제가 발생한다.

## 템플릿 메서드 패턴 (Template Method Pattern)

부모 클래스에 알고리즘의 골격인 템플릿을 정의하고, 일부 변경되는 로직은 자식 클래스에서 정의하는 것이다. 이렇게 하면 자식 클래스가 알고리즘의 전체 구조를 변경하지 않고, 특정 부분만 재정의할 수 있다.

- 상속과 오버라이딩을 통한 다형성으로 문제를 해결하는 것

### 예제

각 메서드의 수행 시간을 측정해야 하는 공통 로직이 있다면, 아래와 같은 방법으로 해결할 수 있다.

> AbstractTemplate

```java
public abstract class AbstractTemplate {

    public void execute() {
        long startTime = System.currentTimeMillis();

        call();

        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        System.out.println("resultTime = " + resultTime);
    }

    protected abstract void call();
}
```

> SubClassLogic1

```java
public class SubClassLogic1 extends AbstractTemplate {

    @Override
    protected void call() {
        System.out.println("비즈니스 로직1 실행");
    }
}
```

> SubClassLogic2

```java
public class SubClassLogic2 extends AbstractTemplate {

    @Override
    protected void call() {
        System.out.println("비즈니스 로직2 실행");
    }
}
```

> 테스트

```java
public class TemplateMethodTest {

    @Test
    void templateMethodV1() {
        AbstractTemplate template1 = new SubClassLogic1();
        template1.execute();

        AbstractTemplate template2 = new SubClassLogic2();
        template2.execute();

        /* [결과]
         * 비즈니스 로직1 실행
         * resultTime=9
         * 비즈니스 로직2 실행
         * resultTime=0
         * */
    }
}
```

### 단점

- 자식 클래스가 부모 클래스와 컴파일 시점에 강하게 결합되는 문제가 있다. (`의존관계에 대한 문제!`)

- 자식 클래스가 부모 클래스에 의존한다는 의미는, `자식 클래스에 부모 클래스 코드가 명확하게 적혀 있다는 뜻이다.`

### 상속의 단점을 제거할 수 있는 방법?

- 상속보다는 위임, 컴포지션, 구성

- 템플릿 메서드 패턴과 비슷한 역할을 하면서 상속의 단점을 제거할 수 있는 **`전략 패턴(Strategy Pattern)`** 이 있다.

## 전략 패턴 (Strategy Pattern)

전략 패턴은 `변하지 않는 부분을 Context` 라는 곳에 두고, `변하는 부분을 Strategy` 라는 인터페이스 를 만들고, 해당 인터페이스를 구현하도록 해서 문제를
해결한다. **`상속이 아니라 위임으로 문제를 해결하는 것이다.`**

- `Context` 는 `변하지 않는 템플릿` 역할을 한다.

- `Strategy` 는 `변하는 알고리즘` 역할은 한다.

GOF 디자인 패턴에서 정의한 전략 패턴의 의도는 다음과 같다.

> 알고리즘 제품군을 정의하고, 각각을 캡슐화하여 상호 교환 가능하게 만들자. 전략을 사용하면, **`알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경`** 할 수 있다.

`전략 패턴의 핵심은 Context가 Strategy에만 의존` 한다는 점이다. 따라서 `Strategy` 의 구현체를 변경하거나, 새로 만들어도 `Context` 코드에는 영향을 주지 않는다.

> **Spring에서 의존관계를 주입할 때, 사용하는 방식이 바로 전략 패턴이다.**

### 예제 (필드에 젼략을 보관하는 방식)

> Strategy (변하는 알고리즘)

```java
public interface Strategy {
    void call();
}
```

```java

@Slf4j
public class StrategyLogic1 implements Strategy {

    @Override
    public void call() {
        log.info("비즈니스 로직1 실행");
    }
}
```

```java

@Slf4j
public class StrategyLogic2 implements Strategy {

    @Override
    public void call() {
        log.info("비즈니스 로직2 실행");
    }
}
```

> Context (변하지 않는 템플릿)

```java

@Slf4j
public class ContextV1 {

    private final Strategy strategy;

    public ContextV1(Strategy strategy) {
        this.strategy = strategy;
    }

    public void execute() {
        long startTime = System.currentTimeMillis();
        // 비즈니스 로직 실행
        strategy.call(); // 위임
        // 비즈니스 로직 종료
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime={}", resultTime);
    }
}
```

인터페이스에 메서드가 하나가 있으면, 익명 내부 클래스를 람다로 변경할 수 있다.

```java

@Slf4j
public class ContextV1Test {
    @Test
    void strategyV3() {
        ContextV1 context1 = new ContextV1(() -> log.info("비즈니스 로직1 실행"));
        context1.execute();

        ContextV1 context2 = new ContextV1(() -> log.info("비즈니스 로직2 실행"));
        context2.execute();
    }
}
```

### 예제 (전략을 파라미터로 전달 받는 방식)

```java

@Slf4j
public class ContextV2 {

    public void execute(Strategy strategy) {
        long startTime = System.currentTimeMillis();
        // 비즈니스 로직 실행
        strategy.call(); // 위임
        // 비즈니스 로직 종료
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime={}", resultTime);
    }
}
```

```java

@Slf4j
public class ContextV2Test {

    @Test
    void strategyV3() {
        ContextV2 context = new ContextV2();
        context.execute(() -> log.info("비즈니스 로직1 실행"));
        context.execute(() -> log.info("비즈니스 로직2 실행"));
    }
}
```

### 정리

`Context1` 의 경우, `Strategy` 를 저장하는 방식으로 구현했다.

- 선 조립 후, 실행 방법에 적합하다.
- `Context` 를 실행하는 시점에는 이미 조립이 끝났기 때문에 단순히 실행만 하면된다.

`Context2` 는 파라미터에 `Strategy` 를 전달받는 방식으로 구현했다.

- 실행할 때 마다 전략을 유연하게 사용할 수 있다.
- 단점은 실행할 때 마다 전략을 계속 지정해줘야 한다는 점이다.

## 템플릿 콜백 패턴

### Callback?

프로그래밍에서 `callback` 또는 `call-after function` 은 다른 코드의 인수로서 넘겨주는 실행 가능한 코드를 말한다. 콜백을 넘겨받는 코드는 필요에 따라 `즉시 실행` 할 수도
있고, `나중에 실행` 할 수도 있다.

스프링에서는 `JdbcTemplate`, `RestTemplate`, `TransactionTemplate`, `RedisTemplate` 의 경우, 템플릿 콜백 패턴이 사용되었고, `xxxTemplate` 이름을
가지고 있다면, 템플릿 콜백 패턴이 사용되었다고 생각하면 된다. 
