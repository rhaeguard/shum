FROM eclipse-temurin:17-alpine

ENV MAVEN_VERSION 3.8.6

RUN apk update \
    && apk add ca-certificates wget \
    && update-ca-certificates

RUN apk update && apk add bash 

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  mv apache-maven-$MAVEN_VERSION /usr/lib/maven

ENV PATH=${PATH}:/usr/lib/maven/bin

WORKDIR /lang

ADD shum /lang/shumlang
ADD examples /lang/examples

WORKDIR /lang/shumlang

RUN mvn clean package
RUN mv ./target/shum.jar /lang/

WORKDIR /lang
COPY ./docker/shum /lang/
RUN chmod +x shum

CMD ["bash"]