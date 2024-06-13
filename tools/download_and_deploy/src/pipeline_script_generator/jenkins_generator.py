from pipeline_script_generator.script_generator import ScriptGenerator


class JenkinsScript(ScriptGenerator):
    name = "jenkins"
    seprator = "/"
    base_template = """
def get_code(GIT_BRANCH, GIT_TARGET_DIR_NAME, GIT_URL) {
    sh '''
    rm -fr "${GIT_TARGET_DIR_NAME}"
    '''
    checkout scmGit(branches: [[name: "*/${GIT_BRANCH}"]], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${GIT_TARGET_DIR_NAME}"], cleanBeforeCheckout(deleteUntrackedNestedRepositories: true)],userRemoteConfigs: [[url: "${GIT_URL}"]])
    sh '''
    rm -rf ./report_dir
    mkdir ./report_dir
    '''
}
    
pipeline {
    agent none
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    environment {
        // 获取源码参数
        // 用于指定Jenkins应该使用哪个凭据来访问源代码存储库，是凭据管理中“唯一标识”的值
        CREDENTIALS_ID = ""
        // 源代码存储库的克隆地址
        GIT_URL = ""
        // 源代码存储库的克隆分支
        GIT_BRANCH = "master"
        // 源代码存储库代码克隆到本地后存储的目录文件名称
        GIT_TARGET_DIR_NAME = ""

        // 源码迁移参数
        // 源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
        SOURCE_CODE_COMMAND = "make"

        // 软件迁移评估参数
        // 被扫描的软件包的路径
        SOFTWARE_PATH = ""

        // 字节对齐检查参数
        // 源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
        BYTE_ALIGNMENT_COMMAND = "make"
        // 构建工具，当前工具支持make、cmake和automake，默认选项为make
        BYTE_ALIGNMENT_TOOL = "make"

        // 内存一致性检查参数
        // BC文件路径
        MEMORY_BC_FILE = ""

        // 向量化检查参数
        // BC文件路径
        VECTORIZED_BC_FILE = ""
        //  源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
        VECTORIZED_COMMAND = "make"

        // Java Performance Analysis功能参数
        // 需要采集的目标程序所在的服务器地址， 多个使用逗号隔离
        JAVA_TARGET_SERVER_IP = ""
        // 需要采集的应用名称，多个采用逗号隔离
        JAVA_APPLICATION_NAME = ""
        // 采集目标应用时间，单位秒。当存在-j参数时，jmeter结束或者到达采集执行时间，结束采集
        JAVA_COLLECTION_APPLICATION_DURATION = ""
        // jmeter执行命令。例如：bash /opt/apache-jmeter-5.6.3/bin/jmeter.sh -nt /home/xxx/Request.jmx -l /home/xxx/result.html -eo /home/xxx/report
        JAVA_JMETER_COMMAND = ""
        // jmeter -l参数，将测试结果以指定的文件名保存到本地
        JAVA_JMETER_RESULT_FILE_NAME = ""
        // jmeter -o参数，jmeter运行结束后生成的测试报告文件路径
        JAVA_JMETER_RESULT_REPORT_DIR = ""
        // Devkit工具部署的环境IP
        DEVKIT_WEB_IP = ""

        // 编译参数
        // 编译命令
        BUILD_COMMAND = '''
        
        '''
        //A-FOT配置文件存放的路径
        A_FOT_CONF_PATH = ""
        
        //病毒扫描路径
        CLAMAV_PATH = ""

    }
    stages{
##STAGES##
    }
}
"""
    source_migration_template = """
        // 源码迁移
        stage('source-code-migration') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                      devkit porting src-mig -i "./${GIT_TARGET_DIR_NAME}" -c "${SOURCE_CODE_COMMAND}" -r html -o ./report_dir
                                      #  devkit porting src-mig -i "./${GIT_TARGET_DIR_NAME}" -s  interpreted -r html -o ./report_dir
                                      #  devkit porting src-mig -i "./${GIT_TARGET_DIR_NAME}" -c "${SOURCE_CODE_COMMAND}" -s asm -r html -o ./report_dir
                                      #  devkit porting src-mig -i "./${GIT_TARGET_DIR_NAME}" -s  go -r html -o ./report_dir
                                        ''')
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【源码迁移】--> 无扫描建议 <--'
                            break
                        case 1:
                            currentBuild.result = 'UNSTABLE'
                            echo '【源码迁移】--> 扫描结果只存在建议项 <--'
                            break
                        case 3:
                            currentBuild.result = 'ABORTED'
                            echo '【源码迁移】--> 扫描结果超时 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【源码迁移】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【源码迁移】--> 扫描结果存在必须修改项 <--'
                            error('【源码迁移】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【源码迁移】--> 异常终断开{Ctrl + C | Ctrl + Z} <--'
                            break
                        }
                }
                sh '''
                    html_file_name=$(find ./report_dir -name src-mig*.html)
                    if [[ ${html_file_name} ]]; then 
                        mv ${html_file_name} ./report_dir/SourceCodeScanningReport.html
                    fi
                '''
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : 'SourceCodeScanningReport.html',
                                reportName           : 'Source Code Scanning Report']
                                )
                }
            }
        }
"""
    package_migration_template = """
        // 软件迁移评估
        stage('software-migration-assessment') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                        devkit porting pkg-mig -i "${SOFTWARE_PATH}" -r html -o ./report_dir

                                        ''')
                    sh '''
                        html_file_name=$(find ./report_dir -name pkg-mig*.html)
                        if [[ ${html_file_name} ]]; then 
                            mv ${html_file_name} ./report_dir/SoftwareMigrationAssessment.html
                        fi
                    '''
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【软件迁移评估】--> 无扫描建议 <--'
                            break
                        case 1:
                            currentBuild.result = 'UNSTABLE'
                            echo '【软件迁移评估】--> 扫描结果只存在建议项 <--'
                            break
                        case 3:
                            currentBuild.result = 'ABORTED'
                            echo '【软件迁移评估】--> 扫描结果超时 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【软件迁移评估】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【软件迁移评估】--> 扫描结果存在必须修改项 <--'
                            error('【软件迁移评估】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【软件迁移评估】--> 异常终断开{Ctrl + C | Ctrl + Z} <--'
                            break
                        }
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : 'SoftwareMigrationAssessment.html',
                                reportName           :  'SoftwareMigrationAssessment Report']
                                )
                }
            }
        }
"""
    mode_check_template = """
        // 64位运行模式检查
        stage('64-bit-running-mode-check') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                        devkit advisor mode-check -i "./${GIT_TARGET_DIR_NAME}" -r html -o ./report_dir

                                        ''')
                    sh '''
                        html_file_name=$(find ./report_dir -name mode_check*.html)
                        if [[ ${html_file_name} ]]; then 
                            mv ${html_file_name} ./report_dir/64-bit-running-mode-check.html
                        fi
                    '''
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【64位运行模式检查】--> 无扫描建议 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【64位运行模式检查】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【64位运行模式检查】--> 扫描结果存在必须修改项 <--'
                            error('【64位运行模式检查】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【64位运行模式检查】--> 扫描失败<--'
                            break
                        }
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : '64-bit-running-mode-check.html',
                                reportName           : '64-bit-running-mode-check Report']
                                )
                }
            }
        }
"""
    byte_alignment_template = """
        // 字节对齐检查
        stage('byte-alignment-check') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                        devkit advisor byte-align -i "./${GIT_TARGET_DIR_NAME}" -c "${BYTE_ALIGNMENT_COMMAND}" -b "${BYTE_ALIGNMENT_TOOL}" -r html -o ./report_dir

                                        ''')
                    sh '''
                        html_file_name=$(find ./report_dir -name byte-align*.html)
                        if [[ ${html_file_name} ]]; then 
                            mv ${html_file_name} ./report_dir/byte-alignment-check.html
                        fi
                    '''
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【字节对齐检查】--> 无扫描建议 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【字节对齐检查】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【字节对齐检查】--> 扫描结果存在必须修改项 <--'
                            error('【字节对齐检查】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【字节对齐检查】--> 扫描失败<--'
                            break
                        }
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : 'byte-alignment-check.html',
                                reportName           : 'byte-alignment-check Report']
                                )
                }
            }
        }
"""
    memory_consistency_template = """
        // 内存一致性检查
        stage('memory-consistency-check') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                 get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                 sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                          devkit advisor mem-cons -i "./${GIT_TARGET_DIR_NAME}" -f "${MEMORY_BC_FILE}" -r html -o ./report_dir

                                        ''')
                    sh '''
                        html_file_name=$(find ./report_dir -name mem-cons*.html)
                        if [[ ${html_file_name} ]]; then 
                            mv ${html_file_name} ./report_dir/memory-consistency-check.html
                        fi
                    '''
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【内存一致性检查】--> 无扫描建议 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【内存一致性检查】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【内存一致性检查】--> 扫描结果存在必须修改项 <--'
                            error('【内存一致性检查】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【内存一致性检查】--> 扫描失败<--'
                            break
                        }
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : 'memory-consistency-check.html',
                                reportName           : 'memory-consistency-check Report']
                                )
                }
            }
        }
"""
    vector_check_template = """
        // 向量化检查
        stage('vectorized-check') {
            agent {
                label 'kunpeng_scanner'
            }
            steps {
                 get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                 sh '''
                    /usr/bin/rm -rf ./report_dir/*.html
                '''
                script{
                    def STATUS_CODE = sh(returnStatus: true, script: '''
                                           devkit advisor vec-check -i "./${GIT_TARGET_DIR_NAME}" -f "${VECTORIZED_BC_FILE}" -c "${VECTORIZED_COMMAND}" -r html -o ./report_dir

                                        ''')
                    sh '''
                        html_file_name=$(find ./report_dir -name vec-check*.html)
                        if [[ ${html_file_name} ]]; then 
                            mv ${html_file_name} ./report_dir/vectorized-check.html
                        fi
                    '''
                   switch(STATUS_CODE) {
                        case 0:
                            echo '【向量化检查】--> 无扫描建议 <--'
                            break
                        case 4:
                            currentBuild.result = 'ABORTED'
                            echo '【向量化检查】--> 扫描命令错误 <--'
                            break
                        case 5:
                            currentBuild.result = 'FAILURE'
                            echo '【向量化检查】--> 扫描结果存在必须修改项 <--'
                            error('【向量化检查】--> 扫描结果存在必须修改项 <--')
                            break
                        default:
                            currentBuild.result = 'ABORTED'
                            echo '【向量化检查】--> 扫描失败<--'
                            break
                        }
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                alwaysLinkToLastBuild: false,
                                keepAll              : true,
                                reportDir            : './report_dir',
                                reportFiles          : 'vectorized-check.html',
                                reportName           : 'vectorized-check Report']
                                )
                }
            }
        }
"""
    java8_build_template = """
        // 普通编译 
        stage('java8-build') {
            agent {
                label 'kunpeng_java_builder_jdk8'
            }
            steps {
            get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
            sh "${BUILD_COMMAND}"
            }
       }    
"""
    java17_build_template = """
            // 普通编译 
            stage('java17-build') {
                agent {
                    label 'kunpeng_java_builder_jdk17'
                }
                steps {
                get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
                sh "${BUILD_COMMAND}"
                }
           }    
"""
    gcc_template = """
        // 普通编译 
        stage('gcc-build') {
            agent {
                label 'kunpeng_c_builder_gcc'
            }
            steps {
            get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
            sh "${BUILD_COMMAND}"
            }
       }
"""
    bisheng_compiler_template = """
        // 使用毕昇编译器编译
        stage('bisheng-build') {
            agent {
                label 'kunpeng_c_builder_bisheng_compiler'
            }
            steps {
            get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
            sh ''' source ${HOME}/.local/wrap-bin/devkit_pipeline.sh '''
            sh "${BUILD_COMMAND}"
            
            }
       }
"""
    a_fot_template = """
        // 使用GCC for openEuler编译，使用A-FOT工具时，需要根据用户指南填写配置项
        stage('A-FOT') {
            agent {
                label 'kunpeng_c_builder_gcc'
            }
            steps {
            get_code("${GIT_BRANCH}", "${GIT_TARGET_DIR_NAME}", "${GIT_URL}")
            sh ''' 
            export PATH=${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin:$PATH
            a-fot --config_file "${A_FOT_CONF_PATH}"/a-fot.ini
            '''
            }
       }
"""
    java_perf_template = """

"""
    compatibility_test_template = """
        // 鲲鹏兼容测试
        stage('compatibility_test') {
            agent {
                label 'kunpeng_executor'
            }
            steps {
                script{
                    sh '''
                        CURDIR=$(pwd)
                        cp -rf ${HOME}/.local/compatibility_testing/template.html.bak ${HOME}/.local/compatibility_testing/template.html
                        ${HOME}/.local/compatibility_testing/bin/compatibility_test 
                        cp -rf ${HOME}/.local/compatibility_testing/compatibility_report.html $CURDIR
                    '''
                    sh(script: "sudo bash ${HOME}/.local/compatibility_testing/report_result.sh", returnStdout:true).trim()
                }
            }
            post {
                always {
                    publishHTML(target: [allowMissing: false,
                                            alwaysLinkToLastBuild: false,
                                            keepAll              : true,
                                            reportDir            : '.',
                                            reportFiles          : 'compatibility_report.html',
                                            reportName           : 'compatibility test Report']
                    )
                }
            }        
        }
"""

    clamav_template = """
        //病毒扫描
        stage('clamav') {
            agent {label 'kunpeng_clamav'}
            steps {
                sh '''
                freshclam
                clamscan -i -r "${CLAMAV_PATH}" -l clamav.log
                '''
            }
        
        }    
"""