# 简介

安全编译选项是防护缓冲区溢出攻击的重要手段，Binscope是一款通过扫描二进制文件并判断该文件在编译阶段是否按照规范添加了相关安全编译选项的工具。工具支持C/C++/GO/RUST语言的常规编译产物进行扫描，即ELF格式（Linux平台）和PE格式（Windows平台）的文件。

>**说明：** 
>工具只支持运行在Linux平台，有x86-64和Aarch64两种版本；可对ELF格式（Linux平台）和PE格式（Windows平台)的文件进行扫描。

**Binscope\_For\_Linux实现原理**

C/C++/GO/RUST代码在编译阶段添加了安全相关编译选项后生成的二进制文件会具有相应的二进制特征，这些特征使程序在运行时更加安全且难以被攻击利用。Binscope工具通过解析二进制文件中是否具有相应安全特征来判断该程序在编译阶段有没有添加安全编译选项。



# 功能说明

## 支持扫描的文件格式以及选项

Binscope支持ELF格式和PE格式文件的安全编译选项实施结果的扫描：

-   ELF格式文件为Linux平台下的可执行文件、动态库.so文件、可重定位文件.o/.ko文件以及一些实时操作系统用到的.elf后缀文件。

    Linux平台的静态库.a文件为非ELF格式文件，但因它是多个.o文件（ELF格式文件）的简单归档，Binscope也会对其解包并扫描；可通过命令行参数来控制是否展示.a文件中每个.o文件的扫描结果。

-   PE格式文件为Windows平台下的可执行.exe文件、动态库.dll文件。

>**说明：** 
>ELF格式和PE格式文件指的是按照特定规则格式存储的文件，和文件后缀并不是强相关。判断文件是否为ELF/PE格式文件，可以借助Linux命令“**file** _<filename\>_”返回信息判断文件实际格式。



### ELF格式文件相关编译选项

**表 1**  ELF格式文件支持扫描的编译选项说明

|选项名|对应的编译选项|编译选项解释|
|--|--|--|
|Stack Protect|-fstack-protector-strong/-fstack-protector-all|栈保护选项，简称为SP选项。|
|RELRO|-Wl,-z,relro,-z,now|分为 “RELRO” 和 “BIND_NOW”两个选项。<br/>-Wl,-z,relro 为GOT表保护选项，又称“RELRO”。<br/>-Wl,-z,now 为立即加载选项，又称“BIND_NOW”。|
|NX|-Wl,-z,noexecstack|堆栈不可执行选项，简称为NX选项。|
|PIC|-fPIC|地址无关代码选项。适用于动态库。|
|PIE|-fPIE -pie|地址无关可执行选项。适用于可执行文件。|
|No Rpath/Runpath|-Wl,-rpath|动态库搜索路径选项。此选项为禁选项，即用了该选项扫描会不通过。|
|Strip|-s|删除符号表选项。|
|Fortify Source|-D_FORTIFY_SOURCE=2 -O2|危险函数替换选项，简称为FS选项。|
|Integer Overflows|-ftrapv|整数溢出检测选项。该选项不支持检测clang编译器编译的产物。|
|LLVMCFI|-fsanitize=cfi -flto -fuse-ld=gold -fvisibility=hidden|llvm提供的控制流保护方案。该选项暂不支持powerpc/powerpc64架构下编译产物检测。|


### PE格式文件相关编译选项

**表 1**  PE格式文件相关编译选项说明

|选项名|对应的编译选项|编译选项解释|
|--|--|--|
|Stack Protect|-fstack-protector-strong/-fstack-protector-all|MinGW编译器的栈保护选项，Visual Studio编译器提供的/GS选项暂不支持。|
|NX|-Wl,--nxcompat/NXCOMPAT|堆栈不可执行选项。|
|DynamicBase|-Wl,--dynamicbase/DYNAMICBASE|地址随机化选项。|
|SafeSEH|-Wl,--no-she/SAFESEH|开启安全异常中断处理函数选项。|


