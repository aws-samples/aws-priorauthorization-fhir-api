wget http://apache.mirrors.pair.com/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
tar xvf apache-maven-3.8.6-bin.tar.gz
sudo mv apache-maven-3.8.6 /usr/local/apache-maven

export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
source ~/.bashrc


#install jdk 1.8
sudo yum -y install java-1.8.0-openjdk-devel

#set jdk to 1.8
sudo alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java

sudo yum remove java-1.7.0-openjdk-devel

sudo yum -y install jq

python -m pip install --user boto3
