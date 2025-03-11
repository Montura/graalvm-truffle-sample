## Reproducing the problem with sandbox-limits in Graal Truffle Engine

1. Working with `org.graalvm.polyglot.Context` for `JS` language.
2. Build JSContext with additional _polyglot-sandbox-limits_ properties `sandbox.MaxCPUTime` and `sandbox.MaxHeapMemory`:
  ```
  Map<String, String> props = new HashMap<>();
          props.put("sandbox.MaxCPUTime", "500ms");
          props.put("sandbox.MaxHeapMemory", "50MB");
  Context.newBuilder("JS")
         .options(props)
         .build();
  ```
4. Then create 1k `JSContexts` with _polyglot-sandbox-limits_ and trivial JS sourceCode
```
let n = 50
let v = 0.0
let k = 0
for (let i = 0; i < n; ++i) {
    let u = x[i]
    v += u
    k += 1
}
let res = v / k
```
6. Then 2 threads polling `JSContexts` and executing sourceCode them like `context.eval(sourceCode)`

### The problem is:
- I got a very huge CPU consumption in this scenario

**I suppose:**
- Sandbox creates motinoring threads for JSContext (at least 1)
- So each execution JSContext-with-sandbox  requires 1 thread for executing  context.eval(sourceCode)  and 2 more for the limits (heap and memory)
- JFR shows:
  - There is a contention between Sandbox Limit Checker Thread-s on some resources inside the sandbox.
  - What am I doing wrong? Or just sandbox limits shouldn't be used in multithreaded scenario?
  - ![image](https://github.com/user-attachments/assets/f64932aa-547a-4689-9fe0-098156005356)
  
### P.S.
- For _Graal under GFTC_ based on `jdk-21.0.2` I the problem above
- For _Graal CE_ build based on `jdk-21.0.2` everything is OK => no sandbox => no problems
