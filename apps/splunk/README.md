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

## Basic Searches

Tabular form:

```
sourcetype="access_combined_wcookie" status=200 file="success.do"  | table action JSESSIONID status | rename JSESSIONID as "User Session"
```

Number of sessions per IP:

```
sourcetype="access_combined_wcookie"| stats dc(JSESSIONID) as Logins by clientip
```

Same, with number of logins, in decreasing order:

```
sourcetype="access_combined_wcookie"| stats dc(JSESSIONID) as Logins by clientip | sort by - Logins
```

Total bytes served:

```
sourcetype="access_combined_wcookie" | stats sum(bytes) as TotalBytes
sourcetype="access_combined_wcookie" | stats sum(bytes) as TotalBytesPerFile by file | sort by - TotalBytesPerFile
```

Time to complete (avg):

```
sourcetype="db_audit" | stats avg(Duration) as TimeToComplete by Command | sort by - TimeToComplete
```