## 支持扫描的文件

-   BinScope支持扫描的ELF文件包括：
    -   C/C++代码通过gcc/clang编译器在x86/ARM/powerpc架构下生成的32位/64位目标文件。
    -   RUST代码在x86/ARM/powerpc架构下生成的32位/64位目标文件。
    -   Go语言代码在x86/ARM架构下生成的32位/64位目标文件。

-   BinScope支持扫描的PE文件包括：
    -   C/C++代码通过mingw/Visual Studio编译生成的32位/64位目标文件。
    -   Go/RUST代码编译生成的32位/64位目标文件。

## 扫描结果呈现方式

Binscope支持将扫描结果直接打印在屏幕上，也支持输出JSON格式的报告，且两种方式均可通过工具参数控制。详情请参见下文的“Binscope扫描报告介绍”章节。

## 约束与限制

待扫描的文件大小上限为1G，待扫描的目录下ELF文件和PE文件的总文件数上限为500000个。



# 下载及部署

**环境要求**

|类型|说明|
|--|--|
|操作系统|Linux平台|
|服务器架构|x86-64、Aarch64|
|Glibc|2.17及以上版本|
|可用内存|4G及以上|

**获取软件包**

|软件包名称|链接|
|:-|--|
|binscope_aarch64.tar.gz| https://gitee.com/openeuler/devkit-pipeline/releases/download/v1.0.4/binscope_aarch64.tar.gz |
|binscope_x86_64.tar.gz|https://gitee.com/openeuler/devkit-pipeline/releases/download/v1.0.4/binscope_x86_64.tar.gz|

**部署**

Binscope本身为单机命令行工具，无需安装，下载后解压至需求目录后，执行**chmod +x binscope**命令赋予可执行权限即可使用。



# Binscope工具使用


## 查看帮助信息

**命令功能**

Binscope作为一个命令行工具，通过命令行拼接相关选项参数来进行扫描操作。

**命令格式**

```
binscope {-h|--help}
```

**使用示例**

执行以下命令，查看支持的功能信息：

```
./binscope -h
```

返回信息如下：

```
Usage: binscope {-d|-f} <path> [-asj]...
       binscope {-h|--help}
       binscope {-v|--version}

    -h, --help              Print help message
    -v, --version           Print version info
    -d, --dir=<path>        Set directory that binary file located.
                            Multiple paths are supported and separated by ','.
    -f, --file=<path>       Set binary file path.
                            Multiple file are supported and separated by ','.
    -e, --exclude=<path>    Set exclude directory.
                            Multiple paths are supported and separated by ','.
                            If the scan directory is an absolute path, the exclude path must also be an absolute path.  Otherwise, the exclude option does not take effect.
    -o, --out=<path>        Set the directory that output result located.
    -x, --excludeSuffix     Exclude file with suffix.
    -a, --afile             Show detail of archive file.
    -s, --silent            Turn off screen printing.
    -j, --json              Turn on JSON file output.
    -g, --log               Enable detailed logging.
```



## 使用示例

**参数说明**

**表 1**  参数说明

|参数|参数选项|参数说明|
|--|--|--|
|-v/--version|-|打印工具的版本信息。|
|-h/--help|-|打印工具的帮助信息。|
|-d/--dir|*<path>*|设置要扫描的目录的路径，支持多个目录，多个目录路径之间用英文逗号隔开。|
|-f/--file|*<filename>*|设置要扫描的文件的路径，支持多个文件，多个文件路径之间用英文逗号隔开。|
|-e/--exclude|*<path>*|设置要排除的目录的路径，支持多个目录，多个目录路径之间用英文逗号隔开，不支持正则。如果被扫描的路径是绝对路径，那么要排除的路径也必须是绝对路径，否则该选项不生效。（该选项需要与-d选项一起使用才会生效）|
|-j/--json|-|设置是否额外输出JSON格式的报告文件，默认状态下（即不使用-j选项时）不会输出JSON报告。|
|-o/--out|*<path>*|设置扫描结果文件存放的目录，默认输出在当前目录下，该选项需要与-j选项一起使用才会生效。|
|-x/--excludeSuffix|*<file_suffix>*|设置需要排除扫描的文件的后缀，设置后以该后缀结尾的文件都不会被扫描。|
|-a/--afile|-|设置是否单独显示归档文件（一般是静态库.a或.lib）中每个.o文件的扫描结果，默认只显示最终.a的结果。|
|-s/--silent|-|设置是否不在终端界面打印扫描结果，默认状态下（即不使用-s选项）会在终端界面打印扫描结果。|
|-g/--log|-|打开详细打印开关，常用于开发进行问题定位。默认状态下（即不使用-g选项）关闭。|

