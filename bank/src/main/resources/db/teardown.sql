
-- 테이블간 연관관계 무효화를 위한 REFERENTIAL_INTEGRITY FALSE 설정 
/*
테이블간 연관관계 무효화를 하는 이유 :
각 테이블들이 연관관계로 묶여있을 경우 삭제하는 순서를 제대로 지켜주지 않으면 테이블이 제대로 삭제되지 않을 수 있음
특히나 연관관계를 맺고 있는 테이블 중에서 외래키를 가지고 있는 테이블. 즉, 부모-자식 관계 테이블에서 외래키를 가지고 있는 자식 테이블이 아닌
부모 테이블을 먼저 삭제하려고 할 경우 참조 오류 발생방지 차원에서 자동으로 삭제가 되지 않을 수 있다.
그렇기 때문에 보통 연관관계를 맺고 있는 테이블들을 삭제할 경우 외래키를 가지고 있는 자식 테이블을 먼저 삭제한 다음 부모 테이블을 삭제해아 
오류없이 깔끔하게 테이블을 삭제할 수 있다.

그런데 테이블들의 숫자가 많아지고 연관관계가 복잡하게 꼬이게 되는 순간이 왔을 때 테이블을 일단 테이블을 삭제하려면
아래와 같이 어떤 관계이든 상관없이 연관관계를 무효화하는 REFERENTIAL_INTEGRITY FALSE 옵션을 SET 쿼리로 실행해서 모든 테이블의 연관관계를 일단 끊어놓고
삭제가 필요한 테이블들을 삭제한 다음 다시 REFERENTIAL_INTEGRITY TRUE 옵션을 실행해서 연관관계를 복원해줄 수 있다.

이와같이 쿼리를 작성해주면 @Transactional 어노테이션을 대체할 수 있다.
테스트 메서드의 실행이 끝나면 데이터베이스를 롤백하는 것이 아니라 테이블 자체를 아예 드랍(삭제) 해버리는 것이다.
*/

/*
SET REFERENTIAL_INTEGRITY FALSE; 
drop table transaction_tb;
drop table account_tb;
drop table user_tb;
SET REFERENTIAL_INTEGRITY TRUE;
*/

/*
그런데 이 방식의 단점은 테스트를 수행함에 있어서 테이블삭제를 통해 데이터와 기본키 값을 쉽게 초기화 해줄 수 있기는 하나,
기본적으로 테이블을 삭제하는 방식이기 때문에 각 테스트 메서드를 실핼할 때마다 테이블을 새로 만들어주어야 한다.(create)
이런 방식은 번거롭기도 하고 성능상으로도 좋지 않기 때문에 drop 이 아닌 truncate 로 쿼리를 대체해줄 수 있다.
*/ 
SET REFERENTIAL_INTEGRITY FALSE; 
TRUNCATE table transaction_tb;
TRUNCATE table account_tb;
TRUNCATE table user_tb;
SET REFERENTIAL_INTEGRITY TRUE;

/*
truncate 는 테이블 안에 있는 모든 내용을 지우는 명령이다.
이러면 굳이 테스트 메서드를 실행할 때마다 테이블을 새로 만들어줄 필요가 없는 동시에 기본키 값 또한 초기화해줄 수 있다.
앞으로 Mock 객체를 사용하지 않고 실제로 테스트 데이터베이스에 객체를 저장하며 진행하는 컨트롤러 테스트에서는 @Transactional 어노테이션 대신
이와같은 teardown.sql 파일의 쿼리를 사용해주자.
*/