Feature: Gestión de animales
  Como granjero con poca experiencia,
  quiero registrar, editar y eliminar animales, asignándoles a recintos específicos,
  para llevar un registro claro y ordenado de cada uno de ellos.

  Scenario: Registrar un animal en un recinto
    Given el usuario accede a la sección de recintos
    When complete el formulario con los  datos del animal y seleccione un recinto
    Then el sistema guardará el animal y lo asociará al recinto elegido

  Scenario: Editar información de un animal
    Given el usuario visualiza un animal en la lista
    When seleccione la opción de editar y realice los cambios
    Then el sistema actualizará la información del animal

  Scenario: Eliminar un animal
    Given el usuario visualiza un animal en la lista
    When seleccione la opción de eliminar el animal
    Then el sistema pedirá confirmación y, al aceptarla, eliminará al animal del registro