**对目录进行扫描**

对目录进行扫描。

```
 ./binscope -d BinaryFile
```

返回信息如下：

```
ElfFiles:

Check file: "BinaryFile/elfFile/arm64/fortify_source_ind"
* Stack Protect     : PASS
  description       : Stack Protector Feature Found.
* RELRO             : PASS
  description       : Full RELRO.
* NX                : PASS
  description       : Stack Has Not Executable Permission.
* PIC               : N/A
  description       : Not Shared Object.
* PIE               : PASS
  description       : Position Independent Executable.
* No Rpath/Runpath  : PASS
  description       : No RPATH/RUNPATH Feature Found.
* Strip             : FAIL
  description       : Static Symbol Table Found.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : IND
  description       : Indeterminate, No Ftrapv Feature Found.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
...
...
...
PeFiles:
 
Check file: "BinaryFile/peFile/win64/go/all_fail.exe"
* Stack Protect     : IND
  description       : No Stack Protector Feature Found.
* NX                : FAIL
  description       : No NxCompat Feature Found.
* DynamicBase       : FAIL
  description       : No DYNAMICBASE Feature Found.
* SafeSEH           : PASS
  description       : x86_64 Does Not Involve SafeSEH Feature.
```

**对文件进行扫描**

```
 ./binscope -f BinaryFile/strip_pass
```

返回信息如下：

```
ElfFiles:

Check file: "BinaryFile/strip_pass"
* Stack Protect     : IND
  description       : Stack Protector Feature Not Found.
* RELRO             : PASS
  description       : Full RELRO.
* NX                : PASS
  description       : Stack Has Not Executable Permission.
* PIC               : N/A
  description       : Not Shared Object.
* PIE               : PASS
  description       : Position Independent Executable.
* No Rpath/Runpath  : PASS
  description       : No RPATH/RUNPATH Feature Found.
* Strip             : PASS
  description       : No Static Symbol Table Found.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : N/A
  description       : Release Version Does Not Involve Ftrapv.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
```

**对目录扫描时排除某些目录**

```
 ./binscope -d BinaryFile -e BinaryFile/elfFile/
```

返回信息如下：

```
PeFiles:
 
Check file: "BinaryFile/peFile/win64/go/all_fail.exe"
* Stack Protect     : IND
  description       : No Stack Protector Feature Found.
* NX                : FAIL
  description       : No NxCompat Feature Found.
* DynamicBase       : FAIL
  description       : No DYNAMICBASE Feature Found.
* SafeSEH           : PASS
  description       : x86_64 Does Not Involve SafeSEH Feature.
...
...
...
Check file: "BinaryFile/peFile/win64/vs/VS_X64_NXCOMPAT_FAIL.exe"
* Stack Protect     : IND
  description       : No Stack Protector Feature Found.
* NX                : FAIL
  description       : No NxCompat Feature Found.
* DynamicBase       : PASS
  description       : DYNAMICBASE Feature Found.
* SafeSEH           : PASS
  description       : x86_64 Does Not Involve SafeSEH Feature.
```

**设置扫描结果数据格式和输出目录**

```
 ./binscope -d test/ -j -o output/
ll output/
```

返回信息如下：

