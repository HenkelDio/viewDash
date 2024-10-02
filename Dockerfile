# Usar uma imagem base com JDK 17 ou 21, dependendo da versão do Java utilizada no projeto.
FROM openjdk:21-jdk-slim

# Definir o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copiar o arquivo .jar do build local para o contêiner
COPY app/target/app.jar /app/app.jar

# Definir a variável de ambiente que informa o nome do arquivo .jar
ENV JAVA_APP_JAR=app.jar

# Expor a porta onde a aplicação será executada
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
