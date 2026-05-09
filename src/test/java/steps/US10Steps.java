package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class US10Steps {
    Response response;

    @Given("el usuario quiere loguearse con el servicio")
    public void servicio_esta_disponible() {
        baseURI="http://localhost:8080/api/v1";
    }

    @When("el usuario ingrese su usuario {string} y contraseña {string}")
    public void usuario_ingresa_datos(String username, String password) {
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        response = given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post("/authentication/sign-in");
    }
    @Then("la respuesta del login contiene un status {int}")
    public void respuesta_status(int status) {
        response.then().statusCode(status);
    }
    @And("la respuesta contiene un token {string}")
    public void respuesta_contiene_token(String text) {
        assertNotNull(response.getBody().jsonPath().get("token"));
    }

    @And("la respuesta contiene un error {string}")
    public void respuesta_contiene_mensaje_error(String text) {
        assertNotNull(response.getBody().jsonPath().get("error"));
        assertNotNull(response.getBody().jsonPath().get("message"));
    }


}
