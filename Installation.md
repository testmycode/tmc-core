# Installation Instructions

**Warning,** heavily in development. Features might be unusable or broken.
### Use these instructions to get the TMC commandline client up and running. 

##### NOTE
If you don't wan't to install maven and you want the tmc-cli up and running just copy the /scripts/ -folder from this repository and [download the newest release](https://github.com/tmc-cli/tmc-cli/releases). After that you can just follow these instructions and skip the maven parts. 

If maven is not installed, you won't be able to build the jar. Install it:
```shell
$ apt-get install maven
```
Start by cloning the repo to ~/.tmc-cli:

```shell
$ git clone https://github.com/tmc-cli/tmc-cli  ~/.tmc-cli
```
Now switch to the new project directory
```shell
$ cd ~/.tmc-cli
```
And create the java executable
```shell
$ mvn package
```
Open up ~/.bashrc, or equivalent if using another terminal:
```shell
$ nano ~/.bashrc
```
And add the following lines if you installed directly under ~/.tmc-cli:
```shell
PATH="$HOME/.tmc-cli/scripts:$PATH"
export PATH
```

Otherwise, replace the path with the path you cloned the repo in.

You will have to either restart your terminal the first time after editing .bashrc or run 
```shell
source ~/.bashrc
```

Type the following to get started:
```shell
$ tmc help
```
