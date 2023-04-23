Scent-C-Tive
---------------------------------------------------------------

Purpose: To find code smells within source code that compiles using a terminal tool

Languages tested and compatible: C, C++, Java

Languages that may be compatible but are untested: C#

What is a code smell?
For our purposes, it is a bad coding practice that hurt the Security, Readability, or maintainability of code.

To Use:
------------------------------------------------------------------
1. Download srcML from http://www.srcml.org/. It converts source code into a tagged xml file. This project is reliant on srcML.
2. Download SCT.java in the src file, put it in any folder you wish, and compile it using any java compiler.
3. Put the desired C/C++ source file into that folder.
4. In the terminal, navigate to that folder and run the command: "Java SCT {Source-Code} {Tags}...".

*IMPORTANT*
Make sure input source code is compilable. Inputting source code that does not compile may lead to unknown behavior.

What smells are being searched for
-------------------------------------------------------

Goto Stamtements

Empty Statements -- Originally planned but not included in the final product

Magic Numbers -- Any Expression that contains a constant literal number which is assigned to a non-constant variable. Note, the delcaration of an int/double/long/float variable that is instantiated with a number does not count as Magic Numbers. It is also considered a magic number to have a constant literal number inside a conditional statement. Random constant numbers within an expression or a conditional that are not contained within a variable have no context and hurt readability.

Blockless If-Statements

Blockless Loops

Long Parameter Lists -- By default, the threshhold for what is considered a long parameter list is 6 or more parameters within a function definition. This can be redefined in the terminal call.

Long Functions -- By default, the threshhold for what is considered a long function is 20 or more lines within the function definition. This can be redifined in the terminal call.

Dead Functions -- functions that are never called.

Conditional Complexity -- Originally planned but not included in the final product.

Security Issues -- Functions built into the C/C++ library that pose security risks.

Continue Statements

Break Statements

Bad Variable Names -- variable names that do not conform to camel case. Any variable name that is of a single lowercase word will also be highlighted due to the difficulty of sorting through languages.

Bad Function Names -- function names that do not conform to camel case. Any function name that is of a single lowercase word will also be highlighted due to the difficulty of sorting through languages.

Multiple Variable Declarations on One Line

Global Variables


TAGS
---------------------------------------------------

This program uses tags either to disable smells or to redefine default threshholds. Any tag used is optional.

Tag			        |	Definition

"-g"			      | Disable Goto Statements

"-e"			      | Disable Empty Statements *DOES NOT WORK*

"-m"			      | Disable Magic Numbers

"-i"			      | Disable Blockless If-Statements

"-l"			      | Disable Blockless Loops

"-p"  		      | Disable Long Parameter Lists

"-p {integer}" 	| Redefine the parameter threshhold for Long Parameter Lists (Does not disable)
	
"-f"			      | Disable Long Functions
	
"-f {integer}"	| Redefine the line number threshhold for Long Functions (Does not disable)
	
"-d"			      | Disable Dead Functions
	
"-x"			      | Disable Conditional Complexity *DOES NOT WORK*
	
"-s"			      | Disable Security Issues
	
"-c"			      | Disable Continue Statements
	
"-b"			      | Disable Break Statements
	
"-v"			      | Disable Bad Variable Names
	
"-n"			      | Disable Bad Function Names
