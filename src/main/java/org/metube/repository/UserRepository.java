package org.metube.repository;

import org.metube.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    User findByEmail(String email);

    User findByUsernameAndEmail(String username, String email);
}
