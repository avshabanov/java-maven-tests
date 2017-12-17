import BaseHTTPServer, SimpleHTTPServer
import ssl

httpd = BaseHTTPServer.HTTPServer(
        ('localhost', 8443),
        SimpleHTTPServer.SimpleHTTPRequestHandler)

httpd.socket = ssl.wrap_socket(
        httpd.socket,
        server_side=True,
        certfile='./target/generated-certificates/server/cert.pem',
        keyfile='./target/generated-certificates/server/key.pem')

httpd.serve_forever()

