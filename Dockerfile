FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY BrlaBot.java .
RUN javac BrlaBot.java
CMD ["java", "BrlaBot"]         
