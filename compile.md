# Android系统编译
AOSP 的源码编译成系统并刷机

## 一、编译环境配置		
### 1. 操作系统	
编译所使用的系统 Ubuntu 20.04 LTS		
可以在虚拟机里安装也可以安装到物理机上	

安装必备的软件: git, curl		
sudo apt-get install git curl		

配置python 软件链接		
sudo unlink /usr/bin/python		
sudo ln -s /usr/bin/python3.8 /usr/bin/python		


### 2. 获取Android源代码		
#### 2.1 配置repo 环境		
mkdir \~/bin  <br>
echo "PATH=~/bin:\$PATH" >> \~/.bashrc	<br>
source \~/.bashrc		<br>

#### 2.2 初始化 repo 下载源		
##### 2.2.1 使用Google官方下载源
curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo	<br>
chmod a+x ~/bin/repo	
##### 2.2.2 使用国内的下载源
curl -sSL  'https://gerrit-googlesource.proxy.ustclug.org/git-repo/+/master/repo?format=TEXT' | base64 -d > ~/bin/repo 		<br>
chmod a+x ~/bin/repo

#### .3 repo同步源代码
创建工具目录
mkdir AndroidOS
cd AndroidOS

##### 2.3.1 使用Google源		
###### .3.1.1 repo 指定下载源的工作目录		
指定 master分支		
repo init -u https://android.googlesource.com/platform/manifest		<br>

使用 -b 来指定除master外的其他分支, 代号和细分版本号可查看链接: https://source.android.com/setup/start/build-numbers?hl=zh_cn	<br>	
repo init -u https://android.googlesource.com/platform/manifest -b android-10.0.0_r17		<br>
###### .3.1.2 下载代码		
repo sync		

##### 2.3.2 使用国内源		
###### 2.3.2.1  下载国内源码压缩包		
国内可用镜像源			<br>
清华镜像源: https://mirrors.tuna.tsinghua.edu.cn/help/AOSP/		<br>
中科大镜像源: http://mirrors.ustc.edu.cn/help/aosp.html		

wget https://mirrors.ustc.edu.cn/aosp-monthly/aosp-latest.tar				#  android代码的压缩包下载下来	<br>	
md5sum aosp-latest.tar									# 下载完进行检验,以保证文件下载完整	<br>
tar -xvf aosp-latest.tar								#  压所下载压缩包	<br>
##### 2.3.2.2 repo 指定下载源的工作目录	
repo init -u git://mirrors.ustc.edu.cn/aosp/platform/manifest -b android-10.0.0_r17	<br>
###### 2.3.2.3 下载代码	
export REPO_URL='https://gerrit-googlesource.proxy.ustclug.org/git-repo'	<br>
repo sync	<br>

注释：		<br>
Google官方源码编译文档: https://source.android.com/source/downloading	<br>	
	
repo sync的时候有可能会遇到的问题		<br>
info: A new version of repo is available		
repo: Updating release signing keys to keyset ver 2.3		<br>
warning: repo is not tracking a remote branch, so it will not receive updates	<br>	
repo reset: error: Entry '.github/workflows/test-ci.yml' not uptodate. Cannot merge.	<br>	
fatal: 不能重置索引文件至版本 'v2.16^0'。		<br>
解决方案：		<br>
cd \~/bin/aosp/.repo/repo		<br>
git pull		<br>
cd \~/bin/aosp		<br>
再次repo init 和 repo sync		<br>
	
