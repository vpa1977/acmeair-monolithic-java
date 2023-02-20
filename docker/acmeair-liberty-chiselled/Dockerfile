ARG BASE_IMAGE=ubuntu/chiselled-jre:8_edge
FROM $BASE_IMAGE
COPY --chown=$UID:$GID --from=acmeair \
    /opt /opt

# liberty launcher support
COPY --from=acmeair /bin/grep /usr/bin/
COPY --from=acmeair /usr/bin/tr /usr/bin/
COPY --from=acmeair /bin/cat /usr/bin/
COPY --from=acmeair /bin/uname /usr/bin/
COPY --from=acmeair /usr/bin/dirname /usr/bin/
COPY --from=acmeair /bin/readlink /usr/bin/
COPY --from=acmeair /bin/sed /usr/bin/
COPY --from=acmeair /lib/x86_64-linux-gnu/libacl.so.1 /lib/x86_64-linux-gnu/
COPY --from=acmeair /usr/bin/basename /usr/bin/
COPY --from=acmeair /bin/chmod /usr/bin/
COPY --from=acmeair /bin/sh /bin/sh
COPY --from=acmeair /bin/mkdir /usr/bin

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre

WORKDIR /

ENTRYPOINT ["/opt/ibm/wlp/bin/server", "run", "defaultServer"]


