FROM gcr.io/distroless/java17-debian11@sha256:5cc4322dea54a6732cd2d3feebb18138f62af280a167afcbe94d3be7a607f1e5
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]