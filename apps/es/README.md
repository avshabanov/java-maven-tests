# Elastic Search as a Log Indexer

```bash
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1
docker pull docker.elastic.co/kibana/kibana:7.11.1
```

Run single elasticsearch node:

```bash
docker run --rm --name dev-es \
-p 127.0.0.1:9200:9200 -p 127.0.0.1:9300:9300 \
-e "discovery.type=single-node" \
docker.elastic.co/elasticsearch/elasticsearch:7.11.1
```

Verify elasticsearch node:

```bash
curl -X GET "http://127.0.0.1:9200/_cat/nodes?v&pretty"
# ^ should yield something like below:
# ip         heap.percent ram.percent cpu load_1m load_5m load_15m node.role  master name
# 172.17.0.2            5          99  13    1.48    0.82     0.72 cdhilmrstw *      39a098c8b031
```

Run Kibana:

```bash
docker run --rm --link dev-es:elasticsearch \
-p 127.0.0.1:5601:5601 \
docker.elastic.co/kibana/kibana:7.11.1

# Open Kibana in Browser:
#   http://127.0.0.1:5601/app/home
```

Fluent Bit:

```bash
mkdir -p /tmp/logs && \
docker run --rm -it -v /tmp/logs:/logs --link dev-es:elasticsearch centos-fluentbit

# Verify installation:
/opt/td-agent-bit/bin/td-agent-bit -i tail -p path=/logs/app.log -o stdout

# Channel logs to elasticsearch:
/opt/td-agent-bit/bin/td-agent-bit -i cpu -t cpu \
-o es://elasticsearch:9200/logs-demo/testcpu -m "*" \
-o stdout -m '*'

#curl -X PUT http://elasticsearch:9200/demo
# Verify indices:
curl http://elasticsearch:9200/_cat/indices
```

Channel file:

In `loggen` dir do:

```bash
mkdir -p /tmp/logs && test -d ./fluentbit && \
docker run --rm -it -v /tmp/logs:/logs -v $(pwd)/fluentbit:/etc/fluentbit:ro --link dev-es:elasticsearch centos-fluentbit

# Once bash starts, do:
/opt/td-agent-bit/bin/td-agent-bit -c /etc/fluentbit/fluentbit.conf
```

Generate test log output via `loggen`:

```bash
bash dummy_json_log_gen.sh  | tee /tmp/logs/app.log
```
