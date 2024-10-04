# Etapa 1: Criar imagem customizada com Maven 3.8.6 e OpenJDK 17
FROM eclipse-temurin:17-jdk AS build

# Instalar Maven manualmente
RUN apt-get update && \
    apt-get install -y maven=3.8.6-3

# Definir o diretório de trabalho
WORKDIR /app

# Copiar o arquivo pom.xml e os diretórios dos módulos
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

# Etapa 2: Usar uma imagem base com OpenJDK 17 para executar a aplicação
FROM eclipse-temurin:17-jdk-slim

# Definir o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copiar o arquivo .jar do build anterior para o contêiner
COPY --from=build /app/app/target/app.jar /app/demo.jar

# Expor a porta onde a aplicação será executada
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "demo.jar"]
