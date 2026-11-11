# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Primero solo lo necesario para descargar dependencias (se cachea mientras no cambie el pom)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/*.jar /build/app.jar

# ---------- Etapa 2: imagen final (solo JRE) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --uid 1001 --no-create-home app
COPY --from=build /build/app.jar app.jar
USER app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Bogota"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
