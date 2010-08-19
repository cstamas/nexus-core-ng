# Nexus Core

This is the Nexus Core, a mirror of the Sonatype Nexus Core SVN repository.

## Branches

I used as manual a nice blog entry to create a "one way" mirror of SVN repository:

http://www.fnokd.com/2008/08/20/mirroring-svn-repository-to-github/

So, the setup is similar, we have the following branches:

* vendor - this is 1:1 mirror of the TRUNK of the Nexus SVN, no changes here.
* master - this is where work is, and *rebase* happens regularly (done manually, not cron-ed like Bob did!)

DO NOT FORK THIS REPO, you will be not able to follow it.

Have fun!   
~t~

Original readme follows:

Sonatype Nexus Core
===================

This is the Core codebase of Sonatype Nexus, aka "Nexus OSS".

Project homepage:
http://nexus.sonatype.org/

Public source repository:
http://svn.sonatype.org/nexus

Issue tracking:
https://issues.sonatype.org/browse/NEXUS

Public wiki:
https://docs.sonatype.org/display/Nexus/Home


Have fun,  
Sonatype Team
