# Auto Scale Test
## Aim
the service pods is in high payload, the deployment pods will auto scale to max 3 pods according the hpa.yaml setting
after the payload become low, the pods should be auto scale to default size

## Steps
1. deploy the service to k8s
2. start continue access via stress testing
```shell
#  start the test-pod for running stress testing
kubectl run -i --tty --rm test-pod --image=alibaba-cloud-linux-3-registry.cn-hangzhou.cr.aliyuncs.com/alinux3/alinux3:latest /bin/bash

# install wrk as stress testing tool
yum install -y wrk

# create a post.lua, and see the content
cat post.lua 
wrk.method = "POST"
wrk.body = '{"id": "6d300cd1-0650-42c4-83cb-76063bf0a905","amount": 2000,"age": 16,"country": "china"}'
wrk.headers["Content-Type"] = "application/json"
wrk.headers["x-request-id"] = "6d300cd1-0650-42c4-83cb-76063bf0a905"

# run wrk, i don't use a high value for t and c parameter as it is a my private paid k8s cluster, just for a demo
wrk -t2 -c5 -d30s -s post.lua http://fraud-detect-svc/api/transactions/fraud/detect
Running 30s test @ http://fraud-detect-svc/api/transactions/fraud/detect
  2 threads and 5 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    24.36ms   35.14ms 416.21ms   82.36%
    Req/Sec   182.58     83.22   333.00     61.66%
  10780 requests in 30.03s, 1.72MB read
Requests/sec:    358.99
Transfer/sec:     58.61KB
```
3. watch the pods numbers
```shell
kubectl get pods
NAME                            READY   STATUS    RESTARTS   AGE
fraud-detect-5dc565b85d-6zsc2   1/1     Running   0          3m
fraud-detect-5dc565b85d-cr4xk   1/1     Running   0          3m
fraud-detect-5dc565b85d-vz855   1/1     Running   0          12m
test-pod                        1/1     Running   0          11m
```
the pods number will be up to  max 3 from default 2
after some minutes, the pods will scale down
```shell
kubectl get pods
NAME                            READY   STATUS        RESTARTS   AGE
fraud-detect-5dc565b85d-6zsc2   1/1     Terminating   0          6m46s
fraud-detect-5dc565b85d-cr4xk   1/1     Terminating   0          6m46s
fraud-detect-5dc565b85d-vz855   1/1     Running       0          15m
test-pod                        1/1     Running       0          14m

# finally, there only one pods existed
kubectl get pods 
NAME                            READY   STATUS    RESTARTS   AGE
fraud-detect-5dc565b85d-vz855   1/1     Running   0          17m
test-pod                        1/1     Running   0          16m
```

## Result
According to the steps result, it will  scale up and scale down automatically