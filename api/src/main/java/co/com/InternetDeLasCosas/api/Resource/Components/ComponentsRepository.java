package co.com.InternetDeLasCosas.api.Resource.Components;



import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentsRepository extends JpaRepository<Components,Long> {

    Optional<Components> findTopByOrderByFechaDesc();
}
