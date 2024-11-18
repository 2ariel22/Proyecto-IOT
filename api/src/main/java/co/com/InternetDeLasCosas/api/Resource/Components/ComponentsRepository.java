package co.com.InternetDeLasCosas.api.Resource.Components;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ComponentsRepository extends MongoRepository<Components,String> {

    Optional<Components> findTopByOrderByFechaDesc();
}
