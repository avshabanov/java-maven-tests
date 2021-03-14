
# Fluent Bit and Splunk

## Start Splunk

```bash
docker run --name splunk1 -it \
 -p 127.0.0.1:9000:8000 \
 -v $(pwd)/splunk:/opt/splunk:rw \
 -v /tmp/splunklogs:/tmp/splunklogs:rw \
 centos:8 /bin/bash
```

After stopping resurrect as follows:

```bash
docker start -ia splunk1
```

## Setup HTTP Forwarder in Splunk

* Go to settings -> indexes
* Create a new index (ex:cpubit) for events with default.
* Go to settings -> Data inputs -> HTTP Event Collector
* Create two New Token -> Specify name of token (ex:fluentbit-cpu) -> Next -> Select Allowed Index -> Review -> Submit
* Get your token values and replace it in the following definition as `Splunk_Token`.
* Go to settings -> Data inputs -> HTTP Event Collector
* Go to Global setting,make sure it is enabled (Hit `Enable` button and click save if not). Check the port number. Default is 8088.
* Expose the port. Create a service for HTTP Event Collector based on HTTP Port Number in Global Setting


## Forward with Fluent Bit

```bash
/opt/td-agent-bit/bin/td-agent-bit -i cpu -o splunk -p port=9997 -p tls=off -p splunk_token=1 -m "*"
```
