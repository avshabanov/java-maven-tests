# FDB Setup

## Mac OS X

Use `DYLD_FALLBACK_LIBRARY_PATH` to point to a directory, containing `libfdb_c`.

## Linux

```bash
# Detached mode:
docker run --name fdb-svc-local --rm -it foundationdb/foundationdb /bin/bash

# In a separate terminal:
docker cp fdb-svc-local:/usr/lib/libfdb_c.so ~/opt/lib/

# Initialized mode:
docker run --name fdb-svc-local --rm -it --init \
 -v $(pwd)/fdb-local/fdb.cluster:/var/fdb/fdb.cluster \
 -v $(pwd)/fdb-local/foundationdb.conf:/etc/foundationdb/foundationdb.conf \
 -v /tmp/fdb/data:/var/fdb/data \
 -v /tmp/fdb/logs:/var/fdb/logs \
 -p 127.0.0.1:4580:4580 \
 foundationdb/foundationdb \
 fdbmonitor
 
# Connect as a client
docker exec -it fdb-svc-local /bin/bash

# Then in the bash session do:
fdbcli --exec "configure new single memory ; status"
```

Foundation DB client `6.2.x` must match server version `6.2`.

In IntelliJ use library path: `LD_LIBRARY_PATH=/pathtofdb/lib/`.

Starting a server:

```bash
fdbserver -C /etc/foundationdb/fdb.cluster -p 127.0.0.1:4580
```