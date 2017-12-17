
# Defines length of the CA RSA key
# NB: usually you want 4096 bit key for CA rsa root key, smaller value is used here for demo purposes
CA_KEY_LENGTH ?= 2048

CERT_KEY_LENGTH ?= 2048

PWD := $(shell pwd)

PASSWORDS := ${PWD}/passwords

# Root CA artifacts (including issued certificates)
R ?= ${PWD}/../target/generated-certificates/root-ca

# Intermediate CA 1
I1 ?= ${PWD}/../target/generated-certificates/i1-ca

# Intermediate CA 2
I2 ?= ${PWD}/../target/generated-certificates/i2-ca

# Server certificate and key, uses Intermediate Authority 1 (I2)
SR ?= ${PWD}/../target/generated-certificates/server

# Client certificate and key, uses Intermediate Authority 2 (I2)
CL ?= ${PWD}/../target/generated-certificates/client

KEYSTORE_PASSWORD := $(shell cat ${PASSWORDS}/server-key.txt)

.PHONY: all


all: ${SR}/keystore.jks


# Root CA

# Generate CA certificate along with the files, required for signing
${R}/certs/ca.cert.pem: ${R}/certs ${R}/private/ca.key.pem ${R}/openssl.conf ${R}/index.txt ${R}/serial ${R}/newcerts
	openssl req -config ${R}/openssl.conf \
		-key ${R}/private/ca.key.pem -passin file:${PASSWORDS}/root-ca-key.txt \
		-new -x509 -days 7300 -sha256 -extensions v3_ca \
		-batch -subj "/C=US/ST=WA/O=mTLS Demo by Alex Shabanov/CN=mTLS Demo Primary CA" \
		-out ${R}/certs/ca.cert.pem

${R}/private/ca.key.pem: ${R}/private
	openssl genrsa -aes256 -out ${R}/private/ca.key.pem -passout file:${PASSWORDS}/root-ca-key.txt ${CA_KEY_LENGTH}


${R}/openssl.conf: ${R} ca/root-openssl.cnf.tpl
	cat ca/root-openssl.cnf.tpl | sed s@CERT_TARGET_DIR_PLACEHOLDER@${R}@g > ${R}/openssl.conf


${R}/index.txt:
	touch ${R}/index.txt

${R}/serial:
	echo 1000 > ${R}/serial

${R}/newcerts: ${R}
	mkdir ${R}/newcerts

${R}/crl: ${R}
	mkdir ${R}/crl

${R}/private: ${R}
	mkdir ${R}/private

${R}/certs: ${R}
	mkdir ${R}/certs

${R}:
	mkdir -p ${R}

# Intermediate CA 1

${I1}/certs/ca-chain.cert.pem: ${R}/certs/ca.cert.pem
	make -f ./scripts/intermediate-ca.mk \
		R=${R} \
		ICA=${I1} \
		PASSWORDS=${PASSWORDS} \
		CN=mTLS-Demo-Intermediate-CA-1 \
		ICA_PASSWORD_FILE=${PASSWORDS}/intermediate-ca-1-key.txt

# Intermediate CA 2

${I2}/certs/ca-chain.cert.pem: ${R}/certs/ca.cert.pem
	make -f ./scripts/intermediate-ca.mk \
		R=${R} \
		ICA=${I2} \
		PASSWORDS=${PASSWORDS} \
		CN=mTLS-Demo-Intermediate-CA-2 \
		ICA_PASSWORD_FILE=${PASSWORDS}/intermediate-ca-2-key.txt


# Server Certificate

# NOTE extension type: usr_cert and CN

${SR}/keystore.jks: ${SR}/keystore.p12
	keytool -importkeystore \
		-destkeystore ${SR}/keystore.jks -deststoretype jks -deststorepass ${KEYSTORE_PASSWORD} \
		-srckeystore ${SR}/keystore.p12 -srcstoretype pkcs12 -srcstorepass ${KEYSTORE_PASSWORD}


# cert-chain.pem + key.pem and cert-chain.p12 are interchangeable, use p12 for java
${SR}/keystore.p12: ${SR}/cert-chain.pem
	openssl pkcs12 -export \
		-inkey ${SR}/key.pem \
		-in ${SR}/cert-chain.pem \
		-passin file:${PASSWORDS}/server-key.txt \
		-passout pass:${KEYSTORE_PASSWORD} \
		-name jetty \
		-out ${SR}/keystore.p12

${SR}/cert-chain.pem: ${SR}/cert.pem
	cat ${I1}/certs/ca.cert.pem ${SR}/cert.pem > ${SR}/cert-chain.pem

${SR}/cert.pem: ${SR}/csr.pem
	openssl ca -config ${I1}/openssl.conf \
		-batch \
		-extensions server_cert \
		-days 720 -notext -md sha256 \
		-passin file:${PASSWORDS}/intermediate-ca-1-key.txt \
		-in ${SR}/csr.pem \
		-out ${SR}/cert.pem

${SR}/csr.pem: ${SR}/key.pem ${I1}/certs/ca-chain.cert.pem
	openssl req -config ${I1}/openssl.conf \
		-key ${SR}/key.pem -passin file:${PASSWORDS}/server-key.txt \
		-batch \
		-subj "/C=US/ST=WA/O=mTLS Demo by Alex Shabanov/CN=localhost" \
		-new -sha256 -out ${SR}/csr.pem

${SR}/key.pem: ${SR}
	openssl genrsa -aes256 \
		-passout file:${PASSWORDS}/server-key.txt \
		-out ${SR}/key.pem ${CERT_KEY_LENGTH}

${SR}:
	mkdir -p ${SR}

# Auxiliary commands

.PHONY: clean

clean:
	rm -rf ${R}
