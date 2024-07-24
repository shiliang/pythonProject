#!/bin/bash

show_help() {
    echo "Usage: $0 -t <tag> [-p <true|false>] [-r <registry_address>] [-v <version>] [-d <Dockerfile|Dockerfile_simple>]"
    echo "Options:"
    echo "  -t <tag>                Specify the tag for Docker images."
    echo "  -p <true|false>         Push the Docker images to the registry. (Default: false)"
    echo "  -r <registry_address>   Specify the address of the Docker registry."
    echo "                          If not provided, Docker Hub is assumed as the default registry."
    echo "  -v <version>            Specify the version for the JAR file. If not provided, it will be read from pom.xml."
    echo "  -d <Dockerfile|Dockerfile_simple> Specify which Dockerfile to use. (Default: Dockerfile)"
}

# Default values
push_flag=false
dockerfile="Dockerfile"

# Parse command line options
while getopts ":t:p:r:v:d:" opt; do
    case ${opt} in
        t)
            docker_tag=$OPTARG
            ;;
        p)
            push_flag=$OPTARG
            if [[ ! $OPTARG =~ ^(true|false)$ ]]; then
                echo "Invalid argument for -p. Please provide 'true' or 'false'." >&2
                show_help
                exit 1
            fi
            ;;
        r)
            registry_address=$OPTARG
            ;;
        v)
            jar_version=$OPTARG
            ;;
        d)
            dockerfile=$OPTARG
            if [[ ! $OPTARG =~ ^(Dockerfile|Dockerfile_simple)$ ]]; then
                echo "Invalid argument for -d. Please provide 'Dockerfile' or 'Dockerfile_simple'." >&2
                show_help
                exit 1
            fi
            ;;
        \?)
            echo "Invalid option: $OPTARG" 1>&2
            show_help
            exit 1
            ;;
        :)
            echo "Option -$OPTARG requires an argument." 1>&2
            show_help
            exit 1
            ;;
    esac
done

shift $((OPTIND -1))

# Check if tag is provided
if [ -z "$docker_tag" ]; then
    echo "Tag not specified."
    show_help
    exit 1
fi

# If version is not provided, read it from pom.xml
if [ -z "$jar_version" ]; then
    jar_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    if [ $? -ne 0 ]; then
        echo "Failed to read version from pom.xml"
        exit 1
    fi
fi

echo "Building mira-job-service environment image with version ${jar_version}"

# Build the Docker image
docker build --build-arg JAR_VERSION=${jar_version} -f ./${dockerfile} -t chainweaver/mira-job-service:${docker_tag} .

if [ ! -z "$registry_address" ]; then
    # Tag the Docker image with the registry address
    docker tag chainweaver/mira-job-service:${docker_tag} ${registry_address}/chainweaver/mira-job-service:${docker_tag}
fi

# Push the Docker image if the push flag is set to true
if [ "$push_flag" == "true" ]; then
    if [ -z "$registry_address" ]; then
        docker push chainweaver/mira-job-service:${docker_tag}
    else
        docker push ${registry_address}/chainweaver/mira-job-service:${docker_tag}
    fi
fi
