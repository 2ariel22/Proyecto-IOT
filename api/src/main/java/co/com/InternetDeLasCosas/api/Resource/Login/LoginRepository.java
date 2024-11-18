package co.com.InternetDeLasCosas.api.Resource.Login;

import co.com.InternetDeLasCosas.api.Resource.Components.Components;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LoginRepository extends MongoRepository<Login,String> {

    Optional<Login> findByUserAndPassword(String user, String password);
}
