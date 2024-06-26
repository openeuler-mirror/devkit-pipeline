##            

### 1. 创建dockerfile，内容例如：

注意：RUN命令 执行了yum install 命令 确保git、wget、rubygems和make命令存在，如果你的镜像中存在这些依赖，可以直接删除dockerfile中的yum命令。
如果你的镜像中不存在这些依赖，请确保yum命令可用。

```dockerfile
from openeuler-20.03-lts-sp2:latest
WORKDIR  /root
ADD bisheng-jdk-17.0.10-linux-aarch64.tar.gz /root/.local/
ADD lkp-tests.tar.gz /root/.local/
ADD devkit_distribute.tar.gz /root/.local/
ADD compatibility_testing.tar.gz /root/.local/
COPY gem_dependencies.zip  /usr/share/gems/gems/gem_dependencies.zip
RUN  yum install -y git rubygems make  && \
  cd /usr/share/gems/gems && unzip gem_dependencies.zip && \
  cd /usr/share/gems/gems/gem_dependencies && \
  gems_name=(zeitwerk-2.6.5.gem unicode-display_width-2.5.0.gem tzinfo-2.0.5.gem tins-1.31.1.gem term-ansicolor-1.7.1.gem sync-0.5.0.gem \
simplecov-rcov-0.3.1.gem simplecov-html-0.12.3.gem simplecov-0.21.2.gem simplecov_json_formatter-0.1.4.gem ruby-progressbar-1.11.0.gem \
rubocop-ast-1.17.0.gem rubocop-1.12.1.gem rspec-support-3.12.0.gem rspec-mocks-3.12.0.gem rspec-expectations-3.12.0.gem \
rspec-core-3.12.0.gem rspec-3.12.0.gem rexml-3.2.5.gem regexp_parser-2.6.0.gem rchardet-1.8.0.gem rainbow-3.1.1.gem public_suffix-4.0.7.gem \
parser-3.1.2.1.gem parallel-1.22.1.gem minitest-5.15.0.gem i18n-1.12.0.gem gnuplot-2.6.2.gem git-1.7.0.gem docile-1.4.0.gem diff-lcs-1.5.0.gem \
concurrent-ruby-1.1.10.gem ci_reporter-2.0.0.gem bundler-2.2.33.gem builder-3.2.4.gem ast-2.4.2.gem activesupport-6.1.7.gem) && \
  for each in "${gems_name[@]}"; do \
    gem install --local ${each}; \
  done && \
  cd /root/.local/lkp-tests/ && \
  chmod +x /root/.local/lkp-tests/bin/lkp && \
  make && \
  chmod 777 /root/.local/lkp-tests/programs/compatibility-test/run && \
  ln -s /root/.local/lkp-tests/programs/compatibility-test/run /root/.local/lkp-tests/tests/compatibility-test && \
  cd /root/.local/lkp-tests/programs/compatibility-test/ && \
  lkp split /root/.local/lkp-tests/programs/compatibility-test/jobs/compatibility-test.yaml && \
  chown -R root:root /root/.local/lkp-tests /root/.local/devkit_distribute /root/.local/bisheng-jdk-17.0.10 /root/.local/compatibility_testing
```

#### 下载包到同一目录

[下载毕昇JDK17](https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/bisheng-jdk-17.0.10-linux-aarch64.tar.gz)
![](00_下载资源01.png)
[测试平台依赖下载](https://gitee.com/openeuler/devkit-pipeline/releases/tag/v0.2)
![](00_下载资源02.png)

![](01_准备资源.png)

#### 执行构建命令

```commandline
docker build  -t mine_f2 -f devkit_pipeline.docker .
```

![](02_构建镜像.png)

![](03_构建成功.png)

#### 查看构建成功的镜像

![](04_查看构建成功的镜像.png)