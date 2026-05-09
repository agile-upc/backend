Feature: Visualización de información de un asesor
  Como granjero con poca experiencia
  quiero ver la información de un asesor
  para tomar una decisión informada antes de separar una cita

  Scenario Outline: Ver información de un asesor
    Given el granjero con poca experiencia quiere ver información de un asesor
    And se encuentra en el apartado de Asesores
    When seleccione al cuadro de un <asesor>
    Then el sistema le mostrará la información del asesor como nombre, experiencia, calificación y reseñas

    Examples:
      | asesor |
      | 2      |

  Scenario Outline: Fallar al visualizar la información del asesor
    Given el granjero con poca experiencia quiere ver información relevante del asesor
    And se encuentra en el apartado de Asesores
    When seleccione al cuadro de un <asesor> en la interfaz
    And se encuentre con un <error> al cargar la información
    Then el sistema le mostrará un mensaje de error de carga en la interfaz

    Examples:
      | asesor | error |
      | 0      | 404   |
