# Certificate Generator

This subproject contains a way to re-create all the used certificates, including root CA, intermediate CAs and client and
server CAs.

TBD: more details

## Tools

### Prerequisites

Set the following env variable:

```bash
export G="$PWD/target/generated-certificates"
```

### Show certificate information

```bash
openssl x509 -in $G/root-ca/certs/ca.cert.pem -text -noout
```

### Verify intermediate certificate

```bash
openssl verify -CAfile $G/root-ca/certs/ca.cert.pem $G/i1-ca/certs/ca.cert.pem
```

### Verify server certificate

```bash
openssl verify -CAfile $G/i1-ca/certs/ca-chain.cert.pem $G/server/cert.pem
```

### Verify certificate chain

Approach 1 (verify whole chain, use separate intermediate CA's cert and server cert as separate files)

```bash
openssl verify -CAfile $G/root-ca/certs/ca.cert.pem -untrusted $G/i1-ca/certs/ca.cert.pem $G/server/cert.pem
```

Approach 2 (validate whole server cert chain)

```bash
openssl verify -CAfile $G/root-ca/certs/ca.cert.pem $G/server/cert-chain.pem
openssl verify -CAfile $G/root-ca/certs/ca.cert.pem $G/client/cert-chain.pem
```

### Live verification of the server cert

```bash
# In Terminal 1:
openssl rsa -check -in $G/server/key.pem -passin file:./certgen/passwords/server-key.txt > /tmp/server-key.pem && python ./certgen/scripts/test-server.py

# In Terminal 2:
curl --cacert $G/root-ca/certs/ca.cert.pem https://localhost:8443/
```

### Verify Mutual TLS

```bash
openssl rsa -check -in $G/client/key.pem -passin file:./certgen/passwords/client-key.txt > /tmp/raw-client-key.pem
curl --cacert $G/root-ca/certs/ca.cert.pem --cert $G/client/cert-chain.pem --key /tmp/raw-client-key.pem https://localhost:8443/
```

### Check RSA Passphrase

```bash
openssl rsa -check -in $G/server/key.pem -passin file:./certgen/passwords/server-key.txt
```

### Debugging Certificate Issues

```bash
openssl s_client -connect localhost:8443 -showcerts 2>&1 < /dev/null
```

See also [Manually verify a certificate against an OCSP](https://raymii.org/s/articles/OpenSSL_Manually_Verify_a_certificate_against_an_OCSP.html)

## Links

* Inspiration for this sample - [Jamie's Guide to Openssl Cert Authority](https://jamielinux.com/docs/openssl-certificate-authority/create-the-root-pair.html)
* [OpenSSL Command Line HowTo](https://www.madboa.com/geek/openssl/)

## Alternative Approaches

```bash
curl --cacert $G/i1-ca/certs/ca-chain.cert.pem https://localhost:8443/
```