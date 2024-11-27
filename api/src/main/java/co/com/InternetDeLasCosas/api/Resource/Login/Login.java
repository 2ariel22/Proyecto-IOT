package co.com.InternetDeLasCosas.api.Resource.Login;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "login")
@Entity(name = "Login")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String user;

    private String password;



    // Constructor que toma un DatesRegisterComponents
    public Login(DatesRegisterUser datesRegisterUser) {
        this.name = datesRegisterUser.name();
        this.user = datesRegisterUser.user();
        this.password = datesRegisterUser.password();

    }
    public String getUser(){
        return this.user;
    }


}
