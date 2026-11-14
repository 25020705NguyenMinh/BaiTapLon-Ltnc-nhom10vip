# Đóng gói module server để chương trình backendserver chạy trên cloud đỡ phải chay trên máy cá nhân nhé ae:
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY shared ./shared
COPY server ./server

RUN mvn clean package -pl server -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
