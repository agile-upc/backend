Feature: Visualización de horarios de asesores
  Como granjero con poca experiencia
  quiero ver los horarios disponibles de los asesores
  para seleccionar un horario que se ajuste a mi agenda

  Scenario Outline: Visualizar horarios disponibles
    Given el granjero con poca experiencia desea visualizar los horarios disponibles de un asesor elegido
    And se encuentra viendo la información del perfil de un <asesor>
    When haga clic en el botón Agendar Cita en la interfaz
    Then el sistema le mostrará una interfaz con los horarios disponibles del asesor

    Examples:
      | asesor |
      | 1      |

  Scenario Outline: Fallar al intentar visualizar horarios
    Given el granjero con poca experiencia desea visualizar los horarios disponibles de un asesor elegido
    And se encuentra viendo la información del perfil de un <asesor>
    When haga clic en el botón Agendar Cita en la interfaz
    And el asesor no tenga horarios disponibles
    Then el sistema le mostrará un mensaje de error El asesor no tiene horarios disponibles en la interfaz

    Examples:
      | asesor |
      | 2      |