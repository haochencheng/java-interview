package transaction;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TransactionRollback {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext();
        applicationContext.scan("transaction");
        applicationContext.refresh();
        UserService userService = applicationContext.getBean(UserService.class);
        User user=new User("test");
        userService.addRollBack(user);
    }

}
