FROM openjdk:12
WORKDIR /app/
COPY <源代码> ./
RUN javac main.java
