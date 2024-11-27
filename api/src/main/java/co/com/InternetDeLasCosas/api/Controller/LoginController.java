package co.com.InternetDeLasCosas.api.Controller;

import co.com.InternetDeLasCosas.api.Resource.Login.DatesRegisterUser;
import co.com.InternetDeLasCosas.api.Resource.Login.DatesVerifyLogin;
import co.com.InternetDeLasCosas.api.Resource.Login.Login;
import co.com.InternetDeLasCosas.api.Resource.Login.LoginRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginRepository loginRepository;

    private String currentLoggedInUser = null;

    // Crear un nuevo usuario
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody DatesRegisterUser datesRegisterUser) {
        Login login = new Login(datesRegisterUser);
        loginRepository.save(login);
        return ResponseEntity.ok("success");
    }

    // Autenticar usuario
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody DatesVerifyLogin datesVerifyLogin) {
        Optional<Login> user = loginRepository.findByUserAndPassword(
                datesVerifyLogin.user(), datesVerifyLogin.password());

        if (user.isPresent()) {
            currentLoggedInUser = user.get().getUser();
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // Obtener el nombre del usuario actualmente logueado
    @GetMapping("/getLoggedUser")
    public ResponseEntity<String> getLoggedUser() {
        if (currentLoggedInUser != null) {
            return ResponseEntity.ok(currentLoggedInUser);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user logged in");
        }
    }

    // Cerrar sesión del usuario actual
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        if (currentLoggedInUser != null) {
            currentLoggedInUser = null;
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user logged in");
        }
    }

    // Verificar si un usuario está autenticado
    @GetMapping("/isAuthenticated")
    public ResponseEntity isAuthenticated() {
        if(currentLoggedInUser != null){
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.badRequest().build();
        }

    }


    public String getCurrentLoggedInUser() {
        return currentLoggedInUser;
    }
}
