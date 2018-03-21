FROM tomcat
MAINTAINER evgeniyh@il.ibm.com

RUN rm -rf /usr/local/tomcat/webapps/*
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl \
    && chmod +x kubectl

#RUN export KUBECONFIG=`pwd`/kube-config-lon02-nimble-platform.yml
#RUN echo $KUBECONFIG

#ADD /home/evgeniyh/.bluemix/plugins/container-service/clusters/nimble-platform/kube-config-lon02-nimble-platform.yml ./

ADD ./target/dashboards-service.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]