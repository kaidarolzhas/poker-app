package webapp.sec.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webapp.data.entity.User;

import java.util.Optional;

@Repository
public interface UserRepositoryJava extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

