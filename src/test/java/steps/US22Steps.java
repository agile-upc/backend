package steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class US22Steps {
    Response response;
    String token;
    int userId;
    int enclosureId;

    @Given("el usuario accede a la sección de recintos")
    public void usuario_accede_seccion_recintos() {
        baseURI = "http://localhost:8080/api/v1";
        String jsonBody = "{\"username\":\"farmer@gmail.com\",\"password\":\"123456\"}";
        response = given().contentType("application/json").body(jsonBody).post("/authentication/sign-in");
        token = response.jsonPath().get("token");
        userId = response.jsonPath().getInt("id");
    }

    @When("complete el formulario con los datos del nuevo recinto")
    public void completar_formulario_nuevo_recinto() {
        String body = "{\"name\":\"Recinto A\",\"capacity\":10,\"type\":\"Pequeño\",\"farmerId\":" + userId + "}";
        response = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .post("/enclosures");
    }

    @Then("el sistema guardará el recinto y lo mostrará en la lista")
    public void sistema_guarda_recinto() {
        enclosureId = response.jsonPath().getInt("id");
        assertEquals(201, response.getStatusCode());
        System.out.println("Recinto registrado correctamente con ID: " + enclosureId);
    }

    @Given("el usuario visualiza un recinto en la lista")
    public void usuario_visualiza_recinto_lista() {
        if (token == null || token.isEmpty()) {
            usuario_accede_seccion_recintos();
        }

        // Crear un recinto si no hay
        String recintoBody = "{\"name\":\"Recinto Auto\",\"capacity\":6,\"type\":\"Mediano\",\"farmerId\":" + userId + "}";
        Response createResponse = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(recintoBody)
                .post("/enclosures");

        assertEquals(201, createResponse.getStatusCode(), "No se pudo crear recinto para editar o eliminar");

        enclosureId = createResponse.jsonPath().getInt("id");

        // Verificar que se puede obtener desde la API
        response = given().header("Authorization", "Bearer " + token)
                .get("/enclosures/" + enclosureId);

        assertEquals(200, response.getStatusCode(), "El recinto no se pudo visualizar tras crearse");
        System.out.println("Recinto disponible para editar/eliminar: " + enclosureId);
    }


    @When("seleccione la opción de editar y modifique los datos")
    public void editar_recinto() {
        String body = "{\"name\":\"Recinto B\",\"capacity\":15,\"type\":\"Grande\"}";
        response = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .put("/enclosures/" + enclosureId);
    }

    @Then("el sistema actualizará la información del recinto")
    public void sistema_actualiza_recinto() {
        assertEquals(200, response.getStatusCode());
        System.out.println("Recinto actualizado correctamente");
    }

    @When("seleccione la opción de eliminar")
    public void eliminar_recinto() {
        response = given().header("Authorization", "Bearer " + token)
                .delete("/enclosures/" + enclosureId);
    }

    @Then("el sistema pedirá confirmación y, al aceptarla, eliminará el recinto")
    public void sistema_elimina_recinto() {
        assertEquals(200, response.getStatusCode());
        System.out.println("Recinto eliminado correctamente");
    }
}