package steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class US07Steps {
    Response response;
    String token;
    int userId;
    int advisorId;
    int postId;

    @Given("que el asesor está en el apartado de {string}")
    public void asesor_en_apartado(String apartado) {
        baseURI = "http://localhost:8080/api/v1";
        String jsonBody = "{\"username\":\"example2@gmail.com\",\"password\":\"12345678\"}";
        response = given().contentType("application/json").body(jsonBody).post("/authentication/sign-in");
        token = response.jsonPath().get("token");
        userId = response.jsonPath().getInt("id");

        response = given().header("Authorization", "Bearer " + token)
                .get("/advisors/" + userId + "/user");
        advisorId = response.jsonPath().getInt("id");

        System.out.println("Apartado: " + apartado);
    }

    @When("hace clic en {string}")
    public void clic_en_crear_publicacion(String boton) {
        System.out.println("Clic en: " + boton);
    }

    @And("completa el formulario y presiona {string}")
    public void completa_formulario_y_publica(String accion) {
        System.out.println("Completa el formulario y presiona: " + accion);
        String body = "{\"advisorId\":" + advisorId + ",\"title\":\"Nueva publicación\",\"description\":\"Asesoría en riego por goteo\",\"image\":\"https://example.com/image.jpg\"}";
        response = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .post("/posts");
        postId = response.jsonPath().getInt("id");
    }

    @Then("el sistema confirma la acción y la publicación se vuelve visible para los granjeros")
    public void sistema_confirma_publicacion() {
        assertEquals(201, response.getStatusCode());
        System.out.println("Publicación creada: " + postId);
    }

    @Given("que el asesor tiene una publicación")
    public void asesor_tiene_publicacion() {
        baseURI = "http://localhost:8080/api/v1";
        String jsonBody = "{\"username\":\"example2@gmail.com\",\"password\":\"12345678\"}";
        response = given().contentType("application/json").body(jsonBody).post("/authentication/sign-in");
        token = response.jsonPath().get("token");
        userId = response.jsonPath().getInt("id");

        response = given().header("Authorization", "Bearer " + token)
                .get("/advisors/" + userId + "/user");
        advisorId = response.jsonPath().getInt("id");

        // Asumiendo que el asesor tiene al menos una publicación
        response = given().header("Authorization", "Bearer " + token)
                .get("/posts?advisorId=" + advisorId);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().jsonPath().getList("id"));

        if (response.jsonPath().getList("id").isEmpty()) {
            fail("No hay publicaciones disponibles para el asesor");
        }
        else {
            postId = response.jsonPath().getInt("id[0]");
            System.out.println("Publicación encontrada: " + postId);
        }
    }
    @And("está en el apartado {string}")
    public void esta_en_el_apartado(String apartado) {
        System.out.println("Asesor esta en el apartado: " + apartado);
    }

    @When("selecciona {string}")
    public void selecciona_accion(String accion) {
        System.out.println("Selecciona: " + accion);
    }

    @And("modifica el contenido y guarda los cambios")
    public void modifica_y_guarda() {
        String updated = "{\"title\":\"Publicación actualizada\",\"description\":\"Actualizado\",\"image\":\"https://example.com/updated_image.jpg\"}";
        response = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(updated)
                .put("/posts/" + postId);
    }

    @Then("el sistema confirma la acción y actualiza la publicación")
    public void sistema_actualiza_publicacion() {
        assertEquals(200, response.getStatusCode());
        System.out.println("Publicación actualizada correctamente");
    }

    @And("confirma la acción")
    public void confirma_eliminar() {
        response = given().header("Authorization", "Bearer " + token)
                .delete("/posts/" + postId);
    }

    @Then("el sistema confirma la eliminación y la publicación desaparece de la lista")
    public void sistema_elimina_publicacion() {
        assertEquals(200, response.getStatusCode());
        System.out.println("Publicación eliminada");
    }
}