Feature: Separación de horarios de disponibilidad para asesorías
  Como asesor
  quiero poder separar los horarios en los que estoy disponible
  para que los usuarios interesados puedan agendar una asesoría en un momento conveniente

  Scenario: Registrar disponibilidad para asesorías
    Given el asesor desea registrar su horario de disponibilidad para una asesoría
    And está visualizando la sección de "Horarios disponibles" en su dispositivo
    When haga clic en el botón para registrar un nuevo horario
    And complete los datos del nuevo horario
    Then el sistema actualizará y guardará los horarios y horas seleccionadas como disponibles

  Scenario: Eliminar horario de disponibilidad para asesorías
    Given el asesor desea eliminar un horario de disponibilidad para asesorías
    And está visualizando la página de "Horario disponible" en su dispositivo
    When haga clic en el botón "Eliminar" relacionado al horario que desea eliminar
    And confirme la eliminación del horario
    Then el sistema eliminará el horario de disponibilidad seleccionado