create table if not exists demo.postcode (
    postcode text PRIMARY KEY,
    coord vector<float,2>
);
create custom index if not exists postcode_coord on demo.postcode(coord) using 'StorageAttachedIndex' WITH OPTIONS = { 'similarity_function': 'euclidean' };

create table if not exists demo.postcode_float (
    postcode text PRIMARY KEY,
    coord vector<float,2>
);
create custom index if not exists postcode_float on demo.postcode_float(coord) using 'StorageAttachedIndex' WITH OPTIONS = { 'similarity_function': 'euclidean' };

create table if not exists demo.postcode_int (
    postcode text PRIMARY KEY,
    coord vector<float,2>
);
create custom index if not exists postcode_int on demo.postcode_int(coord) using 'StorageAttachedIndex' WITH OPTIONS = { 'similarity_function': 'euclidean' };
