# This Dockerfile uses Docker Multi-Stage Builds
# See https://docs.docker.com/engine/userguide/eng-image/multistage-build/
# Requires Docker v17.05

# Use OpenJDK JDK image for intermiediate build
FROM openjdk:11-jdk-slim AS build

# Install packages required for build
RUN apt-get -y update
RUN apt-get install -y --no-install-recommends build-essential
RUN apt-get install -y --no-install-recommends git
RUN mkdir -p /usr/share/man/man1
RUN apt-get install -y --no-install-recommends maven

# Run the build
RUN git submodule update --init
RUN git fetch
RUN git pull
RUN mvn clean package

# Use OpenJDK JRE image for runtime
FROM openjdk:11-jre-slim AS run
LABEL maintainer="server4je <https://github.com/PhantomPowered/server4je>"

# Copy launcher from build stage
COPY --from=build /src/launcher/target/launcher.jar /app/launcher.jar

# Ports
EXPOSE 25565

# Run application
ENTRYPOINT ["java"]
CMD [ "-jar", "/app/launcher.jar" ]