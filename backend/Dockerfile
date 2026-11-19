# Build: compila el jar ejecutable con el propio Maven Wrapper del proyecto.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package

# Runtime: solo el JRE y el jar, imagen chica para arrancar rápido en el free tier.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render pisa $PORT; application.yml ya lee server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
