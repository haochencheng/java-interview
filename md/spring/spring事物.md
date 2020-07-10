### spring事务
####    mysql事务
默认自动提交
查看是否自动提交
```sql
show variables like "autocommit";
+---------------+-------+   
| Variable_name | Value |   
+---------------+-------+   
| autocommit    | OFF   |   
```
OFF为关闭，ON为开启
所有的 DML 语句都是要显式提交的(autocommit=OFF情况下)，
也就是说要在执行完DML语句之后，执行 COMMIT 。
而其他的诸如 DDL语句的，都是隐式提交的

####    事务管理器
```
#事务管理器标记接口
TransactionManager
```




