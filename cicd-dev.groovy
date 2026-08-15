node('linux') {
    stage ('Poll') {
      checkout([
        $class: 'GitSCM', branches: [[name: '*/main']], extensions: [],
        userRemoteConfigs: [[url: 'https://github.com/zopencommunity/maturinport.git']]])
    }
    stage('Build') {
        build job: 'Port-Pipeline', parameters: [
            string(name: 'PORT_GITHUB_REPO', value: 'https://github.com/zopencommunity/maturinport.git'),
            string(name: 'PORT_DESCRIPTION', value: 'maturin - build and publish Rust-backed Python wheels'),
            string(name: 'BUILD_LINE', value: 'DEV'),
            booleanParam(name: 'PUBLISH_PYTHON_WHEEL', value: true)
        ]
    }
}
