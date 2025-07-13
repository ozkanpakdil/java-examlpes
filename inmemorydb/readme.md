In Memory Database
Requirements

Your task is to implement a simplified version of an in-memory database. Plan your design according to the level specifications below:


Level 1: In-memory database should support basic operations to manipulate records, fields, and values within fields.
Level 2: In-memory database should support displaying a specific record's fields based on a filter.
Level 3: In-memory database should support TTL (Time-To-Live) configurations on database records.
Level 4: In-memory database should support backup and restore functionality. To move to the next level, you need to pass all the tests at this level.

Note You will receive a list of queries to the system, and the final output should be an array of strings representing the returned values of all queries. Each query will only call one operation.
Level 1

The basic level of the in-memory database contains records. Each record can be accessed with a unique identifier key of string type. A record may contain several field-value pairs, both of which are of string type.

Examples

The example below shows how these operations should work:

Queries
```shell
queries = [
["SET", "A", "B", "E"],
["SET", "A", "C", "F"],
["GET", "A", "B"],
["GET", "A", "D"],
["DELETE", "A", "B"],
["DELETE", "A", "D"]
]
```

Explanations
```shell
returns ""; database state: {"A": {"B": "E"}}
returns ""; database state: {"A": {"C": "F", "B":"E"}}
returns "E"
returns ""
returns "true"; database state: {"A": {"C": "F"}}
returns "false"; database state: {"A": {"C": "F"}}
```

Level 2

The database should support displaying data based on filters. Introduce an operation to support printing some fields of a record.

Examples

The example below shows how these operations should work

Queries
```shell
queries = [
["SET", "A", "BC", "E"],
["SET", "A", "BD", "F"],
["SET", "A", "C", "G"],
["SCAN_BY_PREFIX", "A", "B"],
["SCAN", "A"],
["SCAN_BY_PREFIX", "B", "B"] ]
```

Explanations
```shell
returns ""; database state: {"A": {"BC": "E"}}
returns ""; database state: {"A": {"BC": "E", "BD": "F"}}
returns ""; database state: {"A": {"BC": "E", "BD": "F", "C": "G"}}
returns "BC(E), BD(F)"
returns "BC(E), BD(F), C(G)"
returns ""
```
the output should be ["", "", "", "BC(E), BD(F)", "BC(E), BD(F), C(G)", ""].
Level 3

Support the timeline of operations and TTL (Time-To-Live) settings for records and fields. Each operation from previous levels now has an alternative version with a timestamp parameter to represent when the operation was executed. For each field-value pair in the database, the TTL determines how long that value will persist before being removed. Notes:

Examples

The examples below show how these operations should work

Queries
```shell
queries = [
["SET_AT_WITH_TTL", "A", "BC", "E", "1", "9"],
["SET_AT_WITH_TTL", "A", "BC", "E", "5", "10"],
["SET_AT", "A", "BD", "F", "5"],
["SCAN_BY_PREFIX_AT", "A", "B", "14"],
["SCAN_BY_PREFIX_AT", "A", "B", "15"]
]
```
Explanations
```shell
returns ""; database state: {"A": {"BC": "E"}}
where {"BC": "E"} expires at timestamp 10 returns ""; database state: {"A": {"BC": "E"}}
as field "BC" in record "A" already
exists, it was overwritten,
and {"BC": "E"} now expires at timestamp 15
returns ""; database state: {"A": {"BC": E", "BD": "F"}}
where {"BD": "F"} does not expire
returns "BC(E), BD(F)"
returns "BD(F)"
```
the output should be ["", "", "", "BC(E), BD(F)", "BD(F)"].
Example2

Queries
```shell
queries = [
["SET_AT", "A", "B", "C",
"1"],
["SET_AT_WITH_TTL", "X",
"Y", "Z", "2", "15"],
["GET_AT", "X", "Y", "3"], ["SET_AT_WITH_TTL", "A",
"D", "E", "4", "10"],
["SCAN_AT", "A", "13"],
["SCAN_AT", "X", "16"],
["SCAN_AT", "X", "17"],
["DELETE_AT", "X", "Y",
"20"]
]
```
Explanations
```shell
returns ""; database state: {"A": {"B": "C"}} returns ""; database state: {"X": {"Y": "Z"}, "A": {"B": "C"}}
where {"Y": "Z"} expires at timestamp 17 returns "Z"
returns ""; database state:
{"X": {"Y": "Z"}, "A": {"D": "E", "B": "C"}} where {"D": "E"} expires at timestamp 14 and {"Y": "Z"} expires at timestamp 17
returns "B(C), D(E)"
returns "Y(Z)"
returns ""; Note that all fields in record "X" have expired
returns "false"; the record "X" was expired at timestamp 17 and can't be deleted.
```
the output should be ["", "", "Z", "", "B(C), D(E)", "Y(Z)", "", "false"].
Level 4

The database should be backed up from time to time. Introduce operations to support backing up and restoring the database state based on timestamps. When restoring, ttl expiration times should be recalculated accordingly.

Examples

Queries
```shell
queries = [
["SET_AT_WITH_TTL", "A", "B", "C", "1", "10"],
["BACKUP", "3"],
["SET_AT", "A", "D", "E", "4"],
["BACKUP", "5"],
["DELETE_AT", "A", "B",
"8"],
["BACKUP", "9"],
["RESTORE", "10", "7"],
["BACKUP", "11"],
["SCAN_AT", "A", "15"],
["SCAN_AT", "A", "16"]
]
```
Explanations
```shell
returns ""; database state: {"A": {"B": "C"}} with lifespan [1, 11), meaning that the record should be deleted at timestamp = 11.
returns "1"; saves the database state
returns ""; database state: {"A": {"D": "E", "B": "C"}}
returns "1"; saves the database state
returns "true"; database state: {"A": {"D": "E"}} returns "1"; saves the database state
returns ""; restores the database to state of last backup at timestamp = 5:
{"A": {"D": "E", "B": "C"}}
with {"B": "C"} expiring at timestamp = 16: Since the initial ttl of the field is 10
and the database was restored to the state at timestamp = 5; this field has had
a lifespan of 4 and a remaining ttl of 6, so it will now expire at timestamp = 10 + 6 = 16. returns "1"; saves the database state
returns "B(C), D(E)"
returns "D(E)"
```
the output should be ["", "1", "", "1", "true", "1", "", "1", "B(C), D(E)", "D(E)"].