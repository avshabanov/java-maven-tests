# FDB Setup

## Mac OS X

Use `DYLD_FALLBACK_LIBRARY_PATH` to point to a directory, containing `libfdb_c`.

## Linux

```bash
# Detached mode:
docker run --name fdb-svc-local --rm -it \
 -v $(pwd)/fdb-local/fdb.cluster:/var/fdb/fdb.cluster \
 -p 127.0.0.1:4580:4580 \
 foundationdb/foundationdb /bin/bash

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

# Start stand-alone process
fdbserver -p 127.0.0.1:4580 -C /var/fdb/fdb.cluster -d /var/fdb/data -L /var/fdb/logs \
 --knob_disable_posix_kernel_aio 1 \
 --knob_min_available_space_ratio 0.01

# Then in the bash session do:
fdbcli --exec "configure new single memory ; status"

# OR:
fdbcli --exec "configure new single ssd ; status"
```

Foundation DB client `6.2.x` must match server version `6.2`.

In IntelliJ use library path: `LD_LIBRARY_PATH=/pathtofdb/lib/`.

Starting a server:

```bash
fdbserver -C /etc/foundationdb/fdb.cluster -p 127.0.0.1:4580
```

## Use of Custom Binary

1. Go to https://www.foundationdb.org/download/
2. Download custom binaries
3. Download and use C bindings

```bash
# Set FDBVAR, e.g. as follows:
# FDBVAR=~/opt/var/fdb
mkdir -p ${FDBVAR}/{data,logs}
./fdbserver -p 127.0.0.1:4580 -C ${FDBVAR}/fdb.cluster -d ${FDBVAR}/data -L ${FDBVAR}/logs \
 --knob_disable_posix_kernel_aio 1
```