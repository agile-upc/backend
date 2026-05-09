package steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class US23Steps {
    private Response response;
    private String token;
    private int userId;
    private int enclosureId;
    private int animalId;

    @Given("el usuario accede a la sección de recintos para gestión de animales")
    public void usuario_accede_seccion_recintos() {
        baseURI = "http://localhost:8080/api/v1";

        // Login para obtener token y userId
        String loginJson = "{\"username\":\"farmer@gmail.com\",\"password\":\"123456\"}";
        response = given()
                .contentType("application/json")
                .body(loginJson)
                .post("/authentication/sign-in");

        assertEquals(200, response.getStatusCode(), "Error al iniciar sesión");

        token = response.jsonPath().getString("token");
        userId = response.jsonPath().getInt("id");

        assertNotNull(token, "Token no debe ser nulo");
        assertTrue(userId > 0, "userId inválido");

        // Crear un recinto para asociar animales (si no existe)
        String enclosureJson = String.format(
                "{\"name\":\"Recinto Auto\",\"capacity\":10,\"type\":\"Pequeño\",\"farmerId\":%d}",
                userId
        );

        Response enclosureResponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(enclosureJson)
                .post("/enclosures");

        assertEquals(201, enclosureResponse.getStatusCode(), "No se pudo crear recinto para asociar animales");

        enclosureId = enclosureResponse.jsonPath().getInt("id");
        assertTrue(enclosureId > 0, "EnclosureId inválido");
    }

    @When("complete el formulario con los  datos del animal y seleccione un recinto")
    public void completar_formulario_animal() {
        String animalJson = String.format(
                "{\"name\":\"Animal A\",\"age\":2,\"species\":\"Oveja\",\"breed\":\"Merino\",\"gender\":true,\"weight\":45.0,\"health\":\"HEALTHY\",\"enclosureId\":%d}",
                enclosureId
        );

        response = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(animalJson)
                .post("/animals");
    }

    @Then("el sistema guardará el animal y lo asociará al recinto elegido")
    public void sistema_guarda_animal() {
        if (token == null || token.isEmpty() || userId == 0 || enclosureId == 0) {
            usuario_accede_seccion_recintos();
        }

        assertEquals(200, response.getStatusCode(), "Código de respuesta incorrecto");

        animalId = response.jsonPath().getInt("id");
        assertTrue(animalId > 0, "ID del animal no válido");

        System.out.println("Animal registrado correctamente con ID: " + animalId);
    }

    @Given("el usuario visualiza un animal en la lista")
    public void usuario_visualiza_animal_lista() {
        if (token == null || token.isEmpty() || userId == 0 || enclosureId == 0) {
            usuario_accede_seccion_recintos();
        }

        // Crear un animal para editar o eliminar
        String animalJson = String.format(
                "{\"name\":\"Animal Auto\",\"age\":5,\"species\":\"Caballo\",\"breed\":\"Andaluz\",\"gender\":true,\"weight\":400.5,\"health\":\"HEALTHY\",\"enclosureId\":%d}",
                enclosureId
        );

        Response createResponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(animalJson)
                .post("/animals");

        assertEquals(201, createResponse.getStatusCode(), "No se pudo crear animal para editar o eliminar");

        animalId = createResponse.jsonPath().getInt("id");
        assertTrue(animalId > 0, "ID del animal no válido");

        response = given()
                .header("Authorization", "Bearer " + token)
                .get("/animals/" + animalId);

        assertEquals(200, response.getStatusCode(), "El animal no se pudo visualizar tras crearse");
        System.out.println("Animal disponible para editar/eliminar: " + animalId);
    }

    @When("seleccione la opción de editar y realice los cambios")
    public void editar_animal() {
        String animalUpdateJson = String.format(
                "{\"name\":\"Animal B\",\"age\":3,\"species\":\"Vaca\",\"breed\":\"Holstein\",\"gender\":false,\"weight\":350.0,\"health\":\"SICK\",\"enclosureId\":%d}",
                enclosureId
        );

        response = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(animalUpdateJson)
                .put("/animals/" + animalId);
    }

    @Then("el sistema actualizará la información del animal")
    public void sistema_actualiza_animal() {
        assertEquals(200, response.getStatusCode(), "No se pudo actualizar la información del animal");
        System.out.println("Animal actualizado correctamente");
    }

    @When("seleccione la opción de eliminar el animal")
    public void eliminar_animal() {
        response = given()
                .header("Authorization", "Bearer " + token)
                .delete("/animals/" + animalId);
    }

    @Then("el sistema pedirá confirmación y, al aceptarla, eliminará al animal del registro")
    public void sistema_elimina_animal() {
        assertEquals(200, response.getStatusCode(), "No se pudo eliminar el animal");
        System.out.println("Animal eliminado correctamente");
    }
}
