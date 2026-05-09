Feature: Gestión de recintos
  Como granjero con poca experiencia,
  quiero registrar, editar y eliminar recintos de mis animales,
  para mantener un control organizado y actualizado sobre los espacios donde se encuentran.

  Scenario: Registrar un recinto
    Given el usuario accede a la sección de recintos
    When complete el formulario con los datos del nuevo recinto
    Then el sistema guardará el recinto y lo mostrará en la lista

  Scenario: Editar un recinto existente
    Given el usuario visualiza un recinto en la lista
    When seleccione la opción de editar y modifique los datos
    Then el sistema actualizará la información del recinto

  Scenario: Eliminar un recinto
    Given el usuario visualiza un recinto en la lista
    When seleccione la opción de eliminar
    Then el sistema pedirá confirmación y, al aceptarla, eliminará el recinto