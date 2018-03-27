FROM tomcat
MAINTAINER evgeniyh@il.ibm.com

RUN rm -rf /usr/local/tomcat/webapps/*
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl \
    && chmod +x kubectl \
    && cp kubectl /bin/kubectl

ADD ./target/dashboards-service.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]