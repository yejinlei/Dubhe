FROM harbor.dubhe.ai/dubhe/docker-in-docker-java8-ssh:v3
RUN apk add ffmpeg
ENV LANG=C.UTF-8
COPY */target/*-exec.jar /dubhe/
CMD ["bash", "-c","exec java $JVM_PARAM -Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 /dubhe/$JAR_BALL"]
