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

Lookups (extending search with extra fields, permitting by-field lookups):

```
sourcetype="access_combined_wcookie" file=success.do status=200 | lookup products_lookup productId OUTPUT product_name as ProductName | stats count by ProductName
```

^ note, that ProductName is emerging as a result of this search.

Lookups - use ProductName and Price extra fields from a separate file, output as total revenue based on log events:

```
sourcetype="access_combined_wcookie" file=cart.do | stats sum(Price) as Revenue by ProductName | sort - Revenue
```

