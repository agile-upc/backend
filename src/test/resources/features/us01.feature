Feature: Visualización del catálogo de asesores
  Como granjero con poca experiencia
  quiero explorar el catálogo de asesores
  para conocer quiénes me pueden apoyar con asesorías

  Scenario: Explorar catálogo de asesores
    Given el granjero con poca experiencia quiere explorar el catálogo de asesores
    And se encuentra en la plataforma
    When seleccione el botón relacionado con el "Asesores"
    Then el sistema le mostrará una lista de todos los asesores disponibles en la plataforma

  Scenario: Filtrar búsqueda de asesores
    Given el granjero con poca experiencia quiere personalizar su búsqueda
    And se encuentra en el apartado de "Asesores"
    When seleccione el botón de filtros
    Then el sistema le permitirá filtrar el catálogo de asesores por nombre o reputación

