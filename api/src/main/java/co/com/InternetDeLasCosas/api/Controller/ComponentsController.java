package co.com.InternetDeLasCosas.api.Controller;

import co.com.InternetDeLasCosas.api.Resource.Components.Components;
import co.com.InternetDeLasCosas.api.Resource.Components.ComponentsRepository;
import co.com.InternetDeLasCosas.api.Resource.Components.DatesRegisterComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/components")
public class ComponentsController {

    @Autowired
    private ComponentsRepository componentsRepository;

    @Autowired
    private LoginController loginController; // Dependencia para verificar la autenticación

    // Método para verificar si el usuario está autenticado antes de procesar las solicitudes
    private boolean isUserAuthenticated() {
        return loginController.getCurrentLoggedInUser() != null;
    }

    @PostMapping("/addComponent")
    public ResponseEntity<?> addComponent(@RequestBody DatesRegisterComponents datesRegisterComponents) {
        // Verificar si el usuario está autenticado

        Components component = componentsRepository.save(new Components(datesRegisterComponents));
        return ResponseEntity.ok(component);
    }

    @GetMapping("/getAllComponents")
    public ResponseEntity<?> getAllComponents() {
        // Verificar si el usuario está autenticado

        List<Components> componentsList = componentsRepository.findAll();
        return ResponseEntity.ok(componentsList);
    }

    @GetMapping("/getState")
    public ResponseEntity<?> getLastComponent() {


        Components component = componentsRepository.findTopByOrderByFechaDesc().orElse(null);
        return ResponseEntity.ok(component);
    }
}
