# this is the base index on the full field
FT.CREATE irsf_idx PREFIX 1 "n:" SCHEMA n TAG
# this index only uses the first 2 characters and the next 6 as last 3 are not used for indexing
FT.CREATE irsf_split PREFIX 1 "n:" SCHEMA f2 TAG n6 TAG
# this index is same as above but f2H and n6H cardinality is half that of f2 and n6
FT.CREATE irsf_split_h PREFIX 1 "n:" SCHEMA f2H TAG n6H TAG
