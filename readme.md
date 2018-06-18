# Json streamed statistics

* Captures **slow/fast** *json* input from `STDIN`
* Absorbs faulty *json*
* Calculates running statistics on *json* fields
* Exposes `REST API` for getting pieces of statistics

## Input

> Might not always come correct are ready for parsing

```json
{ 
  "event_type": "baz",     
  "data": "amet",           
  "timestamp": 1529312846   
}
```

## API

|Method| URL               | Description |
|---   | -----------       | ----------- |
|GET   | /words            | `{"lorem":5,"ipsum":5}`       |
|GET   | /event-count      | `{"foo":100,"bar":455}`        |

## Configuration params (application.conf)

- `app.throttling.rate = 1000` 
Milliseconds to conflate running stats
- `app.request.timeout = 3000` 
Async request for statistics timeout limitation (ms)
- `app.design.fast-producer=true` 
Build regular pipeline or conflated (allowing for fast independent producer)


## Build and run instructions

### Build with SBT
```bash
sbt clean stage
```
Executable files could be found in *./target/universal/stage/bin/* folder


### Run with slow generator
```bash
~/Documents/generator-macosx-amd64  |  ./target/universal/stage/bin/jsonstreaming
```

### Run fast with capture file
```bash
cat ~/Documents/generated.txt  |  ./target/universal/stage/bin/jsonstreaming -DFAST_PRODUCER=true
```
