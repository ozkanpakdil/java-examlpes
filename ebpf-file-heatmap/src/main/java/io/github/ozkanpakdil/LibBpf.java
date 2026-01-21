package io.github.ozkanpakdil;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public interface LibBpf extends Library {
    LibBpf INSTANCE = Native.load("bpf", LibBpf.class);

    Pointer bpf_object__open(String path);
    int bpf_object__load(Pointer obj);
    int bpf_object__attach_skeleton(Pointer obj); // Not using skeleton here for simplicity
    
    // Manual attachment
    Pointer bpf_object__find_program_by_name(Pointer obj, String name);
    Pointer bpf_program__attach(Pointer prog);
    
    int bpf_object__find_map_fd_by_name(Pointer obj, String name);
    
    int bpf_map_get_next_key(int fd, Pointer cur_key, Pointer next_key);
    int bpf_map_lookup_elem(int fd, Pointer key, Pointer value);

    void bpf_object__close(Pointer obj);
}
