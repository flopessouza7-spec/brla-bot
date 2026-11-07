FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY EuroBot.java .
RUN javac EuroBot.java
CMD ["java", "EuroBot"]         
