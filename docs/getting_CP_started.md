# Set up a Git repository

The [IntelliJ docs](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)
These instructions will assume a GitHub repoo is beign used. 

## Check out a project from a Git repo

Git will need to be installed. It comes installed on most Mac and Linux systems. Follow the guide her [install Git](https://github.com/git-guides/install-git). 

1. Clone a repo to your Github repo
	- [add steps here](www.google.com)

2. Start a new project by cloning a Git repository: 'File | New | Project from Version Control'
	- Add a GitHub account and log in, you should be able to choose the project fropm your available repos. It should download a git directory top your default working folder.
	- Alternatively, you can add it directly with a cloning link, e.g., 'https://github.com/ashejim/CirclePack.git'

3. Setup as a Java project in your IDE.
	- (In InTelliJ) 'File | Project Structure'; add SDK and make 'src' the source folder.
	- Create a Run Configuration; This may happen automatically, (upper-right next to the green bug, click the dots)
		- CP uses packages not included in yout standard SDK. Add the /jar subfolder to the project depenedcies as follows:
			'File | Project Structure -> Modules -> Dependencies'
		- For CirclePack some necesseary packages may not be included in your JDK by default. For example, if 'org.apache.xerces.parsers' was missing, it can be added as follows: 
		1. 'File | Project Structure -> Modules -> Dependencies'. 
		2. Click the “+” icon → choose Library → From Maven... In the pop-up box type 'xerces:xercesImpl:2.12.2' (or whatever is missing) check “Download to: project library”, and click "OK."  
		3. IntelliJ automatically downloads the Xerces JAR and adds it to your classpath. Press Apply → OK, then Build → Rebuild Project.
		
4. Run CP using SplashMain (./src/allMains/SplashMain). Why? I think the RunCirclePack main is needed to load the C libraries which seem to be already available after adding the above /jar subfolder (?). 
