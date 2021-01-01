package transaction;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TransactionWithPrivateMethodback {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext();
        applicationContext.scan("transaction");
        applicationContext.register(Config.class);
        applicationContext.refresh();
        UserService userService = applicationContext.getBean(UserService.class);
        User user=new User("test");
//        userService.addPrivate(user);
//        userService.addInlineWithoutTransactional(user);
//        userService.addInlineWithTransactional(user);
        userService.addInlineWithoutTransactionalUseAopContext(user);
//        userService.addPrivateWithTransactional(user);
//        userService.privateMethodUseAopContext(user);
    }

}
