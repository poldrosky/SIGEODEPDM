require("knitr")
require("stringr")
require("xtable")

args <- commandArgs(TRUE)

directory <- args[3]

setwd(directory)

fileInput <- args[1]
fileOutput <- args[2]
fileOutput <- paste0(fileOutput,".tex")
print(paste("Done in ", getwd()))
knit2pdf("DataAnalysis.Rnw", output = fileOutput)