```
total 8
-r-------- 1 root root 4553 Jul 31 11:06 binscope.json
```

**对目录扫描时排除某些后缀的文件**

```
 ./binscope -d BinaryFile/elfFile/arm64 -x .ko,.so
```

返回信息如下：

```
ElfFiles:
 
Check file: "BinaryFile/elfFile/arm64/fortify_source_ind"
* Stack Protect     : PASS
  description       : Stack Protector Feature Found.
* RELRO             : PASS
  description       : Full RELRO.
* NX                : PASS
  description       : Stack Has Not Executable Permission.
* PIC               : N/A
  description       : Not Shared Object.
* PIE               : PASS
  description       : Position Independent Executable.
* No Rpath/Runpath  : PASS
  description       : No RPATH/RUNPATH Feature Found.
* Strip             : FAIL
  description       : Static Symbol Table Found.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : IND
  description       : Indeterminate, No Ftrapv Feature Found.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
...
...
...
Check file: "BinaryFile/elfFile/arm64/strip_pass"
* Stack Protect     : IND
  description       : Stack Protector Feature Not Found.
* RELRO             : PASS
  description       : Full RELRO.
* NX                : PASS
  description       : Stack Has Not Executable Permission.
* PIC               : N/A
  description       : Not Shared Object.
* PIE               : PASS
  description       : Position Independent Executable.
* No Rpath/Runpath  : PASS
  description       : No RPATH/RUNPATH Feature Found.
* Strip             : PASS
  description       : No Static Symbol Table Found.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : N/A
  description       : Release Version Does Not Involve Ftrapv.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
```

**单独显示归档文件的扫描结果**

对文件扫描时，单独显示归档文件(一般是静态库.a或.lib)中每个.o文件的扫描结果。

```
 ./binscope -f BinaryFile/elfFile/afile/libssl.a -a
```

返回信息如下：

```
ElfFiles:
 
Check file: "BinaryFile/elfFile/afile/libssl.a(libssl-lib-bio_ssl.o)"
* Stack Protect     : IND
  description       : Stack Protector Feature Not Found.
* RELRO             : N/A
  description       : The File Is Not Linked.
* NX                : N/A
  description       : The File Is Not Linked.
* PIC               : N/A
  description       : The File Is Not Linked.
* PIE               : N/A
  description       : The File Is Not An Executable.
* No Rpath/Runpath  : N/A
  description       : The File Is Not Linked.
* Strip             : N/A
  description       : The File Is Not Linked.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : IND
  description       : Indeterminate, No Ftrapv Feature Found.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
...
...
...
Check file: "BinaryFile/elfFile/afile/libssl.a(libssl-lib-statem_srvr.o)"
* Stack Protect     : PASS
  description       : Stack Protector Feature Found.
* RELRO             : N/A
  description       : The File Is Not Linked.
* NX                : N/A
  description       : The File Is Not Linked.
* PIC               : N/A
  description       : The File Is Not Linked.
* PIE               : N/A
  description       : The File Is Not An Executable.
* No Rpath/Runpath  : N/A
  description       : The File Is Not Linked.
* Strip             : N/A
  description       : The File Is Not Linked.
* Fortify Source    : IND
  description       : Indeterminate, No FORTIFY_SOURCE Feature Found.
* Integer Overflows : IND
  description       : Indeterminate, No Ftrapv Feature Found.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
```

## Binscope扫描报告介绍

Binscope支持两种方式显示扫描结果，一种为直接打印在屏幕上，以文字的方式逐个显示文件的扫描结果，另一种为以JSON文件的方式存储扫描结果。




### 扫描结果状态解释

每一种编译选项的扫描结果有以下四种：

**表 1**  扫描结果说明

|扫描结果|说明|
|--|--|
|PASS|代表扫描通过，不需要整改。|
|N/A|代表不涉及，不需要整改。|
|IND|代表不确定，仍需自行确认。|
|FAIL|代表扫描不通过，需要整改。|


