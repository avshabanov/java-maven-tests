# Mutual TLS Demo with Jetty and HttpsURLConnection

## Overview

This sample demonstrates how to build a working distributed application that uses mutual TLS (or mTLS) between its components.

## Prerequisites

In order to set up mTLS one needs at least a root CA certificate that both client and server trust.
This root CA might then issue an intermediate CA (and so forth) which is then issue a certificate for either client and
server. This certificate chain along with the corresponding key is then participate in the handshake which is happening
at the very beginning of HTTPS interaction.

See [README.md](x509/README.md) in the x509 folder to understand how to generate x509 certificates for both client
and server.

Run ``make -C certgen`` prior to building java project to create certs (don't wipe target folder once you generate certs!).

Then run ``Main`` class with the following arguments: ``-clientMode MUTUAL_TLS -serverAuthMode REQUIRE``.
