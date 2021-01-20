FROM docker
ENV LANG=C.UTF-8

#更换为aliyun源
RUN echo "http://mirrors.aliyun.com/alpine/latest-stable/community" > /etc/apk/repositories
RUN echo "http://mirrors.aliyun.com/alpine/latest-stable/main" >> /etc/apk/repositories
#update apk bash vim wget curl net-tools sshpass
RUN apk update \
	&& apk upgrade \
	&& apk --no-cache add ca-certificates \
	&& apk add bash \
	bash-doc \
	bash-completion \
	vim \
	wget \
	curl \
	net-tools \
	sshpass \
    && rm -rf /var/cache/apk/* \
    && /bin/bash

#setup glibc
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.32-r0/glibc-2.32-r0.apk
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.32-r0/glibc-bin-2.32-r0.apk
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.32-r0/glibc-i18n-2.32-r0.apk
RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    echo \
        "-----BEGIN PUBLIC KEY-----\
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApZ2u1KJKUu/fW4A25y9m\
        y70AGEa/J3Wi5ibNVGNn1gT1r0VfgeWd0pUybS4UmcHdiNzxJPgoWQhV2SSW1JYu\
        tOqKZF5QSN6X937PTUpNBjUvLtTQ1ve1fp39uf/lEXPpFpOPL88LKnDBgbh7wkCp\
        m2KzLVGChf83MS0ShL6G9EQIAUxLm99VpgRjwqTQ/KfzGtpke1wqws4au0Ab4qPY\
        KXvMLSPLUp7cfulWvhmZSegr5AdhNw5KNizPqCJT8ZrGvgHypXyiFvvAH5YRtSsc\
        Zvo9GI2e2MaZyo9/lvb+LbLEJZKEQckqRj4P26gmASrZEPStwc+yqy1ShHLA0j6m\
        1QIDAQAB\
        -----END PUBLIC KEY-----" | sed 's/   */\n/g' > "/etc/apk/keys/sgerrand.rsa.pub"
RUN apk add glibc-2.32-r0.apk
RUN apk add glibc-bin-2.32-r0.apk
RUN apk add glibc-i18n-2.32-r0.apk
RUN rm -rf *.apk

#setup date
RUN apk add tzdata
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#安装jdk
RUN wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.tar.gz"
RUN tar xzf jdk-8u141-linux-x64.tar.gz -C /usr/local/
ENV JAVA_HOME /usr/local/jdk1.8.0_141
ENV PATH ${PATH}:${JAVA_HOME}/bin
ENV CLASSPATH $JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

#设置到nfs-host-ip的免密登录
RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa >/dev/null 2>&1 && \
	#让连接新主机时，不进行公钥确认
    echo "StrictHostKeyChecking no">>/etc/ssh/ssh_config && \
	#发送公钥到目标服务器
    sshpass -p nfs-host-pwd ssh-copy-id -p 22 -i ~/.ssh/id_rsa.pub nfs-host-user@nfs-host-ip
	
COPY dubhe-admin/target/dubhe-admin-1.0-exec.jar /dubhe/
COPY dubhe-task/target/dubhe-task-1.0.jar /dubhe/
COPY dubhe-serving-gateway/target/dubhe-serving-gateway.jar /dubhe/