-   结果为N/A的场景：
    1.  非动态库文件的PIC扫描结果都为不涉及N/A；
    2.  非可执行文件的PIE扫描结果都为不涉及N/A；
    3.  可重定位文件等未经过链接的文件的链接选项的扫描结果都为不涉及N/A。

-   结果为IND的场景：
    1.  符号表中未找到栈保护选项（Stack Protect）特征，且无其他信息进行补充判断的，基于栈保护选项生效原理，此种情况扫描结果为不确定IND；
    2.  符号表中未找到Fortify Source选项特征，且无其他信息进行补充判断的，基于该选项生效原理，此种情况扫描结果为不确定IND；
    3.  符号表中未找到Integer Overflows选项特征，且无其他信息进行补充判断的，基于该选项生效原理，此种情况扫描结果为不确定IND。

### 屏幕打印的扫描报告

直接输出在终端的结果信息一般适合在本地进行少量文件的结果展示，文件较多时会大量占用屏幕空间，影响阅读体验。可通过选项“-s/--silent”关闭直接打印在终端的输出方式。

在输出结果中，每一项不同的结果会以不同颜色进行显示，其中代表不通过的IND和FAIL会闪烁提示。以上颜色及闪烁效果需要终端支持。

示例如下：

```
./binscope -f BinaryFile/elfFile/arm64/fortify_source_pass_symtab
```

返回信息如下：

```
ElfFiles:

Check file: "BinaryFile/elfFile/arm64/fortify_source_pass_symtab"
* Stack Protect     : PASS
  description       : Stack Protector Feature Found.
* RELRO             : PASS
  description       : Full RELRO.
* NX                : PASS
  description       : Stack Has Not Executable Permission.
* PIC               : N/A
  description       : Not Shared Object.
* PIE               : PASS
  description       : Position Independent Executable.
* No Rpath/Runpath  : PASS
  description       : No RPATH/RUNPATH Feature Found.
* Strip             : FAIL
  description       : Static Symbol Table Found.
* Fortify Source    : PASS
  description       : FORTIFY_SOURCE Feature Found.
* Integer Overflows : IND
  description       : Indeterminate, No Ftrapv Feature Found.
* LLVMCFI           : IND
  description       : Indeterminate, No LLVM CFI Feature Found.
```

> **说明：** 
>每个\*号代表单独一项的扫描结果，\*号后为编译选项名称，如Stack Protect、RELRO，编译选项冒号后面为扫描结果，description是对编译选项的简单描述。

### JSON文件存储报告

Binscope工具支持将扫描报告存储在JSON文件中，便于同其他平台对接。其他平台在调用了Binscope工具后，可读取JSON文件获取扫描结果。Binscope工具的JSON报告中有四个JSON数组，分别为ELF文件的“ElfFeatures”、“ElfFils”和PE文件的“PeFeatures”和“PeFiles”。

**表 1**  JSON文件说明

|数组/字段名|说明|
|--|--|
|ElfFeatures|显示会扫描ELF文件的特性。|
|ElfFiles|显示扫描到的ELF格式文件的扫描结果。|
|PeFeatures|显示会扫描PE文件的特性。|
|PeFiles|显示扫描到的PE格式文件的扫描结果。|
|command|存储调用Binscope扫描时执行的命令|
|time|存储扫描时间。|
|version|存储当前的版本。|


>**说明：** 
>其中ElfFiles和PeFiles数组的成员是每一个文件扫描结果的JSON对象，该对象包含对应文件各个编译选项扫描结果的features数组、存储文件路径的“path”以及存储文件哈希值的“sha256”字段。features数组的每一个成员代表着一个选项的扫描结果，其中name字段表示编译选项名称，result字段表示扫描结果，description表示对扫描结果的简单描述。

JSON文件示例如下：

