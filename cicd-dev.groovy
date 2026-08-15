node('linux') {
    stage('Build') {
        build job: 'Port-Pipeline',
            parameters: [
                string(name: 'PORT_GITHUB_REPO', value: 'https://github.com/zopencommunity/maturinport'),
                string(name: 'PORT_DESCRIPTION', value: 'maturin - Build and publish Rust-backed Python wheels'),
                string(name: 'BUILD_LINE', value: 'DEV')
            ]
    }
}
