require("rJava")
require("RWeka")
require("knitr")
require("RPostgreSQL")
require("stringr")
require("xtable")
require("classInt")

args <- commandArgs(TRUE)

directory <- args[3]

setwd(directory)

fileInput <- args[1]
fileOutput <- args[2]
nameFile <- args[2]
path <- paste0(directory,nameFile)
fileOutput <- paste0(fileOutput,".tex")
print(paste("Done in ", getwd()))

tag <- args[5]

N <- as.numeric(args[4])
if (N%%2 == 0){
  COLS_PER_PAGE <- ceiling(N/2) - 1
}else{
  COLS_PER_PAGE <- ceiling(N/2)
}

system(paste0('rm -R ',path))
system(paste0('mkdir ',path))
knit2pdf("runClustersSIGEODEP.Rnw", output = fileOutput)
system(paste0('mv ',nameFile,'.pdf ', path,'/'))
system(paste0('mv ',nameFile,'.tex ', path,'/'))
system(paste0('mv ',nameFile,'.aux ', path,'/'))
system(paste0('mv ',nameFile,'.lof ', path,'/'))
system(paste0('mv ',nameFile,'.log ', path,'/'))
system(paste0('mv ',nameFile,'.lot ', path,'/'))
system(paste0('mv ',nameFile,'.out ', path,'/'))
system(paste0('mv ',nameFile,'.toc ', path,'/'))
system(paste0('cp udenar.jpg ',path,'/udenar.jpg'))
system(paste0('cp colciencias.jpg ',path,'/colciencias.jpg'))
system(paste0('cp alcaldia.jpg ',path,'/alcaldia.jpg'))