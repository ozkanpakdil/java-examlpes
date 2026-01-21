#!/bin/bash
# Generate disk I/O by writing and reading a large file
FILE="test_io_file.tmp"
echo "Generating disk I/O..."
dd if=/dev/urandom of=$FILE bs=1M count=100 conv=fsync
echo "Reading file back..."
dd if=$FILE of=/dev/null bs=1M
rm $FILE
echo "Done."
