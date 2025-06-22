# Usamos una imagen base de Java
FROM eclipse-temurin:17-jdk-alpine

# Crear directorio para la app
WORKDIR /app

# Copiar el archivo JAR compilado
COPY target/usuarioback-0.0.1-SNAPSHOT.jar app.jar

# Copiar el wallet (TNS_ADMIN apunta aquí)
COPY src/main/resources/Wallet_GN7WCCPBMJLR2TGC /opt/oracle/wallet

# Exponer el puerto que usa Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
