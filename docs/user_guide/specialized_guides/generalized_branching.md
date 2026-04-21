# Generalized Branching

Generalized branching tools are included in the ['branching'](../docs/html/namespacebranching.html) package.  

1. For a *traditional* discrete branch point v, the subcomplex is just its flower and an angle sum condition is placed at the center $v$, e.g. $\text{aim} = 4 \pi$.

2. For *singular* and "fractured" branching the subcomplex consists of a face $\{v,u,w\}$, so the eventHorizon is the link of face edges. The internal structure involves distributing an extra 2 pi angle sum among $v$, $u$, and $w $and/or distributing overlaps on the edges of the face.

3. A *quad* branch point involves fracturing two contiguous faces.

4. For *chaperone* branching at $v$, the subcomplex is the flower augmented with a 'sister' to $v$ and two chaperones and their overlaps.

Traditional branching is traditonally done by setting Setting vertices branching is done using the 'PackData>setAim'.
In CirclePack, the command is:
'set_aim  [{a v..}] -[cdx%]'
For example, 
'set_aim 4.0 1 3'
creates traditional branch points with angle sums of $4\pi$ at vertices 1 and 3. And everything is beautiful and as it should be when things are simply connected.
The problem is when things are not. What if it needs to go inside a circle or in the interstice? Then discreteness limits where we can place the branch point. We need more fleaxiblity, and this is where non-traditional branching comes in. 
<!-- TODO Is this the right way to write this? 'PackData>setAim' -->
<!-- TODO add link to simply connected info -->

We carefully introduce this flexibility into areas where the branching will occur. Outside this area, bounded by the "event horizon," all the packings rules will be retained. That is, elsewhere it will be a "normal" circle packing. Just like in complex analysis, the necessary ambiguity will be confined (to an area instead of a point). The trick is introduing enough flexibility that a circle packing can be realized outside the branching, but not so much that all rigidity is lost. Hopefully, we retain all the beautiful theory ensuring behaves like classical complex functions. I say hopefully because we really don't know!  
<!-- Prove existnece and uniqueness! -->

