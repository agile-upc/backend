Feature: Gestión de publicaciones de asesores
  Como asesor
  quiero hacer publicaciones referentes a mis trabajos
  para tener una mayor visibilidad con los granjeros inexpertos

  Scenario: Crear una nueva publicación
    Given que el asesor está en el apartado de "Mis publicaciones"
    When hace clic en "Crear Publicación"
    And completa el formulario y presiona "Publicar"
    Then el sistema confirma la acción y la publicación se vuelve visible para los granjeros

  Scenario: Editar una publicación existente
    Given que el asesor tiene una publicación
    And está en el apartado "Mis publicaciones"
    When selecciona "Editar"
    And modifica el contenido y guarda los cambios
    Then el sistema confirma la acción y actualiza la publicación

  Scenario: Eliminar una publicación existente
    Given que el asesor tiene una publicación
    And está en el apartado "Mis publicaciones"
    When selecciona "Eliminar"
    And confirma la acción
    Then el sistema confirma la eliminación y la publicación desaparece de la lista
