# System Design

## Tech Stacks

### Language
- Java 11 
### Framework
- Spring Boot 2.7
- sl44j & Logback
- Lombok

### Build
- Maven 3.6

### Test Framework
- JUnit 5
- TestContainers
- Jacoco

### Middleware
- ALI Redis
- ALI MNS for message queue
- ALI SLS


### Deployment
- ALI ACK

## Architecture (Base Hexagonal Architecture)

```mermaid
flowchart TB
%% API Layer
    subgraph API
        SyncRest[Sync REST Endpoint]
        AsyncRest[Async REST Endpoint]
        MnsListener[MNS Message Endpoint]
    end

%% Application Layer
    subgraph Application
        CommandHandler[FraudDetectCommandHandler]
        RedisChecker[Redis Idempotency Check]
        Lock[Redis Lock]
        EventPublisher[Spring Event Publisher]
        EventListener[Spring Event Listener]
    end

%% Domain Layer
    subgraph Domain
        FraudService[FraudService]
        Command[FraudDetectCommand]
        Result[FraudDetectCommandResult]
        Event[FraudDetectResultEvent]
    end

%% Infra Layer
    subgraph Infra
        Redis[(Redis)]
        MNS[Aliyun MNS Queue]
        ResponseSender[MNS Response Sender]
        DLT[DLT Queue]
    end

%% REST and async flow
    SyncRest --> CommandHandler
    AsyncRest --> MNS

%% MNS poller flow
    MNS --> MnsListener
    MnsListener --> CommandHandler

%% CommandHandler processing
    CommandHandler --> RedisChecker --> Redis
    CommandHandler --> Lock --> Redis
    CommandHandler --> FraudService
    FraudService --> Command
    FraudService --> Result
    CommandHandler --> EventPublisher --> EventListener
    EventListener --> Event --> ResponseSender --> MNS

%% Error flow
    MnsListener --> DLT:::error

    class DLT error;

 ```

### API Layer (Inbound Adapter)
1.	Serves as the entry point for the application via restful api, one async api will just send a message to queue.
2.	In this system, the main inbound adapter is the Aliyun MNS message polling module:
3.	Periodically polls a specific MNS queue.
4.	Retrieves messages in batches and processes them one by one.

### Application Layer (Use Case Orchestration)
Orchestrates business workflows and coordinates dependencies. Core responsibilities:
1.	Idempotency Check. Looks up Redis by requestId to determine if the request was already processed.
2.	Distributed Locking. If not processed, acquires a Redis-based lock to prevent duplicate execution.
3.	Fraud Detection Execution. Calls FraudService.detect() to evaluate fraud logic.
4.	Event Publishing. Uses ApplicationEventPublisher to publish a result event after execution.

### Domain Layer (Business Logic)
The FraudSevice is a key Domain service, it processes the detect logic finally, 
Currently, as a demo project, it implements in simple way. However, in real case, it maybe invoke an external rule engine.


### Infra Layer (Infrastructure and Outbound Adapters)
Implements external dependencies and technical details. In this way, the system will keep no aware about middlewares