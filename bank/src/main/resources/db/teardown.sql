
-- 테스트 메서드 수행 이후 데이터베이스 초기화 쿼리
SET REFERENTIAL_INTEGRITY FALSE; 
TRUNCATE table transaction_tb;
TRUNCATE table account_tb;
TRUNCATE table user_tb;
SET REFERENTIAL_INTEGRITY TRUE;