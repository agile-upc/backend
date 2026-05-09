package steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Date;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class US06Steps {
    Response response;
    String token;
    int userId;
    int advisorId;
    int availableDateId;

    @Given("el asesor desea registrar su horario de disponibilidad para una asesoría")
    public void asesor_desea_registrar_disponibilidad() {
        baseURI = "http://localhost:8080/api/v1";
        String jsonBody = "{\"username\":\"example2@gmail.com\",\"password\":\"12345678\"}";
        response = given().contentType("application/json").body(jsonBody).post("/authentication/sign-in");
        token = response.jsonPath().get("token");
        userId = response.jsonPath().getInt("id");

        response = given().header("Authorization", "Bearer " + token)
                .get("/advisors/" + userId + "/user");
        advisorId = response.jsonPath().getInt("id");
    }

    @And("está visualizando la sección de {string} en su dispositivo")
    public void viendo_seccion(String seccion) {
        System.out.println("Visualizando sección: " + seccion);
    }

    @When("haga clic en el botón para registrar un nuevo horario")
    public void clic_en_registrar_horario() {
        System.out.println("Clic en botón: Registrar nuevo horario");
    }

    @And("complete los datos del nuevo horario")
    public void completar_datos_nuevo_horario() {
        Date scheduledDate = new Date(System.currentTimeMillis() + 86400000);
        String formattedDate = String.format("%tF", scheduledDate); // Formato YYYY-MM-DD
        String body = String.format("{\"advisorId\":\"%s\",\"scheduledDate\":\"%s\",\"startTime\":\"08:00\",\"endTime\":\"09:00\"}", advisorId, formattedDate);
        response = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .post("/available_dates");

    }

    @Then("el sistema actualizará y guardará los horarios y horas seleccionadas como disponibles")
    public void sistema_guarda_disponibilidad() {
        availableDateId = response.jsonPath().getInt("id");
        assertEquals(201, response.getStatusCode());
        System.out.println("Horario " + availableDateId + " registrado correctamente");
    }

    @Given("el asesor desea eliminar un horario de disponibilidad para asesorías")
    public void asesor_desea_eliminar_horario() {
        baseURI = "http://localhost:8080/api/v1";
        String jsonBody = "{\"username\":\"example2@gmail.com\",\"password\":\"12345678\"}";
        response = given().contentType("application/json").body(jsonBody).post("/authentication/sign-in");
        token = response.jsonPath().get("token");
        userId = response.jsonPath().getInt("id");
        response = given().header("Authorization", "Bearer " + token)
                .get("/advisors/" + userId + "/user");
        advisorId = response.jsonPath().getInt("id");

        // Asegurarse de que hay un horario disponible para eliminar
        response = given().header("Authorization", "Bearer " + token)
                .get("/available_dates?advisorId=" + advisorId);

        assertEquals(200, response.getStatusCode());
        if (response.jsonPath().getList("id").isEmpty()) {
            fail("No hay horarios disponibles para eliminar");
        } else {
            availableDateId = response.jsonPath().getInt("id[0]"); // Tomar el primer horario disponible
            System.out.println("Horario disponible para eliminar: " + availableDateId);
        }
    }

    @And("está visualizando la página de {string} en su dispositivo")
    public void esta_visualizando_pagina_en_Dispositivo(String pagina) {
        System.out.println("Visualizando página: " + pagina);
    }

    @When("haga clic en el botón {string} relacionado al horario que desea eliminar")
    public void clic_en_eliminar_horario(String boton) {
        System.out.println("Clic en botón: " + boton);
    }

    @And("confirme la eliminación del horario")
    public void confirmar_eliminacion_horario() {
        response = given().header("Authorization", "Bearer " + token)
                .delete("/available_dates/" + availableDateId);
    }

    @Then("el sistema eliminará el horario de disponibilidad seleccionado")
    public void sistema_elimina_horario() {
        assertEquals(200, response.getStatusCode());
        System.out.println("Horario eliminado correctamente");
    }
}