## 二、编译系统	
### 1. 安装jdk	
sudo apt-get install openjdk-8-jdk	<br>
### 2. 安装所需依赖 (Ubuntu 20.04)		
sudo apt-get install git-core gnupg flex bison build-essential 		zip	 curl	 zlib1g-dev 	gcc-multilib		 g++-multilib  	libc6-dev-i386 	lib32ncurses5-dev 	x11proto-core-dev 	libx11-dev 	lib32z1-dev 	libgl1-mesa-dev 	libxml2-utils 	xsltproc 	unzip 	fontconfig 	libncurses5	<br>
(官方文档:https://source.android.com/setup/build/initializing?hl=zh-cn)	<br>
### 3. 设备驱动的准备		
谷歌手机设备驱动下载地址: https://developers.google.com/android/drivers	<br>	
下载的驱动要和要安装的 Pexel 手机要对应		<br>
下载完后解压驱动压缩包 到 AndroidOS/aosp目录		<br>
在 aosp 目录里执行上一步解密的两个驱动文件	<br>
如:		
./extract-google_devices-marlin.sh  	<br>
./extract-qcom-marlin.sh	<br>
执行到最后输入 I ACCEPT 就完成了		<br>
### 4. 编译		
#### 4.1 初次编译		
source build/envsetup.sh		<br>
lunch	# 选择设备内核和编译版本		<br>
make -j8			# -jN  N是所分配系统 CPU 核数 的2 倍	<br>	
#### 4.2 再次编译		
如果已经编译过一次但想更改过一些地方 如更换 Pixel 驱动, 这时没必要重新下载源码从头编译	<br>			
source build/envsetup.sh		<br>
lunch				# 备内核和编译版本		<br>
make update-api 		# 所有api		<br>
make -j8			# 次重新编译		<br>
		
注:		<br>
BUILD TYPE则指的是编译类型,通常有三种:		<br>
-user:		这是编译出的系统镜像是可以用来正式发布到市场的版本,其权限是被限制的(如,没有root权限,不能开启dedug等)	<br>	
-userdebug	在user版本的基础上开放了root权限和debug权限.		<br>
-eng:		engineer,也就是所谓的开发工程师的版本,拥有最大的权限(root等),此外还附带了许多debug工具		<br>
	
## 三、刷机	
### 1. 配置刷机环境	
安装 Android Studio 	<br>
在 Android Studio 里安装 Androd Sdk		<br>
设置 Android Studio -> Setting -> Android Sdk		<br>
SDK Patforms 里的 Android 系统版本必须包含所要刷入的Android版本		<br>
比如要刷入的系统是 Android 10 则 SDK Platforms 必须有 Android 10.0(Q)		<br>

将下载的 Android/Sdk/platform-tools 目录加入到环境变量里		<br>
echo "PATH=~/Library/Android/Sdk/platform-tools:$PATH" >> \~/.bashrc		<br>
source \~/.bashrc		<br>

### 2. 刷入安卓系统官方包 即 Factory Images	
下载地址: https://developers.google.com/android/images	<br>
下载对应手机和系统的压缩包 如: https://dl.google.com/dl/android/aosp/marlin-qp1a.191005.007.a3-factory-bef66533.zip	<br>
下载完解压		<br>
如：unzip -q marlin-qp1a.191005.007.a3-factory-bef66533.zip		<br>
进入解压目录		<br>
cd marlin-qp1a.191005.007.a3/		<br>
手机连接系统		<br>
adb reboot bootloader			# 进入 recover 模式		<br>
./flash-all.sh				# 开始刷入系统		<br>

### 3. 刷入自己编译的 Android 系统		
下载 Factory Images 并解压		<br>
flash-all.bat 			# windows 刷机脚本		<br>
flash-all.sh  			# linux 刷机脚本		<br>
flash-base.sh			# linux 刷机脚本		<br>

bootloader-marlin-8996-012001-1908071822.img   	# bootloader		<br>
radio-marlin-8996-130361-1905270421.img		# 基带 radio		<br>

image-marlin-qp1a.191005.007.a3.zip 			# Android 系统	<br>	
解压后是这些文件 		<br>
android-info.txt  		# 系统相关说明		<br>
boot.img  			# 启动相关,  root就是修改这个文件		<br>
system.img  			# 系统 a 分区		<br>
system_other.img 		# 系统 b 分区		<br>
vendor.img			# 驱动相关		<br>


要刷入自己编译的系统只需要把 image-marlin-qp1a.191005.007.a3.zip 替换成自己编译的就可以	<br>	
在自己编译的Android系统输出目录里找到这些文件		<br>
如我这次编译后的输出目录：aosp/out/target/product/marlin/		<br>
下就有 android-info.txt  boot.img  system.img  system_other.img  vendor.img 这些文件		<br>
将这些文件压缩		<br>
zip -rq  image-marlin-qp1a.191005.007.a3.zip  android-info.txt  boot.img  system.img  system_other.img  vendor.img	<br>	
用新生成的 image-marlin-qp1a.191005.007.a3.zip 替换 Factory Images 里的  image-marlin-qp1a.191005.007.a3.zip		<br>
	
然后就可以像刷入 Factory Images 一样刷入自己编译的 系统	<br>
adb reboot bootloader			# 进入 recover 模式	<br>
./flash-all.sh				# 开始刷入系统	<br>
