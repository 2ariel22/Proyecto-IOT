package co.com.InternetDeLasCosas.api.Resource.Components;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "components")
@Entity(name = "Components")
public class Components {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private boolean state_led;
    private boolean state_Motor;
    private LocalDateTime fecha;


    // Constructor que toma un DatesRegisterComponents
    public Components(DatesRegisterComponents datesRegisterComponents) {
        this.state_led = datesRegisterComponents.state_led();
        this.state_Motor = datesRegisterComponents.state_motor();
        this.fecha = LocalDateTime.now();
    }


}
