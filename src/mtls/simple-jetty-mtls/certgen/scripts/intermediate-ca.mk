
.PHONY: all
all: ${ICA}/certs/ca-chain.cert.pem

# Intermediate CA certificate chain creation sequence

${ICA}/certs/ca-chain.cert.pem: ${ICA}/certs/ca.cert.pem ${ICA}/newcerts ${ICA}/index.txt ${ICA}/serial ${ICA}/crlnumber
	cat ${ICA}/certs/ca.cert.pem ${R}/certs/ca.cert.pem > ${ICA}/certs/ca-chain.cert.pem

# Create intermediate certificate using generated CSR by signing it with the root CA's private key
${ICA}/certs/ca.cert.pem: ${ICA}/certs ${ICA}/csr/ca.csr.pem
	openssl ca -config ${R}/openssl.conf \
		-extensions v3_intermediate_ca  -days 3650 -notext -md sha256 \
		-batch \
		-in ${ICA}/csr/ca.csr.pem \
		-passin file:${PASSWORDS}/root-ca-key.txt \
		-out ${ICA}/certs/ca.cert.pem

${ICA}/csr/ca.csr.pem: ${ICA}/csr ${ICA}/private/ca.key.pem ${ICA}/openssl.conf
	openssl req -config ${ICA}/openssl.conf \
		-key ${ICA}/private/ca.key.pem -passin file:${ICA_PASSWORD_FILE} \
		-new -sha256 \
		-batch \
		-subj "/C=US/ST=WA/O=mTLS Demo by Alex Shabanov/CN=${CN}" \
		-out ${ICA}/csr/ca.csr.pem

${ICA}/private/ca.key.pem: ${ICA}/private
	openssl genrsa -aes256 -out ${ICA}/private/ca.key.pem -passout file:${ICA_PASSWORD_FILE} ${CA_KEY_LENGTH}

${ICA}/openssl.conf: ${ICA}
	cat ./ca/intermediate-openssl.cnf.tpl | sed s@CERT_TARGET_DIR_PLACEHOLDER@${ICA}@g > ${ICA}/openssl.conf

${ICA}/serial:
	echo 1000 > ${ICA}/serial

${ICA}/index.txt:
	touch ${ICA}/index.txt

${ICA}/crlnumber:
	echo 1000 > ${ICA}/crlnumber


${ICA}/private: ${ICA}
	mkdir ${ICA}/private

${ICA}/newcerts: ${ICA}
	mkdir ${ICA}/newcerts

${ICA}/csr: ${ICA}
	mkdir ${ICA}/csr

${ICA}/crl: ${ICA}
	mkdir ${ICA}/crl

${ICA}/certs: ${ICA}
	mkdir ${ICA}/certs

${ICA}:
	mkdir -p ${ICA}

