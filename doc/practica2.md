## Funcionalidades desarrolladas

En esta práctica he desarrollado las funcionalidades relacionadas con la gestión de usuarios y la navegación de la aplicación. El resto de la aplicación, incluida la estructura inicial y la gestión básica de tareas, ya estaba proporcionado, por lo que mi trabajo se ha centrado exclusivamente en las tareas que aparecen en el tablero.

Una de las primeras funcionalidades implementadas ha sido la página “Acerca de”. Esta página permite mostrar información general sobre la aplicación y sirve como punto de consulta para el usuario. Para acceder a ella se ha añadido el enlace correspondiente dentro de la navegación de la aplicación.

También he desarrollado la barra de menú. El menú facilita el desplazamiento entre las diferentes páginas disponibles y permite acceder de una forma más ordenada a las funciones principales. Además, se ha integrado con el resto de las vistas para que la navegación mantenga una estructura común en toda la aplicación.

Otra funcionalidad importante es la página de listado de usuarios. En ella se muestran los usuarios registrados en el sistema, junto con la información necesaria para identificarlos. Esta vista permite consultar de forma rápida los usuarios existentes y acceder a la información particular de cada uno.

Relacionado con este listado, he creado la página de descripción de usuarios. Al seleccionar un usuario se puede acceder a una vista con sus datos y con la información asociada a su cuenta. Para realizarlo se utilizan rutas específicas y parámetros que identifican al usuario seleccionado. De esta manera, la aplicación puede cargar los datos correctos y mostrarlos en la plantilla correspondiente.

También he implementado la funcionalidad del usuario administrador. Este usuario dispone de permisos adicionales para consultar y gestionar la información de otros usuarios. La aplicación diferencia el acceso normal del acceso administrativo y permite que determinadas operaciones estén disponibles únicamente para quien tenga los permisos necesarios.

Para proteger el listado y la descripción de usuarios he añadido comprobaciones de seguridad. Antes de permitir el acceso a determinadas páginas, se verifica que el usuario haya iniciado sesión y que tenga autorización para realizar la operación solicitada. Así se evita que una persona pueda acceder directamente a una dirección web protegida sin cumplir las condiciones necesarias.

Finalmente, he implementado el bloqueo de usuarios. Esta función permite impedir que un usuario bloqueado pueda utilizar normalmente la aplicación. De esta forma se puede controlar el acceso de las cuentas y aplicar una restricción cuando sea necesario. La comprobación del estado del usuario se realiza antes de permitir el acceso a las funcionalidades protegidas.

El código de estas funcionalidades lo he escrito yo, siguiendo la estructura del proyecto y utilizando sus controladores, servicios, modelos y plantillas. Durante el desarrollo, cuando aparecía algún error de compilación, utilizaba la inteligencia artificial como ayuda para localizar el problema y corregirlo. La implementación y las decisiones sobre las funcionalidades fueron realizadas por mí; la IA se utilizó únicamente como apoyo durante la corrección de esos errores.
