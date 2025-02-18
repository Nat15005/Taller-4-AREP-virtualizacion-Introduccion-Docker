FROM openjdk:21
WORKDIR /usrapp/bin
ENV PORT 6000
COPY target/Taller1-1.0-SNAPSHOT.jar /usrapp/bin/app.jar
# Copia los archivos estáticos (HTML, CSS, JS, imágenes, etc.)
COPY src/main/resources/static /usrapp/bin/static
CMD ["java", "-cp", "app.jar", "edu.escuelaing.arep.Application"]

