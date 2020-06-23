package transaction;

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

}
