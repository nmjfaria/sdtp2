# base image - an image with openjdk  8
FROM nunopreguica/sd1920tpbase

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd1920.jar

# copy the file of properties to the docker image
COPY messages.props messages.props

# COPY server key (keystore.ks)
COPY server.ks server.ks

# COPY client truststore (client-truststore.ks)
COPY client-truststore.ks client-truststore.ks

# run Discovery when starting the docker image
CMD ["java", 	"-Djavax.net.ssl.keyStore=/home/sd/server.ks",\
				"-Djavax.net.ssl.keyStorePassword=password",\
				"-Djavax.net.ssl.trustStore=/home/sd/truststore.ks",\
				"-Djavax.net.ssl.trustStorePassoword=changeit",\
				"-cp", "/home/sd/sd1920.jar", "sd1920.trab2.EmailServerRest"]
