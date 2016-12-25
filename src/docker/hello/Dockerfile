FROM java:8
ADD hello-app-${project.version}-jar-with-dependencies.jar hello-app.jar
EXPOSE 50051 50505
ENTRYPOINT ["java", "-jar", "hello-app.jar", "-Dcamino=true"]
