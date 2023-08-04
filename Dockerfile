FROM gcr.io/distroless/java17
WORKDIR /app
COPY build/libs/*-all.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]