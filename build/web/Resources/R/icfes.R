require("knitr")
require("RPostgreSQL")
require("stringr")
require("xtable")

args <- commandArgs(TRUE)

directory <- args[3]

setwd(directory)
driver <- dbDriver("PostgreSQL", max.con = 250)
connection <- dbConnect(driver, dbname="icfes", user= "omar", password="123", host="localhost")



table <- args[1]
fileOutput <- args[2]
fileOutput <- paste0(fileOutput,".tex")
print(paste("Done in ", getwd()))
knit2pdf("DataAnalysis.Rnw", output = fileOutput)
