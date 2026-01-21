# Makefile for eBPF and Java Heatmap Project

# Tools
CLANG = clang
BPFTOOL = /usr/sbin/bpftool

# Paths to SDKMAN binaries
JAVA_HOME = /home/ozkan/.sdkman/candidates/java/25-graalce
MVN = /home/ozkan/.sdkman/candidates/maven/current/bin/mvn
JAVA = $(JAVA_HOME)/bin/java

# Paths
EBPF_DIR = src/main/resources/ebpf
EBPF_SRC = $(EBPF_DIR)/file_heatmap.bpf.c
EBPF_OBJ = $(EBPF_DIR)/file_heatmap.bpf.o
VMLINUX_H = $(EBPF_DIR)/vmlinux.h

# Flags
CLANG_FLAGS = -g -O2 -target bpf -D__TARGET_ARCH_x86

all: bpf java

$(VMLINUX_H):
	@echo "Generating full vmlinux.h for initial compilation..."
	$(BPFTOOL) btf dump file /sys/kernel/btf/vmlinux format c > $@

# Target to explicitly minify vmlinux.h
minify: $(EBPF_OBJ)
	@echo "Minifying vmlinux.h based on $(EBPF_OBJ)..."
	$(BPFTOOL) gen min_core_btf /sys/kernel/btf/vmlinux $(EBPF_DIR)/vmlinux.btf.min $(EBPF_OBJ)
	$(BPFTOOL) btf dump file $(EBPF_DIR)/vmlinux.btf.min format c > $(VMLINUX_H)
	rm $(EBPF_DIR)/vmlinux.btf.min
	@echo "Recompiling with minified vmlinux.h..."
	$(CLANG) $(CLANG_FLAGS) -I$(EBPF_DIR) -I/usr/include/$(shell uname -m)-linux-gnu -c $(EBPF_SRC) -o $(EBPF_OBJ)

bpf: $(VMLINUX_H) $(EBPF_OBJ)

$(EBPF_OBJ): $(EBPF_SRC) $(VMLINUX_H)
	$(CLANG) $(CLANG_FLAGS) -I$(EBPF_DIR) -I/usr/include/$(shell uname -m)-linux-gnu -c $< -o $@

java:
	JAVA_HOME=$(JAVA_HOME) $(MVN) clean compile

run: all
	JAVA_HOME=$(JAVA_HOME) $(MVN) dependency:build-classpath -Dmdep.outputFile=.classpath
	sudo JAVA_HOME=$(JAVA_HOME) $(JAVA) -cp target/classes:$$(cat .classpath) io.github.ozkanpakdil.Main $(EBPF_OBJ)

clean:
	rm -f $(EBPF_OBJ) .classpath
	sudo rm -rf target/
	JAVA_HOME=$(JAVA_HOME) $(MVN) clean

.PHONY: all bpf java run clean minify
