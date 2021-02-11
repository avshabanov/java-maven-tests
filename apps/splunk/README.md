# Learning Splunk

## Quick Start

```bash
docker run --rm -it \
 -p 127.0.0.1:9000:8000 \
 -v $(pwd)/splunk:/opt/splunk:rw \
 -v /tmp/splunklogs:/tmp/splunklogs:rw \
 centos:8 /bin/bash
```

Note, that it is critical to match FS path for splunk logs location above.

Start/Stop within the container:

```bash
/opt/splunk/bin/splunk start

/opt/splunk/bin/splunk stop
```

## Setup

Use creds:`testuser=testtest`.

Add to `/opt/splunk/etc/splunk-launch.conf`:

```
OPTIMISTIC_ABOUT_FILE_LOCKING=1
```

## How to prepare file

```bash
cat source | grep -e "^{" > /tmp/samplelog.json
```
