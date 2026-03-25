##
## Multi-stage build:
## - Stage 1: compila usando Maven Wrapper (mvnw)
## - Stage 2: ejecuta con JRE 21 (más liviano)
##

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos todo el repo (incluye mvnw/.mvn)
COPY . .

# Asegura permisos del wrapper y compila
RUN chmod +x ./mvnw && ./mvnw -DskipTests=true clean package


FROM eclipse-temurin:21-jre
WORKDIR /app

# Jar generado por Spring Boot (artifactId/version del pom)
COPY --from=build /app/target/astrourl-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Render suele inyectar PORT. El server.port ya se toma de PORT vía application.yaml,
# pero lo dejamos reforzado para que el contenedor siempre escuche el puerto esperado.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]

