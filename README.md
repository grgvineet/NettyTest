# NettyTest
A simple Android application that can run ssl server or client using Netty

Either use two Android devices to test this application.
Or you can use OpenSSL CLI to act as other device

For OpenSSL Client, use command
openssl s_client -connect <ip-address>:<port>
e.g. openssl s_client -connect 192.168.1.6:1234

For OpenSSL Server, you have to first create a certificate and a key for server
Use command
openssl req -x509 -newkey rsa:<key-size, bits> -keyout <key file name> -out <certificate file name> -days <certificate validity>
e.g. openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365

To start a server, use command
openssl s_server -accept <port number> -cert <certificate file> -key <key file>
e.g. openssl s_server -accept 1234 -cert cert.pem -key key.pem

