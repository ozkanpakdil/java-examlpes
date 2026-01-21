#define _GNU_SOURCE
#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include <openssl/ssl.h>
#include <openssl/err.h>
#include <openssl/x509.h>

/*
 * Minimal HTTPS 1.1 server using OpenSSL. If your OpenSSL is built with
 * --with-ktls / enable-ktls and the Linux kernel supports it, OpenSSL will
 * offload record encryption/decryption to kTLS automatically for supported
 * ciphers. We report whether OpenSSL thinks kTLS is active (best-effort).
 *
 * This server:
 * - binds to 0.0.0.0:10443 by default (override with PORT env)
 * - serves a fixed-size payload (bytes) on every request to '/'
 *   size can be configured via SIZE env (default 16384).
 * - uses a self-signed certificate generated on first run (cert.pem/key.pem)
 */

static volatile int keep_running = 1;

static void on_sigint(int sig) { (void)sig; keep_running = 0; }

__attribute__((unused)) static int set_nonblock(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags < 0) return -1;
    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

static int generate_self_signed(const char *cert_path, const char *key_path) {
    /* For simplicity, shell out to OpenSSL if available. */
    char cmd[1024];
    snprintf(cmd, sizeof(cmd),
             "openssl req -x509 -newkey rsa:2048 -sha256 -nodes -keyout %s -out %s -subj /CN=localhost -days 1 >/dev/null 2>&1",
             key_path, cert_path);
    int rc = system(cmd);
    return rc == 0 ? 0 : -1;
}

__attribute__((unused)) static int write_all(int fd, const char *buf, size_t len) {
    size_t off = 0;
    while (off < len) {
        ssize_t n = write(fd, buf + off, len - off);
        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        off += (size_t)n;
    }
    return 0;
}

static void https_respond(SSL *ssl, const unsigned char *body, size_t body_len) {
    char hdr[256];
    int hdr_len = snprintf(hdr, sizeof(hdr),
                           "HTTP/1.1 200 OK\r\n"
                           "Content-Type: application/octet-stream\r\n"
                           "Content-Length: %zu\r\n"
                           "Connection: close\r\n\r\n",
                           body_len);
    SSL_write(ssl, hdr, hdr_len);
    size_t off = 0;
    while (off < body_len) {
        int n = SSL_write(ssl, body + off, (int)(body_len - off));
        if (n <= 0) break;
        off += (size_t)n;
    }
}

static int ktls_is_active(SSL *ssl) {
#if OPENSSL_VERSION_NUMBER >= 0x10101000L
    /* OpenSSL 1.1.1+ exposes BIO socket and internal ktls helpers.
     * Public "is ktls active" API isn't guaranteed; best effort: */
#ifdef BIO_get_ktls_send
    BIO *wbio = SSL_get_wbio(ssl);
    if (wbio && BIO_get_ktls_send(wbio)) return 1;
#endif
#ifdef BIO_get_ktls_recv
    BIO *rbio = SSL_get_rbio(ssl);
    if (rbio && BIO_get_ktls_recv(rbio)) return 1;
#endif
#endif
    return 0;
}

