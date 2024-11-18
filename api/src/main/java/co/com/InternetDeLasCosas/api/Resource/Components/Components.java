package co.com.InternetDeLasCosas.api.Resource.Components;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
@Document
public class Components {
    @Id
    private String components_id;
    private boolean state_led;
    private boolean state_Motor;
    private LocalDateTime fecha;

    // Constructor sin parámetros (requerido por Spring)
    public Components() {}

    // Constructor que toma un DatesRegisterComponents
    public Components(DatesRegisterComponents datesRegisterComponents) {
        this.state_led = datesRegisterComponents.state_led();
        this.state_Motor = datesRegisterComponents.state_motor();
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public String getComponents_id() {
        return components_id;
    }

    public void setComponents_id(String components_id) {
        this.components_id = components_id;
    }

    public boolean isState_led() {
        return state_led;
    }

    public void setState_led(boolean state_led) {
        this.state_led = state_led;
    }

    public boolean isState_Motor() {
        return state_Motor;
    }

    public void setState_Motor(boolean state_Motor) {
        this.state_Motor = state_Motor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
