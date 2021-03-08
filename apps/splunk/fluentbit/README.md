
# Fluent Bit and Splunk

```bash
docker run --name splunk1 -it \
 -p 127.0.0.1:9000:8000 \
 -v $(pwd)/splunk:/opt/splunk:rw \
 -v /tmp/splunklogs:/tmp/splunklogs:rw \
 centos:8 /bin/bash

#After stopping resurrect as follows:

docker start -ia splunk1
```

```bash
./splunk enable listen 9997 -auth testuser:testtest
```

Forward:

```bash
/opt/td-agent-bit/bin/td-agent-bit -i cpu -o splunk -p port=9997 -p tls=off -p splunk_token=1 -m "*"
```
