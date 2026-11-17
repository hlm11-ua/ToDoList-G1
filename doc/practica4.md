# Práctica 4: Documentación técnica

En este caso se realizaron un total de 4 historias de usuario nuevas.

## Historia 012: Creación y vista tableros (Ariana)
En esta historia, un usuario debe poder crear tableros dentro de un equipo.
En caso de pertenecer a este, puede crear tareas y asignarlas a uno mismo o a los
distintos miembros del equipo.

En cuanto a la parte visual, se realizó una página simulando un tablero de trello
kanban, donde se pueden ver las distintas tareas y su estado (pendiente, en proceso o finalizada). Además,
se puede cambiar el estado de la tarea arrastrándola a la columna correspondiente.

El cambio de estado de la tarea no se realiza en esta historia de usuario.

## Historia 013: Cambio de estado de tareas (Crystal)
En esta historia, un usuario debe poder cambiar el estado de las tareas que se encuentran en un tablero.

Para ello en la parte visual se implementó un botón para poder cambiar el estado de la tarea.

Esta implementación de funcionalidad implica que al arrastrar una tarea a otra columna,
se debe cambiar el estado de la misma en la base de datos haciendo funcional la vista
implementada en la historia 012.

## Historia 014: Arreglo de funcionalidad admin (Adan)
Arreglo en la vista de administración de usuarios, 
donde se implementó un botón para poder bloquear o desbloquear a un usuario.

El error era que aparecía el botón para también bloquear al administrador,
cuando este no debería poder ser bloqueado.

## Historia 015: Editar perfil usuario (Adan)
En esta historia, un usuario debe poder editar su perfil,
cambiando su nombre y fecha de nacimiento.

Para ello se implementó un formulario en la vista de perfil del usuario, 
donde se pueden cambiar los datos mencionados.

## Historia 016: Administrar tableros (Adan)
En esta historia, un usuario debe poder administrar los tableros dentro de un equipo.
Se debe permitir la modificación del nombre de un tablero y la eliminación del mismo.

Para ello se ha modificado la vista de equipo para poder mostrar los tableros existentes y permitir su edición o eliminación. Además,
se ha implementado botones para la modificación y eliminación del tablero.

## Despliegue de la aplicación

### Construir la imagen y subirla
```
./mvnw package -DskipTests
docker build -t helenchuu/mads-todolist-equipo01:1.3.0-snapshot .
docker login
docker push helenchuu/mads-todolist-equipo01:1.3.0-snapshot
```
### Entorno de desarrollo local

```
# Levantar solo la BD en local mapeando puerto 5432
docker run -d -p 5432:5432 -v "${PWD}:/mi-host" --name db-equipo -e POSTGRES_USER=mads -e POSTGRES_PASSWORD=mads -e POSTGRES_DB=mads postgres:13

# Ejecutar la app localmente conectada a la BD de Docker
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=postgres"

# Probar validación de producción sobre BD local (debe fallar si no hay tablas)
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=postgres-prod"
```

### Gestión de esquemas y backups
Los esquemas de datos se pueden encontrar en la carpeta `sql` del proyecto.

```
# Crear carpeta sql si no existe
mkdir sql

# Extraer esquema inicial base 1.2.0 (sin datos)
docker exec -i db-equipo pg_dump -U mads -s mads > sql/schema-1.2.0.sql

# Guardar copia de seguridad completa con datos
docker exec -i db-equipo pg_dump -U mads --clean mads > sql/backup11112026.sql

# Restaurar copia de seguridad / esquema en la BD
docker exec -i db-equipo psql -U mads mads < sql/backup11112026.sql
```

### Despliegue completo de producción
```
# 1. Crear la red (solo 1 vez)
docker network create network-equipo

# 2. Levantar BD aislada en la red
docker run -d --network network-equipo --network-alias postgres -v "${PWD}:/mi-host" --name db-equipo -e POSTGRES_USER=mads -e POSTGRES_PASSWORD=mads -e POSTGRES_DB=mads postgres:13

# 3. Cargar el backup con datos en la BD
docker exec -i db-equipo psql -U mads mads < sql/backup11112026.sql

# 4. Descargar e iniciar la App en producción (puerto 8080:8081)
docker pull helenchuu/mads-todolist-equipo01:1.3.0-snapshot
docker run -d --name app-equipo --network network-equipo -p 8080:8081 helenchuu/mads-todolist-equipo01:1.3.0-snapshot --spring.profiles.active=postgres-prod --POSTGRES_HOST=postgres
```
### URL de la imagen docker
helenchuu/mads-todolist-equipo01
