require("knitr")
require("stringr")
require("xtable")

args <- commandArgs(TRUE)

directory <- args[3]

setwd(directory)

fileInput <- args[1]
fileOutput <- args[2]
path <- args[2]
path <- paste0(directory,path)
nameFile <- args[2]
fileOutput <- paste0(fileOutput,".tex")
print(paste("Done in ", getwd()))

system(paste0('rm -R ',path))
system(paste0('mkdir ',path))
knit2pdf("DataAnalysis.Rnw", output = fileOutput)

system(paste0('mv ',nameFile,'.pdf ', path,'/'))
system(paste0('mv ',nameFile,'.tex ', path,'/'))
system(paste0('mv ',nameFile,'.aux ', path,'/'))
system(paste0('mv ',nameFile,'.lof ', path,'/'))
system(paste0('mv ',nameFile,'.log ', path,'/'))
system(paste0('mv ',nameFile,'.lot ', path,'/'))
system(paste0('mv ',nameFile,'.out ', path,'/'))
system(paste0('mv ',nameFile,'.toc ', path,'/'))
system(paste0('mv ','figure ', path,'/'))
system(paste0('cp udenar.jpg ',path,'/udenar.jpg'))
system(paste0('cp colciencias.jpg ',path,'/colciencias.jpg'))
system(paste0('cp alcaldia.jpg ',path,'/alcaldia.jpg'))
