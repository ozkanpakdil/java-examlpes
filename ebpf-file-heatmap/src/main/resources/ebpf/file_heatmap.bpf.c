#include "bpf_base_types.h"
#include <vmlinux.h>
#include <bpf/bpf_helpers.h>
#include <bpf/bpf_tracing.h>
#include <bpf/bpf_core_read.h>

#ifndef BPF_ANY
#define BPF_ANY 0
#endif

#define MAX_FILENAME_LEN 256
#define S_IFMT  00170000
#define S_IFREG 0100000
#define S_IFDIR 0040000

struct file_stats {
    u64 reads;
    u64 writes;
    u64 read_bytes;
    u64 write_bytes;
};

// Map to store stats per filename
struct {
    __uint(type, BPF_MAP_TYPE_HASH);
    __uint(max_entries, 10240);
    __type(key, char[MAX_FILENAME_LEN]);
    __type(value, struct file_stats);
} file_map SEC(".maps");

// Helper to update stats
static __always_inline void update_stats(struct file *file, u64 bytes, bool is_write) {
    if (!file) return;

    // Filter to show only regular files and directories to avoid socket/pipe noise
    struct inode *inode = BPF_CORE_READ(file, f_inode);
    if (inode) {
        u16 mode = BPF_CORE_READ(inode, i_mode);
        if (!((mode & S_IFMT) == S_IFREG || (mode & S_IFMT) == S_IFDIR)) {
            return;
        }
    }

    char filename[MAX_FILENAME_LEN];
    
    struct dentry *dentry = BPF_CORE_READ(file, f_path.dentry);
    struct qstr d_name = BPF_CORE_READ(dentry, d_name);
    
    // We will try to get at least the last 3 components of the path to make it more descriptive
    // Reconstructing a full path of unknown length is restricted in BPF.
    // Let's try to get: /parent2/parent1/filename
    
    char p1[32] = {0}, p2[32] = {0};
    struct dentry *parent1 = BPF_CORE_READ(dentry, d_parent);
    if (parent1 && parent1 != dentry) {
        struct qstr d_name1 = BPF_CORE_READ(parent1, d_name);
        bpf_probe_read_kernel_str(p1, sizeof(p1), d_name1.name);
        
        struct dentry *parent2 = BPF_CORE_READ(parent1, d_parent);
        if (parent2 && parent2 != parent1) {
            struct qstr d_name2 = BPF_CORE_READ(parent2, d_name);
            bpf_probe_read_kernel_str(p2, sizeof(p2), d_name2.name);
        }
    }

    char name[64] = {0};
    bpf_probe_read_kernel_str(name, sizeof(name), d_name.name);

    if (p2[0] != '\0' && p2[0] != '/') {
        __u64 args[3] = { (__u64)p2, (__u64)p1, (__u64)name };
        bpf_snprintf(filename, sizeof(filename), "/%s/%s/%s", args, sizeof(args));
    } else if (p1[0] != '\0' && p1[0] != '/') {
        __u64 args[2] = { (__u64)p1, (__u64)name };
        bpf_snprintf(filename, sizeof(filename), "/%s/%s", args, sizeof(args));
    } else {
        __u64 args[1] = { (__u64)name };
        bpf_snprintf(filename, sizeof(filename), "/%s", args, sizeof(args));
    }

    struct file_stats *stats = bpf_map_lookup_elem(&file_map, &filename);
    if (stats) {
        if (is_write) {
            stats->writes++;
            stats->write_bytes += bytes;
        } else {
            stats->reads++;
            stats->read_bytes += bytes;
        }
    } else {
        struct file_stats new_stats = {0};
        if (is_write) {
            new_stats.writes = 1;
            new_stats.write_bytes = bytes;
        } else {
            new_stats.reads = 1;
            new_stats.read_bytes = bytes;
        }
        bpf_map_update_elem(&file_map, &filename, &new_stats, BPF_ANY);
    }
}

SEC("kprobe/vfs_read")
int BPF_KPROBE(vfs_read, struct file *file, char *buf, size_t count) {
    update_stats(file, count, false);
    return 0;
}

SEC("kprobe/vfs_write")
int BPF_KPROBE(vfs_write, struct file *file, const char *buf, size_t count) {
    update_stats(file, count, true);
    return 0;
}

char LICENSE[] SEC("license") = "GPL";
