#!/bin/bash

git pull

sh ./docker_build_simple.sh

ns_arr=("mira1" "mira2")

for ns in ${ns_arr[*]}; do
    echo "$ns update pod"
    kubectl get pods -n $ns|grep job-service|awk '{print $1}'|xargs -I{} -n1 -p  kubectl delete pod {} -n $ns
done
