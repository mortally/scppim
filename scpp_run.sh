#!/bin/bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
java -cp ./bin:./lib/snakeyaml-1.7.jar:./lib/javacsv.jar:./lib/lpsolve55j.jar org.srg.scpp_im.game.SelfContainedGameRunner $@
