package co.com.InternetDeLasCosas.api.Resource.Login;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login,Long> {

    Optional<Login> findByUserAndPassword(String user, String password);
}
