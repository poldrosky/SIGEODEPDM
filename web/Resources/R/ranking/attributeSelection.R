require('FSelector')
require('stringr')
require('sqldf')

args <- commandArgs(TRUE)
directory <- args[2]
setwd(directory)

fileInput <- args[1]

CLASS <- 'TIPO_EVENTO'
TOP <- as.numeric(args[3])
classifier <- args[4]

data <- read.csv(file=fileInput, header=TRUE, sep=",")
data <- as.data.frame(sapply(data, str_trim))
data <- as.data.frame(sapply(data, as.factor))

formula <- paste(CLASS,'~ .')

if(classifier=='ig'){
  weights <- information.gain(formula, data)  
}else if(classifier=='gr'){
  weights <- gain.ratio(formula, data)
}else if(classifier=='su'){
  weights <- symmetrical.uncertainty(formula, data)
}else if(classifier=='cs'){
  weights <- chi.squared(formula, data)
}else if(classifier=='or'){
  weights <- oneR(formula, data)
}

ranking <- (rownames(weights)[order(weights, decreasing = T)][1:TOP])
for(i in ranking){
  print(i)
}