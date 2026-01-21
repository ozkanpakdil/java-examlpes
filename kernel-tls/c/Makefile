CC ?= gcc
CFLAGS ?= -O2 -Wall -Wextra -Wno-deprecated-declarations
LDFLAGS ?=

# Try to use pkg-config for OpenSSL 3 first, then fallback
OPENSSL_CFLAGS := $(shell pkg-config --cflags openssl 2>/dev/null || pkg-config --cflags libssl 2>/dev/null)
OPENSSL_LIBS   := $(shell pkg-config --libs openssl 2>/dev/null || pkg-config --libs libssl 2>/dev/null)

CFLAGS += $(OPENSSL_CFLAGS)
LDFLAGS += $(OPENSSL_LIBS)

BIN := ktls_https_server
SRC := server.c
OBJ := $(SRC:.c=.o)

all: $(BIN)

$(BIN): $(OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	rm -f $(OBJ) $(BIN)

.PHONY: all clean
