1 #!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <path_to_zip_file>"
    echo "Usage: $1 <path_to_install>"
    exit 1
fi

ZIP_PACKAGE=$(realpath $0)
INSTALL_DIR=$(realpath $1)

echo "${ZIP_PACKAGE} ${INSTALL_DIR}"
#unzip "$ZIP_PACKAGE" -d "$INSTALL_DIR"

MAVEN_HOME=$(realpath $(ls -d "$INSTALL_DIR"/* |grep maven| head -n 1))
JAVA_HOME=$(realpath $(ls -d "$INSTALL_DIR"/* |grep jdk| head -n 1))

echo "${MAVEN_HOME} ${JAVA_HOME}"
if [ ! -f "${JAVA_HOME}/bin/java" ]; then
    echo "Java not found at ${JAVA_HOME}/bin/java"
    exit 1
else
    echo "Java found at ${JAVA_HOME}/bin/java"
fi

if [ ! -f "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Maven script not found at ${MAVEN_HOME}/bin/mvn"
    exit 1
else
    echo "Maven script found at ${MAVEN_HOME}/bin/mvn"
fi
mv maven/settings.xml ${MAVEN_HOME}/conf/


if ! grep -q "alias mvn11=" ~/.bashrc; then
    echo "add alias mvn11='JAVA_HOME=${JAVA_HOME} ${MAVEN_HOME}/bin/mvn' to ~/.bashrc"
    echo "alias mvn11='JAVA_HOME=${JAVA_HOME} ${MAVEN_HOME}/bin/mvn'" >> ~/.bashrc
fi

source ~/.bashrc
echo "Maven has been installed successfully to $INSTALL_DIR and environment variables have been set."