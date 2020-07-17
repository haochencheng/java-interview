-- 查看正在执行的事务
SELECT * FROM information_schema.INNODB_TRX;

-- session是否自动提交
show session variables like 'autocommit';

-- 查看数据库最大连接
show global variables like 'max_conn%';

-- 查看数据库拒绝信息
show global status like 'aborted%';

-- 查看数据库超时时间
show global variables like '%timeout%';