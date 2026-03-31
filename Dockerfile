# --- ETAPA 1: Compilar el Frontend (Vite) ---
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
# Copia los archivos de configuración de Node
COPY frontend/package*.json ./
RUN npm install
# Copia el código del front y compila
COPY frontend/ ./
RUN npm run build

# --- ETAPA 2: Compilar el Backend (Spring Boot) ---
FROM maven:3.8.5-openjdk-17 AS backend-build
WORKDIR /app
# Copiamos el pom y descargamos dependencias para ahorrar tiempo
COPY pom.xml .
RUN mvn dependency:go-offline
# Copiamos el código del back
COPY src ./src
# Copiamos el resultado de Vite a la carpeta de recursos estáticos de Spring
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
# Compilamos el JAR final
RUN mvn clean package -DskipTests

# --- ETAPA 3: Imagen de Ejecución Final ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]