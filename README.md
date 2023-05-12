# redisearchPhoneSizing
dialing it around with redisearch and phone numbers-concentrating on index size
## helpful links
* [redisearch 2.0 getting started blog](https://redis.com/blog/getting-started-with-redisearch-2-0/)
* [redisearch 2.0 getting started github](https://github.com/RediSearch/redisearch-getting-started)
* [redisearch documentation](https://oss.redis.com/redisearch/master/)
* [redisearch github](https://github.com/Redislabs-Solution-Architects/redisaml#example-redisearch-queries)
* [redisearch sample query github](https://github.com/Redislabs-Solution-Architects/contracts#sample-queries)
* [redisearch client libraries](https://github.com/RediSearch/RediSearch#client-libraries)
* [another redisearch client libraries](https://oss.redis.com/redisearch/Clients/)

## Initial project setup
Get this github code
```bash
get clone https://github.com/jphaugla/redisearchPhoneSizing.git
```
Can use docker with redis-stack or use redis cloud
```bash
docker-compose up -d
```
## Modify java code
### Modify the redis connection
The change needs to be made in [this java code](src/main/java/jph/lettuce/examples/irsf/IrsfGenerator.java)
```bash
    private static String redis_host="redis-digits.another.us-east-1-2.ec2.cloud.redislabs.com";
    private static String redis_port="12076";
    private static String redis_pw="somewildstringfromrediscloud";
    private static int num_elements=500000;
```
### Choose faker data or random
The default is to use random generation.  I also tested out the java fakerdata library but did not find the phone number generation very helpful
The change is in the [same java code](src/main/java/jph/lettuce/examples/irsf/IrsfGenerator.java) as above here
```bash
    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
        lettuceGen();
        // fakerGen();
    }
```
## compile and run
* compile
```bash
mvn clean package
```
* run
```bash
java -jar target/irsf-1.0-SNAPSHOT-jar-with-dependencies.jar
```
## check out different size
the information on index sizing using ft.info is not accurate
to size the index:
1. get the size of the database without any indexes
2. create the index
3. get size of the database with this index
4. subtract number 3 from number 1
5. repeat for each index combination

### Detail
A [create index file](create_index.sh) is created with suggested index combinations and also has commented explanations
* to get a redis-cli shell, edit and use provided redis.sh script
```bash
./redis.sh
```
* to get the size of the database
```bash
info memory
# Memory
used_memory:149588152
used_memory_human:142.65M
used_memory_rss:149588152
used_memory_peak:302281216
used_memory_peak_human:288.27M
used_memory_lua:32768
mem_fragmentation_ratio:1
mem_allocator:jemalloc-5.1.0
```
Use the "used memory human" in this case *142.65M*
* create the index using line from [create index file](create_index.sh)
```bash
FT.CREATE irsf_idx PREFIX 1 "n:" SCHEMA n TAG
```
* wait for the index build to complete
  * can tell when it is complete when the num_records field in ft.info reaches number of hash records
```bash
FT.INFO irsf_idx
```
look for the column called "num_docs"  to quit increasing
* use *info memory* to get the memory with the index
* do this for each in the create-index

## sample output

|                | num_docs | Size of Date (MB) | index size | percent of original |
|----------------|----------|-------------------|------------|---------------------|
| no index       | 500000   | 73.4              |            |                     |
| original index | 500000   | 210.78            | 137.38     | 100%                |
| f2 n6 index    | 500000   | 194.15            | 120.75     | 87.89%              |
| f2H n6H index  | 500000   | 149.89            | 76.49      | 55.68%              |

## what it means
* the original index is all eleven digits of the phone number
* the data is generated using random so there is no overlap in the data causing a very sparse index
  * this is not the way phone numbers would be in the real world as there would be more overlap
  * it is making the index larger than if real data was used
* the f2/n6 index is separating out the phone number
  * the first 2 characters that are like a country calling code
  * the next 6 characters are relevant for searching
  * the final 3 characters are not relevant for searching but can be returned
  * the index is 88% of the original index
* the f2H/n6H is same as f2/n6 except cardinality is dropped in half for each
  * shows index becomes much smaller with lower cardinality 55.68% of original
  * likely, this is overly optimistic on the cardinality but want to illustrate the effect of cardinality