git pull
sh ./docker_build_simple.sh
kubectl get pods -nmira|grep job-service|awk '{print $1}'|xargs -I{} -n1 -p  kubectl delete pod {} -nmira