int main(int argc, char **argv) {
    (void)argc; (void)argv;
    signal(SIGINT, on_sigint);
    signal(SIGTERM, on_sigint);

    const char *port_env = getenv("PORT");
    int port = port_env ? atoi(port_env) : 10443;
    const char *size_env = getenv("SIZE");
    size_t body_len = size_env ? (size_t)strtoul(size_env, NULL, 10) : 16384;

    unsigned char *body = malloc(body_len);
    if (!body) { perror("malloc"); return 1; }
    for (size_t i = 0; i < body_len; i++) body[i] = (unsigned char)(i * 31u);

    const char *cert_path = "cert.pem";
    const char *key_path = "key.pem";
    if (access(cert_path, R_OK) != 0 || access(key_path, R_OK) != 0) {
        fprintf(stderr, "Generating self-signed certificate...\n");
        if (generate_self_signed(cert_path, key_path) != 0) {
            fprintf(stderr, "Failed to generate self-signed certs. Install OpenSSL CLI or provide cert.pem/key.pem.\n");
            return 2;
        }
    }

    SSL_library_init();
    SSL_load_error_strings();
    OpenSSL_add_ssl_algorithms();

    const SSL_METHOD *method = TLS_server_method();
    SSL_CTX *ctx = SSL_CTX_new(method);
    if (!ctx) {
        ERR_print_errors_fp(stderr);
        return 3;
    }

    /* Protocol selection: default min TLS1.2, max TLS1.3; override via PROTOCOL env */
    const char *proto_env = getenv("PROTOCOL");
    if (proto_env && strcmp(proto_env, "TLS1.2") == 0) {
        SSL_CTX_set_min_proto_version(ctx, TLS1_2_VERSION);
        SSL_CTX_set_max_proto_version(ctx, TLS1_2_VERSION);
        fprintf(stderr, "Forcing protocol: TLS1.2\n");
    } else if (proto_env && strcmp(proto_env, "TLS1.3") == 0) {
#if defined(TLS1_3_VERSION)
        SSL_CTX_set_min_proto_version(ctx, TLS1_3_VERSION);
        SSL_CTX_set_max_proto_version(ctx, TLS1_3_VERSION);
        fprintf(stderr, "Forcing protocol: TLS1.3\n");
#else
        SSL_CTX_set_min_proto_version(ctx, TLS1_2_VERSION);
        SSL_CTX_set_max_proto_version(ctx, TLS1_2_VERSION);
        fprintf(stderr, "Requested TLS1.3 not available; falling back to TLS1.2\n");
#endif
    } else {
        SSL_CTX_set_min_proto_version(ctx, TLS1_2_VERSION);
#if defined(TLS1_3_VERSION)
        SSL_CTX_set_max_proto_version(ctx, TLS1_3_VERSION);
#endif
    }
    SSL_CTX_set_ecdh_auto(ctx, 1);
    SSL_CTX_set_options(ctx, SSL_OP_ALL | SSL_OP_NO_COMPRESSION);

    /* Optionally enable kTLS explicitly when supported (OpenSSL 3.x) */
#ifdef SSL_OP_ENABLE_KTLS
    const char *ktls_enable = getenv("KTLS_ENABLE");
    if (ktls_enable && (strcmp(ktls_enable, "1") == 0 || strcasecmp(ktls_enable, "on") == 0 || strcasecmp(ktls_enable, "true") == 0)) {
        unsigned long before = SSL_CTX_get_options(ctx);
        SSL_CTX_set_options(ctx, SSL_OP_ENABLE_KTLS);
        unsigned long after = SSL_CTX_get_options(ctx);
        fprintf(stderr, "SSL_OP_ENABLE_KTLS set (opts before=0x%lx, after=0x%lx)\n", before, after);
    }
#endif

    /* Optional: allow selecting cipher suites via env to influence kTLS on/off */
#if defined(TLS1_3_VERSION)
    const char *tls13_cipher = getenv("TLS13_CIPHER");
    if (tls13_cipher && tls13_cipher[0]) {
        if (SSL_CTX_set_ciphersuites(ctx, tls13_cipher) != 1) {
            fprintf(stderr, "Warning: failed to set TLS1.3 ciphersuite '%s'\n", tls13_cipher);
        } else {
            fprintf(stderr, "Using TLS1.3 ciphersuite: %s\n", tls13_cipher);
        }
    }
#endif
    const char *tls12_cipher = getenv("TLS12_CIPHER");
    if (tls12_cipher && tls12_cipher[0]) {
        if (SSL_CTX_set_cipher_list(ctx, tls12_cipher) != 1) {
            fprintf(stderr, "Warning: failed to set TLS1.2 cipher list '%s'\n", tls12_cipher);
        } else {
            fprintf(stderr, "Using TLS1.2 cipher list: %s\n", tls12_cipher);
        }
    }

    if (SSL_CTX_use_certificate_file(ctx, cert_path, SSL_FILETYPE_PEM) <= 0 ||
        SSL_CTX_use_PrivateKey_file(ctx, key_path, SSL_FILETYPE_PEM) <= 0 ||
        !SSL_CTX_check_private_key(ctx)) {
        ERR_print_errors_fp(stderr);
        return 4;
    }

    int ls = socket(AF_INET, SOCK_STREAM, 0);
    if (ls < 0) { perror("socket"); return 5; }
    int one = 1;
    setsockopt(ls, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port = htons((uint16_t)port);

    if (bind(ls, (struct sockaddr*)&addr, sizeof(addr)) < 0) { perror("bind"); return 6; }
    if (listen(ls, 512) < 0) { perror("listen"); return 7; }

    /* Print OpenSSL version and compile-time kTLS capability */
#if defined(OPENSSL_VERSION)
    printf("OpenSSL: %s\n", OpenSSL_version(OPENSSL_VERSION));
#else
    printf("OpenSSL version: %s\n", SSLeay_version(SSLEAY_VERSION));
#endif
#ifdef OPENSSL_NO_KTLS
    printf("OpenSSL kTLS support: not compiled in (OPENSSL_NO_KTLS defined)\n");
#else
    printf("OpenSSL kTLS support: compiled in\n");
#endif

    printf("kTLS-capable HTTPS server listening on 0.0.0.0:%d (SIZE=%zu)\n", port, body_len);

    while (keep_running) {
        struct sockaddr_in cli;
        socklen_t clen = sizeof(cli);
        int cs = accept(ls, (struct sockaddr*)&cli, &clen);
        if (cs < 0) {
            if (errno == EINTR) continue;
            perror("accept");
            break;
        }

        SSL *ssl = SSL_new(ctx);
        SSL_set_fd(ssl, cs);
        if (SSL_accept(ssl) <= 0) {
            ERR_print_errors_fp(stderr);
            SSL_free(ssl);
            close(cs);
            continue;
        }

        int ktls = ktls_is_active(ssl);
        printf("TLS handshake complete from %s:%d, kTLS active=%s\n",
               inet_ntoa(cli.sin_addr), ntohs(cli.sin_port), ktls ? "yes" : "no");

        /* Very simple HTTP/1.1 loop: read a request, ignore headers, respond, then close (no keep-alive) */
        char buf[8192];
        for (;;) {
            int n = SSL_read(ssl, buf, sizeof(buf));
            if (n <= 0) break;
            /* crude: consider end of headers when we see \r\n\r\n */
            char *eoh = NULL;
            for (int i = 0; i + 3 < n; i++) {
                if (buf[i]=='\r'&&buf[i+1]=='\n'&&buf[i+2]=='\r'&&buf[i+3]=='\n'){ eoh = &buf[i+4]; break; }
            }
            if (eoh) {
                https_respond(ssl, body, body_len);
                /* We advertise Connection: close; close after one response to align with ab -H 'Connection: close' */
                break;
            }
        }

        SSL_shutdown(ssl);
        SSL_free(ssl);
        close(cs);
    }

    close(ls);
    SSL_CTX_free(ctx);
    EVP_cleanup();
    free(body);
    printf("Server exiting\n");
    return 0;
}
