# Build stage: compiles and packages the jar. Dependencies are resolved before the source is
# copied in, so an edit to application code does not force Maven to re-download the internet.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B package -DskipTests

# Runtime stage: just a JRE and the built jar, run as a non-root user.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN useradd --system --no-create-home fruitmachine
USER fruitmachine

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
