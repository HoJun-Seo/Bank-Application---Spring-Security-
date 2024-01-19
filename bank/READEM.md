# Junit Bank App

### JPA LocalDateTime 자동으로 생성하는 법
- @EnableJpaAuditing (Main 클래스)
- @EntityListeners(AuditingEntityListener.class) (Entity 클래스)
```java
    @CreatedDate // 데이터 insert 시 날짜가 자동으로 입력됨
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 데이터 insert, update 시 날짜가 자동으로 입력됨
    @Column(nullable = false)
    private LocalDateTime updatedAt;
```