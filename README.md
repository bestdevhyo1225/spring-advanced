# Spring 핵심 원리 - 고급편

## Thread Local

싱글톤 클래스에 필드를 선언해서 사용하면, 동시성 문제가 반드시 발생하는데, 필드를 읽는 경우에는 동시성 문제가 발생하지 않지만, 필드를 `동시에 수정하는 경우` 에 동시성 문제가 발생한다. 이 때, `ThreadLocal` 객체를 사용하면, 동시성 문제를 해결할 수 있다. ThreadLocal 특징을 다음과 같이 요약할 수 있다.

- 해당 스레드만 접근할 수 있는 특별한 저장소이다.
- 각 스레드의 별도 내부 저장소이다.
- 같은 인스턴스의 스레드 로컬 필드에 접근해도 문제가 없다.

### 주의할 점

ThreadLocal을 사용하고 나면, 반드시 `remove()` 메서드를 통해 저장된 값을 제거해야한다. WAS(Tomcat) 처럼 Thread Pool을 사용하는 경우 심각한 문제가 발생한다.

- `remove()` 를 하지 않으면, 이전 사용자의 정보가 남아있는 치명적인 문제가 발생한다.

## 템플릿 메서드 패턴

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
