acCLImate
=========

Automated Class-based Command Line Interfaces (using) Methods Annotated To Execute

Why another command-line parser?
--------------------------------

I had tried a number of other command-line parsers (args4j, spullara's cli-parser, jcommander, ...) and they all
*almost* did what I wanted. acCLImate allows for options to be specified multiple times, with the corresponding
function being called every time the option is provided. Options can be re-ordered by a `priority` (for instance,
to move `-h` to the front so that it is called before anything else), but otherwise will be processed in the
order in which they are provided.

This allows the command line to be used almost as a scripting language for the client of the acCLImate class.
