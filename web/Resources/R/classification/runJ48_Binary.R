options( java.parameters = "-Xmx4g" )
require("knitr")
require("RWeka")
require("rJava")
require("xtable")
require("stringr")
require("ROCR")
require("igraph")

############# PARAMETERS ################
args <- commandArgs(TRUE)
directory <- args[3]

setwd(directory)

fileInput <- args[1]
nameFile <- args[2]
fileOutput <- paste0(nameFile,".tex")

name <- paste0(directory,nameFile)
class_value <- args[4]
format = 'png'
the_date = '\\today'
bestConf <- 0
bestM <- 0
bestC <- 0
bestLabel <- ''
maxM <- as.numeric(args[5])  # max 10 % tamaño del conjunto
minM <- as.numeric(args[6])  # minimo 2
deltaM <- as.numeric(args[7]) # entre 2 y max
maxC <- as.numeric(args[8])
minC <- as.numeric(args[9])   # entre 1 y 50 %
deltaC <- as.numeric(args[10]) # porcentaje de delta
CONFIDENCE <- as.numeric(args[11]) # porcentaje
SUPPORT <- as.numeric(args[12]) # porcentaje
N <- length(seq(minM, maxM, deltaM))*length(seq(minC, maxC, deltaC))
i <- 1
conf_table <- data.frame(M=rep(NA, N),C=rep(NA, N),Conf =rep(NA, N), Detalle=rep("", N),  stringsAsFactors=FALSE) 
path <- paste(name)

print(path)
############# PARAMETERS ################

############# DATA ######################
# data <- read.csv(url("http://190.254.4.36:9191/resources/fatales.csv"))
data <- read.csv(file=fileInput, header=TRUE, sep=",") 

#data <- data[data$date_year_number == 2013, ]

data <- as.data.frame(sapply(data, str_trim))
data <- as.data.frame(sapply(data, as.factor))

data$TIPO_EVENTO <- sapply(data$TIPO_EVENTO, as.character)
data$TIPO_EVENTO[data$TIPO_EVENTO != class_value] <- 'N'
data$TIPO_EVENTO[data$TIPO_EVENTO == class_value] <- 'Y'
data$TIPO_EVENTO <- sapply(data$TIPO_EVENTO, as.factor)
names(data)[1] <- class_value
formula <- paste(class_value, "~ .")
############# DATA ################

system(paste0('rm -R ',path))
system(paste0('mkdir ',path))
knit2pdf('runJ48_Binary.Rnw', output=fileOutput, )
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
system(paste0('mkdir ',path,'/',path))
system(paste0('mv ',path,'/model_* ',path,'/',path,'/'))
system(paste0('mv ',path,'/pruned_* ',path,'/',path,'/'))
system(paste0('mv /tmp/ROC.pdf ',path,'/'))
