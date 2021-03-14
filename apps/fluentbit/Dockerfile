FROM centos:8

COPY td-agent-bit.repo /etc/yum.repos.d/td-agent-bit.repo

RUN yum update -y
RUN yum install -y td-agent-bit
RUN yum clean all

CMD ["/bin/bash"]

