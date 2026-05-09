Feature: Registro de un usuario nuevo
  Como usuario
  quiero registrarme
  para acceder a las funciones de usuario

  Scenario Outline: Registro exitoso
    Given el usuario quiere registrarse con el servicio
    When el usuario se registra con su usuario "<user>" y contraseña "<password>"
    Then la respuesta del registro contiene un status 201

    Examples:
    | user        | password |
    | unique_user | 12345678 |

  Scenario Outline: Registro fallido
    Given el usuario quiere registrarse con el servicio
    When el usuario se registra con su usuario "<invalid_user>" y contraseña "<password>"
    Then la respuesta del registro contiene un status 400
    And la respuesta contiene un mensaje de error

    Examples:
    | invalid_user      | password |
    | example2          | 12345678 |
    | example@gmail.com | 1234567  |