Experiments and intuition (and hope) point towards "yes." For the former and the general theory the mechanisms for generalized branching have been built on existing code and theory. The parameters (for felxibility) also aim to be manageable. For details see [citation number her](https://arxiv.org/pdf/1607.03404).
Be patient when first looking at these methods! Even the authors (or at least one) admit confusion when revisting these methods. A little expertimation in CirclePack can help things come into focus. 
<!-- TODO add ciation links. How do you do that in markdown/RST? -->
 <!--TODO Add experimentation script --> 

 ## Experiments in Generalized Branching.

<!-- TODEO prob move this to its own spot -->
 ## Extenders

'PackExtenders' are Java classes which can be launched for particular packings, extending storage and functionality for them. The intention is that users can write extenders tailored to their purposes but without the need to delve into 'CirclePack' core operations. Extenders process commands directed to them via two-letter abbreviations, as with 'cf' for the 'CurvatureFlow' extender. When a extender is started for a packing, the "Pack Extender" tab of the Help Frame displays help information.

To activate an extender, use the CirclePack command:
'extender -[xr] {type}'

**Options:**
- '{type}' Where {type} is two letter abbreviation for the extender. (Currently, 'CirclePack' must already know of the associated class.)
- '-x [{typ}]'
close extenders of the specified type, or all extenders if the type is omitted.
- '-r {typ}'
close any extenders of type 'typ' and then start an extender of this same type.
- '?' Lists active extenders (blank if none)
- ' '(blank e.g., 'extender') opens a load screen for opening an extender (class) file. 

**Examples**
- 'extender cf'
would start a "CurvatureFlow" pack extender.
- 'extender -x'
would close all extenders for this packing.
- 'extender -r ct'
would close any current 'ConformalTiling' extenders for this packing and start a new one.

[list of avaliable](../docs/html/namespaceftn_theory.html)

The abbrevations can be found in
[CommandStrParser.java](../docs/html/_command_str_parser_8java.html)
under 'cmd.startsWith("extender")' or as 'extensionAbbrev' in their class constructor.
<!-- Compile a list of extenders and create a doc subpage for them. -->

### Generalized Branching Commands

CirclePack commands added by extensiton can be fouund in their Class file. They are defined within a 'cmdParser' method/function interates through the cmd idenifying flags and performing actions. Additionally, information is passed to the CmdStruct object.
 <!--TODO I don't understand how CP implements commands. It looks like this simplifies command parsing and execution. I need to watch this in action in debug mode.  -->

#### event 
Append event horizon vertices to packing's 'vlist'
'|gb| event'
This command iterates thourgh the branch points stored in tge 'branchPts' object and add the event horizon vertives to 'vlist'.
 <!--TODO check this. function is probably wrong -->

#### click
        "create a chaparone or a singular branch point (as appropriate) at the point z (relative to 'refPack'); '-x' flag means to remove other nearby branch points '-X' remove all others; -a set aim to x*Pi"
<!-- TODO I dnk how this works -->

#### reset_over
"For resetting overlaps for 'singular' and 'chaperone' branch points." Currently, generalized branching works by using chaperones between circles (interstices), singular inside circles, and traditional at vertices. 
 <!--TODO I want a single method to rule them all. This jumping around is awkward. Is it possible to traditional as a special case of singular (twins) and singular as a special case of chaperones (interstice) by adjusting where we place the ovelap parameters? Note: I think I can show that the original gb methods ("shift points") is a special case of singular branching and (as setup) incapabale of expressing branching on interstices. -->

Example 
'|gb| event 

        cmdStruct.add(new CmdStruct("event",null,null,
                "));
        cmdStruct.add(new CmdStruct("'Comment'","-b{b}",null,
                "first flag '-b' designates a branch point ID number"));
        cmdStruct.add(new CmdStruct("disp","-[shyj] {usual}",null,
                "display on parents packing. For chaperone: s=sisters, "+
                "h=chaperones, y=putative branch point, "+
                        "j=jumps. Also, {usual} display options"));
        cmdStruct.add(new CmdStruct("status","-b{b}",null,
                "report the status of branch point 'b'"));
        cmdStruct.add(new CmdStruct("angsum_err",null,null,
                "report the l^2 anglesum error of parent and "+
                        "all branch points"));
        cmdStruct.add(new CmdStruct("get_param",null,null,
                "report branch point parameters"));
        cmdStruct.add(new CmdStruct("click","-[xX] z [-a {x}] ",null,
                "create a chaparone or a singular branch point "+
                        "(as appropriate) at the "+
                "point z (relative to 'refPack'); '-x' flag means "+
                        "to remove other nearby "+
                "branch points; '-X' remove all others; -a set aim to x*Pi"));
        cmdStruct.add(new CmdStruct("reset_over","o1 o2",null,
                "For resetting overlaps for 'singular' and 'chaperone' "+
                        "branch points"));
        cmdStruct.add(new CmdStruct("delete","-b{b}",null,
                "delete branch point 'b'"));
        cmdStruct.add(new CmdStruct("set_param","{param list}",null,
                "Set parameters for branch point, format depends on type: "+
                "sing '-a {a} -o {o1 o2}'; "+
                "chap '-a {a} -j {w1 w2} -o {o1 o2}'"));
        cmdStruct.add(new CmdStruct("copy","{pnum}",null,
                "write 'refPack' into designated packing"));
        cmdStruct.add(new CmdStruct("revert",null,null,
                "revert to the original unbranched packing 'refPack'"));
        
        // creating branch point types:
        cmdStruct.add(new CmdStruct("bp_trad","-a {a} -i {v}",null,
                "Create 'traditional' branch point, aim 'a', vert 'v'."));
        cmdStruct.add(new CmdStruct("bp_sing",
                "-a {a} -i {f} -o {o1 o2} [-b {blist}]",null,
                "Create 'singular' branch point, aim 'a'; face 'f'; "+
                "overlaps 'o1', 'o2' in [0,1], o1+o2 in [0,1]. "+
                "'blist' is 'BaryLink' option for face and overlaps."));
        cmdStruct.add(new CmdStruct("bp_chap",
                "-a {a} -i {v} -j {w1 w2} -o {o1 o2}",null,
                "Create 'chaperone' branch point, aim 'a', vert 'v'; "+
                "optional jump vertices, petals 'w1' 'w2', "+
                "overlap parameters 'o1', 'o2' in [0,1]."));