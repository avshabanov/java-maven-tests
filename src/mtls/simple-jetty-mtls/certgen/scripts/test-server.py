import BaseHTTPServer, SimpleHTTPServer
import ssl

httpd = BaseHTTPServer.HTTPServer(
        ('localhost', 8443),
        SimpleHTTPServer.SimpleHTTPRequestHandler)

httpd.socket = ssl.wrap_socket(
        httpd.socket,
        server_side=True,
        certfile='./target/generated-certificates/server/cert.pem',
        ca_certs='./target/generated-certificates/i1-ca/certs/ca-chain.cert.pem',
        keyfile='/tmp/raw-server-key.pem')

httpd.serve_forever()

