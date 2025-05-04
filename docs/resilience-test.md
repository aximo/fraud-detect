# Resilience Test
## Aim
when access the service continuously, the service restart or upgrade should not cause the request failed

## Steps
1. deploy the service to k8s
2. start continue access via stress testing
we use hey as it have a good result summary for testing
```shell

# install hey
curl -O https://hey-release.s3.us-east-2.amazonaws.com/hey_linux_amd64
chmod 700 ./hey_linux_amd64

# run testing for 120s
./hey_linux_amd64 -z 120s -c 2 -m POST \
   -H "Content-Type: application/json" \
   -H "x-request-id: 6d300cd1-0650-42c4-83cb-76063bf0a905" \
   -d '{"id": "6d300cd1-0650-42c4-83cb-76063bf0a905","amount": 2000,"age": 16,"country": "china"}' \
   http://fraud-detect-svc/api/transactions/fraud/detect  
```
3. restart the deployment via
```shell
kubectl rollout restart deployment fraud-detect
# check the pods
kubectl get pods                               
NAME                           READY   STATUS              RESTARTS   AGE
fraud-detect-5ff97c9f8-qwdmd   0/1     ContainerCreating   0          3s
fraud-detect-5ff97c9f8-zlnk5   0/1     Running             0          3s
fraud-detect-9f87f5b6f-crk98   1/1     Running             0          99s
fraud-detect-9f87f5b6f-d5ht6   1/1     Terminating         0          99s
test-pod                       1/1     Running             0          57m

# check the pods again
kubectl get pods
NAME                           READY   STATUS        RESTARTS   AGE
fraud-detect-5ff97c9f8-qwdmd   1/1     Running       0          42s
fraud-detect-5ff97c9f8-z4dhj   0/1     Running       0          25s
fraud-detect-5ff97c9f8-zlnk5   1/1     Running       0          42s
fraud-detect-9f87f5b6f-crk98   1/1     Terminating   0          2m18s
test-pod                       1/1     Running       0          58m

# finally
kubectl get pods
NAME                           READY   STATUS    RESTARTS   AGE
fraud-detect-5ff97c9f8-qwdmd   1/1     Running   0          104s
fraud-detect-5ff97c9f8-z4dhj   1/1     Running   0          87s
fraud-detect-5ff97c9f8-zlnk5   1/1     Running   0          104s
test-pod                       1/1     Running   0          59m

```
4. wait the `hey` command completed, and analyse the report
```shell
Summary:
  Total:        120.0152 secs
  Slowest:      0.4674 secs
  Fastest:      0.0005 secs
  Average:      0.0046 secs
  Requests/sec: 435.5697
  

Response time histogram:
  0.000 [1]     |
  0.047 [50132] |■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.094 [2098]  |■■
  0.141 [37]    |
  0.187 [2]     |
  0.234 [1]     |
  0.281 [0]     |
  0.327 [0]     |
  0.374 [0]     |
  0.421 [3]     |
  0.467 [1]     |


Latency distribution:
  10% in 0.0007 secs
  25% in 0.0008 secs
  50% in 0.0012 secs
  75% in 0.0015 secs
  90% in 0.0032 secs
  95% in 0.0096 secs
  99% in 0.0817 secs

Details (average, fastest, slowest):
  DNS+dialup:   0.0000 secs, 0.0005 secs, 0.4674 secs
  DNS-lookup:   0.0000 secs, 0.0000 secs, 0.0193 secs
  req write:    0.0000 secs, 0.0000 secs, 0.0016 secs
  resp wait:    0.0044 secs, 0.0004 secs, 0.4665 secs
  resp read:    0.0001 secs, 0.0000 secs, 0.0872 secs

Status code distribution:
  [200] 52275 responses
```
the output show there is no failed request during the restart, it is reliable


## Result
According to the above report, the system is reliable as no request failed.