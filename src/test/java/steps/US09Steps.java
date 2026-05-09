package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class US09Steps {
    Response response;
    String uniqueEmail;
    String[] role;

    @Given("el usuario quiere registrarse con el servicio")
    public void servicio_disponible() {
        baseURI = "http://localhost:8080/api/v1";
    }

    @When("el usuario se registra con su usuario {string} y contraseña {string}")
    public void usuario_ingresa_datos(String user, String password) {
        if (user.equals("unique_user")) {
            uniqueEmail = "testuser_" + System.currentTimeMillis() + "@gmail.com";
        } else {
            uniqueEmail = user;
        }

        role = new String[]{"ROLE_USER"};

        String jsonBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\", \"roles\":[\"%s\"]}",
                uniqueEmail, password, String.join("\",\"", role));

        response = given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post("/authentication/sign-up");
    }

    @Then("la respuesta del registro contiene un status {int}")
    public void respuesta_contiene_status(int status) {
        response.then().statusCode(status);
    }

    @And("la respuesta contiene un mensaje de error")
    public void respuesta_contiene() {
        assertNotNull(response.getBody().jsonPath().get("message"));
    }
}