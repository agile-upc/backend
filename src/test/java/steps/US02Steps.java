package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class US02Steps {
    Response response;
    String token;
    int error;

    @Given("el granjero con poca experiencia quiere ver información de un asesor")
    public void el_granjero_con_poca_experiencia_quiere_ver_asesor() {
        System.out.println("El granjero está en la plataforma.");
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

    @And("se encuentra en el apartado de Asesores")
    public void se_encuentra_en_el_apartado_asesores() {
        System.out.println("El granjero se encuentra en: Asesores");

    }

    @When("seleccione al cuadro de un {int}")
    public void seleccione_el_cuadro_de_un_asesor(int id) {
        // Simulate selecting an advisor
        response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/profiles/" + id + "/user");

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().jsonPath().get(""));
    }

    @Then("el sistema le mostrará la información del asesor como nombre, experiencia, calificación y reseñas")
    public void el_sistema_le_mostrará_la_informacion_del_asesor() {
        // armar salida
        String name = response.getBody().jsonPath().get("firstName") + " " + response.getBody().jsonPath().get("lastName");
        String location = response.getBody().jsonPath().get("city") + ", " + response.getBody().jsonPath().get("country");
        String description = response.getBody().jsonPath().get("description");
        String occupation = response.getBody().jsonPath().get("occupation");
        String experience = response.getBody().jsonPath().get("experience") + " años";
        System.out.println("Información del asesor: "
                + "\nNombre: " + name
                + "\nUbicación: " + location
                + "\nDescripción: " + description
                + "\nOcupación: " + occupation
                + "\nExperiencia: " + experience);
    }

    @Given("el granjero con poca experiencia quiere ver información relevante del asesor")
    public void el_granjero_con_poca_experiencia_quiere_ver_informacion_relevante_del_asesor() {
        System.out.println("El granjero está en la plataforma.");
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

    @When("seleccione al cuadro de un {int} en la interfaz")
    public void seleccione_al_cuadro_de_un_asesor_en_la_interfaz(int id) {
        // Simulate selecting an advisor
        response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/profiles/" + id + "/user");

    }

    @And("se encuentre con un {int} al cargar la información")
    public void se_encuentre_con_un_404_al_cargar_informacion(int error) {
        this.error = error;
    }

    @Then("el sistema le mostrará un mensaje de error de carga en la interfaz")
    public void el_sistema_le_mostrará_un_mensaje_de_error_al_no_encontrar_el_asesor() {
        assertEquals(error, response.getStatusCode());
        System.out.println("Error: " + response.getStatusCode());
    }
}
