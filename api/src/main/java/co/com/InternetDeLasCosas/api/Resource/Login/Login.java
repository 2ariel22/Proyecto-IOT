package co.com.InternetDeLasCosas.api.Resource.Login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Login {
    @Id
    private String components_id;
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
