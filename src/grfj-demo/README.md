
# Overview

Experiment web app that uses Guice + RestEasy + Freemarker + Jetty + Protobuf + Jackson + Brikar (Error Model).

# How to demo

* Run GuiceResteasyDemoMain.
* Do ``GET`` for ``http://127.0.0.1:8080/g/index`` (opens HTML page)
* Do ``GET`` for ``http://127.0.0.1:8080/rest/hello/myapi``.
* Do ``GET`` for ``http://127.0.0.1:8080/rest/user/1``.

## Sample JSON request/response

```
curl -s -H 'Accept: application/json' http://127.0.0.1:8080/rest/user/1

{"id":1,"nickname":"Echo1","passwordHash":"pC3vOskl0HuaYPnVX4UVHJmFOrILgk2DPn7fN84iii2rg1k325zqeYAlSGuZU1bA","contacts":[{"number":"+1 123 456 78 90","type":"PHONE"},{"number":"test@localhost","type":"EMAIL"},{"number":"sample@demo","type":"EMAIL"},{"number":"urn:twitter:12345678"}],"authorities":["ROLE_USER","ROLE_READER","ROLE_UPLOADER"],"created":1469407607582,"active":true}
```

Or more sofisticated (compression+formatted output):

```
curl -v -s --compressed -H 'Accept: application/json' http://127.0.0.1:8080/rest/user/1 | python -mjson.tool

> GET /rest/user/1 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: 127.0.0.1:8080
> Accept-Encoding: deflate, gzip
> Accept: application/json
>
< HTTP/1.1 200 OK
< Date: Mon, 25 Jul 2016 00:48:03 GMT
< Content-Encoding: gzip
< Content-Type: application/json
< Content-Length: 289
* Server Jetty(9.3.10.v20160621) is not blacklisted
< Server: Jetty(9.3.10.v20160621)
<
{ [data not shown]
* Connection #0 to host 127.0.0.1 left intact
{
    "active": true,
    "authorities": [
        "ROLE_USER",
        "ROLE_READER",
        "ROLE_UPLOADER"
    ],
    "contacts": [
        {
            "number": "+1 123 456 78 90",
            "type": "PHONE"
        },
        {
            "number": "test@localhost",
            "type": "EMAIL"
        },
        {
            "number": "sample@demo",
            "type": "EMAIL"
        },
        {
            "number": "urn:twitter:12345678"
        }
    ],
    "created": 1469407683827,
    "id": "1",
    "nickname": "Echo1",
    "passwordHash": "pC3vOskl0HuaYPnVX4UVHJmFOrILgk2DPn7fN84iii2rg1k325zqeYAlSGuZU1bA"
}

```

## Sample protobuf request/response

```
curl -s -H 'Accept: application/x-protobuf' http://127.0.0.1:8080/rest/user/1 | protoc --decode_raw

1: "1"
2 {
  8: 0x316f6863
}
3: "pC3vOskl0HuaYPnVX4UVHJmFOrILgk2DPn7fN84iii2rg1k325zqeYAlSGuZU1bA"
4 {
  1: "+1 123 456 78 90"
  2: 1
}
4 {
  1: "test@localhost"
  2: 2
}
4 {
  1: "sample@demo"
  2: 2
}
4 {
  1: "urn:twitter:12345678"
}
5: "ROLE_USER"
5: "ROLE_READER"
5: "ROLE_UPLOADER"
6: 1469406886621
7: 1
```

Request gzipped content:

```
curl --compressed -v -H 'Accept: application/x-protobuf' http://127.0.0.1:8080/rest/user/1 | protoc --decode_raw

> GET /rest/user/1 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: 127.0.0.1:8080
> Accept: */*
> Accept-Encoding: deflate, gzip
>
< HTTP/1.1 200 OK
< Date: Mon, 25 Jul 2016 00:37:51 GMT
< Content-Encoding: gzip
< Content-Type: application/x-protobuf
< Content-Length: 214
* Server Jetty(9.3.10.v20160621) is not blacklisted
< Server: Jetty(9.3.10.v20160621)
<
{ [data not shown]
100   214  100   214    0     0  87740      0 --:--:-- --:--:-- --:--:--  104k
* Connection #0 to host 127.0.0.1 left intact
1: "1"
2 {
  8: 0x316f6863
}
3: "pC3vOskl0HuaYPnVX4UVHJmFOrILgk2DPn7fN84iii2rg1k325zqeYAlSGuZU1bA"
4 {
  1: "+1 123 456 78 90"
  2: 1
}
4 {
  1: "test@localhost"
  2: 2
}
4 {
  1: "sample@demo"
  2: 2
}
4 {
  1: "urn:twitter:12345678"
}
5: "ROLE_USER"
5: "ROLE_READER"
5: "ROLE_UPLOADER"
6: 1469407071191
7: 1
```
