package transaction;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void add(User user){
        this.jdbcTemplate.update(
                "insert into user (name) values (?)",
                user.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addRollBack(User user){
        this.jdbcTemplate.update(
                "insert into user (name) values (?)",
                user.getName());
        throw new RuntimeException();
    }

    /**
     * 事物不生效
     * @param user
     */
    public void addInlineWithoutTransactional(User user){
        addRollBack(user);
    }

    /**
     * 事物生效 走代理类了
     * @param user
     */
    public void addInlineWithoutTransactionalUseAopContext(User user){
        UserService userService = (UserService)AopContext.currentProxy();
        userService.addRollBack(user);
    }

    /**
     * 事物生效 走了代理方法
     * @param user
     */
    @Transactional(rollbackFor = Exception.class)
    public void addInlineWithTransactional(User user){
        addRollBackWithoutTransactional(user);
    }

    public void addRollBackWithoutTransactional(User user){
        this.jdbcTemplate.update(
                "insert into user (name) values (?)",
                user.getName());
        throw new RuntimeException();
    }

    /**
     * 事物失效
     * @param user
     */
    public void addPrivate(User user){
        privateMethod(user);
    }

    /**
     * 事物生效
     * @param user
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPrivateWithTransactional(User user){
        jdbcTemplate.update(
                "insert into user (name) values (?)",
                "test1");
        privateMethod(user);
    }

    /**
     * 如果用 jdk代理 报错
     * Exception in thread "main" java.lang.IllegalStateException: Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available, and ensure that AopContext.currentProxy() is invoked in the same thread as the AOP invocation context.
     * 如果用 AspectJ 代理， @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
     * Exception in thread "main" java.lang.NullPointerException
     * 	at transaction.UserService.privateMethod(UserService.java:97)
     * 	at transaction.UserService.privateMethodUseAopContext(UserService.java:91)
     * @param user
     */
    public void privateMethodUseAopContext(User user){
        UserService userService = (UserService)AopContext.currentProxy();
        // 没走代理类 jdbcTemplate没有注入
        userService.privateMethod(user);
    }

    @Transactional(rollbackFor = Exception.class)
    private void privateMethod(User user) {
        jdbcTemplate.update(
                "insert into user (name) values (?)",
                user.getName());
        throw new RuntimeException("有异常！");
    }



}
