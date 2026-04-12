# CirclePack

## 📑 Table of Contents
- [CirclePack](#circlepack)
  - [📑 Table of Contents](#-table-of-contents)
  - [About](#about)
  - [🛠 Installation](#-installation)
    - [Setup a GitHub repo](#setup-a-github-repo)
    - [Setup as a Java project in your IDE](#setup-as-a-java-project-in-your-ide)
  - [Contributing](#contributing)
    - [Programming polish](#programming-polish)

## About
Open source Java code for creation, manipulation, analysis, and display of circle packing (in the sense of Bill Thurston). See Ken Stephenson's website: [circlepack.com](http://www.circlepack.com/).

A "circle packing" is a configuration of circles satisfying a prescribed pattern of tangencies. It is also the term for the mathematical topic which studies the theory and application of these configurations. On the theory side, circle packing now provides a remarkably comprehensive and faithful theory for discrete analytic functions and discrete conformal geometry. Those interested are directed to my Cambridge University Press book "Introduction to Circle Packing: the Theory of Discrete Analytic Functions", 2005. But circle packing has been applied both inside and outside of mathematics without particular reference to this theory. Indeed, the images of circle packings give patterns evocative of many patterns one sees in nature, and modelling with circle packing provides many advantages --- thanks to the background mathematical theory --- in diverse topics such as brain imaging, physics of jammed packings, knot and general graph embedding, random graph theory, 3D printing, etc. 

CirclePack software is pure Java code, graphically based, with input and scripting features intended to be user friendly. However, this is aspirational, as the topic is continually growing, and the desire to run new experiments overwhelms the desire to make the software more accessible. Sorry. Perhaps a community can be developed to improve things. In using CirclePacking, I have numerous scripts that can be executed by any user; I am working on organizing these as a resource that potential users can access. Again, this is aspirational: wish me luck.

A brief history of CirclePack development: Circle packing was introduced by Bill Thurston, first in his famous "Notes" in the late 1970s, but more broadly in a fantastic talk at the 1985 Conference celebrating de Branges ' solution of the Bieberbach conjecture. I was fascinated by that talk and started to move my research towards circle packing. My initial interest was in the intriguing circle packing images, leading to some minor coding in Basic. Thinking about the topic for coding was more valuable than the coding itself, and I soon began to see substantial theoretical potential in the topic. Among other things, I proved Thurston's Conjecture from that 1985 talk using probabilistic methods, and I published papers with Alan Beardon on a discrete Schwarz lemma and Uniformization theorem. Many others took part in the rapid growth of this topic: I will mention, in particular, Oded Schramm and Z-X He, who proved quite astounding results. It became evident to me early on that developing the theory depended on doing experiments, so I became more serious about coding, starting my early versions of CirclePack in the early 1990's. That coding continues to this day, but the underlying machinery has morphed many times. Basic and Mathematica did not last long, and I quickly moved to C, using SunView and later XView as GUI platforms. When XView was killed, I jumped to Java (and Swing). This is the package occupying this repository. This history explains some of the shortcomings of CirclePack: since it was not initially conceived in an object-oriented way, the code is unnecessarily convoluted. The main difficulty, however, is that the next experiment, the next theorem, the next application, is more attractive than cleaning up the code. Fortunately, the code is sufficiently well structured that it generally works, and for those in the know (that includes me, perhaps only me) it can be modified quickly to incorporate new features. Though I'm not originally a programmer, I would love to share the clever bits of my CirclePack work. Does anyone want to hear that?

Current projects include 3D printing (patterns for infill) with John Bowers and a group at Oak Ridge National Lab, rigidity connections such as particle jamming in 2D, conformal tiling with Phil Bowers, and the discrete Schwarzian derivative, inspired by my former student, Gerald Orick.

I would love to find a way to open CirclePack to interested contributors; I won't be around forever, and circle packing is too valuable to be lost. If you are interested in such an undertaking, I'll be happy to talk with you.

-Ken Stephenson

<!-- TODO
Add some links to CP book, paper, and Bowers paper  -->

## 🛠 Installation

CirclePack is a Java app, and should run on any Java platform. If not installed, download and install the lateset JRE or [JDK](https://www.oracle.com/java/technologies/downloads/). Chosse your OS, download an installation file and install.

For development, the CirclePack source files can be run from an IDE **using SplashMain** (./src/allMains/SplashMain). The additional jar files found in CirclePack/jars/ may need to be manual added to the project configuration files.

### Setup a GitHub repo

1. Clone a repo to your Github repo
   - [add steps here](www.google.com)

2. Start a new project by cloning a Git repository: 'File | New | Project from Version Control'. You can add it directly with a cloning link:
   > <https://github.com/ashejim/CirclePack.git>

### Setup as a Java project in your IDE

   0. (in IntelliJ) 'Projects' choose 'New Project'
   
   1. (In IntelliJ) 'File | Project Structure'; add SDK of your choice, in 'Project Structure | Modules' make 'src' the source folder.

   2. Create a Run Configuration; This may happen automatically, (upper-right next to the green bug, click the dots). CP uses packages not included in your standard SDK. Add the /jar subfolder to the project depenedcies as follows: 'File | Project Structure -> Modules -> Dependencies'

   3. For CirclePack some necesseary packages may not be included in your JDK by default. For example, if 'org.apache.xerces.parsers' was missing, it can be added as follows:

      1. 'File | Project Structure -> Modules -> Dependencies'.

      2. Click the “+” icon → choose Library → From Maven... In the pop-up box type 'xerces:xercesImpl:2.12.2' (or whatever is missing) check “Download to: project library”, and click "OK."
  
      3. IntelliJ automatically downloads the Xerces JAR and adds it to your classpath. Press Apply → OK, then Build → Rebuild Project.
  
   4. Run CP using SplashMain (./src/allMains/SplashMain).

## Contributing

CirclePack needs your help! What kind of help?

### Programming polish

"Since it was not initially conceived in an object-oriented way, the code is unnecessarily convoluted..." Stephenson and most every CP contributor, are mathematicians who picked up programming as it fit their academic needs. 

- Aesthetic and UI improvments
- Documentation

"Since it was not initially conceived in an object-oriented way, the code is unnecessarily convoluted..."

 The main difficulty, however, is that the next experiment, the next theorem, the next application, is more attractive than cleaning up the code. Fortunately, the code is sufficiently well structured that it generally works, and for those in the know (that includes me, perhaps only me) it can be modified quickly to incorporate new features. Though I'm not originally a programmer, I would love to share the clever bits of my CirclePack work. Does anyone want to hear that?