```
{
    "ElfFeatures": [
        "Stack Protect",
        "RELRO",
        "NX",
        "PIC",
        "PIE",
        "No Rpath/Runpath",
        "Strip",
        "Fortify Source",
        "Integer Overflows",
        "LLVMCFI"
    ],
    "ElfFiles": [
        {
            "features": [
                {
                    "description": "Stack Protector Feature Found.",
                    "name": "Stack Protect",
                    "result": "PASS"
                },
                {
                    "description": "Full RELRO.",
                    "name": "RELRO",
                    "result": "PASS"
                },
                {
                    "description": "Stack Has Not Executable Permission.",
                    "name": "NX",
                    "result": "PASS"
                },
                {
                    "description": "Not Shared Object.",
                    "name": "PIC",
                    "result": "N/A"
                },
                {
                    "description": "Position Independent Executable.",
                    "name": "PIE",
                    "result": "PASS"
                },
                {
                    "description": "No RPATH/RUNPATH Feature Found.",
                    "name": "No Rpath/Runpath",
                    "result": "PASS"
                },
                {
                    "description": "No Static Symbol Table Found.",
                    "name": "Strip",
                    "result": "PASS"
                },
                {
                    "description": "FORTIFY_SOURCE Feature Found.",
                    "name": "Fortify Source",
                    "result": "PASS"
                },
                {
                    "description": "Release Version Does Not Involve Ftrapv.",
                    "name": "Integer Overflows",
                    "result": "N/A"
                },
                {
                    "description": "Indeterminate, No LLVM CFI Feature Found.",
                    "name": "LLVMCFI",
                    "result": "IND"
                }
            ],
            "path": "test/elf_file1",
            "sha256": "fd1e4e211bf605559c83756229ba2e4e949cab6aeae97c2cde699e0dba281ebf"
        }
    ],
    "PeFeatures": [
        "Stack Protect",
        "NX",
        "DynamicBase",
        "SafeSEH"
    ],
    "PeFiles": [],
    "command": "./binscope -d test/ -j",
    "time": "2024-07-31 14:10:39",
    "version": "BinScope 24.0.0"
}
```

# 安全管理与加固

**防病毒软件例行检查**

定期开展对集群的防病毒扫描，防病毒例行检查会帮助集群免受病毒、恶意代码、间谍软件以及程序侵害，降低系统瘫痪、信息泄露等风险。可以使用业界主流防病毒软件进行防病毒检查。

**漏洞修复**

为保证生产环境的安全，降低被攻击的风险，请定期修复以下漏洞：

-   操作系统漏洞
-   OpenSSL漏洞
-   glibc漏洞
-   libstdc++漏洞
-   JSON for Modern C++漏洞
-   elfio漏洞

# FAQ

**Binscope的安装文件只有一个binscope文件？**

Binscope为一个可执行文件，只依赖libm.so、ld-linux-aarch64.so.1和libc.so三个系统基础库，这些库作为Linux系统的基础库，每个发行版Linux系统上都有，不需要一起打包发布。

**Binscope运行时报错'GLIBC\_2.17' not found，该如何解决？**

Binscope依赖环境上的系统基础库libc.so，而这个库中部分符号是具有版本的，出现该报错说明这个库的版本不兼容，请升级Glibc版本到2.17及以上版本。libc库跟系统强相关，如果libc版本不支持则该环境不支持运行Binscope。

**Binscope对软链接文件、软链接目录如何处理的？**

Binscope不处理软链接文件、软链接目录，遇见软链接文件直接跳过、软链接目录不再进行遍历。因为如果软链接文件/目录指向的是扫描目录下的文件/目录，则会造成重复扫描，如果指向的是扫描目录外的文件，则没必要扫描，同时还有可能泄露运行环境上文件信息，存在安全风险。

**Binscope运行时提示“no binary file!”？**

当扫描的文件不是ELF/PE格式文件，或扫描的目录下找不到ELF/PE格式文件时会提示该信息。可通过"file _{filename}_"命令判断该文件的格式。

# 修订记录

|**文档版本**|**发布日期**|**修改说明**|
|--|--|--|
|01|2024-09-30|第一次正式发布。|

