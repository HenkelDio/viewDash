# Usar uma imagem do Maven com OpenJDK 21
FROM maven:3.8.6-openjdk-21-slim AS builder

# Definir o diretório de trabalho
WORKDIR /app

# Copiar o pom.xml e os diretórios dos módulos
COPY pom.xml .
COPY app/pom.xml ./app/
COPY controller/pom.xml ./controller/
COPY service/pom.xml ./service/
COPY document/pom.xml ./document/
COPY repository/pom.xml ./repository/
COPY security/pom.xml ./security/
COPY app/src ./app/src
COPY controller/src ./controller/src
COPY service/src ./service/src
COPY document/src ./document/src
COPY repository/src ./repository/src
COPY security/src ./security/src

# Fazer o build da aplicação
RUN mvn clean package -DskipTests

# Usar uma imagem base com JDK 21 para executar a aplicação
FROM openjdk:21-jdk-slim

# Definir o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copiar o arquivo .jar do build anterior para o contêiner
COPY --from=builder /app/app/target/app.jar /app/app.jar

# Expor a porta onde a aplicação será executada
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
