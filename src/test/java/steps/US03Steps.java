package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class US03Steps {
    Response response;
    String token;
    int advisorId;

    @Given("el granjero con poca experiencia desea visualizar los horarios disponibles de un asesor elegido")
    public void el_granjero_con_poca_experiencia_desea_visualizar_horarios_disponibles() {
        System.out.println("El granjero quiere ver los horarios de un asesor.");
        baseURI = "http://localhost:8080/api/v1";
        // Simulate user login
        String username = "example@gmail.com";
        String password = "12345678";

        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        response = given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post("/authentication/sign-in");

        token = response.getBody().jsonPath().get("token");
    }

    @And("se encuentra viendo la información del perfil de un {int}")
    public void se_encuentra_viendo_informacion_perfil_asesor(int id) {
        System.out.println("El granjero está viendo el perfil del asesor con ID: " + id);
        advisorId = id;
        response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/profiles/" + id + "/user");

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().jsonPath().get(""));
    }

    @When("haga clic en el botón Agendar Cita en la interfaz")
    public void haga_clic_en_boton_agendar_cita() {
        System.out.println("El granjero hace clic en el botón Agendar Cita.");
        // Load dates
        response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/available_dates?advisorId=" + advisorId);
    }

    @Then("el sistema le mostrará una interfaz con los horarios disponibles del asesor")
    public void el_sistema_le_mostrará_horarios_disponibles() {
        System.out.println("El sistema muestra los horarios disponibles del asesor.");
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().jsonPath().getList(""));
        System.out.println("Horarios disponibles: " + response.getBody().jsonPath().getList(""));
    }


    @And("el asesor no tenga horarios disponibles")
    public void el_asesor_no_tenga_horarios_disponibles() {
        System.out.println("El asesor no tiene horarios disponibles.");
    }

    @Then("el sistema le mostrará un mensaje de error El asesor no tiene horarios disponibles en la interfaz")
    public void el_sistema_le_mostrará_mensaje_error_horarios_no_disponibles() {
        assertTrue(response.getBody().jsonPath().getList("").isEmpty());
        System.out.println("Horarios disponibles: " + response.getBody().jsonPath().getList(""));
        System.out.println("El asesor no tiene horarios disponibles.");
    }
}
