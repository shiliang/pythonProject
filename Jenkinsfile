node('jenkins-slave-k8s') {
    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    def gitBranch = myRepo.GIT_BRANCH
    def imageTag = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    def dockerUser = "docker.oa.com:5000"
    def imageEndpoint = "mpc/job-service"
    def image = "${dockerUser}/${imageEndpoint}"
    stage('Test') {
        echo "1. 测试阶段"
    }
    stage('Package') {
        container('docker') {
            echo "2.代码打包阶段"
            sh "docker build -t ${image}:${imageTag} ."
        }
    }
    stage('Push') {
        container('docker') {
            echo "3. 上传镜像阶段"
            withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                sh "docker login docker.oa.com:5000 -u ${dockerHubUser} -p ${dockerHubPassword}"
                sh "docker push ${image}:${imageTag}"
            }
        }
    }
    stage('Deploy') {
        container('helm') {
            withCredentials([file(credentialsId: 'k3s-243', variable: 'KUBECONFIG')]) {
                echo "4. 部署阶段"
                sh "mkdir -p /root/.kube && cp ${KUBECONFIG} /root/.kube/config"
                sh "helm upgrade --install job-service ./job-service --set image.repository=${image} --set image.tag=${imageTag} --namespace=mpc"
            }
        }
    }
}
