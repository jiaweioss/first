FROM openjdk:1.8
WORKDIR /app/
COPY <源代码> ./
RUN javac main.java
